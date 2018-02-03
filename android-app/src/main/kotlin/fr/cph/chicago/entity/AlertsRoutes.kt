package fr.cph.chicago.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AlertsRoutes(@JsonProperty("CTARoutes")
                        var ctaRoutes: CTARoutes? = null)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CTARoutes(
    @JsonProperty("TimeStamp")
    var timeStamp: String? = null,
    @JsonProperty("ErrorCode")
    var errorCode: List<String>? = null,
    @JsonProperty("ErrorMessage")
    var errorMessage: List<Any>? = null,
    @JsonProperty("RouteInfo")
    var routeInfo: List<RouteInfo>? = null)

@JsonInclude(JsonInclude.Include.NON_NULL)
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

@JsonInclude(JsonInclude.Include.NON_NULL)
class RouteURL(
    @JsonProperty("#cdata-section")
    var cdataSection: String? = null)
