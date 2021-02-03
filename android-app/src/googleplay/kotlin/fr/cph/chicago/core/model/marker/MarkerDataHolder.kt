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

package fr.cph.chicago.core.model.marker

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import fr.cph.chicago.core.model.Station

class MarkerDataHolder {
    private val data: MutableMap<LatLng, Station> = mutableMapOf()

    fun addData(marker: Marker, station: Station) {
        val latLng = marker.position
        data[latLng] = station
    }

    fun clear() {
        data.clear()
    }

    fun getStation(marker: Marker): Station? {
        return data[marker.position]
    }
}
