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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.cph.chicago.App;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.BikeStationActivity;
import fr.cph.chicago.activity.BusActivity;
import fr.cph.chicago.activity.BusMapActivity;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.activity.StationActivity;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.Favorites;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusDetailsDTO;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.enumeration.BusDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.listener.FavoritesTrainOnClickListener;
import fr.cph.chicago.listener.GoogleMapOnClickListener;
import fr.cph.chicago.util.LayoutUtil;
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

    private static final int GREY_5 = ContextCompat.getColor(App.getContext(), R.color.grey_5);

    private final Context context;
    private final MainActivity activity;
    private final Favorites favorites;
    private String lastUpdate;
    private final int marginLeftPixel;
    private final int pixels;
    private final int pixelsHalf;
    private final int pixelsQuarter;

    public FavoritesAdapter(@NonNull final MainActivity activity) {
        this.context = App.getContext();

        this.activity = activity;
        this.favorites = Favorites.getInstance();

        this.marginLeftPixel = Util.convertDpToPixel(this.activity, 10);
        this.pixels = Util.convertDpToPixel(this.activity, 16);
        this.pixelsHalf = pixels / 2;
        this.pixelsQuarter = pixels / 4;
    }

    static class FavoritesViewHolder extends RecyclerView.ViewHolder {
        public final LinearLayout mainLayout;
        public final RelativeLayout buttonsLayout;
        public final TextView lastUpdateTextView;
        public final TextView stationNameTextView;
        public final ImageView favoriteImage;
        public final Button detailsButton;
        public final Button mapButton;

        public FavoritesViewHolder(final View view) {
            super(view);
            this.mainLayout = (LinearLayout) view.findViewById(R.id.favorites_arrival_layout);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                this.mainLayout.setBackground(ContextCompat.getDrawable(App.getContext(), R.drawable.any_selector));
            }
            this.buttonsLayout = (RelativeLayout) view.findViewById(R.id.favorites_buttons);
            this.favoriteImage = (ImageView) view.findViewById(R.id.favorites_icon);

            this.stationNameTextView = (TextView) view.findViewById(R.id.favorites_station_name);
            this.stationNameTextView.setLines(1);
            this.stationNameTextView.setEllipsize(TextUtils.TruncateAt.END);

            this.lastUpdateTextView = (TextView) view.findViewById(R.id.last_update);

            this.detailsButton = (Button) view.findViewById(R.id.details_button);
            this.mapButton = (Button) view.findViewById(R.id.view_map_button);
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
        holder.lastUpdateTextView.setText(lastUpdate);
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
        }
    }

    private void resetData(@NonNull final FavoritesViewHolder holder) {
        holder.mainLayout.removeAllViews();
    }

    private void handleStation(@NonNull final FavoritesViewHolder holder, @NonNull final Station station) {
        final int stationId = station.getId();
        final Set<TrainLine> trainLines = station.getLines();

        holder.favoriteImage.setImageResource(R.drawable.ic_train_white_24dp);
        holder.stationNameTextView.setText(station.getName());
        holder.detailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // Start station activity
                final Bundle extras = new Bundle();
                final Intent intent = new Intent(App.getContext(), StationActivity.class);
                extras.putInt(activity.getString(R.string.bundle_train_stationId), stationId);
                intent.putExtras(extras);
                activity.startActivity(intent);
            }
        });

        holder.mapButton.setText(activity.getString(R.string.favorites_view_trains));
        holder.mapButton.setOnClickListener(new FavoritesTrainOnClickListener(activity, trainLines));

        for (final TrainLine trainLine : trainLines) {
            boolean newLine = true;
            int i = 0;
            final Map<String, StringBuilder> etas = favorites.getTrainArrivalByLine(stationId, trainLine);
            for (final Entry<String, StringBuilder> entry : etas.entrySet()) {
                final LinearLayout.LayoutParams containParam = getInsideParams(newLine, i == etas.size() - 1);
                final LinearLayout container = new LinearLayout(context);
                container.setOrientation(LinearLayout.HORIZONTAL);
                container.setLayoutParams(containParam);

                // Left
                final RelativeLayout.LayoutParams leftParam = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                final RelativeLayout left = new RelativeLayout(context);
                left.setLayoutParams(leftParam);

                final RelativeLayout lineIndication = LayoutUtil.createColoredRoundForFavorites(trainLine);
                int lineId = Util.generateViewId();
                lineIndication.setId(lineId);

                final RelativeLayout.LayoutParams destinationParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                destinationParams.addRule(RelativeLayout.RIGHT_OF, lineId);
                destinationParams.setMargins(pixelsHalf, 0, 0, 0);

                final String destination = entry.getKey();
                final TextView destinationTextView = new TextView(context);
                destinationTextView.setTextColor(GREY_5);
                destinationTextView.setText(destination);
                destinationTextView.setLines(1);
                destinationTextView.setLayoutParams(destinationParams);

                left.addView(lineIndication);
                left.addView(destinationTextView);

                // Right
                final LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                rightParams.setMargins(marginLeftPixel, 0, 0, 0);
                final LinearLayout right = new LinearLayout(context);
                right.setOrientation(LinearLayout.VERTICAL);
                right.setLayoutParams(rightParams);

                final StringBuilder currentEtas = entry.getValue();
                final TextView arrivalText = new TextView(context);
                arrivalText.setText(currentEtas);
                arrivalText.setGravity(Gravity.RIGHT);
                arrivalText.setGravity(Gravity.END);
                arrivalText.setSingleLine(true);
                arrivalText.setTextColor(GREY_5);
                arrivalText.setEllipsize(TextUtils.TruncateAt.END);

                right.addView(arrivalText);

                container.addView(left);
                container.addView(right);

                holder.mainLayout.addView(container);

                newLine = false;
                i++;
            }
        }
    }

    @NonNull
    private LinearLayout.LayoutParams getInsideParams(final boolean newLine, final boolean lastLine) {
        final LinearLayout.LayoutParams paramsLeft = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        if (newLine && lastLine) {
            paramsLeft.setMargins(pixels, pixelsQuarter, pixels, pixelsQuarter);
        } else if (newLine) {
            paramsLeft.setMargins(pixels, pixelsQuarter, pixels, 0);
        } else if (lastLine) {
            paramsLeft.setMargins(pixels, 0, pixels, pixelsQuarter);
        } else {
            paramsLeft.setMargins(pixels, 0, pixels, 0);
        }
        return paramsLeft;
    }

    private void handleBusRoute(@NonNull final FavoritesViewHolder holder, @NonNull final BusRoute busRoute) {
        holder.stationNameTextView.setText(busRoute.getId());
        holder.favoriteImage.setImageResource(R.drawable.ic_directions_bus_white_24dp);

        final List<BusDetailsDTO> busDetailsDTOs = new ArrayList<>();

        final Map<String, Map<String, List<BusArrival>>> busArrivals = favorites.getBusArrivalsMapped(busRoute.getId());
        for (final Entry<String, Map<String, List<BusArrival>>> entry : busArrivals.entrySet()) {
            // Build data for button outside of the loop
            final String stopName = entry.getKey();
            final String stopNameTrimmed = Util.trimBusStopNameIfNeeded(stopName);
            final Map<String, List<BusArrival>> value = entry.getValue();
            for (final String key2 : value.keySet()) {
                final BusArrival busArrival = value.get(key2).get(0);

                final String boundTitle = busArrival.getRouteDirection();
                final BusDirection.BusDirectionEnum busDirectionEnum = BusDirection.BusDirectionEnum.fromString(boundTitle);
                final BusDetailsDTO busDetails = new BusDetailsDTO();
                busDetails.setBusRouteId(busArrival.getRouteId());
                busDetails.setBound(busDirectionEnum.getShortUpperCase());
                busDetails.setBoundTitle(boundTitle);
                busDetails.setStopId(Integer.toString(busArrival.getStopId()));
                busDetails.setRouteName(busRoute.getName());
                busDetails.setStopName(stopName);
                busDetailsDTOs.add(busDetails);
            }

            boolean newLine = true;
            int i = 0;

            for (final Entry<String, List<BusArrival>> entry2 : value.entrySet()) {
                final LinearLayout.LayoutParams containParams = getInsideParams(newLine, i == value.size() - 1);
                final LinearLayout container = new LinearLayout(context);
                container.setOrientation(LinearLayout.HORIZONTAL);
                container.setLayoutParams(containParams);

                // Left
                final LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                final RelativeLayout left = new RelativeLayout(context);
                left.setLayoutParams(leftParams);

                final RelativeLayout lineIndication = LayoutUtil.createColoredRoundForFavorites(TrainLine.NA);
                int lineId = Util.generateViewId();
                lineIndication.setId(lineId);

                final RelativeLayout.LayoutParams destinationParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                destinationParams.addRule(RelativeLayout.RIGHT_OF, lineId);
                destinationParams.setMargins(pixelsHalf, 0, 0, 0);

                final String bound = BusDirection.BusDirectionEnum.fromString(entry2.getKey()).getShortLowerCase();
                final String leftString = stopNameTrimmed + " " + bound;
                final SpannableString destinationSpannable = new SpannableString(leftString);
                destinationSpannable.setSpan(new RelativeSizeSpan(0.65f), stopNameTrimmed.length(), leftString.length(), 0); // set size
                destinationSpannable.setSpan(new ForegroundColorSpan(GREY_5), 0, leftString.length(), 0); // set color

                final TextView boundCustomTextView = new TextView(context);
                boundCustomTextView.setText(destinationSpannable);
                boundCustomTextView.setSingleLine(true);
                boundCustomTextView.setLayoutParams(destinationParams);

                left.addView(lineIndication);
                left.addView(boundCustomTextView);

                // Right
                final LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                rightParams.setMargins(marginLeftPixel, 0, 0, 0);
                final LinearLayout right = new LinearLayout(context);
                right.setOrientation(LinearLayout.VERTICAL);
                right.setLayoutParams(rightParams);

                final List<BusArrival> buses = entry2.getValue();
                final StringBuilder currentEtas = new StringBuilder();
                for (final BusArrival arri : buses) {
                    currentEtas.append(" ").append(arri.getTimeLeftDueDelay());
                }
                final TextView arrivalText = new TextView(context);
                arrivalText.setText(currentEtas);
                arrivalText.setGravity(Gravity.RIGHT);
                arrivalText.setGravity(Gravity.END);
                arrivalText.setSingleLine(true);
                arrivalText.setTextColor(GREY_5);
                arrivalText.setEllipsize(TextUtils.TruncateAt.END);

                right.addView(arrivalText);

                container.addView(left);
                container.addView(right);

                holder.mainLayout.addView(container);

                newLine = false;
                i++;
            }
        }

        holder.mapButton.setText(activity.getString(R.string.favorites_view_buses));
        holder.detailsButton.setOnClickListener(new View.OnClickListener() {
            // TODO find a solution when stop name are too long (54A stop nam for example)
            @Override
            public void onClick(final View v) {
                if (busDetailsDTOs.size() == 1) {
                    final BusDetailsDTO busDetails = busDetailsDTOs.get(0);
                    new FavoritesAdapter.BusBoundAsyncTask(activity)
                        .execute(busDetails.getBusRouteId(), busDetails.getBound(), busDetails.getBoundTitle(), busDetails.getStopId(), busDetails.getRouteName());
                } else {
                    final PopupBusDetailsFavoritesAdapter ada = new PopupBusDetailsFavoritesAdapter(activity, busDetailsDTOs);
                    final View popupView = activity.getLayoutInflater().inflate(R.layout.popup_bus, null);
                    final ListView listView = (ListView) popupView.findViewById(R.id.details);
                    listView.setAdapter(ada);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setAdapter(ada, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int position) {
                            final BusDetailsDTO busDetails = busDetailsDTOs.get(position);
                            new FavoritesAdapter.BusBoundAsyncTask(activity)
                                .execute(busDetails.getBusRouteId(), busDetails.getBound(), busDetails.getBoundTitle(), busDetails.getStopId(), busDetails.getRouteName());
                        }
                    });
                    final int[] screenSize = Util.getScreenSize();
                    final AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getWindow().setLayout((int) (screenSize[0] * 0.7), ViewGroup.LayoutParams.WRAP_CONTENT);
                }
            }
        });
        holder.mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final Set<String> bounds = new HashSet<>();
                for (final BusDetailsDTO busDetail : busDetailsDTOs) {
                    bounds.add(busDetail.getBound());
                }
                final Intent intent = new Intent(App.getContext(), BusMapActivity.class);
                final Bundle extras = new Bundle();
                extras.putString(activity.getString(R.string.bundle_bus_route_id), busRoute.getId());
                extras.putStringArray(activity.getString(R.string.bundle_bus_bounds), bounds.toArray(new String[bounds.size()]));
                intent.putExtras(extras);
                activity.startActivity(intent);
            }
        });
    }

    private void handleBikeStation(@NonNull final FavoritesViewHolder holder, @NonNull final BikeStation bikeStation) {
        holder.stationNameTextView.setText(bikeStation.getName());
        holder.favoriteImage.setImageResource(R.drawable.ic_directions_bike_white_24dp);
        
        holder.detailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (!Util.isNetworkAvailable()) {
                    Util.showNetworkErrorMessage(activity);
                } else if (bikeStation.getLatitude() != 0 && bikeStation.getLongitude() != 0) {
                    final Intent intent = new Intent(App.getContext(), BikeStationActivity.class);
                    final Bundle extras = new Bundle();
                    extras.putParcelable(activity.getString(R.string.bundle_bike_station), bikeStation);
                    intent.putExtras(extras);
                    activity.startActivity(intent);
                } else {
                    Util.showMessage(activity, R.string.message_not_ready);
                }
            }
        });

        holder.mapButton.setText(activity.getString(R.string.favorites_view_station));
        holder.mapButton.setOnClickListener(new GoogleMapOnClickListener(activity, bikeStation.getLatitude(), bikeStation.getLongitude()));

        final LinearLayout.LayoutParams containerParams = getInsideParams(true, true);
        final LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(containerParams);

        final LinearLayout firstLine = createBikeFirstLine(bikeStation);
        container.addView(firstLine);

        final LinearLayout secondLine = createBikeSecondLine(bikeStation);
        container.addView(secondLine);

        holder.mainLayout.addView(container);
    }

    @NonNull
    private LinearLayout createBikeFirstLine(@NonNull final BikeStation bikeStation) {
        return createBikeLine(bikeStation, true);
    }

    @NonNull
    private LinearLayout createBikeSecondLine(@NonNull final BikeStation bikeStation) {
        return createBikeLine(bikeStation, false);
    }

    @NonNull
    private LinearLayout createBikeLine(@NonNull final BikeStation bikeStation, final boolean firstLine) {
        final LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        final LinearLayout line = new LinearLayout(context);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setLayoutParams(lineParams);

        // Left
        final LinearLayout.LayoutParams leftParam = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        final RelativeLayout left = new RelativeLayout(context);
        left.setLayoutParams(leftParam);

        final RelativeLayout lineIndication = LayoutUtil.createColoredRoundForFavorites(TrainLine.NA);
        int lineId = Util.generateViewId();
        lineIndication.setId(lineId);

        final RelativeLayout.LayoutParams availableParam = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        availableParam.addRule(RelativeLayout.RIGHT_OF, lineId);
        availableParam.setMargins(pixelsHalf, 0, 0, 0);

        final TextView boundCustomTextView = new TextView(context);
        boundCustomTextView.setText(activity.getString(R.string.bike_available_docks));
        boundCustomTextView.setSingleLine(true);
        boundCustomTextView.setLayoutParams(availableParam);
        boundCustomTextView.setTextColor(GREY_5);
        int availableId = Util.generateViewId();
        boundCustomTextView.setId(availableId);

        final RelativeLayout.LayoutParams availableValueParam = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        availableValueParam.addRule(RelativeLayout.RIGHT_OF, availableId);
        availableValueParam.setMargins(pixelsHalf, 0, 0, 0);

        final TextView amountBike = new TextView(context);

        final Integer data;
        if (firstLine) {
            boundCustomTextView.setText(activity.getString(R.string.bike_available_bikes));
            data = bikeStation.getAvailableBikes();
        } else {
            boundCustomTextView.setText(activity.getString(R.string.bike_available_docks));
            data = bikeStation.getAvailableDocks();
        }
        if (data == null) {
            amountBike.setText("?");
            amountBike.setTextColor(ContextCompat.getColor(App.getContext(), R.color.orange));
        } else {
            final String availableBikesText = String.valueOf(data);
            amountBike.setText(availableBikesText);
            if (data == 0) {
                amountBike.setTextColor(ContextCompat.getColor(App.getContext(), R.color.red));
            } else {
                amountBike.setTextColor(ContextCompat.getColor(App.getContext(), R.color.green));
            }
        }
        amountBike.setLayoutParams(availableValueParam);

        left.addView(lineIndication);
        left.addView(boundCustomTextView);
        left.addView(amountBike);
        line.addView(left);
        return line;
    }

    @Override
    public final long getItemId(final int position) {
        return position;
    }

    @Override
    public final int getItemCount() {
        return favorites.size();
    }

    /**
     * Set favorites
     *
     * @param arrivals    the trains arrivals
     * @param busArrivals the buses arrivals
     */
    public final void setArrivalsAndBikeStations(@NonNull final SparseArray<TrainArrival> arrivals, @NonNull final List<BusArrival> busArrivals, @NonNull final List<BikeStation> bikeStations) {
        favorites.setArrivalsAndBikeStations(arrivals, busArrivals, bikeStations);
    }

    public final void setBikeStations(@NonNull final List<BikeStation> bikeStations) {
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
        App.modifyLastUpdate(Calendar.getInstance().getTime());
    }

    /**
     * Refresh udpdated view
     */
    public final void refreshUpdatedView() {
        lastUpdate = getLastUpdateInMinutes();
        notifyDataSetChanged();
    }

    /**
     * Get last update in minutes
     *
     * @return a string
     */
    private String getLastUpdateInMinutes() {
        final StringBuilder lastUpdateInMinutes = new StringBuilder();
        final Date currentDate = Calendar.getInstance().getTime();
        final long[] diff = getTimeDifference(App.getLastUpdate(), currentDate);
        final long hours = diff[0];
        final long minutes = diff[1];
        if (hours == 0 && minutes == 0) {
            lastUpdateInMinutes.append(activity.getString(R.string.time_now));
        } else {
            if (hours == 0) {
                lastUpdateInMinutes.append(minutes).append(activity.getString(R.string.time_min));
            } else {
                lastUpdateInMinutes.append(hours).append(activity.getString(R.string.time_hour)).append(minutes).append(activity.getString(R.string.time_min));
            }
        }
        return lastUpdateInMinutes.toString();
    }

    /**
     * Get time difference between 2 dates
     *
     * @param date1 the date one
     * @param date2 the date two
     * @return a tab containing in 0 the hour and in 1 the minutes
     */
    private long[] getTimeDifference(@NonNull final Date date1, @NonNull final Date date2) {
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
        result[0] = h;
        result[1] = m;
        return result;
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
        private final MainActivity activity;

        public BusBoundAsyncTask(@NonNull final MainActivity activity) {
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
                // It would change the param from Southbound to SOUTH.
                final List<BusStop> busStops = DataHolder.getInstance().getBusData().loadBusStop(busRouteId, boundTitle);

                for (final BusStop bus : busStops) {
                    if (Integer.toString(bus.getId()).equals(stopId)) {
                        res = bus;
                        break;
                    }
                }
            } catch (final ConnectException | ParserException e) {
                this.trackerException = e;
            }
            Util.trackAction(activity, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.url_bus_stop, 0);
            return res;
        }

        @Override
        protected final void onPostExecute(final BusStop busStop) {
            if (trackerException == null) {
                final Intent intent = new Intent(App.getContext(), BusActivity.class);
                final Bundle extras = new Bundle();
                extras.putInt(activity.getString(R.string.bundle_bus_stop_id), busStop.getId());
                extras.putString(activity.getString(R.string.bundle_bus_stop_name), busStop.getName());
                extras.putString(activity.getString(R.string.bundle_bus_route_id), busRouteId);
                extras.putString(activity.getString(R.string.bundle_bus_route_name), busRouteName);
                extras.putString(activity.getString(R.string.bundle_bus_bound), bound);
                extras.putString(activity.getString(R.string.bundle_bus_bound_title), boundTitle);
                extras.putDouble(activity.getString(R.string.bundle_bus_latitude), busStop.getPosition().getLatitude());
                extras.putDouble(activity.getString(R.string.bundle_bus_longitude), busStop.getPosition().getLongitude());

                intent.putExtras(extras);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
            } else {
                Util.showNetworkErrorMessage(activity);
            }
        }
    }
}
