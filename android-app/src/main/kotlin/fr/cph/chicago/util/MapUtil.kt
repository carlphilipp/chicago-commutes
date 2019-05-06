package fr.cph.chicago.util

import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.rx.RxUtil.createSingleFromCallable
import fr.cph.chicago.rx.RxUtil.handleError
import io.reactivex.Single
import java.util.concurrent.Callable

object MapUtil {

    private const val DEFAULT_RANGE = 0.008

    val chicagoPosition: Position by lazy {
        Position(41.8819, -87.6278)
    }

    fun getBounds(positions: List<Position>): Pair<Position, Position> {
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
        val topLeft = Position(minLatitude, maxLongitude)
        val bottomRight = Position(maxLatitude, minLongitude)
        return Pair(topLeft, bottomRight)
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

    fun readNearbyStation(position: Position, bikeStations: List<BikeStation>): Single<List<BikeStation>> {
        return createSingleFromCallable(
            Callable {
                val latitude = position.latitude
                val longitude = position.longitude

                val latMax = latitude + DEFAULT_RANGE
                val latMin = latitude - DEFAULT_RANGE
                val lonMax = longitude + DEFAULT_RANGE
                val lonMin = longitude - DEFAULT_RANGE

                bikeStations
                    .filter { station -> station.latitude <= latMax }
                    .filter { station -> station.latitude >= latMin }
                    .filter { station -> station.longitude <= lonMax }
                    .filter { station -> station.longitude >= lonMin }
            })
            .onErrorReturn(handleError())
    }
}
