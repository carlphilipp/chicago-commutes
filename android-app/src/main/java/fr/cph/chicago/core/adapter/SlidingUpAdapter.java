package fr.cph.chicago.core.adapter;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
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
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.dto.BusArrivalRouteDTO;
import fr.cph.chicago.entity.enumeration.BusDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.util.LayoutUtil;
import fr.cph.chicago.util.Util;

public class SlidingUpAdapter {

    private static final int LINE_HEIGHT = 77;
    private static final int HEADER_HEIGHT = 130;

    private final NearbyFragment nearbyFragment;

    public SlidingUpAdapter(@NonNull final NearbyFragment nearbyFragment) {
        this.nearbyFragment = nearbyFragment;
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
        final RelativeLayout relativeLayout = (RelativeLayout) nearbyFragment.getLayoutContainer().getChildAt(0);
        final LinearLayout linearLayout = (LinearLayout) relativeLayout.findViewById(R.id.nearby_results);

        final int[] nbOfLine = {0};

        if (trainArrivalOptional.isPresent()) {
            Stream.of(TrainLine.values()).forEach(trainLine -> {
                final Map<String, String> etas = Stream.of(trainArrivalOptional.get().getEtas(trainLine)).collect(CommutesCollectors.toTrainArrivalByLine());
                boolean newLine = true;
                int i = 0;
                for (final Map.Entry<String, String> entry : etas.entrySet()) {
                    final LinearLayout.LayoutParams containParams = LayoutUtil.getInsideParams(nearbyFragment.getContext(), newLine, i == etas.size() - 1);
                    final LinearLayout container = LayoutUtil.createTrainArrivalsLayout(nearbyFragment.getContext(), containParams, entry, trainLine);

                    linearLayout.addView(container);
                    newLine = false;
                    i++;
                }
                nbOfLine[0] = nbOfLine[0] + etas.size();
            });
        } else {
            // TODO do something here I guess
        }
        updatePanelStateAndHeight((nbOfLine[0]));
    }

    private int getSlidingPanelHeight(final int nbLine) {
        return (LINE_HEIGHT * nbLine) + HEADER_HEIGHT;
    }

    public void addBusArrival(final BusArrivalRouteDTO busArrivalRouteDTO) {
        final RelativeLayout relativeLayout = (RelativeLayout) nearbyFragment.getLayoutContainer().getChildAt(0);
        final LinearLayout linearLayout = (LinearLayout) relativeLayout.findViewById(R.id.nearby_results);

        final int[] nbOfLine = {0};

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
            final LinearLayout.LayoutParams containParams = LayoutUtil.getInsideParams(nearbyFragment.getContext(), true, true);
            final LinearLayout container = LayoutUtil.createBusArrivalsNoResult(nearbyFragment.getContext(), containParams, "No results");
            linearLayout.addView(container);
            nbOfLine[0]++;
        }
        updatePanelStateAndHeight(nbOfLine[0]);
    }

    public void addBike(final Optional<BikeStation> bikeStationOptional) {
        final RelativeLayout relativeLayout = (RelativeLayout) nearbyFragment.getLayoutContainer().getChildAt(0);
        final LinearLayout linearLayout = (LinearLayout) relativeLayout.findViewById(R.id.nearby_results);
        final LinearLayout bikeResultLayout = LayoutUtil.createBikeLayout(nearbyFragment.getContext(), bikeStationOptional.get());
        linearLayout.addView(bikeResultLayout);
        updatePanelStateAndHeight(2);
    }

    private void updatePanelStateAndHeight(final int height) {
        nearbyFragment.getSlidingUpPanelLayout().setPanelHeight(getSlidingPanelHeight(height));
        if (nearbyFragment.getSlidingUpPanelLayout().getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
            nearbyFragment.getSlidingUpPanelLayout().setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
    }
}
