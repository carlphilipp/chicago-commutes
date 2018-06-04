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

data class AlertsRoutes(@JsonProperty("CTARoutes") val ctaRoutes: CTARoutes)

data class CTARoutes(
    @JsonProperty("TimeStamp")
    var timeStamp: String? = null,
    @JsonProperty("ErrorCode")
    var errorCode: List<String>? = null,
    @JsonProperty("ErrorMessage")
    var errorMessage: List<Any>? = null,
    @JsonProperty("RouteInfo")
    var routeInfo: List<RouteInfo>? = null)

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

class RouteURL(
    @JsonProperty("#cdata-section")
    var cdataSection: String? = null)
