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

import fr.cph.chicago.Constants.*
import fr.cph.chicago.client.CtaRequestType.*
import fr.cph.chicago.core.App
import fr.cph.chicago.exception.ConnectException
import org.apache.commons.collections4.MultiValuedMap
import java.io.InputStream

/**
 * Class that build url and connect to CTA API

 * @author Carl-Philipp Harmant
 * *
 * @version 1
 */
object CtaClient {

    private val QUERY_PARAM_KEY = "?key="

    /**
     * HttpClient

     * @param requestType the type of request
     * *
     * @param params      the params
     * *
     * @return a string
     * *
     * @throws ConnectException the connection exception
     */
    @Throws(ConnectException::class)
    fun connect(requestType: CtaRequestType, params: MultiValuedMap<String, String>): InputStream {
        val ctaTrainKey = App.getCtaTrainKey()
        val ctaBusKey = App.getCtaBusKey()
        val address: StringBuilder
        when (requestType) {
            TRAIN_ARRIVALS -> address = StringBuilder(TRAINS_ARRIVALS_URL + QUERY_PARAM_KEY + ctaTrainKey)
            TRAIN_FOLLOW -> address = StringBuilder(TRAINS_FOLLOW_URL + QUERY_PARAM_KEY + ctaTrainKey)
            TRAIN_LOCATION -> address = StringBuilder(TRAINS_LOCATION_URL + QUERY_PARAM_KEY + ctaTrainKey)
            BUS_ROUTES -> address = StringBuilder(BUSES_ROUTES_URL + QUERY_PARAM_KEY + ctaBusKey)
            BUS_DIRECTION -> address = StringBuilder(BUSES_DIRECTION_URL + QUERY_PARAM_KEY + ctaBusKey)
            BUS_STOP_LIST -> address = StringBuilder(BUSES_STOP_URL + QUERY_PARAM_KEY + ctaBusKey)
            BUS_VEHICLES -> address = StringBuilder(BUSES_VEHICLES_URL + QUERY_PARAM_KEY + ctaBusKey)
            BUS_ARRIVALS -> address = StringBuilder(BUSES_ARRIVAL_URL + QUERY_PARAM_KEY + ctaBusKey)
            BUS_PATTERN -> address = StringBuilder(BUSES_PATTERN_URL + QUERY_PARAM_KEY + ctaBusKey)
            else -> address = StringBuilder()
        }
        params.asMap()
            .flatMap { entry -> entry.value.map { value -> StringBuilder().append("&").append(entry.key).append("=").append(value) } }
            .forEach({ address.append(it) })
        return HttpClient.connect(address.toString())
    }
}
