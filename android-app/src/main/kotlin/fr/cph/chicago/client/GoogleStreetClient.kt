/**
 * Copyright 2020 Carl-Philipp Harmant
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
import fr.cph.chicago.Constants.GOOGLE_STREET_VIEW_URL2
import fr.cph.chicago.redux.store
import io.reactivex.rxjava3.core.Single
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.ByteArrayInputStream

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

    fun connect(latitude: Double, longitude: Double): Single<Drawable> {
        return googleStreetHttpClient.getStreetViewImage(
            key = store.state.googleStreetKey,
            sensor = false,
            size = "${WIDTH}x${HEIGHT}",
            fov = "$FOV",
            location = "$latitude,$longitude",
            source = "outdoor")
            .map { response -> Drawable.createFromStream(ByteArrayInputStream(response.bytes()), "src name") }
    }
}

private interface GoogleStreetClientRetrofit {
    @GET("/maps/api/streetview")
    fun getStreetViewImage(
        @Query("key") key: String,
        @Query("sensor") sensor: Boolean,
        @Query("size") size: String,
        @Query("fov") fov: String,
        @Query("location") location: String,
        @Query("source") source: String,
    ): Single<ResponseBody>
}

private val googleStreetHttpClient: GoogleStreetClientRetrofit by lazy {
    val retrofit = Retrofit.Builder()
        .baseUrl(GOOGLE_STREET_VIEW_URL2)
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .client(okHttpClient)
        .build();
    retrofit.create(GoogleStreetClientRetrofit::class.java)
}
