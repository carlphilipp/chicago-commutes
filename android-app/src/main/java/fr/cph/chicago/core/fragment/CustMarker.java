package fr.cph.chicago.core.fragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import lombok.Builder;
import lombok.Setter;

@Builder
public class CustMarker {
    @Setter
    private Marker marker;

    public void remove() {
        marker.remove();
    }

    public LatLng getPosition() {
        return marker.getPosition();
    }

    @Override
    public int hashCode() {
        int result = 32 + Double.valueOf(marker.getPosition().latitude).hashCode();
        result = 32 * result + Double.valueOf(marker.getPosition().longitude).hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final CustMarker other = (CustMarker) o;
        if (marker.getPosition().latitude != other.marker.getPosition().latitude) {
            return false;
        }
        return marker.getPosition().longitude == other.marker.getPosition().longitude;
    }
}
