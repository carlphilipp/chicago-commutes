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

package fr.cph.chicago.client

import android.graphics.drawable.Drawable
import android.util.Log
import com.annimon.stream.Optional
import fr.cph.chicago.Constants.GOOGLE_STREET_VIEW_URL
import fr.cph.chicago.core.App
import org.apache.commons.io.IOUtils
import java.io.InputStream
import java.net.URL

/**
 * Class that access google street api. Singleton

 * @author Carl-Philipp Harmant
 * *
 * @version 1
 */
class GoogleStreetClient private constructor() {

    private object Holder {
        val INSTANCE = GoogleStreetClient()
    }

    fun connect(latitude: Double, longitude: Double): Optional<Drawable> {
        val address = GOOGLE_STREET_VIEW_URL + "?key=" +
            App.getGoogleStreetKey() +
            "&sensor=false" +
            "&size=" + WIDTH + "x" + HEIGHT +
            "&fov=120" +
            "&location=" +
            latitude +
            "," + longitude
        return connectUrl(address)
    }

    /**
     * HttpClient to the API and get the MAP

     * @param address the address to connect to
     * *
     * @return a drawable map
     */
    private fun connectUrl(address: String): Optional<Drawable> {
        Log.v(TAG, "Address: " + address)
        var inputStream: InputStream? = null
        try {
            inputStream = URL(address).content as InputStream
            return Optional.of(Drawable.createFromStream(inputStream, "src name"))
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            // TODO add a temporary image here
            return Optional.empty<Drawable>()
        } finally {
            IOUtils.closeQuietly(inputStream)
        }
    }

    companion object {
        private val TAG = GoogleStreetClient::class.java.simpleName
        private val WIDTH = 1000
        private val HEIGHT = 300
        val INSTANCE: GoogleStreetClient by lazy { Holder.INSTANCE }
    }
}
