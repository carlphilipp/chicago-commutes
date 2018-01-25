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

        private const val TRAINS_BASE = "http://lapi.transitchicago.com/api/1.0/"
        private const val BUSES_BASE = "http://www.ctabustracker.com/bustime/api/v1/"
        private const val ALERTS_BASE = "http://www.transitchicago.com/api/1.0/"

        // CTA - Trains
        const val TRAINS_ARRIVALS_URL = TRAINS_BASE + "ttarrivals.aspx"
        const val TRAINS_FOLLOW_URL = TRAINS_BASE + "ttfollow.aspx"
        const val TRAINS_LOCATION_URL = TRAINS_BASE + "ttpositions.aspx"


        // CTA - Buses
        const val BUSES_ROUTES_URL = BUSES_BASE + "getroutes"
        const val BUSES_DIRECTION_URL = BUSES_BASE + "getdirections"
        const val BUSES_STOP_URL = BUSES_BASE + "getstops"
        const val BUSES_VEHICLES_URL = BUSES_BASE + "getvehicles"
        const val BUSES_ARRIVAL_URL = BUSES_BASE + "getpredictions"
        const val BUSES_PATTERN_URL = BUSES_BASE + "getpatterns"

        // CTA - Alerts
        const val ALERTS_ROUTES_URL = ALERTS_BASE + "routes.aspx"

        // Divvy
        const val DIVYY_URL = "https://feeds.divvybikes.com/stations/stations.json"

        // Google
        const val GOOGLE_STREET_VIEW_URL = "http://maps.googleapis.com/maps/api/streetview"
    }
}
