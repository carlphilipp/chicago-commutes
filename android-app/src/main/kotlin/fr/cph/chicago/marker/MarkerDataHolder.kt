package fr.cph.chicago.marker

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import fr.cph.chicago.entity.AStation

class MarkerDataHolder {
    private val data: MutableMap<LatLng, AStation> = mutableMapOf()

    fun addData(marker: Marker, station: AStation) {
        val latLng = marker.position
        data.put(latLng, station)
    }

    fun clear() {
        data.clear()
    }

    fun getStation(marker: Marker): AStation? {
        return data[marker.position]
    }
}
