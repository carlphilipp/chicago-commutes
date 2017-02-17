package fr.cph.chicago.core.listener;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import fr.cph.chicago.core.fragment.NearbyFragment;
import fr.cph.chicago.entity.AStation;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Station;
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
        nearbyFragment.getLayoutContainer().removeAllViews();

        loadAllArrivals(station);
        return false;
    }


    private void loadAllArrivals(@NonNull final AStation station) {
        if (station instanceof Station) {
            final Station trainStation = (Station) station;
            nearbyFragment.updateBottomTitleTrain(trainStation.getName());
            ObservableUtil.createTrainArrivalsObservable(nearbyFragment.getContext(), trainStation)
                .subscribe(
                    result -> {
                        Log.i(TAG, "Done Train with " + result);
                        nearbyFragment.addTrainStation(result);
                    },
                    onError -> Log.e(TAG, onError.getMessage(), onError)
                );
        } else if (station instanceof BusStop) {
            final BusStop busStop = (BusStop) station;
            nearbyFragment.updateBottomTitleBus(busStop.getName() + " nop");
            ObservableUtil.createBusArrivalsObservable(nearbyFragment.getContext(), (BusStop) station)
                .subscribe(
                    result -> {
                        Log.i(TAG, "Done Bus with " + result);
                    },
                    onError -> Log.e(TAG, onError.getMessage(), onError)
                );
        } else if (station instanceof BikeStation) {
            final BikeStation bikeStation = (BikeStation) station;
            nearbyFragment.updateBottomTitleBike(bikeStation.getName());
            ObservableUtil.createBikeStationsObservable((BikeStation) station)
                .subscribe(
                    result -> {
                        Log.i(TAG, "Done Bike with " + result);
                        nearbyFragment.addBike(result);
                    },
                    onError -> Log.e(TAG, onError.getMessage(), onError)
                );
        }
    }
}
