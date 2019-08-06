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

package fr.cph.chicago.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class StationInformationResponse(val data: DataStationInformationResponse)

data class DataStationInformationResponse(val stations: List<DivvyStationInformation>)

data class DivvyStationInformation(
    @JsonProperty("station_id")
    val id: String,
    var name: String,
    @JsonProperty("lat")
    val latitude: Double,
    @JsonProperty("lon")
    val longitude: Double,
    val capacity: Int)

data class StationStatusResponse(val data: DataStationStatusResponse)

data class DataStationStatusResponse(val stations: List<DivvyStationStatus>)

data class DivvyStationStatus(
    @JsonProperty("station_id")
    val id: String,
    @JsonProperty("num_bikes_available")
    val availableBikes: Int,
    @JsonProperty("num_docks_available")
    val availableDocks: Int)
