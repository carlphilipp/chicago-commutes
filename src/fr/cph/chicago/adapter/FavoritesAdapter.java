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
import android.text.TextUtils;
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
import android.widget.Toast;

import java.util.ArrayList;
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
    //private String lastUpdate;
    private int line1Padding;
    private int stopsPaddingTop;
    private int pixels;
    private int pixelsHalf;

    public FavoritesAdapter(@NonNull final MainActivity activity) {
        this.context = ChicagoTracker.getContext();

        this.mainActivity = activity;
        this.favorites = new Favorites();

        this.paramsLayout = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        this.paramsTextView = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        this.line1Padding = (int) context.getResources().getDimension(R.dimen.activity_station_stops_line1_padding_color);
        this.stopsPaddingTop = (int) context.getResources().getDimension(R.dimen.activity_station_stops_padding_top);
        this.pixels = Util.convertDpToPixel(mainActivity, 16);
        this.pixelsHalf = pixels / 2;
    }


    static class FavoritesViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout mainLayout;
        public LinearLayout buttonsLayout;
        public TextView stationNameTextView;
        public ImageView favoriteImage;
        public Button detailsButton;
        public Button mapButton;

        public FavoritesViewHolder(final View view) {
            super(view);
            this.mainLayout = (LinearLayout) view.findViewById(R.id.favorites_arrival_layout);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                this.mainLayout.setBackground(ContextCompat.getDrawable(ChicagoTracker.getContext(), R.drawable.any_selector));
            }
            this.buttonsLayout = (LinearLayout) view.findViewById(R.id.favorites_buttons);
            this.favoriteImage = (ImageView) view.findViewById(R.id.favorites_icon);

            this.stationNameTextView = (TextView) view.findViewById(R.id.favorites_station_name);
            this.stationNameTextView.setLines(1);
            this.stationNameTextView.setEllipsize(TextUtils.TruncateAt.END);

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
                final Intent intent = new Intent(ChicagoTracker.getContext(), StationActivity.class);
                extras.putInt(mainActivity.getString(R.string.bundle_train_stationId), stationId);
                intent.putExtras(extras);
                mainActivity.startActivity(intent);
            }
        });
        holder.mapButton.setOnClickListener(new FavoritesTrainOnClickListener(mainActivity, stationId, trainLines));

        // TODO create a method to check if empty
        if (favorites.getTrainArrival(stationId) != null) {
            for (final TrainLine trainLine : trainLines) {
                boolean newLine = true;
                final Map<String, StringBuilder> etas = favorites.getTrainArrivalByLine(stationId, trainLine);
                int i = 0;
                for (final Entry<String, StringBuilder> entry : etas.entrySet()) {
                    final LinearLayout.LayoutParams insideParam = getInsideParams(newLine, i == etas.size() - 1);
                    final RelativeLayout.LayoutParams paramsRight = getRightParams();

                    final RelativeLayout insideLayout = new RelativeLayout(context);
                    insideLayout.setLayoutParams(insideParam);

                    final LinearLayout leftLayout = new LinearLayout(context);
                    leftLayout.setOrientation(LinearLayout.HORIZONTAL);
                    leftLayout.setGravity(Gravity.CENTER);

                    final LinearLayout lineIndication = new LinearLayout(context);
                    final LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    params.height = 30;
                    params.width = 30;
                    lineIndication.setBackgroundColor(trainLine.getColor());
                    lineIndication.setLayoutParams(params);

                    leftLayout.addView(lineIndication);

                    final String destination = "  " + entry.getKey();
                    final TextView destinationTextView = new TextView(context);
                    destinationTextView.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.grey_5));
                    destinationTextView.setText(destination);
                    leftLayout.addView(destinationTextView);

                    insideLayout.addView(leftLayout);

                    final TextView listTiming = new TextView(context);
                    final StringBuilder currentEtas = entry.getValue();
                    listTiming.setText(currentEtas.toString());
                    listTiming.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.grey));
                    listTiming.setLines(1);
                    listTiming.setEllipsize(TextUtils.TruncateAt.END);

                    listTiming.setLayoutParams(paramsRight);
                    insideLayout.addView(listTiming);
                    holder.mainLayout.addView(insideLayout);

                    newLine = false;
                    i++;
                }
            }
        }
    }

    private LinearLayout.LayoutParams getInsideParams(boolean newLine, boolean lastLine) {
        final LinearLayout.LayoutParams paramsLeft = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        if (newLine && lastLine) {
            paramsLeft.setMargins(pixels, pixelsHalf, pixels, pixelsHalf);
        } else if (newLine) {
            paramsLeft.setMargins(pixels, pixelsHalf, pixels, 0);
        } else if (lastLine) {
            paramsLeft.setMargins(pixels, 0, pixels, pixelsHalf);
        } else {
            paramsLeft.setMargins(pixels, 0, pixels, 0);
        }
        return paramsLeft;
    }

    private RelativeLayout.LayoutParams getRightParams() {
        final RelativeLayout.LayoutParams paramsRight = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            paramsRight.addRule(RelativeLayout.ALIGN_PARENT_END);
        }
        paramsRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        return paramsRight;
    }

    private void handleBusRoute(@NonNull final FavoritesViewHolder holder, @NonNull final BusRoute busRoute) {
        holder.stationNameTextView.setText(busRoute.getId());
        holder.favoriteImage.setImageResource(R.drawable.ic_directions_bus_white_24dp);

        final List<BusDetailsDTO> busDetailses = new ArrayList<>();

        final Map<String, Map<String, List<BusArrival>>> busArrivals = favorites.getBusArrivalsMapped(busRoute.getId());
        for (final Entry<String, Map<String, List<BusArrival>>> entry : busArrivals.entrySet()) {
            final LinearLayout llh = new LinearLayout(context);
            llh.setLayoutParams(paramsLayout);
            llh.setOrientation(LinearLayout.HORIZONTAL);
            llh.setPadding(line1Padding, stopsPaddingTop, 0, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                llh.setBackground(ContextCompat.getDrawable(ChicagoTracker.getContext(), R.drawable.any_selector));
            }

            // Build data for button outside of the loop
            final String stopName = entry.getKey();
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
                busDetailses.add(busDetails);
            }

            llh.setOnClickListener(new FavoritesBusOnClickListener(mainActivity, null, busRoute, value));

            boolean newLine = true;
            int i = 0;

            for (final Entry<String, List<BusArrival>> entry2 : value.entrySet()) {
                final LinearLayout.LayoutParams insideParam = getInsideParams(newLine, i == value.size() - 1);
                final RelativeLayout.LayoutParams paramsRight = getRightParams();

                final RelativeLayout insideLayout = new RelativeLayout(context);
                insideLayout.setLayoutParams(insideParam);

                final LinearLayout leftLayout = new LinearLayout(context);
                leftLayout.setOrientation(LinearLayout.HORIZONTAL);
                leftLayout.setGravity(Gravity.CENTER);

                final LinearLayout lineIndication = new LinearLayout(context);
                final LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params.height = 30;
                params.width = 30;
                lineIndication.setBackgroundColor(TrainLine.NA.getColor());
                lineIndication.setLayoutParams(params);

                leftLayout.addView(lineIndication);

                final String bound = BusDirection.BusDirectionEnum.fromString(entry2.getKey()).getShortLowerCase();
                final List<BusArrival> buses = entry2.getValue();

                final RelativeLayout.LayoutParams wrapParam = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                final RelativeLayout stopLayout = new RelativeLayout(context);
                stopLayout.setLayoutParams(wrapParam);

                final String stopNameCusto = "  " + stopName;
                final TextView destinationTextView = new TextView(context);
                destinationTextView.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.grey_5));
                destinationTextView.setText(stopNameCusto);
                int id = Util.generateViewId();
                destinationTextView.setId(id);
                final String boundCusto = " " + bound;
                final TextView boundCustomTextView = new TextView(context);
                boundCustomTextView.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.grey_5));
                boundCustomTextView.setText(boundCusto);
                boundCustomTextView.setTextSize(10);

                // Param to put bound textview at the right of stop name
                final RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                p.addRule(RelativeLayout.RIGHT_OF, id);
                p.addRule(RelativeLayout.ALIGN_BASELINE, id);
                boundCustomTextView.setLayoutParams(p);

                stopLayout.addView(destinationTextView);
                stopLayout.addView(boundCustomTextView);
                leftLayout.addView(stopLayout);

                insideLayout.addView(leftLayout);

                // Eta of arrival trains
                final TextView listTiming = new TextView(context);
                final StringBuilder currentEtas = new StringBuilder();
                for (final BusArrival arri : buses) {
                    currentEtas.append(" ").append(arri.getTimeLeftDueDelay());
                }
                listTiming.setText(currentEtas.toString());
                listTiming.setTextColor(ContextCompat.getColor(ChicagoTracker.getContext(), R.color.grey));
                listTiming.setLines(1);
                listTiming.setEllipsize(TextUtils.TruncateAt.END);

                listTiming.setLayoutParams(paramsRight);
                insideLayout.addView(listTiming);
                holder.mainLayout.addView(insideLayout);

                newLine = false;
                i++;
            }
        }

        holder.detailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (busDetailses.size() == 1) {
                    final BusDetailsDTO busDetails = busDetailses.get(0);
                    new FavoritesAdapter.BusBoundAsyncTask(mainActivity).execute(busDetails.getBusRouteId(), busDetails.getBound(), busDetails.getBoundTitle(), busDetails.getStopId(), busDetails.getRouteName());
                } else {
                    final PopupBusDetailsFavoritesAdapter ada = new PopupBusDetailsFavoritesAdapter(mainActivity, busDetailses);
                    final View popupView = mainActivity.getLayoutInflater().inflate(R.layout.popup_bus, null);
                    final ListView listView = (ListView) popupView.findViewById(R.id.details);
                    listView.setAdapter(ada);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
                    builder.setAdapter(ada, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int position) {
                            final BusDetailsDTO busDetails = busDetailses.get(position);
                            new FavoritesAdapter.BusBoundAsyncTask(mainActivity).execute(busDetails.getBusRouteId(), busDetails.getBound(), busDetails.getBoundTitle(), busDetails.getStopId(), busDetails.getRouteName());
                        }
                    });
                    final int[] screenSize = Util.getScreenSize();
                    final AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getWindow().setLayout((int) (screenSize[0] * 0.7), ViewGroup.LayoutParams.WRAP_CONTENT);
                }
            }
        });
        //holder.mapButton.setOnClickListener();
    }

    private void handleBikeStation(@NonNull final FavoritesViewHolder holder, @NonNull final BikeStation bikeStation) {
        holder.stationNameTextView.setText(bikeStation.getName());
        holder.favoriteImage.setImageResource(R.drawable.ic_directions_bike_white_24dp);

        // TODO extract those identical listener
        holder.detailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final boolean isNetworkAvailable = Util.isNetworkAvailable();
                if (!isNetworkAvailable) {
                    Toast.makeText(mainActivity, "No network connection detected!", Toast.LENGTH_LONG).show();
                } else if (bikeStation.getLatitude() != 0 && bikeStation.getLongitude() != 0) {
                    final Intent intent = new Intent(ChicagoTracker.getContext(), BikeStationActivity.class);
                    final Bundle extras = new Bundle();
                    extras.putParcelable(mainActivity.getString(R.string.bundle_bike_station), bikeStation);
                    intent.putExtras(extras);
                    mainActivity.startActivity(intent);
                } else {
                    Toast.makeText(mainActivity, "Not ready yet. Please try again in few seconds!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // TODO create a listener that open Google map with the position of the Station
        holder.mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final boolean isNetworkAvailable = Util.isNetworkAvailable();
                if (!isNetworkAvailable) {
                    Toast.makeText(mainActivity, "No network connection detected!", Toast.LENGTH_LONG).show();
                } else if (bikeStation.getLatitude() != 0 && bikeStation.getLongitude() != 0) {
                    final Intent intent = new Intent(ChicagoTracker.getContext(), BikeStationActivity.class);
                    final Bundle extras = new Bundle();
                    extras.putParcelable(mainActivity.getString(R.string.bundle_bike_station), bikeStation);
                    intent.putExtras(extras);
                    mainActivity.startActivity(intent);
                } else {
                    Toast.makeText(mainActivity, "Not ready yet. Please try again in few seconds!", Toast.LENGTH_SHORT).show();
                }
            }
        });

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
    }

    @Override
    public final long getItemId(final int position) {
        return position;
    }

    @Override
    public int getItemCount() {
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
        ChicagoTracker.modifyLastUpdate(Calendar.getInstance().getTime());
    }

    /**
     * Refresh udpdated view
     */
    public final void refreshUpdatedView() {
        final Date lastUpdate = ChicagoTracker.getLastUpdate();
        // FIXME What the hell? Did it ever worked?
        if (!String.valueOf(getLastUpdateInMinutes(lastUpdate)).equals(lastUpdate)) {
            // this.lastUpdate = String.valueOf(getLastUpdateInMinutes(lastUpdate));
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
    private String getLastUpdateInMinutes(@NonNull final Date lastUpdate) {
        final String res;
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
        return res;
    }
}
