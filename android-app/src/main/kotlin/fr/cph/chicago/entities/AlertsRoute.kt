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

package fr.cph.chicago.entities

import com.fasterxml.jackson.annotation.JsonProperty

class AlertsRoute(
    @JsonProperty("CTAAlerts")
    val ctaAlerts: CTAAlerts
)

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
    @JsonProperty("FullDescription")
    val fullDescription: FullDescription,
    @JsonProperty("SeverityScore")
    val severityScore: String,
    @JsonProperty("SeverityColor")
    val severityColor: String,
    @JsonProperty("SeverityCSS")
    val severityCSS: String,
    @JsonProperty("Impact")
    val impact: String,
    @JsonProperty("EventStart")
    val eventStart: String,
    @JsonProperty("EventEnd")
    var eventEnd: String? = null,
    @JsonProperty("TBD")
    val tbd: String,
    @JsonProperty("MajorAlert")
    val majorAlert: String,
    @JsonProperty("AlertURL")
    val alertURL: AlertURL,
    @JsonProperty("ImpactedService")
    val impactedService: ImpactedService,
    val ttim: String,
    @JsonProperty("GUID")
    val guid: String)

class AlertURL(
    @JsonProperty("#cdata-section")
    var cdataSection: String? = null
)

class FullDescription(
    @JsonProperty("#cdata-section")
    var cdataSection: String? = null
)

class ImpactedService(
    @JsonProperty("Service")
    var service: List<Service>? = null
)

class Service(
    @JsonProperty("ServiceType")
    var serviceType: String? = null,
    @JsonProperty("ServiceTypeDescription")
    var serviceTypeDescription: String? = null,
    @JsonProperty("ServiceName")
    var serviceName: String? = null,
    @JsonProperty("ServiceId")
    var serviceId: String? = null,
    @JsonProperty("ServiceBackColor")
    var serviceBackColor: String? = null,
    @JsonProperty("ServiceTextColor")
    var serviceTextColor: String? = null,
    @JsonProperty("ServiceURL")
    var serviceURL: ServiceURL? = null
)

class ServiceURL(
    @JsonProperty("#cdata-section")
    var cdataSection: String? = null
)
