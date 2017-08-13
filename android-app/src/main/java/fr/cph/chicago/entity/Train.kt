/**
 * Copyright 2017 Carl-Philipp Harmant
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

package fr.cph.chicago.entity

class Train {

    var routeNumber: Int = 0
    val destStation: Int = 0
    var destName: String? = null
    val routeDirection: Int = 0
    val app: Boolean = false
    val dly: Boolean = false
    var position: Position? = null
    var heading: Int = 0

    companion object {

        fun getBestPosition(trains: List<Train>): Position {
            val position = Position()
            var maxLatitude: Double = 0.0
            var minLatitude: Double = 0.0
            var maxLongitude: Double = 0.0
            var minLongitude: Double = 0.0
            var i = 0
            for (train in trains) {
                val temp = train.position
                if (i == 0) {
                    maxLatitude = temp!!.latitude
                    minLatitude = temp.latitude
                    maxLongitude = temp.longitude
                    minLongitude = temp.longitude
                } else {
                    if (temp!!.latitude > maxLatitude) {
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
                i++
            }
            position.latitude = (maxLatitude!! + minLatitude!!) / 2
            position.longitude = (maxLongitude!! + minLongitude!!) / 2
            return position
        }
    }
}
