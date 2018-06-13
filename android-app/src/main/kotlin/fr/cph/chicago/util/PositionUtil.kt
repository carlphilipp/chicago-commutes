package fr.cph.chicago.util

import com.google.android.gms.maps.model.LatLng
import fr.cph.chicago.core.model.Position

object PositionUtil {

    val chicago: LatLng by lazy {
        LatLng(41.8819, -87.6278)
    }

    val chicagoPosition: Position by lazy {
        Position(chicago.latitude, chicago.longitude)
    }

    fun getBestPosition(positions: List<Position>): Position {
        var maxLatitude = 0.0
        var minLatitude = 0.0
        var maxLongitude = 0.0
        var minLongitude = 0.0
        positions
            .filter { it.latitude != 0.0 && it.latitude != 0.0 }
            .forEachIndexed { i, temp ->
                if (i == 0) {
                    maxLatitude = temp.latitude
                    minLatitude = temp.latitude
                    maxLongitude = temp.longitude
                    minLongitude = temp.longitude
                } else {
                    if (temp.latitude > maxLatitude) {
                        maxLatitude = temp.latitude
                    }
                    if (temp.latitude < minLatitude) {
                        minLatitude = temp.latitude
                    }
                    if (temp.longitude > maxLongitude) {
                        maxLongitude = temp.longitude
                    }
                    if (temp.longitude < minLongitude) {
                        minLongitude = temp.longitude
                    }
                }
            }
        return Position(
            latitude = (maxLatitude + minLatitude) / 2,
            longitude = (maxLongitude + minLongitude) / 2
        )
    }
}
