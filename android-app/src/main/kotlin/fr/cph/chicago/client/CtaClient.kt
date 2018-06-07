/**
 * Copyright 2018 Carl-Philipp Harmant
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

import fr.cph.chicago.Constants.Companion.ALERTS_ROUTES_URL
import fr.cph.chicago.Constants.Companion.ALERT_ROUTES_URL
import fr.cph.chicago.Constants.Companion.BUSES_ARRIVAL_URL
import fr.cph.chicago.Constants.Companion.BUSES_DIRECTION_URL
import fr.cph.chicago.Constants.Companion.BUSES_PATTERN_URL
import fr.cph.chicago.Constants.Companion.BUSES_ROUTES_URL
import fr.cph.chicago.Constants.Companion.BUSES_STOP_URL
import fr.cph.chicago.Constants.Companion.BUSES_VEHICLES_URL
import fr.cph.chicago.Constants.Companion.TRAINS_ARRIVALS_URL
import fr.cph.chicago.Constants.Companion.TRAINS_FOLLOW_URL
import fr.cph.chicago.Constants.Companion.TRAINS_LOCATION_URL
import fr.cph.chicago.client.CtaRequestType.*
import fr.cph.chicago.core.App
import fr.cph.chicago.exception.ConnectException
import fr.cph.chicago.parser.JsonParser
import org.apache.commons.collections4.MultiValuedMap
import java.io.InputStream

/**
 * Class that build url and connect to CTA API
 *
 * @author Carl-Philipp Harmant
 *
 * @version 1
 */
object CtaClient {

    private const val QUERY_PARAM_KEY = "?key="
    private const val QUERY_PARAM_JSON = "?outputType=json"

    private val jsonParser = JsonParser

    @Deprecated(message = "Use get instead")
    @Throws(ConnectException::class)
    fun connect(requestType: CtaRequestType, params: MultiValuedMap<String, String>): InputStream {
        val ctaTrainKey = App.ctaTrainKey
        val ctaBusKey = App.ctaBusKey
        val address: StringBuilder
        address = when (requestType) {
            TRAIN_ARRIVALS -> StringBuilder(TRAINS_ARRIVALS_URL + QUERY_PARAM_KEY + ctaTrainKey)
            TRAIN_FOLLOW -> StringBuilder(TRAINS_FOLLOW_URL + QUERY_PARAM_KEY + ctaTrainKey)
            TRAIN_LOCATION -> StringBuilder(TRAINS_LOCATION_URL + QUERY_PARAM_KEY + ctaTrainKey)
            BUS_ROUTES -> StringBuilder(BUSES_ROUTES_URL + QUERY_PARAM_KEY + ctaBusKey + "&format=json")
            BUS_DIRECTION -> StringBuilder(BUSES_DIRECTION_URL + QUERY_PARAM_KEY + ctaBusKey)
            BUS_STOP_LIST -> StringBuilder(BUSES_STOP_URL + QUERY_PARAM_KEY + ctaBusKey)
            BUS_VEHICLES -> StringBuilder(BUSES_VEHICLES_URL + QUERY_PARAM_KEY + ctaBusKey)
            BUS_ARRIVALS -> StringBuilder(BUSES_ARRIVAL_URL + QUERY_PARAM_KEY + ctaBusKey)
            BUS_PATTERN -> StringBuilder(BUSES_PATTERN_URL + QUERY_PARAM_KEY + ctaBusKey)
            ALERTS_ROUTES -> StringBuilder(ALERTS_ROUTES_URL + QUERY_PARAM_JSON)
            ALERTS_ROUTE -> StringBuilder(ALERT_ROUTES_URL + QUERY_PARAM_JSON)
            else -> StringBuilder()
        }
        params.asMap()
            .flatMap { entry -> entry.value.map { value -> StringBuilder().append("&").append(entry.key).append("=").append(value) } }
            .forEach({ address.append(it) })
        return HttpClient.connect(address.toString())
    }

    // TODO: Create a method with no params
    fun <T> get(requestType: CtaRequestType, params: MultiValuedMap<String, String>, clazz: Class<T>): T {
        val ctaTrainKey = App.ctaTrainKey
        val ctaBusKey = App.ctaBusKey
        val address: StringBuilder
        address = when (requestType) {
            TRAIN_ARRIVALS -> StringBuilder(TRAINS_ARRIVALS_URL + QUERY_PARAM_KEY + ctaTrainKey)
            TRAIN_FOLLOW -> StringBuilder(TRAINS_FOLLOW_URL + QUERY_PARAM_KEY + ctaTrainKey)
            TRAIN_LOCATION -> StringBuilder(TRAINS_LOCATION_URL + QUERY_PARAM_KEY + ctaTrainKey + "&outputType=JSON")
            BUS_ROUTES -> StringBuilder(BUSES_ROUTES_URL + QUERY_PARAM_KEY + ctaBusKey + "&format=json")
            BUS_DIRECTION -> StringBuilder(BUSES_DIRECTION_URL + QUERY_PARAM_KEY + ctaBusKey + "&format=json")
            BUS_STOP_LIST -> StringBuilder(BUSES_STOP_URL + QUERY_PARAM_KEY + ctaBusKey + "&format=json")
            BUS_VEHICLES -> StringBuilder(BUSES_VEHICLES_URL + QUERY_PARAM_KEY + ctaBusKey + "&format=json")
            BUS_ARRIVALS -> StringBuilder(BUSES_ARRIVAL_URL + QUERY_PARAM_KEY + ctaBusKey + "&format=json")
            BUS_PATTERN -> StringBuilder(BUSES_PATTERN_URL + QUERY_PARAM_KEY + ctaBusKey + "&format=json")
            ALERTS_ROUTES -> StringBuilder(ALERTS_ROUTES_URL + QUERY_PARAM_JSON)
            ALERTS_ROUTE -> StringBuilder(ALERT_ROUTES_URL + QUERY_PARAM_JSON)
            else -> StringBuilder()
        }
        params.asMap()
            .flatMap { entry -> entry.value.map { value -> StringBuilder().append("&").append(entry.key).append("=").append(value) } }
            .forEach({ address.append(it) })
        val inputStream = HttpClient.connect(address.toString())
        return jsonParser.parse(inputStream, clazz)
    }
}
