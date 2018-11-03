package fr.cph.chicago.core.model.marker

import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.geometry.LatLng
import fr.cph.chicago.core.model.Station

class MarkerDataHolder {
    private val data: MutableMap<LatLng, Station> = mutableMapOf()

    fun addData(marker: Marker, station: Station) {
        val latLng = marker.position
        data[latLng] = station
    }

    fun clear() {
        data.clear()
    }

    fun getStation(marker: Marker): Station? {
        return data[marker.position]
    }
}
