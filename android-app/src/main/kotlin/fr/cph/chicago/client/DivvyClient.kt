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

import fr.cph.chicago.entity.DivvyStationInformation
import fr.cph.chicago.entity.DivvyStationStatus
import fr.cph.chicago.entity.StationInformationResponse
import fr.cph.chicago.entity.StationStatusResponse
import fr.cph.chicago.parser.JsonParser
import fr.cph.chicago.redux.store
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET

/**
 * Class that connect to the Divvy API.
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
object DivvyClient {

    fun getStationsInformation(): Single<Map<String, DivvyStationInformation>> {
        return divvyHttpClient.stationInformation()
            .map { stationInfo -> stationInfo.data.stations.associateBy { it.id } }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
    }

    fun getStationsStatus(): Single<Map<String, DivvyStationStatus>> {
        return divvyHttpClient.stationStatus()
            .map { stationStatus -> stationStatus.data.stations.associateBy { it.id } }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
    }
}

private interface DivvyClientRetrofit {
    @GET("gbfs/en/station_information.json")
    fun stationInformation(): Single<StationInformationResponse>

    @GET("gbfs/en/station_status.json")
    fun stationStatus(): Single<StationStatusResponse>
}

private val divvyHttpClient: DivvyClientRetrofit by lazy {
    val retrofit = Retrofit.Builder()
        .baseUrl(store.state.divvyUrl)
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .addConverterFactory(JacksonConverterFactory.create(JsonParser.mapper))
        .client(okHttpClient)
        .build();
    retrofit.create(DivvyClientRetrofit::class.java)
}
