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

package fr.cph.chicago.app.activity;

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
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import fr.cph.chicago.app.App;
import fr.cph.chicago.R;
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
import fr.cph.chicago.app.listener.GoogleMapDirectionOnClickListener;
import fr.cph.chicago.app.listener.GoogleMapOnClickListener;
import fr.cph.chicago.app.listener.GoogleStreetOnClickListener;
import fr.cph.chicago.rx.subscriber.SubscriberTrainArrival;
import fr.cph.chicago.service.TrainService;
import fr.cph.chicago.service.impl.TrainServiceImpl;
import fr.cph.chicago.util.Util;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Activity that represents the train station
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class StationActivity extends AbstractStationActivity {

    private ViewGroup viewGroup;
    private ScrollView scrollView;
    private ImageView favoritesImage;
    private LinearLayout.LayoutParams paramsStop;
    private SwipeRefreshLayout swipeRefreshLayout;

    private boolean isFavorite;
    private int stationId;
    private Station station;
    private Map<String, Integer> ids;

    private Observable<TrainArrival> trainArrivalObservable;

    private final TrainService trainService;

    public StationActivity() {
        trainService = new TrainServiceImpl();
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.checkTrainData(this);
        if (!this.isFinishing()) {
            // Get station id from bundle
            stationId = getIntent().getExtras().getInt(getString(R.string.bundle_train_stationId), 0);
            if (stationId != 0) {

                // Layout setup
                setContentView(R.layout.activity_station);
                scrollView = (ScrollView) findViewById(R.id.scrollViewTrainStation);
                viewGroup = (ViewGroup) findViewById(android.R.id.content);
                swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_station_swipe_refresh_layout);
                favoritesImage = (ImageView) findViewById(R.id.activity_favorite_star);
                paramsStop = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

                // Get station
                final TrainData trainData = DataHolder.getInstance().getTrainData();
                station = trainData.getStation(stationId);

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

                streetViewImage.setOnClickListener(new GoogleStreetOnClickListener(position.getLatitude(), position.getLongitude()));
                streetViewImage.setLayoutParams(params);
                streetViewText.setTypeface(null, Typeface.BOLD);
                swipeRefreshLayout.setOnRefreshListener(() -> trainArrivalObservable.subscribe(new SubscriberTrainArrival(this, swipeRefreshLayout)));
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
                mapContainer.setOnClickListener(new GoogleMapOnClickListener(position.getLatitude(), position.getLongitude()));
                walkContainer.setOnClickListener(new GoogleMapDirectionOnClickListener(position.getLatitude(), position.getLongitude()));

                final Map<TrainLine, List<Stop>> stopByLines = station.getStopByLines();
                final TrainLine randomTrainLine = getRandomLine(stopByLines);
                setUpStopLayouts(stopByLines);
                swipeRefreshLayout.setColorSchemeColors(randomTrainLine.getColor());
                setToolBar(randomTrainLine);

                Util.trackScreen(getApplicationContext(), getString(R.string.analytics_train_details));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void setUpStopLayouts(@NonNull final Map<TrainLine, List<Stop>> stopByLines) {
        final LinearLayout stopsView = (LinearLayout) findViewById(R.id.activity_train_station_details);
        Stream.of(stopByLines.entrySet()).forEach(entry -> {
            final TrainLine line = entry.getKey();
            final List<Stop> stops = entry.getValue();
            final View lineTitleView = getLayoutInflater().inflate(R.layout.activity_station_line_title, viewGroup, false);

            final TextView testView = (TextView) lineTitleView.findViewById(R.id.train_line_title);
            testView.setText(line.toStringWithLine());
            testView.setBackgroundColor(line.getColor());
            if (line == TrainLine.YELLOW) {
                testView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.yellowLine));
            }

            stopsView.addView(lineTitleView);

            Stream.of(stops).sorted().forEach(stop -> {
                final LinearLayout linearLayout = new LinearLayout(this);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout.setLayoutParams(paramsStop);

                final AppCompatCheckBox checkBox = new AppCompatCheckBox(this);
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> Preferences.saveTrainFilter(getApplicationContext(), stationId, line, stop.getDirection(), isChecked));
                checkBox.setOnClickListener(v -> {
                    if (checkBox.isChecked()) {
                        trainArrivalObservable.subscribe(new SubscriberTrainArrival(this, swipeRefreshLayout));
                    }
                });
                checkBox.setChecked(Preferences.getTrainFilter(getApplicationContext(), stationId, line, stop.getDirection()));
                checkBox.setTypeface(checkBox.getTypeface(), Typeface.BOLD);
                checkBox.setText(stop.getDirection().toString());
                checkBox.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.grey));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    checkBox.setBackgroundTintList(ColorStateList.valueOf(line.getColor()));
                    checkBox.setButtonTintList(ColorStateList.valueOf(line.getColor()));
                    if (line == TrainLine.YELLOW) {
                        checkBox.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.yellowLine)));
                        checkBox.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.yellowLine)));
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkBox.setForegroundTintList(ColorStateList.valueOf(line.getColor()));
                    if (line == TrainLine.YELLOW) {
                        checkBox.setForegroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.yellowLine)));
                    }
                }

                linearLayout.addView(checkBox);

                final LinearLayout arrivalTrainsLayout = new LinearLayout(this);
                arrivalTrainsLayout.setOrientation(LinearLayout.VERTICAL);
                arrivalTrainsLayout.setLayoutParams(paramsStop);
                int id = Util.generateViewId();
                arrivalTrainsLayout.setId(id);
                ids.put(line.toString() + "_" + stop.getDirection().toString(), id);

                linearLayout.addView(arrivalTrainsLayout);
                stopsView.addView(linearLayout);
            });
        });
    }

    private void setToolBar(@NonNull final TrainLine randomTrainLine) {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.main);
        toolbar.setOnMenuItemClickListener(item -> {
            swipeRefreshLayout.setRefreshing(true);
            trainArrivalObservable.subscribe(new SubscriberTrainArrival(this, swipeRefreshLayout));
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
    @Override
    protected boolean isFavorite() {
        final List<Integer> favorites = Preferences.getTrainFavorites(getApplicationContext(), App.PREFERENCE_FAVORITES_TRAIN);
        return Stream.of(favorites)
            .filter(favorite -> favorite == stationId)
            .findFirst()
            .isPresent();
    }

    // FIXME: delete view instead of hiding it
    public void hideAllArrivalViews() {
        Stream.of(station.getLines())
            .flatMap(trainLine ->
                Stream.of(TrainDirection.values())
                    .map(trainDirection -> trainLine.toString() + "_" + trainDirection.toString())
            )
            .forEach(key -> {
                if (ids.containsKey(key)) {
                    final int id = ids.get(key);
                    final LinearLayout line3View = (LinearLayout) findViewById(id);
                    if (line3View != null) {
                        line3View.setVisibility(View.GONE);
                        if (line3View.getChildCount() > 0) {
                            Stream.range(0, line3View.getChildCount()).forEach(i -> {
                                final LinearLayout view = (LinearLayout) line3View.getChildAt(i);
                                final TextView timing = (TextView) view.getChildAt(1);
                                if (timing != null) {
                                    timing.setText("");
                                }
                            });
                        }
                    }
                }
            });
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
                stopName.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.grey));
                stopName.setPadding(line3PaddingLeft, line3PaddingTop, 0, 0);
                insideLayout.addView(stopName);

                final TextView timing = new TextView(this);
                final String timingData = eta.getTimeLeftDueDelay() + " ";
                timing.setText(timingData);
                timing.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.grey));
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
            Util.removeFromTrainFavorites(stationId, scrollView);
            isFavorite = false;
            favoritesImage.setColorFilter(ContextCompat.getColor(this, R.color.grey_5));
        } else {
            Util.addToTrainFavorites(stationId, scrollView);
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
        trainArrivalObservable = Observable.create((Subscriber<? super TrainArrival> subscriber) -> {
            subscriber.onNext(trainService.loadStationTrainArrival(getApplicationContext(), station.getId()));
            subscriber.onCompleted();
        })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
        trainArrivalObservable.subscribe(new SubscriberTrainArrival(this, swipeRefreshLayout));
    }
}
