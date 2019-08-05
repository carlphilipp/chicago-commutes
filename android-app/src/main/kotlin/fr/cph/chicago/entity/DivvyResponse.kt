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

data class StationInformationResponse(val data: DataStationInformationResponse)

data class DataStationInformationResponse(val stations: List<DivvyStationInformation>)

data class DivvyStationInformation(
    val station_id: String,
    var name: String,
    val short_name: String,
    val lat: Double,
    val lon: Double,
    val capacity: Int)

data class StationStatusResponse(val data: DataStationStatusResponse)

data class DataStationStatusResponse(val stations: List<DivvyStationStatus>)

data class DivvyStationStatus(
    val station_id: String,
    val num_bikes_available: Int,
    val num_docks_available: Int)
