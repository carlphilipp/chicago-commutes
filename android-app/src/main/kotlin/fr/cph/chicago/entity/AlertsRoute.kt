package fr.cph.chicago.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonInclude(JsonInclude.Include.NON_NULL)
class Alert {

    @JsonProperty("AlertId")
    var alertId: String? = null
    @JsonProperty("Headline")
    var headline: String? = null
    @JsonProperty("ShortDescription")
    var shortDescription: String? = null
    @JsonProperty("FullDescription")
    var fullDescription: FullDescription? = null
    @JsonProperty("SeverityScore")
    var severityScore: String? = null
    @JsonProperty("SeverityColor")
    var severityColor: String? = null
    @JsonProperty("SeverityCSS")
    var severityCSS: String? = null
    @JsonProperty("Impact")
    var impact: String? = null
    @JsonProperty("EventStart")
    var eventStart: String? = null
    @JsonProperty("EventEnd")
    var eventEnd: String? = null
    @JsonProperty("TBD")
    var tbd: String? = null
    @JsonProperty("MajorAlert")
    var majorAlert: String? = null
    @JsonProperty("AlertURL")
    var alertURL: AlertURL? = null
    @JsonProperty("ImpactedService")
    var impactedService: ImpactedService? = null
    @JsonProperty("ttim")
    var ttim: String? = null
    @JsonProperty("GUID")
    var guid: String? = null
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class AlertURL {

    @JsonProperty("#cdata-section")
    var cdataSection: String? = null
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class AlertsRoute {

    @JsonProperty("CTAAlerts")
    var ctaAlerts: CTAAlerts? = null
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class CTAAlerts {

    @JsonProperty("TimeStamp")
    var timeStamp: String? = null
    @JsonProperty("ErrorCode")
    var errorCode: String? = null
    @JsonProperty("ErrorMessage")
    var errorMessage: Any? = null
    @JsonProperty("Alert")
    var alert: List<Alert>? = null
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class FullDescription {

    @JsonProperty("#cdata-section")
    var cdataSection: String? = null

}

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("Service")
class ImpactedService {

    @JsonProperty("Service")
    var service: List<Service>? = null
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class Service {

    @JsonProperty("ServiceType")
    var serviceType: String? = null
    @JsonProperty("ServiceTypeDescription")
    var serviceTypeDescription: String? = null
    @JsonProperty("ServiceName")
    var serviceName: String? = null
    @JsonProperty("ServiceId")
    var serviceId: String? = null
    @JsonProperty("ServiceBackColor")
    var serviceBackColor: String? = null
    @JsonProperty("ServiceTextColor")
    var serviceTextColor: String? = null
    @JsonProperty("ServiceURL")
    var serviceURL: ServiceURL? = null
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class ServiceURL {

    @JsonProperty("#cdata-section")
    var cdataSection: String? = null
}
