package fr.cph.chicago;

/**
 * Constants needed across the application.
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public enum Constants {
    ;

    // CTA - Trains
    public static final String TRAINS_ARRIVALS_URL = "http://lapi.transitchicago.com/api/1.0/ttarrivals.aspx";
    public static final String TRAINS_FOLLOW_URL = "http://lapi.transitchicago.com/api/1.0/ttfollow.aspx";
    public static final String TRAINS_LOCATION_URL = "http://lapi.transitchicago.com/api/1.0/ttpositions.aspx";

    // CTA - Buses
    public static final String BUSES_ROUTES_URL = "http://www.ctabustracker.com/bustime/api/v1/getroutes";
    public static final String BUSES_DIRECTION_URL = "http://www.ctabustracker.com/bustime/api/v1/getdirections";
    public static final String BUSES_STOP_URL = "http://www.ctabustracker.com/bustime/api/v1/getstops";
    public static final String BUSES_VEHICLES_URL = "http://www.ctabustracker.com/bustime/api/v1/getvehicles";
    public static final String BUSES_ARRIVAL_URL = "http://www.ctabustracker.com/bustime/api/v1/getpredictions";
    public static final String BUSES_PATTERN_URL = "http://www.ctabustracker.com/bustime/api/v1/getpatterns";

    // Divvy
    public static final String DIVYY_URL = "http://www.divvybikes.com/stations/json";

    // Google
    public static final String GOOGLE_STREET_VIEW_URL = "http://maps.googleapis.com/maps/api/streetview";
}
