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
import fr.cph.chicago.Constants.ALERTS_BASE_PATH
import fr.cph.chicago.Constants.BUSES_BASE
import fr.cph.chicago.Constants.BUSES_BASE_PATH
import fr.cph.chicago.Constants.REQUEST_DIR
import fr.cph.chicago.Constants.REQUEST_FORMAT
import fr.cph.chicago.Constants.REQUEST_KEY
import fr.cph.chicago.Constants.REQUEST_MAP_ID
import fr.cph.chicago.Constants.REQUEST_OUTPUT_TYPE
import fr.cph.chicago.Constants.REQUEST_ROUTE
import fr.cph.chicago.Constants.REQUEST_ROUTE_ID
import fr.cph.chicago.Constants.REQUEST_RUN_NUMBER
import fr.cph.chicago.Constants.REQUEST_STOP_ID
import fr.cph.chicago.Constants.REQUEST_TYPE
import fr.cph.chicago.Constants.REQUEST_VID
import fr.cph.chicago.Constants.TRAINS_BASE
import fr.cph.chicago.Constants.TRAINS_BASE_PATH
import fr.cph.chicago.entity.AlertsRouteResponse
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
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Class that build url and connect to CTA API
 *
 * @author Carl-Philipp Harmant
 *
 * @version 1
 */
object CtaClient {

    fun getTrainArrivals(stationIds: List<String>): Single<TrainArrivalResponse> {
        return ctaTrainClient.arrivals(ids = stationIds)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
    }

    fun getTrainFollow(runNumber: String): Single<TrainArrivalResponse> {
        return ctaTrainClient.follow(runNumber = runNumber)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
    }

    fun getTrainLocations(line: String): Single<TrainLocationResponse> {
        return ctaTrainClient.location(line = line)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
    }

    fun getBusRoutes(): Single<BusRoutesResponse> {
        return ctaBusClient.routes()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
    }

    fun getBusDirections(busRouteId: String): Single<BusDirectionResponse> {
        return ctaBusClient.directions(busRouteId = busRouteId)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
    }

    fun getBusStops(route: String, bound: String): Single<BusStopsResponse> {
        return ctaBusClient.stops(route = route, bound = bound)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
    }

    fun getBusVehicles(busRouteId: String): Single<BusPositionResponse> {
        return ctaBusClient.vehicles(busRouteId = busRouteId)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
    }

    fun getBusArrivals(stopIds: List<String>? = null, routes: List<String>? = null, busId: String? = null): Single<BusArrivalResponse> {
        return ctaBusClient.arrivals(
            stopIds = stopIds?.joinToString(separator = ","),
            routes = routes?.joinToString(separator = ","),
            busId = busId
        )
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
    }

    fun getBusPatterns(route: String): Single<BusPatternResponse> {
        return ctaBusClient.patterns(route = route)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
    }

    fun getAlertRoutes(): Single<AlertsRoutesResponse> {
        return ctaAlertClient.routes()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
    }

    fun getAlertAlerts(routeId: String): Single<AlertsRouteResponse> {
        return ctaAlertClient.alerts(routeId = routeId)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
    }
}

private interface CtaTrainClient {
    @GET("${TRAINS_BASE_PATH}/ttarrivals.aspx")
    fun arrivals(
        @Query(REQUEST_KEY) key: String = store.state.ctaTrainKey,
        @Query(REQUEST_OUTPUT_TYPE) outputType: String = "JSON",
        @Query(REQUEST_MAP_ID) ids: List<String>
    ): Single<TrainArrivalResponse>

    @GET("${TRAINS_BASE_PATH}/ttfollow.aspx")
    fun follow(
        @Query(REQUEST_KEY) key: String = store.state.ctaTrainKey,
        @Query(REQUEST_OUTPUT_TYPE) outputType: String = "JSON",
        @Query(REQUEST_RUN_NUMBER) runNumber: String
    ): Single<TrainArrivalResponse>

    @GET("${TRAINS_BASE_PATH}/ttpositions.aspx")
    fun location(
        @Query(REQUEST_KEY) key: String = store.state.ctaTrainKey,
        @Query(REQUEST_OUTPUT_TYPE) outputType: String = "JSON",
        @Query(REQUEST_ROUTE) line: String
    ): Single<TrainLocationResponse>
}

private interface CtaBusClient {
    @GET("${BUSES_BASE_PATH}/getroutes")
    fun routes(
        @Query(REQUEST_KEY) key: String = store.state.ctaBusKey,
        @Query(REQUEST_FORMAT) outputType: String = "json"
    ): Single<BusRoutesResponse>

    @GET("${BUSES_BASE_PATH}/getdirections")
    fun directions(
        @Query(REQUEST_KEY) key: String = store.state.ctaBusKey,
        @Query(REQUEST_FORMAT) outputType: String = "json",
        @Query(REQUEST_ROUTE) busRouteId: String
    ): Single<BusDirectionResponse>

    @GET("${BUSES_BASE_PATH}/getstops")
    fun stops(
        @Query(REQUEST_KEY) key: String = store.state.ctaBusKey,
        @Query(REQUEST_FORMAT) outputType: String = "json",
        @Query(REQUEST_ROUTE) route: String,
        @Query(REQUEST_DIR) bound: String
    ): Single<BusStopsResponse>

    @GET("${BUSES_BASE_PATH}/getvehicles")
    fun vehicles(
        @Query(REQUEST_KEY) key: String = store.state.ctaBusKey,
        @Query(REQUEST_FORMAT) outputType: String = "json",
        @Query(REQUEST_ROUTE) busRouteId: String
    ): Single<BusPositionResponse>

    @GET("${BUSES_BASE_PATH}/getpredictions")
    fun arrivals(
        @Query(REQUEST_KEY) key: String = store.state.ctaBusKey,
        @Query(REQUEST_FORMAT) outputType: String = "json",
        @Query(REQUEST_STOP_ID) stopIds: String? = null,
        @Query(REQUEST_ROUTE) routes: String? = null,
        @Query(REQUEST_VID) busId: String? = null,
    ): Single<BusArrivalResponse>

    @GET("${BUSES_BASE_PATH}/getpatterns")
    fun patterns(
        @Query(REQUEST_KEY) key: String = store.state.ctaBusKey,
        @Query(REQUEST_FORMAT) outputType: String = "json",
        @Query(REQUEST_ROUTE) route: String,
    ): Single<BusPatternResponse>
}

private interface CtaAlertClient {
    @GET("${ALERTS_BASE_PATH}/routes.aspx")
    fun routes(
        @Query(REQUEST_OUTPUT_TYPE) key: String = "json",
        @Query(REQUEST_TYPE) rail: String = "rail",
        @Query(REQUEST_TYPE) bus: String = "bus"
    ): Single<AlertsRoutesResponse>

    @GET("${ALERTS_BASE_PATH}/alerts.aspx")
    fun alerts(
        @Query(REQUEST_OUTPUT_TYPE) key: String = "json",
        @Query(REQUEST_ROUTE_ID) routeId: String
    ): Single<AlertsRouteResponse>
}

private val ctaTrainClient: CtaTrainClient by lazy {
    val retrofit = Retrofit.Builder()
        .baseUrl(TRAINS_BASE)
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .addConverterFactory(JacksonConverterFactory.create(JsonParser.mapper))
        .client(httpClient)
        .build();
    retrofit.create(CtaTrainClient::class.java)
}

private val ctaBusClient: CtaBusClient by lazy {
    val retrofit = Retrofit.Builder()
        .baseUrl(BUSES_BASE)
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .addConverterFactory(JacksonConverterFactory.create(JsonParser.mapper))
        .client(httpClient)
        .build();
    retrofit.create(CtaBusClient::class.java)
}

private val ctaAlertClient: CtaAlertClient by lazy {
    val retrofit = Retrofit.Builder()
        .baseUrl(ALERTS_BASE)
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .addConverterFactory(JacksonConverterFactory.create(JsonParser.mapper))
        .client(httpClient)
        .build();
    retrofit.create(CtaAlertClient::class.java)
}
