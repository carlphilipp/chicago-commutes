package fr.cph.chicago.entity

import com.fasterxml.jackson.annotation.*


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("CTARoutes")
class AlertsRoutes {

    @JsonProperty("CTARoutes")
    @get:JsonProperty("CTARoutes")
    @set:JsonProperty("CTARoutes")
    var ctaRoutes: CTARoutes? = null

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
@JsonPropertyOrder("TimeStamp", "ErrorCode", "ErrorMessage", "RouteInfo")
class CTARoutes {

    @JsonProperty("TimeStamp")
    @get:JsonProperty("TimeStamp")
    @set:JsonProperty("TimeStamp")
    var timeStamp: String? = null
    @JsonProperty("ErrorCode")
    @get:JsonProperty("ErrorCode")
    @set:JsonProperty("ErrorCode")
    var errorCode: List<String>? = null
    @JsonProperty("ErrorMessage")
    @get:JsonProperty("ErrorMessage")
    @set:JsonProperty("ErrorMessage")
    var errorMessage: List<Any>? = null
    @JsonProperty("RouteInfo")
    @get:JsonProperty("RouteInfo")
    @set:JsonProperty("RouteInfo")
    var routeInfo: List<RouteInfo>? = null
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
@JsonPropertyOrder("Route", "RouteColorCode", "RouteTextColor", "ServiceId", "RouteURL", "RouteStatus", "RouteStatusColor")
class RouteInfo {

    @JsonProperty("Route")
    @get:JsonProperty("Route")
    @set:JsonProperty("Route")
    var route: String? = null
    @JsonProperty("RouteColorCode")
    @get:JsonProperty("RouteColorCode")
    @set:JsonProperty("RouteColorCode")
    var routeColorCode: String? = null
    @JsonProperty("RouteTextColor")
    @get:JsonProperty("RouteTextColor")
    @set:JsonProperty("RouteTextColor")
    var routeTextColor: String? = null
    @JsonProperty("ServiceId")
    @get:JsonProperty("ServiceId")
    @set:JsonProperty("ServiceId")
    var serviceId: String? = null
    @JsonProperty("RouteURL")
    @get:JsonProperty("RouteURL")
    @set:JsonProperty("RouteURL")
    var routeURL: RouteURL? = null
    @JsonProperty("RouteStatus")
    @get:JsonProperty("RouteStatus")
    @set:JsonProperty("RouteStatus")
    var routeStatus: String? = null
    @JsonProperty("RouteStatusColor")
    @get:JsonProperty("RouteStatusColor")
    @set:JsonProperty("RouteStatusColor")
    var routeStatusColor: String? = null
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
class RouteURL {

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
