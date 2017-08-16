/**
 * Copyright 2017 Carl-Philipp Harmant
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

package fr.cph.chicago.core.activity;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.ViewGroup;
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

import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import fr.cph.chicago.R;
import fr.cph.chicago.core.App;
import fr.cph.chicago.core.listener.GoogleMapDirectionOnClickListener;
import fr.cph.chicago.core.listener.GoogleMapOnClickListener;
import fr.cph.chicago.core.listener.GoogleStreetOnClickListener;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.PreferencesImpl;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.Stop;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.rx.ObservableUtil;
import fr.cph.chicago.rx.TrainArrivalObserver;
import fr.cph.chicago.util.Util;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * Activity that represents the train station
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
@SuppressWarnings("WeakerAccess")
public class StationActivity extends AbstractStationActivity {

    @BindView(android.R.id.content)
    ViewGroup viewGroup;
    @BindView(R.id.activity_train_station_streetview_image)
    ImageView streetViewImage;
    @BindView(R.id.scrollViewTrainStation)
    ScrollView scrollView;
    @BindView(R.id.activity_station_swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.activity_favorite_star)
    ImageView favoritesImage;
    @BindView(R.id.activity_train_station_steetview_text)
    TextView streetViewText;
    @BindView(R.id.activity_map_image)
    ImageView mapImage;
    @BindView(R.id.map_container)
    LinearLayout mapContainer;
    @BindView(R.id.activity_map_direction)
    ImageView directionImage;
    @BindView(R.id.walk_container)
    LinearLayout walkContainer;
    @BindView(R.id.favorites_container)
    LinearLayout favoritesImageContainer;
    @BindView(R.id.activity_train_station_details)
    LinearLayout stopsView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindString(R.string.bundle_train_stationId)
    String bundleTrainStationId;
    @BindString(R.string.analytics_train_details)
    String trainDetails;

    @BindDimen(R.dimen.activity_station_street_map_height)
    int height;
    @BindDimen(R.dimen.activity_station_stops_line3_padding_left)
    int line3PaddingLeft;
    @BindDimen(R.dimen.activity_station_stops_line3_padding_top)
    int line3PaddingTop;

    @BindColor(R.color.grey_5)
    int grey_5;
    @BindColor(R.color.grey)
    int grey;
    @BindColor(R.color.yellowLineDark)
    int yellowLineDark;
    @BindColor(R.color.yellowLine)
    int yellowLine;
    @BindDrawable(R.drawable.ic_arrow_back_white_24dp)
    Drawable arrowBackWhite;

    private LinearLayout.LayoutParams paramsStop;
    private boolean isFavorite;
    private int stationId;
    private Station station;
    private Map<String, Integer> ids;
    private Observable<TrainArrival> trainArrivalObservable;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.Companion.checkTrainData(this);
        if (!this.isFinishing()) {
            // Layout setup
            setContentView(R.layout.activity_station);
            ButterKnife.bind(this);
            // Get station id from bundle
            stationId = getIntent().getExtras().getInt(bundleTrainStationId, 0);
            if (stationId != 0) {
                // Get station
                final TrainData trainData = DataHolder.INSTANCE.getTrainData();
                station = trainData.getStation(stationId);

                paramsStop = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) streetViewImage.getLayoutParams();
                final Position position = station.getStops().get(0).getPosition();
                final ViewGroup.LayoutParams params = streetViewImage.getLayoutParams();

                ids = new HashMap<>();
                isFavorite = isFavorite();

                loadGoogleStreetImage(position, streetViewImage, streetViewText);
                createTrainArrivalObservableAndSubscribe();

                streetViewImage.setOnClickListener(new GoogleStreetOnClickListener(position.getLatitude(), position.getLongitude()));
                streetViewImage.setLayoutParams(params);
                streetViewText.setTypeface(null, Typeface.BOLD);
                swipeRefreshLayout.setOnRefreshListener(() -> trainArrivalObservable.subscribe(new TrainArrivalObserver(this, swipeRefreshLayout)));
                if (isFavorite) {
                    favoritesImage.setColorFilter(yellowLineDark);
                } else {
                    favoritesImage.setColorFilter(grey_5);
                }

                params.height = height;
                params.width = layoutParams.width;
                mapImage.setColorFilter(grey_5);
                directionImage.setColorFilter(grey_5);
                favoritesImageContainer.setOnClickListener(v -> switchFavorite());
                mapContainer.setOnClickListener(new GoogleMapOnClickListener(position.getLatitude(), position.getLongitude()));
                walkContainer.setOnClickListener(new GoogleMapDirectionOnClickListener(position.getLatitude(), position.getLongitude()));

                final Map<TrainLine, List<Stop>> stopByLines = station.getStopByLines();
                final TrainLine randomTrainLine = getRandomLine(stopByLines);
                setUpStopLayouts(stopByLines);
                swipeRefreshLayout.setColorSchemeColors(randomTrainLine.getColor());
                setToolBar(randomTrainLine);

                Util.INSTANCE.trackScreen(getApplicationContext(), trainDetails);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void setUpStopLayouts(@NonNull final Map<TrainLine, List<Stop>> stopByLines) {
        Stream.of(stopByLines.entrySet()).forEach(entry -> {
            final TrainLine line = entry.getKey();
            final List<Stop> stops = entry.getValue();
            final View lineTitleView = getLayoutInflater().inflate(R.layout.activity_station_line_title, viewGroup, false);

            final TextView testView = (TextView) lineTitleView.findViewById(R.id.train_line_title);
            testView.setText(line.toStringWithLine());
            testView.setBackgroundColor(line.getColor());
            if (line == TrainLine.YELLOW) {
                testView.setBackgroundColor(yellowLine);
            }

            stopsView.addView(lineTitleView);

            Stream.of(stops).sorted().forEach(stop -> {
                final LinearLayout linearLayout = new LinearLayout(this);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout.setLayoutParams(paramsStop);

                final AppCompatCheckBox checkBox = new AppCompatCheckBox(this);
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> PreferencesImpl.INSTANCE.saveTrainFilter(getApplicationContext(), stationId, line, stop.getDirection(), isChecked));
                checkBox.setOnClickListener(v -> {
                    if (checkBox.isChecked()) {
                        trainArrivalObservable.subscribe(new TrainArrivalObserver(this, swipeRefreshLayout));
                    }
                });
                checkBox.setChecked(PreferencesImpl.INSTANCE.getTrainFilter(getApplicationContext(), stationId, line, stop.getDirection()));
                checkBox.setTypeface(checkBox.getTypeface(), Typeface.BOLD);
                checkBox.setText(stop.getDirection().toString());
                checkBox.setTextColor(grey);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    checkBox.setBackgroundTintList(ColorStateList.valueOf(line.getColor()));
                    checkBox.setButtonTintList(ColorStateList.valueOf(line.getColor()));
                    if (line == TrainLine.YELLOW) {
                        checkBox.setBackgroundTintList(ColorStateList.valueOf(yellowLine));
                        checkBox.setButtonTintList(ColorStateList.valueOf(yellowLine));
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkBox.setForegroundTintList(ColorStateList.valueOf(line.getColor()));
                    if (line == TrainLine.YELLOW) {
                        checkBox.setForegroundTintList(ColorStateList.valueOf(yellowLine));
                    }
                }

                linearLayout.addView(checkBox);

                final LinearLayout arrivalTrainsLayout = new LinearLayout(this);
                arrivalTrainsLayout.setOrientation(LinearLayout.VERTICAL);
                arrivalTrainsLayout.setLayoutParams(paramsStop);
                int id = Util.INSTANCE.generateViewId();
                arrivalTrainsLayout.setId(id);
                ids.put(line.toString() + "_" + stop.getDirection().toString(), id);

                linearLayout.addView(arrivalTrainsLayout);
                stopsView.addView(linearLayout);
            });
        });
    }

    private void setToolBar(@NonNull final TrainLine randomTrainLine) {
        toolbar.inflateMenu(R.menu.main);
        toolbar.setOnMenuItemClickListener(item -> {
            swipeRefreshLayout.setRefreshing(true);
            trainArrivalObservable.subscribe(new TrainArrivalObserver(this, swipeRefreshLayout));
            return false;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(4);
        }

        Util.INSTANCE.setWindowsColor(this, toolbar, randomTrainLine);

        toolbar.setTitle(station.getName());
        toolbar.setNavigationIcon(arrowBackWhite);

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (trainArrivalObservable != null) {
            trainArrivalObservable.unsubscribeOn(Schedulers.io());
        }
    }

    /**
     * Is favorite or not ?
     *
     * @return if the station is favorite
     */
    @Override
    protected boolean isFavorite() {
        final List<Integer> favorites = PreferencesImpl.INSTANCE.getTrainFavorites(getApplicationContext());
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
                final int newId = Util.INSTANCE.generateViewId();
                insideLayout.setId(newId);
                ids.put(line.toString() + "_" + stop.getDirection().toString() + "_" + eta.getDestName(), newId);

                final TextView stopName = new TextView(this);
                final String stopNameData = eta.getDestName() + ": ";
                stopName.setText(stopNameData);
                stopName.setTextColor(grey);
                stopName.setPadding(line3PaddingLeft, line3PaddingTop, 0, 0);
                insideLayout.addView(stopName);

                final TextView timing = new TextView(this);
                final String timingData = eta.getTimeLeftDueDelay() + " ";
                timing.setText(timingData);
                timing.setTextColor(grey);
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
            Util.INSTANCE.removeFromTrainFavorites(stationId, scrollView);
            isFavorite = false;
            favoritesImage.setColorFilter(grey);
        } else {
            Util.INSTANCE.addToTrainFavorites(stationId, scrollView);
            isFavorite = true;
            favoritesImage.setColorFilter(yellowLineDark);
        }
    }

    private TrainLine getRandomLine(@NonNull final Map<TrainLine, List<Stop>> stops) {
        final Random random = new Random();
        final List<TrainLine> keys = new ArrayList<>(stops.keySet());
        return keys.get(random.nextInt(keys.size()));
    }

    private void createTrainArrivalObservableAndSubscribe() {
        trainArrivalObservable = ObservableUtil.INSTANCE.createTrainArrivalsObservable(getApplicationContext(), station);
        trainArrivalObservable.subscribe(new TrainArrivalObserver(this, swipeRefreshLayout));
    }
}
