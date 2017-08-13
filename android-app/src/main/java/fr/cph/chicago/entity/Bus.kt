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

import java.security.Timestamp

class Bus(var id: Int = 0,
          val timestamp: Timestamp? = null,
          var position: Position? = null,
          var heading: Int = 0,
          var patternId: Int = 0,
          var patternDistance: Int = 0,
          var routeId: String? = null,
          var destination: String? = null,
          val delay: Boolean? = null) {

    companion object {

        fun getBestPosition(buses: List<Bus>): Position {
            val position = Position()
            var maxLatitude: Double? = 0.0
            var minLatitude: Double? = 0.0
            var maxLongitude: Double? = 0.0
            var minLongitude: Double? = 0.0
            var i = 0
            // FIXME kotlin
/*            for (bus in buses) {
                val temp = bus.position
                if (i == 0) {
                    maxLatitude = temp?.latitude
                    minLatitude = temp?.latitude
                    maxLongitude = temp?.longitude
                    minLongitude = temp?.longitude
                } else {
                    if (temp.latitude > maxLatitude) {
                        maxLatitude = temp.latitude
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
