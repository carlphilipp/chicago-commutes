package fr.cph.chicago.core.listener;

import android.support.annotation.NonNull;
import android.util.Log;

import com.annimon.stream.Stream;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import fr.cph.chicago.core.fragment.NearbyFragment;
import fr.cph.chicago.data.MarkerDataHolder;
import fr.cph.chicago.entity.AStation;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.dto.BusArrivalRouteDTO;
import fr.cph.chicago.rx.observable.ObservableUtil;

public class OnMarkerClickListener implements GoogleMap.OnMarkerClickListener {

    private static final String TAG = OnMarkerClickListener.class.getSimpleName();

    private final NearbyFragment nearbyFragment;
    private final MarkerDataHolder markerDataHolder;

    public OnMarkerClickListener(final MarkerDataHolder markerDataHolder, final NearbyFragment nearbyFragment) {
        this.markerDataHolder = markerDataHolder;
        this.nearbyFragment = nearbyFragment;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        nearbyFragment.showProgress(true);
        final AStation station = markerDataHolder.getStation(marker);
        if (nearbyFragment.getLayoutContainer().getChildCount() != 0) {
            nearbyFragment.getLayoutContainer().removeViewAt(0);
        }
        loadArrivals(station);
        return false;
    }


    private void loadArrivals(@NonNull final AStation station) {
        if (station instanceof Station) {
            loadTrainArrivals((Station) station);
        } else if (station instanceof BusStop) {
            loadBusArrivals((BusStop) station);
        } else if (station instanceof BikeStation) {
            loadBikes((BikeStation) station);
        }
    }

    private void loadTrainArrivals(final Station trainStation) {
        nearbyFragment.getSlidingUpAdapter().updateTitleTrain(trainStation.getName());
        ObservableUtil.INSTANCE.createTrainArrivalsObservable(nearbyFragment.getContext(), trainStation)
            .subscribe(
                nearbyFragment.getSlidingUpAdapter()::addTrainStation,
                onError -> Log.e(TAG, onError.getMessage(), onError)
            );
    }

    private void loadBusArrivals(final BusStop busStop) {
        nearbyFragment.getSlidingUpAdapter().updateTitleBus(busStop.getName());
        ObservableUtil.INSTANCE.createBusArrivalsObservable(nearbyFragment.getContext(), busStop)
            .subscribe(
                result -> {
                    final BusArrivalRouteDTO busArrivalRouteDTO = new BusArrivalRouteDTO();
                    Stream.of(result).forEach(busArrivalRouteDTO::addBusArrival);
                    nearbyFragment.getSlidingUpAdapter().addBusArrival(busArrivalRouteDTO);
                },
                onError -> Log.e(TAG, onError.getMessage(), onError)
            );
    }

    private void loadBikes(final BikeStation bikeStation) {
        nearbyFragment.getSlidingUpAdapter().updateTitleBike(bikeStation.getName());
        ObservableUtil.INSTANCE.createBikeStationsObservable(bikeStation)
            .subscribe(
                nearbyFragment.getSlidingUpAdapter()::addBike,
                onError -> Log.e(TAG, onError.getMessage(), onError)
            );
    }
}
