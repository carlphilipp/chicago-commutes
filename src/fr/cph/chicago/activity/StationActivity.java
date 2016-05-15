/**
 * Copyright 2016 Carl-Philipp Harmant
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.activity;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import fr.cph.chicago.App;
import fr.cph.chicago.R;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.Stop;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.listener.GoogleMapDirectionOnClickListener;
import fr.cph.chicago.listener.GoogleMapOnClickListener;
import fr.cph.chicago.listener.GoogleStreetOnClickListener;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.XmlParser;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.schedulers.Schedulers;

import static fr.cph.chicago.connection.CtaRequestType.TRAIN_ARRIVALS;

/**
 * Activity that represents the train station
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class StationActivity extends AbstractStationActivity {

    private static final String TAG = StationActivity.class.getSimpleName();

    private ViewGroup viewGroup;
    private ScrollView scrollView;
    private ImageView favoritesImage;
    private LinearLayout.LayoutParams paramsStop;
    private SwipeRefreshLayout swipeRefreshLayout;

    private boolean isFavorite;
    private TrainData trainData;
    private int stationId;
    private Station station;
    private Map<String, Integer> ids;

    private Observable<TrainArrival> trainArrivalObservable;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.checkTrainData(this);
        if (!this.isFinishing()) {
            // Get station id from bundle
            stationId = getIntent().getExtras().getInt(getString(R.string.bundle_train_stationId), 0);
            if (stationId != 0) {

                // Get station
                final DataHolder dataHolder = DataHolder.getInstance();
                trainData = dataHolder.getTrainData();
                station = trainData.getStation(stationId);

                // Layout setup
                setContentView(R.layout.activity_station);
                scrollView = (ScrollView) findViewById(R.id.scrollViewTrainStation);
                viewGroup = (ViewGroup) findViewById(android.R.id.content);
                swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_station_swipe_refresh_layout);
                favoritesImage = (ImageView) findViewById(R.id.activity_favorite_star);
                paramsStop = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

                final ImageView streetViewImage = (ImageView) findViewById(R.id.activity_train_station_streetview_image);
                final TextView streetViewText = (TextView) findViewById(R.id.activity_train_station_steetview_text);
                final ImageView mapImage = (ImageView) findViewById(R.id.activity_map_image);
                final LinearLayout mapContainer = (LinearLayout) findViewById(R.id.map_container);
                final ImageView directionImage = (ImageView) findViewById(R.id.activity_map_direction);
                final LinearLayout walkContainer = (LinearLayout) findViewById(R.id.walk_container);
                final LinearLayout favoritesImageContainer = (LinearLayout) findViewById(R.id.favorites_container);
                final int height = (int) getResources().getDimension(R.dimen.activity_station_street_map_height);
                final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) streetViewImage.getLayoutParams();
                final Position position = station.getStops().get(0).getPosition();
                final ViewGroup.LayoutParams params = streetViewImage.getLayoutParams();

                ids = new HashMap<>();
                isFavorite = isFavorite();

                createGoogleStreetObservable(position.getLatitude(), position.getLongitude());
                subscribeToGoogleStreet(streetViewImage, streetViewText);
                createTrainArrivalObservableAndSubscribe();

                streetViewImage.setOnClickListener(new GoogleStreetOnClickListener(this, position.getLatitude(), position.getLongitude()));
                streetViewImage.setLayoutParams(params);
                streetViewText.setTypeface(null, Typeface.BOLD);
                swipeRefreshLayout.setOnRefreshListener(() -> trainArrivalObservable.subscribe(new SubscriberTrainArrival()));
                if (isFavorite) {
                    favoritesImage.setColorFilter(ContextCompat.getColor(this, R.color.yellowLineDark));
                } else {
                    favoritesImage.setColorFilter(ContextCompat.getColor(this, R.color.grey_5));
                }

                params.height = height;
                params.width = layoutParams.width;
                mapImage.setColorFilter(ContextCompat.getColor(this, R.color.grey_5));
                directionImage.setColorFilter(ContextCompat.getColor(this, R.color.grey_5));
                favoritesImageContainer.setOnClickListener(v -> StationActivity.this.switchFavorite());
                mapContainer.setOnClickListener(new GoogleMapOnClickListener(this, position.getLatitude(), position.getLongitude()));
                walkContainer.setOnClickListener(new GoogleMapDirectionOnClickListener(this, position.getLatitude(), position.getLongitude()));

                final Map<TrainLine, List<Stop>> stopByLines = station.getStopByLines();
                final TrainLine randomTrainLine = getRandomLine(stopByLines);
                setUpStopLayouts(stopByLines);
                swipeRefreshLayout.setColorSchemeColors(randomTrainLine.getColor());
                setToolBar(randomTrainLine);

                Util.trackScreen(getString(R.string.analytics_train_details));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void setUpStopLayouts(@NonNull final Map<TrainLine, List<Stop>> stopByLines) {
        final LinearLayout stopsView = (LinearLayout) findViewById(R.id.activity_train_station_details);
        for (final Entry<TrainLine, List<Stop>> entry : stopByLines.entrySet()) {
            final TrainLine line = entry.getKey();
            final List<Stop> stops = entry.getValue();
            Collections.sort(stops);
            final View lineTitleView = getLayoutInflater().inflate(R.layout.activity_station_line_title, viewGroup, false);

            final TextView testView = (TextView) lineTitleView.findViewById(R.id.train_line_title);
            testView.setText(line.toStringWithLine());
            testView.setBackgroundColor(line.getColor());
            if (line == TrainLine.YELLOW) {
                testView.setBackgroundColor(ContextCompat.getColor(App.getContext(), R.color.yellowLine));
            }

            stopsView.addView(lineTitleView);

            for (final Stop stop : stops) {
                final LinearLayout line2 = new LinearLayout(this);
                line2.setOrientation(LinearLayout.HORIZONTAL);
                line2.setLayoutParams(paramsStop);

                final AppCompatCheckBox checkBox = new AppCompatCheckBox(this);
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> Preferences.saveTrainFilter(stationId, line, stop.getDirection(), isChecked));
                checkBox.setOnClickListener(v -> {
                    if (checkBox.isChecked()) {
                        trainArrivalObservable.subscribe(new SubscriberTrainArrival());
                    }
                });
                checkBox.setChecked(Preferences.getTrainFilter(stationId, line, stop.getDirection()));
                checkBox.setTypeface(checkBox.getTypeface(), Typeface.BOLD);
                checkBox.setText(stop.getDirection().toString());
                checkBox.setTextColor(ContextCompat.getColor(App.getContext(), R.color.grey));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    checkBox.setBackgroundTintList(ColorStateList.valueOf(line.getColor()));
                    checkBox.setButtonTintList(ColorStateList.valueOf(line.getColor()));
                    if (line == TrainLine.YELLOW) {
                        checkBox.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(App.getContext(), R.color.yellowLine)));
                        checkBox.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(App.getContext(), R.color.yellowLine)));
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkBox.setForegroundTintList(ColorStateList.valueOf(line.getColor()));
                    if (line == TrainLine.YELLOW) {
                        checkBox.setForegroundTintList(ColorStateList.valueOf(ContextCompat.getColor(App.getContext(), R.color.yellowLine)));
                    }
                }

                line2.addView(checkBox);

                final LinearLayout arrivalTrainsLayout = new LinearLayout(this);
                arrivalTrainsLayout.setOrientation(LinearLayout.VERTICAL);
                arrivalTrainsLayout.setLayoutParams(paramsStop);
                int id3 = Util.generateViewId();
                arrivalTrainsLayout.setId(id3);
                ids.put(line.toString() + "_" + stop.getDirection().toString(), id3);

                line2.addView(arrivalTrainsLayout);
                stopsView.addView(line2);
            }
        }
    }

    private void setToolBar(@NonNull final TrainLine randomTrainLine) {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.main);
        toolbar.setOnMenuItemClickListener(item -> {
            swipeRefreshLayout.setRefreshing(true);
            trainArrivalObservable.subscribe(new SubscriberTrainArrival());
            return false;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(4);
        }

        Util.setWindowsColor(this, toolbar, randomTrainLine);

        toolbar.setTitle(station.getName());
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        toolbar.setOnClickListener(v -> finish());
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        stationId = savedInstanceState.getInt(getString(R.string.bundle_train_stationId));
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putInt(getString(R.string.bundle_train_stationId), stationId);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Is favorite or not ?
     *
     * @return if the station is favorite
     */
    private boolean isFavorite() {
        final List<Integer> favorites = Preferences.getTrainFavorites(App.PREFERENCE_FAVORITES_TRAIN);
        for (final Integer favorite : favorites) {
            if (favorite == stationId) {
                return true;
            }
        }
        return false;
    }

    // FIXME: delete view instead of hiding it
    public void hideAllArrivalViews() {
        final Set<TrainLine> trainLines = station.getLines();
        for (final TrainLine trainLine : trainLines) {
            for (final TrainDirection trainDirection : TrainDirection.values()) {
                final String key = trainLine.toString() + "_" + trainDirection.toString();
                if (ids.containsKey(key)) {
                    final int id = ids.get(key);
                    final LinearLayout line3View = (LinearLayout) findViewById(id);
                    if (line3View != null) {
                        line3View.setVisibility(View.GONE);
                        if (line3View.getChildCount() > 0) {
                            for (int i = 0; i < line3View.getChildCount(); i++) {
                                final LinearLayout view = (LinearLayout) line3View.getChildAt(i);
                                final TextView timing = (TextView) view.getChildAt(1);
                                if (timing != null) {
                                    timing.setText("");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Draw line
     *
     * @param eta the eta
     */
    public void drawAllArrivalsTrain(@NonNull final Eta eta) {
        final TrainLine line = eta.getRouteName();
        final Stop stop = eta.getStop();
        final int line3PaddingLeft = (int) getResources().getDimension(R.dimen.activity_station_stops_line3_padding_left);
        final int line3PaddingTop = (int) getResources().getDimension(R.dimen.activity_station_stops_line3_padding_top);
        final String key = line.toString() + "_" + stop.getDirection().toString();
        // viewId might be not there if CTA API provide wrong data
        if (ids.containsKey(key)) {
            final int viewId = ids.get(key);
            final LinearLayout line3View = (LinearLayout) findViewById(viewId);
            final Integer id = ids.get(line.toString() + "_" + stop.getDirection().toString() + "_" + eta.getDestName());
            if (id == null) {
                final LinearLayout insideLayout = new LinearLayout(this);
                insideLayout.setOrientation(LinearLayout.HORIZONTAL);
                insideLayout.setLayoutParams(paramsStop);
                final int newId = Util.generateViewId();
                insideLayout.setId(newId);
                ids.put(line.toString() + "_" + stop.getDirection().toString() + "_" + eta.getDestName(), newId);

                final TextView stopName = new TextView(this);
                final String stopNameData = eta.getDestName() + ": ";
                stopName.setText(stopNameData);
                stopName.setTextColor(ContextCompat.getColor(App.getContext(), R.color.grey));
                stopName.setPadding(line3PaddingLeft, line3PaddingTop, 0, 0);
                insideLayout.addView(stopName);

                final TextView timing = new TextView(this);
                final String timingData = eta.getTimeLeftDueDelay() + " ";
                timing.setText(timingData);
                timing.setTextColor(ContextCompat.getColor(App.getContext(), R.color.grey));
                timing.setLines(1);
                timing.setEllipsize(TruncateAt.END);
                insideLayout.addView(timing);

                line3View.addView(insideLayout);
            } else {
                final LinearLayout insideLayout = (LinearLayout) findViewById(id);
                final TextView timing = (TextView) insideLayout.getChildAt(1);
                final String timingData = timing.getText() + eta.getTimeLeftDueDelay() + " ";
                timing.setText(timingData);
            }
            line3View.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Add/remove favorites
     */
    private void switchFavorite() {
        if (isFavorite) {
            Util.removeFromTrainFavorites(stationId, App.PREFERENCE_FAVORITES_TRAIN, scrollView);
            isFavorite = false;
            favoritesImage.setColorFilter(ContextCompat.getColor(this, R.color.grey_5));
        } else {
            Util.addToTrainFavorites(stationId, App.PREFERENCE_FAVORITES_TRAIN, scrollView);
            isFavorite = true;
            favoritesImage.setColorFilter(ContextCompat.getColor(this, R.color.yellowLineDark));
        }
    }

    private TrainLine getRandomLine(@NonNull final Map<TrainLine, List<Stop>> stops) {
        final Random random = new Random();
        final List<TrainLine> keys = new ArrayList<>(stops.keySet());
        return keys.get(random.nextInt(keys.size()));
    }

    private void createTrainArrivalObservableAndSubscribe() {
        final MultiValuedMap<String, String> params = new ArrayListValuedHashMap<>();
        params.put(getString(R.string.request_map_id), Integer.toString(station.getId()));

        trainArrivalObservable = Observable.create(new Observable.OnSubscribe<TrainArrival>() {
            @Override
            public void call(final Subscriber<? super TrainArrival> subscriber) {
                subscriber.onNext(requestTrainArrival(params));
                subscriber.onCompleted();
            }
        })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
        trainArrivalObservable.subscribe(new SubscriberTrainArrival());
    }

    @SafeVarargs
    public final TrainArrival requestTrainArrival(final MultiValuedMap<String, String>... params) {
        SparseArray<TrainArrival> arrivals;
        final CtaConnect connect = CtaConnect.getInstance();
        try {
            final XmlParser xml = XmlParser.getInstance();
            final InputStream xmlResult = connect.connect(TRAIN_ARRIVALS, params[0]);
            arrivals = xml.parseArrivals(xmlResult, trainData);
            // Apply filters
            int index = 0;
            while (index < arrivals.size()) {
                final TrainArrival arri = arrivals.valueAt(index++);
                final List<Eta> etas = arri.getEtas();
                // Sort Eta by arriving time
                Collections.sort(etas);
                // Copy data into new list to be able to avoid looping on a list that we want to modify
                final List<Eta> etasCopy = new ArrayList<>();
                etasCopy.addAll(etas);
                int j = 0;
                for (int i = 0; i < etasCopy.size(); i++) {
                    final Eta eta = etasCopy.get(i);
                    final Station station = eta.getStation();
                    final TrainLine line = eta.getRouteName();
                    final TrainDirection direction = eta.getStop().getDirection();
                    final boolean toRemove = Preferences.getTrainFilter(station.getId(), line, direction);
                    if (!toRemove) {
                        etas.remove(i - j++);
                    }
                }
            }
        } catch (final ParserException | ConnectException e) {
            throw Exceptions.propagate(e);
        }
        if (arrivals.size() == 1) {
            final String id = ((List<String>) params[0].get(getString(R.string.request_map_id))).get(0);
            return arrivals.get(Integer.parseInt(id));
        } else {
            return null;
        }
    }

    private class SubscriberTrainArrival extends Subscriber<TrainArrival> {

        @Override
        public void onNext(final TrainArrival trainArrival) {
            Log.d(TAG, "Found train arrival: " + trainArrival);
            final List<Eta> etas;
            if (trainArrival != null) {
                etas = trainArrival.getEtas();
            } else {
                etas = Collections.emptyList();
            }
            StationActivity.this.hideAllArrivalViews();
            for (final Eta eta : etas) {
                StationActivity.this.drawAllArrivalsTrain(eta);
            }
        }

        @Override
        public void onError(final Throwable e) {
            Log.e(TAG, "Error while getting trains arrival time: " + e.getMessage(), e);
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
            Util.showNetworkErrorMessage(swipeRefreshLayout);
        }

        @Override
        public void onCompleted() {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }
}
