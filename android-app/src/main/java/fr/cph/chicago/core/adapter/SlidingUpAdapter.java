package fr.cph.chicago.core.adapter;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.List;
import java.util.Map;

import fr.cph.chicago.R;
import fr.cph.chicago.collector.CommutesCollectors;
import fr.cph.chicago.core.fragment.NearbyFragment;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.dto.BusArrivalRouteDTO;
import fr.cph.chicago.entity.enumeration.BusDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.util.LayoutUtil;
import fr.cph.chicago.util.Util;

public class SlidingUpAdapter {

    private static final int LINE_HEIGHT = 27;
    private static final int HEADER_HEIGHT = 40;

    private final NearbyFragment nearbyFragment;
    // Can't be used as local variable because of ProGuard
    private int[] nbOfLine;

    public SlidingUpAdapter(@NonNull final NearbyFragment nearbyFragment) {
        this.nearbyFragment = nearbyFragment;
        nbOfLine = new int[]{0};
    }

    public void updateTitleTrain(@NonNull final String title) {
        createStationHeaderView(title, R.drawable.ic_train_white_24dp);
    }

    public void updateTitleBus(@NonNull final String title) {
        createStationHeaderView(title, R.drawable.ic_directions_bus_white_24dp);
    }

    public void updateTitleBike(@NonNull final String title) {
        createStationHeaderView(title, R.drawable.ic_directions_bike_white_24dp);
    }

    private void createStationHeaderView(@NonNull final String title, @DrawableRes final int drawable) {
        final LayoutInflater vi = (LayoutInflater) nearbyFragment.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View convertView = vi.inflate(R.layout.nearby_station_main, nearbyFragment.getSlidingUpPanelLayout(), false);

        final TextView stationNameView = (TextView) convertView.findViewById(R.id.station_name);
        final ImageView imageView = (ImageView) convertView.findViewById(R.id.icon);

        stationNameView.setText(title);
        stationNameView.setMaxLines(1);
        stationNameView.setEllipsize(TextUtils.TruncateAt.END);
        imageView.setImageDrawable(ContextCompat.getDrawable(nearbyFragment.getContext(), drawable));

        nearbyFragment.getLayoutContainer().addView(convertView);
    }

    public void addTrainStation(final Optional<TrainArrival> trainArrivalOptional) {
        final LinearLayout linearLayout = getNearbyResultsView();

        /*
         * Handle the case where a user clicks quickly from one marker to another. Will not update anything if a child view is already present,
         * it just mean that the view has been updated already with a faster request.
         */
        if (linearLayout.getChildCount() == 0) {
            int nbOfLine = 0;

            if (trainArrivalOptional.isPresent()) {
                for (TrainLine trainLine : TrainLine.values()) {
                    final List<Eta> etaResult = trainArrivalOptional.get().getEtas(trainLine);
                    final Map<String, String> etas = Stream.of(etaResult).collect(CommutesCollectors.toTrainArrivalByLine());

                    boolean newLine = true;
                    int i = 0;
                    for (final Map.Entry<String, String> entry : etas.entrySet()) {
                        final LinearLayout.LayoutParams containParams = LayoutUtil.getInsideParams(nearbyFragment.getContext(), newLine, i == etas.size() - 1);
                        final LinearLayout container = LayoutUtil.createTrainArrivalsLayout(nearbyFragment.getContext(), containParams, entry, trainLine);

                        linearLayout.addView(container);
                        newLine = false;
                        i++;
                    }
                    nbOfLine = nbOfLine + etas.size();
                }
            } else {
                handleNoResults(linearLayout);
                nbOfLine++;
            }
            nearbyFragment.getSlidingUpPanelLayout().setPanelHeight(getSlidingPanelHeight(nbOfLine));
            updatePanelState();
        }
    }

    public void addBusArrival(final BusArrivalRouteDTO busArrivalRouteDTO) {
        final LinearLayout linearLayout = getNearbyResultsView();

        /*
         * Handle the case where a user clicks quickly from one marker to another. Will not update anything if a child view is already present,
         * it just mean that the view has been updated already with a faster request.
         */
        if (linearLayout.getChildCount() == 0) {
            nbOfLine = new int[]{0};
            Stream.of(busArrivalRouteDTO.entrySet()).forEach(entry -> {
                final String stopNameTrimmed = Util.trimBusStopNameIfNeeded(entry.getKey());
                final Map<String, List<BusArrival>> boundMap = entry.getValue();

                boolean newLine = true;
                int i = 0;

                for (final Map.Entry<String, List<BusArrival>> entry2 : boundMap.entrySet()) {
                    final LinearLayout.LayoutParams containParams = LayoutUtil.getInsideParams(nearbyFragment.getContext(), newLine, i == boundMap.size() - 1);
                    final LinearLayout container = LayoutUtil.createBusArrivalsLayout(nearbyFragment.getContext(), containParams, stopNameTrimmed, BusDirection.BusDirectionEnum.fromString(entry2.getKey()), entry2.getValue());

                    linearLayout.addView(container);
                    newLine = false;
                    i++;
                }
                nbOfLine[0] = nbOfLine[0] + boundMap.size();
            });

            // Handle the case when we have no bus returned.
            if (busArrivalRouteDTO.size() == 0) {
                handleNoResults(linearLayout);
                nbOfLine[0]++;
            }
            nearbyFragment.getSlidingUpPanelLayout().setPanelHeight(getSlidingPanelHeight(nbOfLine[0]));
            updatePanelState();
        }
    }

    public void addBike(final Optional<BikeStation> bikeStationOptional) {
        final LinearLayout linearLayout = getNearbyResultsView();
        /*
         * Handle the case where a user clicks quickly from one marker to another. Will not update anything if a child view is already present,
         * it just mean that the view has been updated already with a faster request.
         */
        if (linearLayout.getChildCount() == 0) {
            final LinearLayout bikeResultLayout = LayoutUtil.createBikeLayout(nearbyFragment.getContext(), bikeStationOptional.get());
            linearLayout.addView(bikeResultLayout);
            nearbyFragment.getSlidingUpPanelLayout().setPanelHeight(getSlidingPanelHeight(2));
            updatePanelState();
        }
    }

    private LinearLayout getNearbyResultsView() {
        final RelativeLayout relativeLayout = (RelativeLayout) nearbyFragment.getLayoutContainer().getChildAt(0);
        return (LinearLayout) relativeLayout.findViewById(R.id.nearby_results);
    }

    private void handleNoResults(final LinearLayout linearLayout) {
        final LinearLayout.LayoutParams containParams = LayoutUtil.getInsideParams(nearbyFragment.getContext(), true, true);
        final LinearLayout container = LayoutUtil.createBusArrivalsNoResult(nearbyFragment.getContext(), containParams);
        linearLayout.addView(container);
    }

    private int getSlidingPanelHeight(final int nbLine) {
        int line = Util.convertDpToPixel(nearbyFragment.getContext(), LINE_HEIGHT);
        int header = Util.convertDpToPixel(nearbyFragment.getContext(), HEADER_HEIGHT);
        return (line * nbLine) + header;
    }

    private void updatePanelState() {
        Log.i("DERP", "8");
        if (nearbyFragment.getSlidingUpPanelLayout().getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
            nearbyFragment.getSlidingUpPanelLayout().setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
        nearbyFragment.showProgress(false);
        Log.i("DERP", "9");
    }
}