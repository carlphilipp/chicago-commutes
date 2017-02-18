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

package fr.cph.chicago.core.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.cph.chicago.R;
import fr.cph.chicago.core.App;
import fr.cph.chicago.core.activity.BikeStationActivity;
import fr.cph.chicago.core.activity.BusMapActivity;
import fr.cph.chicago.core.activity.MainActivity;
import fr.cph.chicago.core.activity.StationActivity;
import fr.cph.chicago.core.activity.TrainMapActivity;
import fr.cph.chicago.core.listener.BusStopOnClickListener;
import fr.cph.chicago.core.listener.GoogleMapOnClickListener;
import fr.cph.chicago.data.FavoritesData;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.dto.BusArrivalMappedDTO;
import fr.cph.chicago.entity.dto.BusDetailsDTO;
import fr.cph.chicago.entity.enumeration.BusDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.util.LayoutUtil;
import fr.cph.chicago.util.Util;

import static java.util.Map.Entry;

/**
 * Adapter that will handle favoritesData
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
// TODO to analyze and refactor
public final class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder> {

    private final int grey5;
    private final Context context;
    private final MainActivity activity;
    private final int marginLeftPixel;
    private final int pixels;
    private final int pixelsHalf;
    private final int pixelsQuarter;

    private String lastUpdate;

    public FavoritesAdapter(@NonNull final MainActivity activity) {
        this.context = activity.getApplicationContext();
        this.grey5 = ContextCompat.getColor(context, R.color.grey_5);

        this.activity = activity;

        this.marginLeftPixel = Util.convertDpToPixel(context, 10);
        this.pixels = Util.convertDpToPixel(context, 16);
        this.pixelsHalf = pixels / 2;
        this.pixelsQuarter = pixels / 4;
    }

    static class FavoritesViewHolder extends RecyclerView.ViewHolder {
        final ViewGroup parent;
        final LinearLayout mainLayout;
        final RelativeLayout buttonsLayout;
        final TextView lastUpdateTextView;
        final TextView stationNameTextView;
        final ImageView favoriteImage;
        final Button detailsButton;
        final Button mapButton;

        private FavoritesViewHolder(final View view, final ViewGroup parent) {
            super(view);
            this.parent = parent;
            this.mainLayout = (LinearLayout) view.findViewById(R.id.favorites_arrival_layout);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                this.mainLayout.setBackground(ContextCompat.getDrawable(parent.getContext(), R.drawable.any_selector));
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
        return new FavoritesViewHolder(v, parent);
    }

    @Override
    public void onBindViewHolder(final FavoritesViewHolder holder, final int position) {
        resetData(holder);
        final Optional<?> optional = FavoritesData.INSTANCE.getObject(position, context);
        holder.lastUpdateTextView.setText(lastUpdate);
        if (optional.isPresent()) {
            final Object object = optional.get();
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
        holder.detailsButton.setOnClickListener(v -> {
            if (!Util.isNetworkAvailable(context)) {
                Util.showNetworkErrorMessage(activity);
            } else {
                // Start station activity
                final Bundle extras = new Bundle();
                final Intent intent = new Intent(context, StationActivity.class);
                extras.putInt(activity.getString(R.string.bundle_train_stationId), stationId);
                intent.putExtras(extras);
                activity.startActivity(intent);
            }
        });

        holder.mapButton.setText(activity.getString(R.string.favorites_view_trains));
        holder.mapButton.setOnClickListener(v -> {
            if (!Util.isNetworkAvailable(context)) {
                Util.showNetworkErrorMessage(activity);
            } else {
                if (trainLines.size() == 1) {
                    startActivity(trainLines.iterator().next());
                } else {
                    final List<Integer> colors = new ArrayList<>();
                    final List<String> values = Stream.of(trainLines)
                        .flatMap(line -> {
                            final int color = line != TrainLine.YELLOW ? line.getColor() : ContextCompat.getColor(context, R.color.yellowLine);
                            colors.add(color);
                            return Stream.of(line.toStringWithLine());
                        }).collect(Collectors.toList());

                    final PopupFavoritesTrainAdapter ada = new PopupFavoritesTrainAdapter(activity, values, colors);

                    final List<TrainLine> lines = new ArrayList<>();
                    lines.addAll(trainLines);

                    final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setAdapter(ada, (dialog, position) -> startActivity(lines.get(position)));

                    final AlertDialog dialog = builder.create();
                    dialog.show();
                    if (dialog.getWindow() != null) {
                        dialog.getWindow().setLayout((int) (App.getScreenWidth() * 0.7), LayoutParams.WRAP_CONTENT);
                    }
                }
            }
        });

        Stream.of(trainLines).forEach(trainLine -> {
            boolean newLine = true;
            int i = 0;
            final Map<String, String> etas = FavoritesData.INSTANCE.getTrainArrivalByLine(stationId, trainLine);
            for (final Entry<String, String> entry : etas.entrySet()) {
                final LinearLayout.LayoutParams containParams = LayoutUtil.getInsideParams(activity.getApplicationContext(), newLine, i == etas.size() - 1);
                final LinearLayout container = LayoutUtil.createTrainArrivalsLayout(context, containParams, entry, trainLine);

                holder.mainLayout.addView(container);

                newLine = false;
                i++;
            }
        });
    }

    private void startActivity(final TrainLine trainLine) {
        final Bundle extras = new Bundle();
        final Intent intent = new Intent(context, TrainMapActivity.class);
        extras.putString(activity.getString(R.string.bundle_train_line), trainLine.toTextString());
        intent.putExtras(extras);
        activity.startActivity(intent);
    }

    private void handleBusRoute(@NonNull final FavoritesViewHolder holder, @NonNull final BusRoute busRoute) {
        holder.stationNameTextView.setText(busRoute.getId());
        holder.favoriteImage.setImageResource(R.drawable.ic_directions_bus_white_24dp);

        final List<BusDetailsDTO> busDetailsDTOs = new ArrayList<>();

        final BusArrivalMappedDTO busArrivalDTO = FavoritesData.INSTANCE.getBusArrivalsMapped(busRoute.getId(), context);

        for (final Entry<String, Map<String, List<BusArrival>>> entry : busArrivalDTO.entrySet()) {
            final String stopName = entry.getKey();
            final String stopNameTrimmed = Util.trimBusStopNameIfNeeded(stopName);
            final Map<String, List<BusArrival>> boundMap = entry.getValue();

            boolean newLine = true;
            int i = 0;

            for (final Entry<String, List<BusArrival>> entry2 : boundMap.entrySet()) {

                // Build data for button outside of the loop
                final BusArrival busArrival = entry2.getValue().get(0);
                final String boundTitle = busArrival.getRouteDirection();
                final BusDirection.BusDirectionEnum busDirectionEnum = BusDirection.BusDirectionEnum.fromString(boundTitle);
                final BusDetailsDTO busDetails = BusDetailsDTO.builder()
                    .busRouteId(busArrival.getRouteId())
                    .bound(busDirectionEnum.getShortUpperCase())
                    .boundTitle(boundTitle)
                    .stopId(Integer.toString(busArrival.getStopId()))
                    .routeName(busRoute.getName())
                    .stopName(stopName)
                    .build();
                busDetailsDTOs.add(busDetails);

                // Build UI
                final LinearLayout.LayoutParams containParams = LayoutUtil.getInsideParams(activity.getApplicationContext(), newLine, i == boundMap.size() - 1);
                final LinearLayout container = LayoutUtil.createBusArrivalsLayout(activity.getApplicationContext(), containParams, stopNameTrimmed, entry2);

                holder.mainLayout.addView(container);

                newLine = false;
                i++;
            }
        }

        holder.mapButton.setText(activity.getString(R.string.favorites_view_buses));
        holder.detailsButton.setOnClickListener(new BusStopOnClickListener(activity, holder.parent, busDetailsDTOs));
        holder.mapButton.setOnClickListener(v -> {
            if (!Util.isNetworkAvailable(context)) {
                Util.showNetworkErrorMessage(activity);
            } else {
                final Set<String> bounds = Stream.of(busDetailsDTOs).map(BusDetailsDTO::getBound).collect(Collectors.toSet());
                final Intent intent = new Intent(activity.getApplicationContext(), BusMapActivity.class);
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

        holder.detailsButton.setOnClickListener(v -> {
            if (!Util.isNetworkAvailable(context)) {
                Util.showNetworkErrorMessage(activity);
            } else if (bikeStation.getLatitude() != 0 && bikeStation.getLongitude() != 0) {
                final Intent intent = new Intent(activity.getApplicationContext(), BikeStationActivity.class);
                final Bundle extras = new Bundle();
                extras.putParcelable(activity.getString(R.string.bundle_bike_station), bikeStation);
                intent.putExtras(extras);
                activity.startActivity(intent);
            } else {
                Util.showMessage(activity, R.string.message_not_ready);
            }
        });

        holder.mapButton.setText(activity.getString(R.string.favorites_view_station));
        holder.mapButton.setOnClickListener(new GoogleMapOnClickListener(bikeStation.getLatitude(), bikeStation.getLongitude()));

        final LinearLayout bikeResultLayout = LayoutUtil.createBikeLayout(activity.getApplicationContext(), bikeStation);

        holder.mainLayout.addView(bikeResultLayout);
    }

    @Override
    public final long getItemId(final int position) {
        return position;
    }

    @Override
    public final int getItemCount() {
        return FavoritesData.INSTANCE.size();
    }

    /**
     * Set favoritesData
     */
    public final void setFavorites() {
        // TODO delete that method but see if we can pass context properly
        FavoritesData.INSTANCE.setFavorites(context);
    }

    /**
     * Refresh date update
     */
    public final void refreshUpdated() {
        App.setLastUpdate(Calendar.getInstance().getTime());
    }

    /**
     * Refresh updated view
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
}
