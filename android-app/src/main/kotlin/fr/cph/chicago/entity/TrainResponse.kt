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

package fr.cph.chicago.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class TrainArrivalResponse(val ctatt: Ctatt) {
    data class Ctatt(
        val tmst: String,
        val errCd: String,
        val errNm: String? = null,
        val eta: List<Eta>? = null)

    data class Eta(
        val staId: String,
        val stpId: String,
        val staNm: String,
        val stpDe: String,
        val rn: String,
        val rt: String,
        val destSt: String,
        val destNm: String,
        val trDr: String,
        val prdt: String,
        val arrT: String,
        val isApp: String,
        val isSch: String,
        val isDly: String,
        val isFlt: String,
        val lat: String? = null,
        val lon: String? = null,
        val heading: String? = null
    )
}

data class TrainLocationResponse(val ctatt: Ctatt) {
    data class Ctatt(
        val tmst: String,
        var errCd: String,
        var errNm: String? = null,
        var route: List<Route>? = null)


    data class Route(
        @JsonProperty("@name")
        val name: String,
        val train: List<Train> = listOf())

    data class Train(
        var rn: String,
        var destSt: String,
        var destNm: String,
        var trDr: String,
        var nextStaId: String,
        var nextStpId: String,
        var nextStaNm: String,
        var prdt: String,
        var arrT: String,
        var isApp: String,
        var isDly: String,
        var lat: String,
        var lon: String,
        var heading: String)
}
