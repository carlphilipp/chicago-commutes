/**
 * Copyright 2016 Carl-Philipp Harmant
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.activity;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.text.WordUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import fr.cph.chicago.ChicagoTracker;
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
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.listener.GoogleMapDirectionOnClickListener;
import fr.cph.chicago.listener.GoogleMapOnClickListener;
import fr.cph.chicago.listener.GoogleStreetOnClickListener;
import fr.cph.chicago.task.DisplayGoogleStreetPictureTask;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.XmlParser;

import static fr.cph.chicago.connection.CtaRequestType.TRAIN_ARRIVALS;

/**
 * Activity that represents the train station
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class StationActivity extends Activity {

    private static final String TAG = StationActivity.class.getSimpleName();

    private ViewGroup viewGroup;
    private ImageView streetViewImage;
    private ImageView favoritesImage;
    private LinearLayout.LayoutParams paramsStop;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView streetViewText;

    private boolean isFavorite;
    private TrainData trainData;
    private int stationId;
    private Station station;
    private Map<String, Integer> ids;

    @SuppressWarnings("unchecked")
    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChicagoTracker.checkTrainData(this);
        if (!this.isFinishing()) {

            setContentView(R.layout.activity_station);

            viewGroup = (ViewGroup) findViewById(android.R.id.content);
            // Load data
            final DataHolder dataHolder = DataHolder.getInstance();
            trainData = dataHolder.getTrainData();

            ids = new HashMap<>();

            // Get station id from bundle extra
            stationId = getIntent().getExtras().getInt("stationId", 0);
            if (stationId != 0) {
                // Get station from station id
                station = trainData.getStation(stationId);

                final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>();
                reqParams.put(getString(R.string.request_map_id), String.valueOf(station.getId()));

                swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_station_swipe_refresh_layout);
                swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        new LoadData().execute(reqParams);
                    }
                });

                new LoadData().execute(reqParams);

                // Call google street api to load image
                final Position position = station.getStops().get(0).getPosition();
                final double latitude = position.getLatitude();
                final double longitude = position.getLongitude();

                isFavorite = isFavorite();

                streetViewImage = (ImageView) findViewById(R.id.activity_train_station_streetview_image);
                streetViewText = (TextView) findViewById(R.id.activity_train_station_steetview_text);

                new DisplayGoogleStreetPictureTask(this, streetViewImage, streetViewText).execute(position.getLatitude(), position.getLongitude());

                final ImageView mapImage = (ImageView) findViewById(R.id.activity_map_image);
                mapImage.setColorFilter(ContextCompat.getColor(this, R.color.grey_5));
                final LinearLayout mapContainer = (LinearLayout) findViewById(R.id.map_container);
                final ImageView directionImage = (ImageView) findViewById(R.id.activity_map_direction);
                directionImage.setColorFilter(ContextCompat.getColor(this, R.color.grey_5));
                final LinearLayout walkContainer = (LinearLayout) findViewById(R.id.walk_container);
                favoritesImage = (ImageView) findViewById(R.id.activity_favorite_star);
                final LinearLayout favoritesImageContainer = (LinearLayout) findViewById(R.id.favorites_container);
                favoritesImageContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        StationActivity.this.switchFavorite();
                    }
                });
                if (isFavorite) {
                    favoritesImage.setColorFilter(ContextCompat.getColor(this, R.color.yellowLineDark));
                } else {
                    favoritesImage.setColorFilter(ContextCompat.getColor(this, R.color.grey_5));
                }

                paramsStop = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

                final Map<TrainLine, List<Stop>> stopByLines = station.getStopByLines();
                final TrainLine randomTrainLine = getRandomLine(stopByLines);
                swipeRefreshLayout.setColorSchemeColors(randomTrainLine.getColor());

                streetViewImage.setOnClickListener(new GoogleStreetOnClickListener(this, latitude, longitude));
                mapContainer.setOnClickListener(new GoogleMapOnClickListener(this, latitude, longitude));
                walkContainer.setOnClickListener(new GoogleMapDirectionOnClickListener(this, latitude, longitude));

                setUpStopLayouts(stopByLines);

                setToolBar(randomTrainLine);

                Util.trackScreen(getString(R.string.analytics_train_details));
            }
        }
    }

    private void setUpStopLayouts(final Map<TrainLine, List<Stop>> stopByLines) {
        final LinearLayout stopsView = (LinearLayout) findViewById(R.id.activity_train_station_details);
        for (final Entry<TrainLine, List<Stop>> entry : stopByLines.entrySet()) {
            final TrainLine line = entry.getKey();
            final List<Stop> stops = entry.getValue();
            Collections.sort(stops);
            final View lineTitleView = getLayoutInflater().inflate(R.layout.activity_station_line_title, viewGroup, false);

            final TextView testView = (TextView) lineTitleView.findViewById(R.id.train_line_title);
            testView.setText(WordUtils.capitalize(line.toStringWithLine()));
            testView.setBackgroundColor(line.getColor());
            if (line == TrainLine.YELLOW) {
                testView.setBackgroundColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.yellowLine));
            }

            stopsView.addView(lineTitleView);

            for (final Stop stop : stops) {
                final LinearLayout line2 = new LinearLayout(this);
                line2.setOrientation(LinearLayout.HORIZONTAL);
                line2.setLayoutParams(paramsStop);

                final AppCompatCheckBox checkBox = new AppCompatCheckBox(this);
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                        Preferences.saveTrainFilter(stationId, line, stop.getDirection(), isChecked);
                    }
                });
                checkBox.setOnClickListener(new View.OnClickListener() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onClick(final View v) {
                        // Update timing
                        final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>();
                        reqParams.put(getString(R.string.request_map_id), String.valueOf(station.getId()));
                        new LoadData().execute(reqParams);
                    }
                });
                checkBox.setChecked(Preferences.getTrainFilter(stationId, line, stop.getDirection()));
                checkBox.setTypeface(checkBox.getTypeface(), Typeface.BOLD);
                checkBox.setText(stop.getDirection().toString());
                checkBox.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.grey));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    checkBox.setBackgroundTintList(ColorStateList.valueOf(line.getColor()));
                    checkBox.setButtonTintList(ColorStateList.valueOf(line.getColor()));
                    if (line == TrainLine.YELLOW) {
                        checkBox.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.yellowLine)));
                        checkBox.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.yellowLine)));
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkBox.setForegroundTintList(ColorStateList.valueOf(line.getColor()));
                    if (line == TrainLine.YELLOW) {
                        checkBox.setForegroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.yellowLine)));
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

    @SuppressWarnings("unchecked")
    private void setToolBar(final TrainLine randomTrainLine) {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.main);
        toolbar.setOnMenuItemClickListener((new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final MenuItem item) {
                swipeRefreshLayout.setRefreshing(true);
                final MultiValuedMap<String, String> reqParams = new ArrayListValuedHashMap<>();
                reqParams.put(getString(R.string.request_map_id), String.valueOf(station.getId()));
                new LoadData().execute(reqParams);
                return false;
            }
        }));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(4);
        }

        Util.setToolbarColor(this, toolbar, randomTrainLine);

        toolbar.setTitle(station.getName());
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        stationId = savedInstanceState.getInt("stationId");
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putInt("stationId", stationId);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Is favorite or not ?
     *
     * @return if the station is favorite
     */
    private boolean isFavorite() {
        final List<Integer> favorites = Preferences.getTrainFavorites(ChicagoTracker.PREFERENCE_FAVORITES_TRAIN);
        for (final Integer favorite : favorites) {
            if (favorite == stationId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Load train arrivals
     *
     * @author Carl-Philipp Harmant
     * @version 1
     */
    private class LoadData extends AsyncTask<MultiValuedMap<String, String>, Void, TrainArrival> {

        /**
         * The exception that might be thrown
         **/
        private TrackerException trackerException;

        @SafeVarargs
        @Override
        protected final TrainArrival doInBackground(final MultiValuedMap<String, String>... params) {
            // Get menu item and put it to loading mod
            publishProgress((Void[]) null);
            SparseArray<TrainArrival> arrivals = new SparseArray<>();
            final CtaConnect connect = CtaConnect.getInstance();
            try {
                final XmlParser xml = XmlParser.getInstance();
                final InputStream xmlResult = connect.connect(TRAIN_ARRIVALS, params[0]);
                //String xmlResult = connectTest();
                arrivals = xml.parseArrivals(xmlResult, StationActivity.this.trainData);
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
                Log.e(TAG, e.getMessage(), e);
                trackerException = e;
            }
            Util.trackAction(StationActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_train, R.string.analytics_action_get_train_arrivals, 0);
            if (arrivals.size() == 1) {
                final String id = ((List<String>) params[0].get(getString(R.string.request_map_id))).get(0);
                return arrivals.get(Integer.valueOf(id));
            } else {
                return null;
            }
        }

        @Override
        protected final void onProgressUpdate(final Void... values) {
        }

        @Override
        protected final void onPostExecute(@Nullable final TrainArrival trainArrival) {
            if (trackerException == null) {
                List<Eta> etas;
                if (trainArrival != null) {
                    etas = trainArrival.getEtas();
                } else {
                    etas = Collections.emptyList();
                }
                hideAllArrivalViews(StationActivity.this.station);
                for (final Eta eta : etas) {
                    drawAllArrivalsTrain(eta);
                }
            } else {
                ChicagoTracker.displayError(StationActivity.this, trackerException);
            }
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    /**
     * Reset arrival layouts
     *
     * @param station the station
     */
    // FIXME: delete view instead of hiding it
    private void hideAllArrivalViews(final Station station) {
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
    private void drawAllArrivalsTrain(final Eta eta) {
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
                stopName.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.grey));
                stopName.setPadding(line3PaddingLeft, line3PaddingTop, 0, 0);
                insideLayout.addView(stopName);

                final TextView timing = new TextView(this);
                final String timingData = eta.getTimeLeftDueDelay() + " ";
                timing.setText(timingData);
                timing.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.grey));
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
            Util.removeFromTrainFavorites(stationId, ChicagoTracker.PREFERENCE_FAVORITES_TRAIN);
            isFavorite = false;
            favoritesImage.setColorFilter(ContextCompat.getColor(this, R.color.grey_5));
        } else {
            Util.addToTrainFavorites(stationId, ChicagoTracker.PREFERENCE_FAVORITES_TRAIN);
            isFavorite = true;
            favoritesImage.setColorFilter(ContextCompat.getColor(this, R.color.yellowLineDark));
        }
    }

    private TrainLine getRandomLine(final Map<TrainLine, List<Stop>> stops) {
        final Random random = new Random();
        final List<TrainLine> keys = new ArrayList<>(stops.keySet());
        return keys.get(random.nextInt(keys.size()));
    }
}
