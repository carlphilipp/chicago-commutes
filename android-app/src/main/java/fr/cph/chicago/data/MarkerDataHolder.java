package fr.cph.chicago.data;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Map;

import fr.cph.chicago.entity.AStation;

public class MarkerDataHolder {
    private final Map<LatLng, MarkerDataHolder.MarkerHolder> data;

    public MarkerDataHolder() {
        data = new HashMap<>();
    }

    public void addData(final Marker marker, final AStation station) {
        final MarkerDataHolder.MarkerHolder markerHolder = new MarkerDataHolder.MarkerHolder();
        markerHolder.setMarker(marker);
        markerHolder.setStation(station);
        final LatLng latLng = marker.getPosition();
        data.put(latLng, markerHolder);
    }

    public void clear() {
        data.clear();
    }

    public AStation getStation(final Marker marker) {
        return data.get(marker.getPosition()).getStation();
    }

    private class MarkerHolder {
        private Marker marker;
        private AStation station;

        public AStation getStation() {
            return station;
        }

        public void setStation(AStation station) {
            this.station = station;
        }

        public Marker getMarker() {
            return marker;
        }

        public void setMarker(Marker marker) {
            this.marker = marker;
        }
    }
}
