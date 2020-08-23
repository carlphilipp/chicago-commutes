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

import fr.cph.chicago.Constants.ALERTS_BASE
import fr.cph.chicago.Constants.ALERTS_ROUTES_URL
import fr.cph.chicago.Constants.ALERT_ROUTES_URL
import fr.cph.chicago.Constants.BUSES_ARRIVAL_URL
import fr.cph.chicago.Constants.BUSES_BASE
import fr.cph.chicago.Constants.BUSES_DIRECTION_URL
import fr.cph.chicago.Constants.BUSES_PATTERN_URL
import fr.cph.chicago.Constants.BUSES_ROUTES_URL
import fr.cph.chicago.Constants.BUSES_STOP_URL
import fr.cph.chicago.Constants.BUSES_VEHICLES_URL
import fr.cph.chicago.Constants.TRAINS_BASE
import fr.cph.chicago.client.CtaRequestType.ALERTS_ROUTE
import fr.cph.chicago.client.CtaRequestType.ALERTS_ROUTES
import fr.cph.chicago.client.CtaRequestType.BUS_ARRIVALS
import fr.cph.chicago.client.CtaRequestType.BUS_DIRECTION
import fr.cph.chicago.client.CtaRequestType.BUS_PATTERN
import fr.cph.chicago.client.CtaRequestType.BUS_ROUTES
import fr.cph.chicago.client.CtaRequestType.BUS_STOP_LIST
import fr.cph.chicago.client.CtaRequestType.BUS_VEHICLES
import fr.cph.chicago.client.CtaRequestType.TRAIN_ARRIVALS
import fr.cph.chicago.client.CtaRequestType.TRAIN_FOLLOW
import fr.cph.chicago.client.CtaRequestType.TRAIN_LOCATION
import fr.cph.chicago.entity.AlertsRoutesResponse
import fr.cph.chicago.entity.BusArrivalResponse
import fr.cph.chicago.entity.BusDirectionResponse
import fr.cph.chicago.entity.BusPatternResponse
import fr.cph.chicago.entity.BusPositionResponse
import fr.cph.chicago.entity.BusRoutesResponse
import fr.cph.chicago.entity.BusStopsResponse
import fr.cph.chicago.entity.TrainArrivalResponse
import fr.cph.chicago.entity.TrainLocationResponse
import fr.cph.chicago.parser.JsonParser
import fr.cph.chicago.redux.store
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.apache.commons.collections4.MultiValuedMap
import org.apache.commons.lang3.StringUtils
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryName

/**
 * Class that build url and connect to CTA API
 *
 * @author Carl-Philipp Harmant
 *
 * @version 1
 */
object CtaClient {

    private const val QUERY_PARAM_KEY = "?key="
    private const val QUERY_PARAM_JSON_ALERT = "?outputType=json"

    private val jsonParser = JsonParser
    private val httpClient = HttpClient

    fun <T> get(requestType: CtaRequestType, clazz: Class<T>, params: MultiValuedMap<String, String>): Single<T> {
        return Single.fromCallable { address(requestType, params) }
            .flatMap { address -> httpClient.connect(address) }
            .map { inputStream -> jsonParser.parse(inputStream, clazz) }
            .subscribeOn(Schedulers.computation())
        /*return when (requestType) {
            TRAIN_ARRIVALS -> ctaTrainHttpClient.arrivals(params = params)
            TRAIN_FOLLOW -> ctaTrainHttpClient.follow(params = params)
            TRAIN_LOCATION -> ctaTrainHttpClient.location(params = params)
            BUS_ROUTES -> ctaBusHttpClient.routes(params = params)
            BUS_DIRECTION -> ctaBusHttpClient.directions(params = params)
            BUS_STOP_LIST -> ctaBusHttpClient.stops(params = params)
            BUS_VEHICLES -> ctaBusHttpClient.vehicles(params = params)
            BUS_ARRIVALS -> ctaBusHttpClient.arrivals(params = params)
            BUS_PATTERN -> ctaBusHttpClient.patterns(params = params)
            ALERTS_ROUTES -> ctaAlertHttpClient.alerts()
            ALERTS_ROUTE -> ctaAlertHttpClient.routes()
        }
            .map { response -> clazz.cast(response) }
            .subscribeOn(Schedulers.computation())*/
    }

    private fun address(requestType: CtaRequestType, params: MultiValuedMap<String, String>): String {
        val address = when (requestType) {
            TRAIN_ARRIVALS -> TRAINS_BASE + "/api/1.0/ttarrivals.aspx" + QUERY_PARAM_KEY + store.state.ctaTrainKey + "&outputType=JSON"
            TRAIN_FOLLOW -> TRAINS_BASE + "/api/1.0/ttfollow.aspx" + QUERY_PARAM_KEY + store.state.ctaTrainKey + "&outputType=JSON"
            TRAIN_LOCATION -> TRAINS_BASE + "/api/1.0/ttpositions.aspx" + QUERY_PARAM_KEY + store.state.ctaTrainKey + "&outputType=JSON"

            BUS_ROUTES -> BUSES_ROUTES_URL + QUERY_PARAM_KEY + store.state.ctaBusKey + "&format=json"
            BUS_DIRECTION -> BUSES_DIRECTION_URL + QUERY_PARAM_KEY + store.state.ctaBusKey + "&format=json"
            BUS_STOP_LIST -> BUSES_STOP_URL + QUERY_PARAM_KEY + store.state.ctaBusKey + "&format=json"
            BUS_VEHICLES -> BUSES_VEHICLES_URL + QUERY_PARAM_KEY + store.state.ctaBusKey + "&format=json"
            BUS_ARRIVALS -> BUSES_ARRIVAL_URL + QUERY_PARAM_KEY + store.state.ctaBusKey + "&format=json"
            BUS_PATTERN -> BUSES_PATTERN_URL + QUERY_PARAM_KEY + store.state.ctaBusKey + "&format=json"

            ALERTS_ROUTES -> ALERTS_ROUTES_URL + QUERY_PARAM_JSON_ALERT
            ALERTS_ROUTE -> ALERT_ROUTES_URL + QUERY_PARAM_JSON_ALERT
        }
        return address + params.asMap()
            .flatMap { entry -> entry.value.map<String?, String> { value -> "&${entry.key}=$value" } }
            .joinToString(separator = StringUtils.EMPTY)
    }

    fun getTrainArrivals(stationIds: List<String>): Single<TrainArrivalResponse> {
        return ctaTrainRestTemplate.arrivals(ids = stationIds)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
    }

    fun getTrainFollow(runNumber: String): Single<TrainArrivalResponse> {
        return ctaTrainRestTemplate.follow(runNumber = runNumber)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
    }

    fun getTrainLocations(line: String): Single<TrainLocationResponse> {
        return ctaTrainRestTemplate.location(line = line)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
    }

    fun getBusRoutes(): Single<BusRoutesResponse> {
        return ctaBusHttpClient.routes()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
    }

    fun getBusDirections(busRouteId: String): Single<BusDirectionResponse> {
        return ctaBusHttpClient.directions(busRouteId = busRouteId)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
    }

    fun getBusStops(route: String, bound: String): Single<BusStopsResponse> {
        return ctaBusHttpClient.stops(route = route, bound = bound)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
    }

    fun getBusVehicles(busRouteId: String): Single<BusPositionResponse> {
        return ctaBusHttpClient.vehicles(busRouteId = busRouteId)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
    }

    // TODO start here, issues with params
    fun getBusArrivals(stopIds: List<String>, routes: List<String>): Single<BusArrivalResponse> {
        return ctaBusHttpClient.arrivals(stopIds = stopIds.joinToString(separator = ","), routes = routes.joinToString(separator = ","))
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
    }
}

private interface CtaTrainRetrofit {
    @GET("api/1.0/ttarrivals.aspx")
    fun arrivals(
        @Query("key") key: String = store.state.ctaTrainKey,
        @Query("outputType") outputType: String = "JSON",
        @Query(REQUEST_MAP_ID) ids: List<String>
    ): Single<TrainArrivalResponse>

    @GET("api/1.0/ttfollow.aspx")
    fun follow(
        @Query("key") key: String = store.state.ctaTrainKey,
        @Query("outputType") outputType: String = "JSON",
        @Query(REQUEST_RUN_NUMBER) runNumber: String
    ): Single<TrainArrivalResponse>

    @GET("api/1.0/ttpositions.aspx")
    fun location(
        @Query("key") key: String = store.state.ctaTrainKey,
        @Query("outputType") outputType: String = "JSON",
        @Query(REQUEST_ROUTE) line: String
    ): Single<TrainLocationResponse>
}

private interface CtaBusRetrofit {
    @GET("bustime/api/v2/getroutes")
    fun routes(
        @Query("key") key: String = store.state.ctaBusKey,
        @Query("format") outputType: String = "json"
    ): Single<BusRoutesResponse>

    @GET("bustime/api/v2/getdirections")
    fun directions(
        @Query("key") key: String = store.state.ctaBusKey,
        @Query("format") outputType: String = "json",
        @Query(REQUEST_ROUTE) busRouteId: String
    ): Single<BusDirectionResponse>

    @GET("bustime/api/v2/getstops")
    fun stops(
        @Query("key") key: String = store.state.ctaBusKey,
        @Query("format") outputType: String = "json",
        @Query(REQUEST_ROUTE) route: String,
        @Query(REQUEST_DIR) bound: String
    ): Single<BusStopsResponse>

    @GET("bustime/api/v2/getvehicles")
    fun vehicles(
        @Query("key") key: String = store.state.ctaBusKey,
        @Query("format") outputType: String = "json",
        @Query(REQUEST_ROUTE) busRouteId: String
    ): Single<BusPositionResponse>

    @GET("bustime/api/v2/getpredictions")
    fun arrivals(
        @Query("key") key: String = store.state.ctaBusKey,
        @Query("format") outputType: String = "json",
        @Query(REQUEST_STOP_ID) stopIds : String,
        @Query(REQUEST_ROUTE) routes : String
    ): Single<BusArrivalResponse>

    @GET("bustime/api/v2/getpatterns")
    fun patterns(
        @Query("key") key: String = store.state.ctaBusKey,
        @Query("format") outputType: String = "json",
        @QueryName(encoded = true) vararg params: String = emptyArray(),
    ): Single<BusPatternResponse>
}

private interface CtaAlertRetrofit {
    @GET("api/1.0/routes.aspx")
    fun routes(@Query("outputType") key: String = "json"): Single<AlertsRoutesResponse>

    @GET("api/1.0/alerts.aspx")
    fun alerts(@Query("outputType") key: String = "json"): Single<AlertsRoutesResponse>
}

private val ctaTrainRestTemplate: CtaTrainRetrofit by lazy {
    val retrofit = Retrofit.Builder()
        .baseUrl(TRAINS_BASE)
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .addConverterFactory(JacksonConverterFactory.create(JsonParser.mapper))
        .client(okHttpClient)
        .build();
    retrofit.create(CtaTrainRetrofit::class.java)
}

private val ctaBusHttpClient: CtaBusRetrofit by lazy {
    val retrofit = Retrofit.Builder()
        .baseUrl(BUSES_BASE)
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .addConverterFactory(JacksonConverterFactory.create(JsonParser.mapper))
        .client(okHttpClient)
        .build();
    retrofit.create(CtaBusRetrofit::class.java)
}

private val ctaAlertHttpClient: CtaAlertRetrofit by lazy {
    val retrofit = Retrofit.Builder()
        .baseUrl(ALERTS_BASE)
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .addConverterFactory(JacksonConverterFactory.create(JsonParser.mapper))
        .client(okHttpClient)
        .build();
    retrofit.create(CtaAlertRetrofit::class.java)
}

