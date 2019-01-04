/**
 * Copyright 2019 Carl-Philipp Harmant
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

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.util.MapUtil.chicagoPosition

object GoogleMapUtil {

    val chicago: LatLng by lazy {
        LatLng(chicagoPosition.latitude, chicagoPosition.longitude)
    }

    @Throws(SecurityException::class)
    fun centerMap(mapFragment: SupportMapFragment, position: Position?) {
        mapFragment.getMapAsync { googleMap ->
            googleMap.isMyLocationEnabled = true
            if (position != null) {
                val latLng = LatLng(position.latitude, position.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
            } else {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(GoogleMapUtil.chicago, 10f))
            }
        }
    }
}
