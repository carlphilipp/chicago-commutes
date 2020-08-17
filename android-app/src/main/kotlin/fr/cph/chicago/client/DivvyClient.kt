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
import fr.cph.chicago.exception.ConnectException
import fr.cph.chicago.parser.JsonParser
import fr.cph.chicago.redux.store
import io.reactivex.Single

/**
 * Class that build connect to the Divvy API.
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
object DivvyClient {

    private val httpClient = HttpClient
    private val jsonParser = JsonParser

    @Throws(ConnectException::class)
    fun getStationsInformation(): Single<Map<String, DivvyStationInformation>> {
        return httpClient.connect(store.state.divvyStationInformationUrl)
            .map { inputStream -> jsonParser.parse(inputStream, StationInformationResponse::class.java) }
            .map { stationInfo -> stationInfo.data.stations.associateBy { it.id } }
    }

    @Throws(ConnectException::class)
    fun getStationsStatus(): Single<Map<String, DivvyStationStatus>> {
        return httpClient.connect(store.state.divvyStationStatusUrl)
            .map { inputStream -> jsonParser.parse(inputStream, StationStatusResponse::class.java) }
            .map { stationStatus -> stationStatus.data.stations.associateBy { it.id } }
    }
}
