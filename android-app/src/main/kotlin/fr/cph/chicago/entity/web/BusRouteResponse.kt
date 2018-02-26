package fr.cph.chicago.entity.web

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
class BusRoutesResponse(@JsonProperty("bustime-response") val bustimeResponse: BustimeResponse)

@JsonInclude(JsonInclude.Include.NON_NULL)
class BustimeResponse(@JsonProperty("routes") var routes: List<Route>)

@JsonInclude(JsonInclude.Include.NON_NULL)
class Route(@JsonProperty("rt") val routeId: String, @JsonProperty("rtnm") val routeName: String)

