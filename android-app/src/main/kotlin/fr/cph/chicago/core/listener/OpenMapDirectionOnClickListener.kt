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

package fr.cph.chicago.core.listener

import android.content.Intent
import android.net.Uri
import android.view.View
import fr.cph.chicago.util.Util
import timber.log.Timber

class OpenMapDirectionOnClickListener(latitude: Double, longitude: Double) : GoogleMapListener(latitude, longitude) {

    companion object {
        private val util = Util
    }

    override fun onClick(v: View) {
        try {
            val uri = "http://maps.google.com/?f=d&daddr=$latitude,$longitude&dirflg=w"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (intent.resolveActivity(v.context.packageManager) != null) {
                v.context.startActivity(intent)
            } else {
                util.showSnackBar(v, "Could not find Google Map on device", true)
            }
        } catch (e: Exception) {
            Timber.e(e)
            util.showSnackBar(v, "Could not find Google Map on device", true)
        }
    }
}
