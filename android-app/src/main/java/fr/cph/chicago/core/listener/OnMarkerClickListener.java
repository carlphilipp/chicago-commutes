package fr.cph.chicago.core.listener;

import android.support.annotation.NonNull;
import android.util.Log;

import com.annimon.stream.Stream;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import fr.cph.chicago.core.fragment.NearbyFragment;
import fr.cph.chicago.entity.AStation;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.dto.BusArrivalMappedDTO;
import fr.cph.chicago.rx.observable.ObservableUtil;

public class OnMarkerClickListener implements GoogleMap.OnMarkerClickListener {

    private static final String TAG = OnMarkerClickListener.class.getSimpleName();

    private final NearbyFragment nearbyFragment;
    private final NearbyFragment.MarkerDataHolder markerDataHolder;

    public OnMarkerClickListener(final NearbyFragment.MarkerDataHolder markerDataHolder, final NearbyFragment nearbyFragment) {
        this.markerDataHolder = markerDataHolder;
        this.nearbyFragment = nearbyFragment;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Log.i(TAG, "Marker selected: " + marker.getTag().toString());
        final AStation station = markerDataHolder.getStation(marker);
        nearbyFragment.getSlidingUpPanelLayout().setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        if (nearbyFragment.getLayoutContainer().getChildCount() != 0) {
            nearbyFragment.getLayoutContainer().removeViewAt(0);
        }

        loadAllArrivals(station);
        return false;
    }


    private void loadAllArrivals(@NonNull final AStation station) {
        if (station instanceof Station) {
            final Station trainStation = (Station) station;
            nearbyFragment.updateBottomTitleTrain(trainStation.getName());
            ObservableUtil.createTrainArrivalsObservable(nearbyFragment.getContext(), trainStation)
                .subscribe(
                    nearbyFragment::addTrainStation,
                    onError -> Log.e(TAG, onError.getMessage(), onError)
                );
        } else if (station instanceof BusStop) {
            final BusStop busStop = (BusStop) station;
            //nearbyFragment.updateBottomTitleBus("... loading");
            ObservableUtil.createBusArrivalsObservable(nearbyFragment.getContext(), (BusStop) station)
                .subscribe(
                    result -> {
                        nearbyFragment.updateBottomTitleBus(!result.isEmpty() ? result.get(0).getRouteId() : busStop.getName());
                        final BusArrivalMappedDTO busArrivalDTO = new BusArrivalMappedDTO();
                        Stream.of(result).forEach(busArrivalDTO::addBusArrival);
                        nearbyFragment.addBusArrival(busArrivalDTO);
                    },
                    onError -> Log.e(TAG, onError.getMessage(), onError)
                );
        } else if (station instanceof BikeStation) {
            final BikeStation bikeStation = (BikeStation) station;
            nearbyFragment.updateBottomTitleBike(bikeStation.getName());
            ObservableUtil.createBikeStationsObservable((BikeStation) station)
                .subscribe(
                    nearbyFragment::addBike,
                    onError -> Log.e(TAG, onError.getMessage(), onError)
                );
        }
    }
}
