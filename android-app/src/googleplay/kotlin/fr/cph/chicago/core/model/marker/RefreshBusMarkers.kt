/**
 * Copyright 2018 Carl-Philipp Harmant
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

package fr.cph.chicago.core.model.marker

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Marker
import fr.cph.chicago.R

/**
 * Refresh buses and stop markers
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class RefreshBusMarkers : RefreshMarkers(R.drawable.bus) {

    fun refreshBusAndStation(position: CameraPosition,
                             busMarkers: List<Marker>,
                             busStationMarkers: List<Marker>) {
        refresh(position, busMarkers)
        var currentZoom = -1f
        if (position.zoom != currentZoom) {
            val oldZoom = currentZoom
            currentZoom = position.zoom

            // Handle stops markers
            if (isIn(currentZoom, 21f, 14.5f) && !isIn(oldZoom, 21f, 14.5f)) {
                busStationMarkers.forEach { marker -> marker.isVisible = true }
            } else {
                busStationMarkers.forEach { marker -> marker.isVisible = false }
            }
        }
    }
}
