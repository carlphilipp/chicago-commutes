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

package fr.cph.chicago.client

import android.graphics.drawable.Drawable
import fr.cph.chicago.Constants.GOOGLE_STREET_VIEW_BASE
import fr.cph.chicago.config.httpClient
import fr.cph.chicago.redux.store
import io.reactivex.rxjava3.core.Single
import java.io.ByteArrayInputStream
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Class that access google street api. Singleton
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
object GoogleStreetClient {

    fun getImage(latitude: Double, longitude: Double, width: Int = WIDTH, height: Int = HEIGHT): Single<Drawable> {
        return googleStreetHttpClient.getStreetViewImage(
            location = "$latitude,$longitude",
            size = "${width}x${height}"
        )
            .map { response ->
                Thread.sleep(2000)
                Drawable.createFromStream(ByteArrayInputStream(response.bytes()), "src name")
            }
    }
}

private const val WIDTH = 1000
private const val HEIGHT = 300
private const val FOV = 120

private interface GoogleStreetClientRetrofit {
    @GET("/maps/api/streetview")
    fun getStreetViewImage(
        @Query("key") key: String = store.state.googleStreetKey,
        @Query("sensor") sensor: Boolean = false,
        @Query("size") size: String = "${WIDTH}x${HEIGHT}",
        @Query("fov") fov: String = "$FOV",
        @Query("location") location: String,
        @Query("source") source: String = "outdoor",
    ): Single<ResponseBody>
}

private val googleStreetHttpClient: GoogleStreetClientRetrofit by lazy {
    val retrofit = Retrofit.Builder()
        .baseUrl(GOOGLE_STREET_VIEW_BASE)
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .client(httpClient)
        .build()
    retrofit.create(GoogleStreetClientRetrofit::class.java)
}
