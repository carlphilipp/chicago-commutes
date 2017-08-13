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
            var maxLatitude = 0.0
            var minLatitude = 0.0
            var maxLongitude = 0.0
            var minLongitude = 0.0
            var i = 0
            // FIXME kotlin
/*            for (train in trains) {
                val temp = train.position
                if (i == 0) {
                    maxLatitude = temp.getLatitude()
                    minLatitude = temp.getLatitude()
                    maxLongitude = temp.getLongitude()
                    minLongitude = temp.getLongitude()
                } else {
                    if (temp.getLatitude() > maxLatitude) {
                        maxLatitude = temp.getLatitude()
                    }
                    if (temp.getLatitude() < minLatitude) {
                        minLatitude = temp.getLatitude()
                    }
                    if (temp.getLongitude() > maxLongitude) {
                        maxLongitude = temp.getLongitude()
                    }
                    if (temp.getLongitude() < minLongitude) {
                        minLongitude = temp.getLongitude()
                    }
                }
                i++
            }
            position.setLatitude((maxLatitude + minLatitude) / 2)
            position.setLongitude((maxLongitude + minLongitude) / 2)*/
            return position
        }
    }
}
