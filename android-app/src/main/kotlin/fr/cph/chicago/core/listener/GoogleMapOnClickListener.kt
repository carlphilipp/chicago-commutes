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

package fr.cph.chicago.core.listener

import android.content.Intent
import android.net.Uri
import android.view.View

class GoogleMapOnClickListener(latitude: Double, longitude: Double) : GoogleMapListener(latitude, longitude) {

    override fun onClick(v: View) {
        val uri = "http://maps.google.com/maps?z=12&t=m&q=loc:$latitude+$longitude"
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity")
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        v.context.startActivity(i)
    }
}
