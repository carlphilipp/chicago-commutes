/**
 * Copyright 2018 Carl-Philipp Harmant
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

package fr.cph.chicago.service

import fr.cph.chicago.client.CtaClient
import fr.cph.chicago.client.CtaRequestType
import fr.cph.chicago.entity.AlertsRoute
import fr.cph.chicago.entity.AlertsRoutes
import fr.cph.chicago.entity.dto.AlertType
import fr.cph.chicago.entity.dto.RouteAlertsDTO
import fr.cph.chicago.entity.dto.RoutesAlertsDTO
import fr.cph.chicago.parser.JsonParser
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object AlertService {
    private val ctaClient = CtaClient
    private val jsonParser = JsonParser
    private val formatWithSeconds = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    private val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("MM/dd/yyyy h:mm a", Locale.US)

    fun getAlerts(): List<RoutesAlertsDTO> {
        val params = ArrayListValuedHashMap<String, String>(1, 2)
        params.put("type", "rail")
        params.put("type", "bus")
        val inputStream = ctaClient.connect(CtaRequestType.ALERTS_ROUTES, params)
        val alertRoutes = jsonParser.parse(inputStream, AlertsRoutes::class.java)
        return alertRoutes.ctaRoutes?.routeInfo
            ?.filter { routeInfo -> routeInfo.serviceId!! != "Pexp" }
            ?.map { routeInfo ->
                RoutesAlertsDTO(
                    id = routeInfo.serviceId!!,
                    routeName = routeInfo.route!!,
                    routeBackgroundColor = if (routeInfo.routeColorCode!!.length == 6) "#" + routeInfo.routeColorCode!! else "#000000",
                    routeTextColor = "#" + routeInfo.routeTextColor!!,
                    routeStatus = routeInfo.routeStatus!!,
                    routeStatusColor = "#" + routeInfo.routeStatusColor!!,
                    alertType = if (routeInfo.route!!.contains("Line")) AlertType.TRAIN else AlertType.BUS)
            }
            ?.toList()!!
    }

    fun getRouteAlert(id: String): List<RouteAlertsDTO> {
        val params = ArrayListValuedHashMap<String, String>(1, 1)
        params.put("routeid", id)
        val inputStream = ctaClient.connect(CtaRequestType.ALERTS_ROUTE, params)
        val alertRoutes = jsonParser.parse(inputStream, AlertsRoute::class.java)

        return if (alertRoutes.ctaAlerts!!.errorMessage != null)
            listOf()
        else
            alertRoutes.ctaAlerts!!.alert!!
                .map { alert ->
                    RouteAlertsDTO(
                        id = alert.alertId!!,
                        headLine = alert.headline!!,
                        description = alert.shortDescription!!.replace("\r\n", ""),
                        impact = alert.impact!!,
                        severityScore = alert.severityScore!!.toInt(),
                        start = formatDate(alert.eventStart),
                        end = formatDate(alert.eventEnd)
                    )
                }
                .sortedByDescending { it.severityScore }
                .toList()
    }

    private fun formatDate(str: String?): String {
        if (str == null) return ""
        return try {
            displayFormat.format(formatWithSeconds.parse(str))
        } catch (p: ParseException) {
            displayFormat.format(format.parse(str))
        }
    }
}
