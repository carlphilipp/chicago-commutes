package fr.cph.chicago

/**
 * Constants needed across the application.

 * @author Carl-Philipp Harmant
 * *
 * @version 1
 */
class Constants {
    companion object {
        const val GPS_ACCESS = 1

        // CTA - Trains
        const val TRAINS_ARRIVALS_URL = "http://lapi.transitchicago.com/api/1.0/ttarrivals.aspx"
        const val TRAINS_FOLLOW_URL = "http://lapi.transitchicago.com/api/1.0/ttfollow.aspx"
        const val TRAINS_LOCATION_URL = "http://lapi.transitchicago.com/api/1.0/ttpositions.aspx"

        // CTA - Buses
        const val BUSES_ROUTES_URL = "http://www.ctabustracker.com/bustime/api/v1/getroutes"
        const val BUSES_DIRECTION_URL = "http://www.ctabustracker.com/bustime/api/v1/getdirections"
        const val BUSES_STOP_URL = "http://www.ctabustracker.com/bustime/api/v1/getstops"
        const val BUSES_VEHICLES_URL = "http://www.ctabustracker.com/bustime/api/v1/getvehicles"
        const val BUSES_ARRIVAL_URL = "http://www.ctabustracker.com/bustime/api/v1/getpredictions"
        const val BUSES_PATTERN_URL = "http://www.ctabustracker.com/bustime/api/v1/getpatterns"

        // Divvy
        const val DIVYY_URL = "https://feeds.divvybikes.com/stations/stations.json"

        // Google
        const val GOOGLE_STREET_VIEW_URL = "http://maps.googleapis.com/maps/api/streetview"
    }
}
