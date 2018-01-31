package fr.cph.chicago.entity

import com.fasterxml.jackson.annotation.*
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("AlertId", "Headline", "ShortDescription", "FullDescription", "SeverityScore", "SeverityColor", "SeverityCSS", "Impact", "EventStart", "EventEnd", "TBD", "MajorAlert", "AlertURL", "ImpactedService", "ttim", "GUID")
class Alert {

    @JsonProperty("AlertId")
    @get:JsonProperty("AlertId")
    @set:JsonProperty("AlertId")
    var alertId: String? = null
    @JsonProperty("Headline")
    @get:JsonProperty("Headline")
    @set:JsonProperty("Headline")
    var headline: String? = null
    @JsonProperty("ShortDescription")
    @get:JsonProperty("ShortDescription")
    @set:JsonProperty("ShortDescription")
    var shortDescription: String? = null
    @JsonProperty("FullDescription")
    @get:JsonProperty("FullDescription")
    @set:JsonProperty("FullDescription")
    var fullDescription: FullDescription? = null
    @JsonProperty("SeverityScore")
    @get:JsonProperty("SeverityScore")
    @set:JsonProperty("SeverityScore")
    var severityScore: String? = null
    @JsonProperty("SeverityColor")
    @get:JsonProperty("SeverityColor")
    @set:JsonProperty("SeverityColor")
    var severityColor: String? = null
    @JsonProperty("SeverityCSS")
    @get:JsonProperty("SeverityCSS")
    @set:JsonProperty("SeverityCSS")
    var severityCSS: String? = null
    @JsonProperty("Impact")
    @get:JsonProperty("Impact")
    @set:JsonProperty("Impact")
    var impact: String? = null
    @JsonProperty("EventStart")
    @get:JsonProperty("EventStart")
    @set:JsonProperty("EventStart")
    var eventStart: String? = null
    @JsonProperty("EventEnd")
    @get:JsonProperty("EventEnd")
    @set:JsonProperty("EventEnd")
    var eventEnd: Any? = null
    @JsonProperty("TBD")
    @get:JsonProperty("TBD")
    @set:JsonProperty("TBD")
    var tbd: String? = null
    @JsonProperty("MajorAlert")
    @get:JsonProperty("MajorAlert")
    @set:JsonProperty("MajorAlert")
    var majorAlert: String? = null
    @JsonProperty("AlertURL")
    @get:JsonProperty("AlertURL")
    @set:JsonProperty("AlertURL")
    var alertURL: AlertURL? = null
    @JsonProperty("ImpactedService")
    @get:JsonProperty("ImpactedService")
    @set:JsonProperty("ImpactedService")
    var impactedService: ImpactedService? = null
    @JsonProperty("ttim")
    @get:JsonProperty("ttim")
    @set:JsonProperty("ttim")
    var ttim: String? = null
    @JsonProperty("GUID")
    @get:JsonProperty("GUID")
    @set:JsonProperty("GUID")
    var guid: String? = null
    @JsonIgnore
    private val additionalProperties = HashMap<String, Any>()

    @JsonAnyGetter
    fun getAdditionalProperties(): Map<String, Any> {
        return this.additionalProperties
    }

    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any) {
        this.additionalProperties[name] = value
    }

}

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("#cdata-section")
class AlertURL {

    @JsonProperty("#cdata-section")
    @get:JsonProperty("#cdata-section")
    @set:JsonProperty("#cdata-section")
    var cdataSection: String? = null
    @JsonIgnore
    private val additionalProperties = HashMap<String, Any>()

    @JsonAnyGetter
    fun getAdditionalProperties(): Map<String, Any> {
        return this.additionalProperties
    }

    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any) {
        this.additionalProperties[name] = value
    }

}

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("CTAAlerts")
class AlertsRoute {

    @JsonProperty("CTAAlerts")
    @get:JsonProperty("CTAAlerts")
    @set:JsonProperty("CTAAlerts")
    var ctaAlerts: CTAAlerts? = null
    @JsonIgnore
    private val additionalProperties = HashMap<String, Any>()

    @JsonAnyGetter
    fun getAdditionalProperties(): Map<String, Any> {
        return this.additionalProperties
    }

    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any) {
        this.additionalProperties[name] = value
    }

}

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("TimeStamp", "ErrorCode", "ErrorMessage", "Alert")
class CTAAlerts {

    @JsonProperty("TimeStamp")
    @get:JsonProperty("TimeStamp")
    @set:JsonProperty("TimeStamp")
    var timeStamp: String? = null
    @JsonProperty("ErrorCode")
    @get:JsonProperty("ErrorCode")
    @set:JsonProperty("ErrorCode")
    var errorCode: String? = null
    @JsonProperty("ErrorMessage")
    @get:JsonProperty("ErrorMessage")
    @set:JsonProperty("ErrorMessage")
    var errorMessage: Any? = null
    @JsonProperty("Alert")
    @get:JsonProperty("Alert")
    @set:JsonProperty("Alert")
    var alert: List<Alert>? = null
    @JsonIgnore
    private val additionalProperties = HashMap<String, Any>()

    @JsonAnyGetter
    fun getAdditionalProperties(): Map<String, Any> {
        return this.additionalProperties
    }

    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any) {
        this.additionalProperties[name] = value
    }

}

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("#cdata-section")
class FullDescription {

    @JsonProperty("#cdata-section")
    @get:JsonProperty("#cdata-section")
    @set:JsonProperty("#cdata-section")
    var cdataSection: String? = null
    @JsonIgnore
    private val additionalProperties = HashMap<String, Any>()

    @JsonAnyGetter
    fun getAdditionalProperties(): Map<String, Any> {
        return this.additionalProperties
    }

    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any) {
        this.additionalProperties[name] = value
    }

}

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("Service")
class ImpactedService {

    @JsonProperty("Service")
    @get:JsonProperty("Service")
    @set:JsonProperty("Service")
    var service: List<Service>? = null
    @JsonIgnore
    private val additionalProperties = HashMap<String, Any>()

    @JsonAnyGetter
    fun getAdditionalProperties(): Map<String, Any> {
        return this.additionalProperties
    }

    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any) {
        this.additionalProperties[name] = value
    }

}

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("ServiceType", "ServiceTypeDescription", "ServiceName", "ServiceId", "ServiceBackColor", "ServiceTextColor", "ServiceURL")
class Service {

    @JsonProperty("ServiceType")
    @get:JsonProperty("ServiceType")
    @set:JsonProperty("ServiceType")
    var serviceType: String? = null
    @JsonProperty("ServiceTypeDescription")
    @get:JsonProperty("ServiceTypeDescription")
    @set:JsonProperty("ServiceTypeDescription")
    var serviceTypeDescription: String? = null
    @JsonProperty("ServiceName")
    @get:JsonProperty("ServiceName")
    @set:JsonProperty("ServiceName")
    var serviceName: String? = null
    @JsonProperty("ServiceId")
    @get:JsonProperty("ServiceId")
    @set:JsonProperty("ServiceId")
    var serviceId: String? = null
    @JsonProperty("ServiceBackColor")
    @get:JsonProperty("ServiceBackColor")
    @set:JsonProperty("ServiceBackColor")
    var serviceBackColor: String? = null
    @JsonProperty("ServiceTextColor")
    @get:JsonProperty("ServiceTextColor")
    @set:JsonProperty("ServiceTextColor")
    var serviceTextColor: String? = null
    @JsonProperty("ServiceURL")
    @get:JsonProperty("ServiceURL")
    @set:JsonProperty("ServiceURL")
    var serviceURL: ServiceURL? = null
    @JsonIgnore
    private val additionalProperties = HashMap<String, Any>()

    @JsonAnyGetter
    fun getAdditionalProperties(): Map<String, Any> {
        return this.additionalProperties
    }

    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any) {
        this.additionalProperties[name] = value
    }

}

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("#cdata-section")
class ServiceURL {

    @JsonProperty("#cdata-section")
    @get:JsonProperty("#cdata-section")
    @set:JsonProperty("#cdata-section")
    var cdataSection: String? = null
    @JsonIgnore
    private val additionalProperties = HashMap<String, Any>()

    @JsonAnyGetter
    fun getAdditionalProperties(): Map<String, Any> {
        return this.additionalProperties
    }

    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any) {
        this.additionalProperties[name] = value
    }
}
