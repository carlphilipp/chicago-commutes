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
import com.fasterxml.jackson.annotation.JsonPropertyOrder

class Alert(
    @JsonProperty("AlertId")
    var alertId: String? = null,
    @JsonProperty("Headline")
    var headline: String? = null,
    @JsonProperty("ShortDescription")
    var shortDescription: String? = null,
    @JsonProperty("FullDescription")
    var fullDescription: FullDescription? = null,
    @JsonProperty("SeverityScore")
    var severityScore: String? = null,
    @JsonProperty("SeverityColor")
    var severityColor: String? = null,
    @JsonProperty("SeverityCSS")
    var severityCSS: String? = null,
    @JsonProperty("Impact")
    var impact: String? = null,
    @JsonProperty("EventStart")
    var eventStart: String? = null,
    @JsonProperty("EventEnd")
    var eventEnd: String? = null,
    @JsonProperty("TBD")
    var tbd: String? = null,
    @JsonProperty("MajorAlert")
    var majorAlert: String? = null,
    @JsonProperty("AlertURL")
    var alertURL: AlertURL? = null,
    @JsonProperty("ImpactedService")
    var impactedService: ImpactedService? = null,
    @JsonProperty("ttim")
    var ttim: String? = null,
    @JsonProperty("GUID")
    var guid: String? = null)

class AlertURL(
    @JsonProperty("#cdata-section")
    var cdataSection: String? = null
)

class AlertsRoute(
    @JsonProperty("CTAAlerts")
    var ctaAlerts: CTAAlerts? = null
)

class CTAAlerts(
    @JsonProperty("TimeStamp")
    var timeStamp: String? = null,
    @JsonProperty("ErrorCode")
    var errorCode: String? = null,
    @JsonProperty("ErrorMessage")
    var errorMessage: Any? = null,
    @JsonProperty("Alert")
    var alert: List<Alert>? = null
)

class FullDescription(
    @JsonProperty("#cdata-section")
    var cdataSection: String? = null
)

@JsonPropertyOrder("Service")
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
