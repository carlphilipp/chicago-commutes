/**
 * Copyright 2021 Carl-Philipp Harmant
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.util

import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.rx.RxUtil.handleListError
import fr.cph.chicago.rx.RxUtil.singleFromCallable
import io.reactivex.rxjava3.core.Single
import timber.log.Timber
import java.util.concurrent.Callable

object MapUtil {

    private const val DEFAULT_RANGE = 0.008

    val chicagoPosition: Position by lazy { Position(41.8819, -87.6278) }

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

    fun readNearbyStation(position: Position, bikeStations: Map<String, BikeStation>): Single<List<BikeStation>> {
        return singleFromCallable(
            Callable {
                val latitude = position.latitude
                val longitude = position.longitude

                val latMax = latitude + DEFAULT_RANGE
                val latMin = latitude - DEFAULT_RANGE
                val lonMax = longitude + DEFAULT_RANGE
                val lonMin = longitude - DEFAULT_RANGE

                bikeStations.values
                    .filter { station -> station.latitude <= latMax }
                    .filter { station -> station.latitude >= latMin }
                    .filter { station -> station.longitude <= lonMax }
                    .filter { station -> station.longitude >= lonMin }
            })
            .onErrorReturn(handleListError())
    }
}
