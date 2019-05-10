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

package fr.cph.chicago.client

import android.graphics.drawable.Drawable
import fr.cph.chicago.Constants.GOOGLE_STREET_VIEW_URL
import fr.cph.chicago.redux.store
import fr.cph.chicago.util.Util
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

/**
 * Class that access google street api. Singleton
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
object GoogleStreetClient {

    private const val WIDTH = 1000
    private const val HEIGHT = 300
    private const val FOV = 120

    private val util = Util
    private val httpClient = HttpClient

    fun connect(latitude: Double, longitude: Double): Single<Drawable> {
        val address = "$GOOGLE_STREET_VIEW_URL?key=${store.state.googleStreetKey}&sensor=false&size=${WIDTH}x$HEIGHT&fov=$FOV&location=$latitude,$longitude&source=outdoor"
        return connectUrl(address)
    }

    /**
     * HttpClient to the API and get the MAP
     *
     * @param address the address to connect to
     * @return a drawable map
     */
    private fun connectUrl(address: String): Single<Drawable> {
        return httpClient.connectRx(address)
            .observeOn(Schedulers.computation())
            .map { inputStream ->
                try {
                    Drawable.createFromStream(inputStream, "src name")
                } finally {
                    util.closeQuietly(inputStream)
                }
            }
    }
}
