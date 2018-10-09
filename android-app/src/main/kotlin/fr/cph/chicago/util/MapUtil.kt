package fr.cph.chicago.util

import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.Position

object MapUtil {

    private const val DEFAULT_RANGE = 0.008

    val chicagoPosition: Position by lazy {
        Position(41.8819, -87.6278)
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

    fun readNearbyStation(divvyStations: List<BikeStation>, position: Position): List<BikeStation> {
        val latitude = position.latitude
        val longitude = position.longitude

        val latMax = latitude + DEFAULT_RANGE
        val latMin = latitude - DEFAULT_RANGE
        val lonMax = longitude + DEFAULT_RANGE
        val lonMin = longitude - DEFAULT_RANGE

        return divvyStations
            .filter { station -> station.latitude <= latMax }
            .filter { station -> station.latitude >= latMin }
            .filter { station -> station.longitude <= lonMax }
            .filter { station -> station.longitude >= lonMin }
    }
}
