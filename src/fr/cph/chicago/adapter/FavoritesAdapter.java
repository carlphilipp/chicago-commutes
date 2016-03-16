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

package fr.cph.chicago.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.text.WordUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.BikeStationActivity;
import fr.cph.chicago.activity.BusActivity;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.Favorites;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.listener.FavoritesBusOnClickListener;
import fr.cph.chicago.listener.FavoritesTrainOnClickListener;
import fr.cph.chicago.util.Util;

import static java.util.Map.Entry;

/**
 * Adapter that will handle favorites
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
// TODO to analyze and refactor
public final class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder> {

    private static final String TAG = FavoritesAdapter.class.getSimpleName();

    private Context context;
    private LinearLayout.LayoutParams paramsLayout;
    private LinearLayout.LayoutParams paramsTextView;

    private MainActivity mainActivity;
    private Favorites favorites;
    private String lastUpdate;
    private int line1Padding;
    private int stopsPaddingTop;

    public FavoritesAdapter(final MainActivity activity) {
        this.context = ChicagoTracker.getContext();

        this.mainActivity = activity;
        this.favorites = new Favorites();

        this.paramsLayout = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        this.paramsTextView = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        this.line1Padding = (int) context.getResources().getDimension(R.dimen.activity_station_stops_line1_padding_color);
        this.stopsPaddingTop = (int) context.getResources().getDimension(R.dimen.activity_station_stops_padding_top);
    }


    static class FavoritesViewHolder extends RecyclerView.ViewHolder {
        public TextView timeView;
        public RelativeLayout lineTitleLayout;
        public RelativeLayout titleBottomLayout;
        public LinearLayout mainLayout;
        public TextView nameTitleBottomView;

        public FavoritesViewHolder(final View view) {
            super(view);
            this.timeView = (TextView) view.findViewById(R.id.station_updated);
            this.lineTitleLayout = (RelativeLayout) view.findViewById(R.id.favorites_list_main);
            this.mainLayout = (LinearLayout) view.findViewById(R.id.favorites_arrival_layout);
            this.nameTitleBottomView = (TextView) view.findViewById(R.id.station_name_value);
            this.titleBottomLayout = (RelativeLayout) view.findViewById(R.id.favorites_title_bottom);
        }
    }

    @Override
    public FavoritesViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        // create a new view
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_favorites_train, parent, false);
        return new FavoritesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final FavoritesViewHolder holder, final int position) {
        resetData(holder);
        final Object object = favorites.getObject(position);
        if (object != null) {
            if (object instanceof Station) {
                final Station station = (Station) object;
                handleStation(holder, station);
            } else if (object instanceof BusRoute) {
                final BusRoute busRoute = (BusRoute) object;
                handleBusRoute(holder, busRoute);
            } else {
                final BikeStation bikeStation = (BikeStation) object;
                handleBikeStation(holder, bikeStation);
            }
            holder.timeView.setText(this.lastUpdate);
        }
    }

    private void resetData(final FavoritesViewHolder holder) {
        holder.mainLayout.setOnClickListener(null);
        holder.titleBottomLayout.setOnClickListener(null);
        if (holder.lineTitleLayout.getChildCount() != 0) {
            holder.lineTitleLayout.removeAllViews();
        }
        if (holder.mainLayout.getChildCount() != 0) {
            holder.mainLayout.removeAllViews();
        }
    }

    private void handleStation(final FavoritesViewHolder holder, final Station station) {

        final int stationId = station.getId();

        holder.nameTitleBottomView.setText(station.getName());

        if (holder.lineTitleLayout.getChildCount() != 0) {
            holder.lineTitleLayout.removeAllViews();
        }

        addTitleToView(holder.lineTitleLayout, station.getLines());

        final LinearLayout.LayoutParams paramsArrival = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        final Set<TrainLine> setTL = station.getLines();

        // TODO create a method to check if empty
        if (favorites.getTrainArrival(stationId) != null) {
            for (final TrainLine tl : setTL) {
                final Map<String, List<String>> etas = favorites.getTrainArrivalByLine(stationId, tl);
                if (!etas.isEmpty()) {
                    final RelativeLayout containerTrainLine = new RelativeLayout(mainActivity);
                    final RelativeLayout.LayoutParams containerLayout = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

                    containerTrainLine.setLayoutParams(containerLayout);


                    final TextView trainLineTextView = new TextView(mainActivity);
                    trainLineTextView.setText(tl.toStringWithLine());
                    trainLineTextView.setLines(1);
                    trainLineTextView.setEllipsize(TextUtils.TruncateAt.END);
                    trainLineTextView.setTextColor(ContextCompat.getColor(mainActivity, R.color.white));
                    trainLineTextView.setTypeface(null, Typeface.BOLD);
                    float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, mainActivity.getResources().getDisplayMetrics());
                    trainLineTextView.setWidth((int) pixels);
                    trainLineTextView.setBackgroundColor(tl.getColor());
                    trainLineTextView.setGravity(Gravity.CENTER);

                    containerTrainLine.addView(trainLineTextView);
                    holder.mainLayout.addView(containerTrainLine);

                    for (final Map.Entry<String, List<String>> entry : etas.entrySet()) {
                        final LinearLayout insideLayout = new LinearLayout(context);
                        insideLayout.setOrientation(LinearLayout.HORIZONTAL);
                        insideLayout.setLayoutParams(paramsArrival);

                        final String dest = entry.getKey();
                        final TextView destinationTextView = new TextView(context);
                        destinationTextView.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.grey_5));
                        final String stopNameData = dest + ": ";
                        destinationTextView.setText(stopNameData);
                        insideLayout.addView(destinationTextView);

                        final TextView timing = new TextView(context);
                        final StringBuilder timingData = new StringBuilder();
                        final List<String> res = entry.getValue();
                        for (final String eta : res) {
                            timingData.append(eta).append(" ");
                        }
                        timing.setText(timingData.toString());
                        timing.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.grey));
                        timing.setLines(1);
                        timing.setEllipsize(TextUtils.TruncateAt.END);
                        insideLayout.addView(timing);
                        holder.mainLayout.addView(insideLayout);
                    }
                }
            }
        }
        holder.mainLayout.setOnClickListener(new FavoritesTrainOnClickListener(mainActivity, stationId, setTL));
        holder.titleBottomLayout.setOnClickListener(new FavoritesTrainOnClickListener(mainActivity, stationId, setTL));
    }

    private void handleBusRoute(final FavoritesViewHolder holder, final BusRoute busRoute) {
        holder.nameTitleBottomView.setText(busRoute.getId());

        final Map<String, Map<String, List<BusArrival>>> busArrivals = favorites.getBusArrivalsMapped(busRoute.getId());
        if (!busArrivals.isEmpty()) {
            for (final Entry<String, Map<String, List<BusArrival>>> entry : busArrivals.entrySet()) {
                final LinearLayout llh = new LinearLayout(context);
                llh.setLayoutParams(paramsLayout);
                llh.setOrientation(LinearLayout.HORIZONTAL);
                llh.setPadding(line1Padding, stopsPaddingTop, 0, 0);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    llh.setBackground(ContextCompat.getDrawable(ChicagoTracker.getContext(), R.drawable.any_selector));
                }

                final TextView tlView = new TextView(context);
                tlView.setBackgroundColor(ContextCompat.getColor(context, R.color.black));
                tlView.setText("   ");
                tlView.setLayoutParams(paramsTextView);
                llh.addView(tlView);

                final String key = entry.getKey();
                final Map<String, List<BusArrival>> value = entry.getValue();

                llh.setOnClickListener(new FavoritesBusOnClickListener(mainActivity, null, busRoute, value));

                final LinearLayout stopLayout = new LinearLayout(context);
                stopLayout.setOrientation(LinearLayout.VERTICAL);
                stopLayout.setPadding(line1Padding, 0, 0, 0);

                final TextView stopName = new TextView(context);
                stopName.setText(String.valueOf(key));
                stopName.setTextColor(ContextCompat.getColor(context, R.color.grey_5));
                stopName.setTypeface(Typeface.DEFAULT_BOLD);

                stopLayout.addView(stopName);

                for (final Entry<String, List<BusArrival>> entry2 : value.entrySet()) {
                    final String key2 = entry2.getKey();
                    final List<BusArrival> buses = entry2.getValue();

                    final LinearLayout boundLayout = new LinearLayout(context);
                    boundLayout.setOrientation(LinearLayout.HORIZONTAL);

                    final TextView bound = new TextView(context);
                    final String boundText = key2 + ": ";
                    bound.setText(boundText);
                    bound.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.grey_5));
                    boundLayout.addView(bound);

                    for (final BusArrival arri : buses) {
                        final TextView timeView = new TextView(context);
                        final String timeText = arri.getTimeLeftDueDelay() + " ";
                        timeView.setText(timeText);
                        timeView.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.grey));
                        timeView.setLines(1);
                        timeView.setEllipsize(TextUtils.TruncateAt.END);
                        boundLayout.addView(timeView);
                    }
                    stopLayout.addView(boundLayout);
                }
                llh.addView(stopLayout);
                holder.mainLayout.addView(llh);
            }
        }
    }

    private void handleBikeStation(final FavoritesViewHolder holder, final BikeStation bikeStation) {
        holder.nameTitleBottomView.setText(bikeStation.getName());

        final LinearLayout llh = new LinearLayout(context);
        llh.setLayoutParams(paramsLayout);
        llh.setOrientation(LinearLayout.HORIZONTAL);
        llh.setPadding(line1Padding, stopsPaddingTop, 0, 0);

        final TextView tlView = new TextView(context);
        tlView.setBackgroundColor(ContextCompat.getColor(context, R.color.black));
        tlView.setText("   ");
        tlView.setLayoutParams(paramsTextView);
        llh.addView(tlView);

        final LinearLayout availableLayout = new LinearLayout(context);
        availableLayout.setOrientation(LinearLayout.VERTICAL);

        final LinearLayout availableBikes = new LinearLayout(context);
        availableBikes.setOrientation(LinearLayout.HORIZONTAL);
        availableBikes.setPadding(line1Padding, 0, 0, 0);

        final TextView availableBike = new TextView(context);
        availableBike.setText(mainActivity.getString(R.string.bike_available_bikes));
        availableBike.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.grey_5));
        availableBikes.addView(availableBike);

        final TextView amountBike = new TextView(context);
        if (bikeStation.getAvailableBikes() == null) {
            amountBike.setText("?");
            amountBike.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.orange));
        } else {
            final String availableBikesText = String.valueOf(bikeStation.getAvailableBikes());
            amountBike.setText(availableBikesText);
            if (bikeStation.getAvailableBikes() == 0) {
                amountBike.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.red));
            } else {
                amountBike.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.green));
            }
        }
        availableBikes.addView(amountBike);

        availableLayout.addView(availableBikes);

        final LinearLayout availableDocks = new LinearLayout(context);
        availableDocks.setOrientation(LinearLayout.HORIZONTAL);
        availableDocks.setPadding(line1Padding, 0, 0, 0);

        final TextView availableDock = new TextView(context);
        availableDock.setText(mainActivity.getString(R.string.bike_available_docks));
        availableDock.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.grey_5));
        availableDocks.addView(availableDock);

        final TextView amountDock = new TextView(context);
        if (bikeStation.getAvailableDocks() == null) {
            amountDock.setText("?");
            amountDock.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.orange));
        } else {
            final String availableDocksText = String.valueOf(bikeStation.getAvailableDocks());
            amountDock.setText(availableDocksText);
            if (bikeStation.getAvailableDocks() == 0) {
                amountDock.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.red));
            } else {
                amountDock.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.green));
            }
        }
        availableDocks.addView(amountDock);

        availableLayout.addView(availableDocks);

        llh.addView(availableLayout);

        holder.mainLayout.addView(llh);

        final boolean isNetworkAvailable = Util.isNetworkAvailable();


        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkAvailable) {
                    Toast.makeText(mainActivity, "No network connection detected!", Toast.LENGTH_LONG).show();
                } else if (bikeStation.getLatitude() != 0 && bikeStation.getLongitude() != 0) {
                    final Intent intent = new Intent(ChicagoTracker.getContext(), BikeStationActivity.class);
                    final Bundle extras = new Bundle();
                    extras.putParcelable("station", bikeStation);
                    intent.putExtras(extras);
                    mainActivity.startActivity(intent);
                } else {
                    Toast.makeText(mainActivity, "Not ready yet. Please try again in few seconds!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public final long getItemId(final int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    private void addTitleToView(final RelativeLayout lineTitleLayout, final Set<TrainLine> lines) {
        final LinearLayout layout = new LinearLayout(mainActivity);
        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
        }
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layout.setLayoutParams(params);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        for (final TrainLine line : lines) {
            final RelativeLayout testView = createLineView(line);
            layout.addView(testView);
        }
        lineTitleLayout.addView(layout);
    }

    private RelativeLayout createLineView(final TrainLine line) {
        final RelativeLayout layout = new RelativeLayout(mainActivity);
        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(15, 3, 15, 3);
        layout.setLayoutParams(params);
        layout.setBackgroundColor(line.getColor());

        final TextView trainLineTextView = new TextView(mainActivity);
        trainLineTextView.setTextColor(ContextCompat.getColor(mainActivity, R.color.white));
        trainLineTextView.setText(WordUtils.capitalize(line.toString()));
        trainLineTextView.setTypeface(null, Typeface.BOLD);
        trainLineTextView.setLayoutParams(params);
        trainLineTextView.setTextSize(8);
        layout.addView(trainLineTextView);
        return layout;
    }

    /**
     * Set favorites
     *
     * @param arrivals    the trains arrivals
     * @param busArrivals the buses arrivals
     */
    public final void setArrivalsAndBikeStations(final SparseArray<TrainArrival> arrivals, final List<BusArrival> busArrivals, final List<BikeStation> bikeStations) {
        favorites.setArrivalsAndBikeStations(arrivals, busArrivals, bikeStations);
    }

    public final void setBikeStations(final List<BikeStation> bikeStations) {
        favorites.setBikeStations(bikeStations);
    }

    /**
     * Set favorites
     */
    public final void setFavorites() {
        favorites.setFavorites();
    }

    /**
     * Refresh date update
     */
    public final void refreshUpdated() {
        ChicagoTracker.modifyLastUpdate(Calendar.getInstance().getTime());
    }

    /**
     * Refresh udpdated view
     */
    public final void refreshUpdatedView() {
        final Date lastUpdate = ChicagoTracker.getLastUpdate();
        // FIXME What the hell? Did it ever worked?
        if (!String.valueOf(getLastUpdateInMinutes(lastUpdate)).equals(lastUpdate)) {
            this.lastUpdate = String.valueOf(getLastUpdateInMinutes(lastUpdate));
            this.notifyDataSetChanged();
        }
    }

    /**
     * Bus bound task. Start bus activity
     *
     * @author Carl-Philipp Harmant
     * @version 1
     */
    public static class BusBoundAsyncTask extends AsyncTask<String, Void, BusStop> {

        private String busRouteId;
        private String bound;
        private String boundTitle;
        private String stopId;
        private String busRouteName;
        private TrackerException trackerException;
        private MainActivity activity;

        public BusBoundAsyncTask(final MainActivity activity) {
            this.activity = activity;
        }

        @Override
        protected final BusStop doInBackground(final String... params) {
            BusStop res = null;
            try {
                busRouteId = params[0];
                bound = params[1];
                boundTitle = params[2];
                stopId = params[3];
                busRouteName = params[4];
                // FIXME If the CTA change again what they send back, just need to change from boundTitle to bound
                // It would change the param from Sounthbound to SOUTH.
                final List<BusStop> busStops = DataHolder.getInstance().getBusData().loadBusStop(busRouteId, boundTitle);

                for (final BusStop bus : busStops) {
                    if (String.valueOf(bus.getId()).equals(stopId)) {
                        res = bus;
                        break;
                    }
                }
            } catch (final ConnectException | ParserException e) {
                this.trackerException = e;
            }
            Util.trackAction(activity, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.analytics_action_get_bus_stop, 0);
            return res;
        }

        @Override
        protected final void onPostExecute(final BusStop busStop) {
            if (trackerException == null) {
                final Intent intent = new Intent(ChicagoTracker.getContext(), BusActivity.class);
                final Bundle extras = new Bundle();
                extras.putInt("busStopId", busStop.getId());
                extras.putString("busStopName", busStop.getName());
                extras.putString("busRouteId", busRouteId);
                extras.putString("busRouteName", busRouteName);
                extras.putString("bound", bound);
                extras.putString("boundTitle", boundTitle);
                extras.putDouble("latitude", busStop.getPosition().getLatitude());
                extras.putDouble("longitude", busStop.getPosition().getLongitude());

                intent.putExtras(extras);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
            } else {
                ChicagoTracker.displayError(activity, trackerException);
            }
        }
    }

    /**
     * Get time difference between 2 dates
     *
     * @param date1 the date one
     * @param date2 the date two
     * @return a tab containing in 0 the hour and in 1 the minutes
     */
    private long[] getTimeDifference(final Date date1, final Date date2) {
        final long[] result = new long[2];
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        final long t1 = cal.getTimeInMillis();
        cal.setTime(date2);
        long diff = Math.abs(cal.getTimeInMillis() - t1);
        final int day = 1000 * 60 * 60 * 24;
        final int hour = day / 24;
        final int minute = hour / 60;
        diff %= day;
        long h = diff / hour;
        diff %= hour;
        long m = diff / minute;
        diff %= minute;
        result[0] = h;
        result[1] = m;
        return result;
    }

    /**
     * Get last update in minutes
     *
     * @param lastUpdate the last update
     * @return a string
     */
    private String getLastUpdateInMinutes(final Date lastUpdate) {
        final String res;
        if (lastUpdate != null) {
            final Date currentCDate = Calendar.getInstance().getTime();
            final long[] diff = getTimeDifference(lastUpdate, currentCDate);
            if (diff[0] == 0 && diff[1] == 0) {
                res = "now";
            } else {
                if (diff[0] == 0) {
                    res = String.valueOf(diff[1]) + " min";
                } else {
                    res = String.valueOf(diff[0]) + " h " + String.valueOf(diff[1]) + " min";
                }
            }
        } else {
            res = "";
        }
        return res;
    }
}