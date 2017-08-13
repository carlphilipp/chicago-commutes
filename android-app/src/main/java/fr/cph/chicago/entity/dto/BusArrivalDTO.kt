package fr.cph.chicago.entity.dto

import fr.cph.chicago.entity.BusArrival

/**
 * @author cpharmant
 */
class BusArrivalDTO(val busArrivals: List<BusArrival>? = null, val error: Boolean = false)
