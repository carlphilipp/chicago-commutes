package fr.cph.chicago.data

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import fr.cph.chicago.entity.AStation
import java.util.*

class MarkerDataHolder {
    private val data: MutableMap<LatLng, MarkerDataHolder.MarkerHolder>

    init {
        data = HashMap()
    }

    fun addData(marker: Marker, station: AStation) {
        val markerHolder = MarkerDataHolder.MarkerHolder()
        markerHolder.marker = marker
        markerHolder.station = station
        val latLng = marker.position
        data.put(latLng, markerHolder)
    }

    fun clear() {
        data.clear()
    }

    fun getStation(marker: Marker): AStation? {
        return data[marker.position]!!.station
    }

    private class MarkerHolder {
        var marker: Marker? = null
        var station: AStation? = null
    }
}
