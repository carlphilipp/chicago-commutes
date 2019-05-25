/**
 * Copyright 2019 Carl-Philipp Harmant
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

package fr.cph.chicago

/**
 * Constants needed across the application.
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
object Constants {
    const val GPS_ACCESS = 1

    const val TRAINS_BASE = "http://lapi.transitchicago.com/api/1.0/"
    private const val BUSES_BASE = "http://www.ctabustracker.com/bustime/api/v2/"
    private const val ALERTS_BASE = "http://www.transitchicago.com/api/1.0/"

    // CTA - Buses
    const val BUSES_ROUTES_URL = BUSES_BASE + "getroutes"
    const val BUSES_DIRECTION_URL = BUSES_BASE + "getdirections"
    const val BUSES_STOP_URL = BUSES_BASE + "getstops"
    const val BUSES_VEHICLES_URL = BUSES_BASE + "getvehicles"
    const val BUSES_ARRIVAL_URL = BUSES_BASE + "getpredictions"
    const val BUSES_PATTERN_URL = BUSES_BASE + "getpatterns"

    // CTA - Alerts
    const val ALERTS_ROUTES_URL = ALERTS_BASE + "routes.aspx"
    const val ALERT_ROUTES_URL = ALERTS_BASE + "alerts.aspx"

    // Divvy
    const val DIVYY_URL = "https://feeds.divvybikes.com/stations/stations.json"

    // Google
    const val GOOGLE_STREET_VIEW_URL = "http://maps.googleapis.com/maps/api/streetview"

    const val SELECTED_ID = "SELECTED_ID"
}
