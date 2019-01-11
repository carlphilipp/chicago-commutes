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

data class DivvyResponse(
    @JsonProperty("stationBeanList")
    val stations: List<DivvyStation>)

// Commenting out fields not used
data class DivvyStation(
    val id: Int,
    @JsonProperty("stationName")
    val name: String,
    val availableDocks: Int,
    //val totalDocks: Int,
    val latitude: Double,
    val longitude: Double,
    //val statusValue: String,
    //val statusKey: Int,
    //val status: String,
    val availableBikes: Int,
    val stAddress1: String)
    //val stAddress2: String,
    //val city: String,
    //val postalCode: String,
    //val location: String,
    //val altitude: String,
    //val testStation: Boolean,
    //val lastCommunicationTime: String,
    //val landMark: String,
    //@JsonProperty("is_renting")
    //val isRenting: Boolean)
