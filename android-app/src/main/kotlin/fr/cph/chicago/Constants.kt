/**
 * Copyright 2021 Carl-Philipp Harmant
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
    // Base URLs
    const val TRAINS_BASE = "http://lapi.transitchicago.com"
    const val BUSES_BASE = "http://www.ctabustracker.com"
    const val ALERTS_BASE = "http://www.transitchicago.com"
    const val DIVVY_BASE = "https://gbfs.divvybikes.com"
    const val GOOGLE_STREET_VIEW_BASE = "http://maps.googleapis.com"

    // Base paths
    const val TRAINS_BASE_PATH = "api/1.0"
    const val BUSES_BASE_PATH = "bustime/api/v2"
    const val ALERTS_BASE_PATH = "api/1.0"
    const val DIVVY_BASE_PATH = "gbfs/en"

    // Requests param
    const val REQUEST_ROUTE = "rt"
    const val REQUEST_STOP_ID = "stpid"
    const val REQUEST_DIR = "dir"
    const val REQUEST_MAP_ID = "mapid"
    const val REQUEST_RUN_NUMBER = "runnumber"
    const val REQUEST_VID = "vid"
    const val REQUEST_KEY = "key"
    const val REQUEST_OUTPUT_TYPE = "outputType"
    const val REQUEST_FORMAT = "format"
    const val REQUEST_TYPE = "type"
    const val REQUEST_ROUTE_ID = "routeid"

    // Parsing
    const val STOP_FILE_PATH = "bus_stops.txt"
}
