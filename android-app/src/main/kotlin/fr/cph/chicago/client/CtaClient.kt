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

import fr.cph.chicago.Constants.ALERTS_ROUTES_URL
import fr.cph.chicago.Constants.ALERT_ROUTES_URL
import fr.cph.chicago.Constants.BUSES_ARRIVAL_URL
import fr.cph.chicago.Constants.BUSES_DIRECTION_URL
import fr.cph.chicago.Constants.BUSES_PATTERN_URL
import fr.cph.chicago.Constants.BUSES_ROUTES_URL
import fr.cph.chicago.Constants.BUSES_STOP_URL
import fr.cph.chicago.Constants.BUSES_VEHICLES_URL
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
import fr.cph.chicago.parser.JsonParser
import fr.cph.chicago.redux.store
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.apache.commons.collections4.MultiValuedMap
import org.apache.commons.lang3.StringUtils

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

    fun <T> get(requestType: CtaRequestType, params: MultiValuedMap<String, String>, clazz: Class<T>): Single<T> {
        return Single.fromCallable { address(requestType, params) }
            .flatMap { address -> httpClient.connect(address) }
            .map { inputStream -> jsonParser.parse(inputStream, clazz) }
            .subscribeOn(Schedulers.computation())
    }

    private fun address(requestType: CtaRequestType, params: MultiValuedMap<String, String>): String {
        val address = when (requestType) {
            TRAIN_ARRIVALS -> store.state.trainArrivalsUrl + QUERY_PARAM_KEY + store.state.ctaTrainKey + "&outputType=JSON"
            TRAIN_FOLLOW -> store.state.trainFollowUrl + QUERY_PARAM_KEY + store.state.ctaTrainKey + "&outputType=JSON"
            TRAIN_LOCATION -> store.state.trainLocationUrl + QUERY_PARAM_KEY + store.state.ctaTrainKey + "&outputType=JSON"
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

}
