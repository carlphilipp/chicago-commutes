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

package fr.cph.chicago.entity

import com.fasterxml.jackson.annotation.JsonProperty

class AlertsRouteResponse(
    @JsonProperty("CTAAlerts")
    val ctaAlerts: CTAAlerts) {

    class CTAAlerts(
        @JsonProperty("ErrorMessage")
        var errorMessage: Any? = null,
        @JsonProperty("Alert")
        val alert: List<Alert> = listOf()
    )

    class Alert(
        @JsonProperty("AlertId")
        val alertId: String,
        @JsonProperty("Headline")
        val headline: String,
        @JsonProperty("ShortDescription")
        val shortDescription: String,
        @JsonProperty("SeverityScore")
        val severityScore: String,
        @JsonProperty("Impact")
        val impact: String,
        @JsonProperty("EventStart")
        val eventStart: String,
        @JsonProperty("EventEnd")
        var eventEnd: String? = null)
}

data class AlertsRoutesResponse(@JsonProperty("CTARoutes") val ctaRoutes: CTARoutes) {
    data class CTARoutes(
        @JsonProperty("ErrorMessage")
        val errorMessage: List<Any> = listOf(),
        @JsonProperty("RouteInfo")
        val routeInfo: List<RouteInfo> = listOf())

    data class RouteInfo(
        @JsonProperty("Route")
        var route: String? = null,
        @JsonProperty("RouteColorCode")
        var routeColorCode: String? = null,
        @JsonProperty("RouteTextColor")
        var routeTextColor: String? = null,
        @JsonProperty("ServiceId")
        var serviceId: String? = null,
        @JsonProperty("RouteURL")
        var routeURL: RouteURL? = null,
        @JsonProperty("RouteStatus")
        var routeStatus: String? = null,
        @JsonProperty("RouteStatusColor")
        var routeStatusColor: String? = null)

    class RouteURL
}
