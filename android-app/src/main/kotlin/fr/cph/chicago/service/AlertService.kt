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

package fr.cph.chicago.service

import fr.cph.chicago.client.CtaClient
import fr.cph.chicago.client.CtaRequestType
import fr.cph.chicago.core.model.dto.AlertType
import fr.cph.chicago.core.model.dto.RouteAlertsDTO
import fr.cph.chicago.core.model.dto.RoutesAlertsDTO
import fr.cph.chicago.entity.AlertsRouteResponse
import fr.cph.chicago.entity.AlertsRoutesResponse
import org.apache.commons.collections4.MultiValuedMap
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

object AlertService {

    private val ctaClient = CtaClient
    private val formatWithSeconds = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    private val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("MM/dd/yyyy h:mm a", Locale.US)

    fun getAlerts(): List<RoutesAlertsDTO> {
        val alertRoutes = ctaClient.get(CtaRequestType.ALERTS_ROUTES, buildAlertsParam(), AlertsRoutesResponse::class.java)
        if (alertRoutes.ctaRoutes.routeInfo.isEmpty()) {
            val errors = alertRoutes.ctaRoutes.errorMessage.joinToString()
            Timber.e(errors)
            return listOf()
        }
        return alertRoutes.ctaRoutes.routeInfo
            .filter { routeInfo -> routeInfo.serviceId!! != "Pexp" }
            .map { routeInfo ->
                RoutesAlertsDTO(
                    id = routeInfo.serviceId!!,
                    routeName = routeInfo.route!!,
                    routeBackgroundColor = if (routeInfo.routeColorCode!!.length == 6) "#" + routeInfo.routeColorCode!! else "#000000",
                    routeTextColor = "#" + routeInfo.routeTextColor!!,
                    routeStatus = routeInfo.routeStatus!!,
                    routeStatusColor = "#" + routeInfo.routeStatusColor!!,
                    alertType = if (routeInfo.route!!.contains("Line")) AlertType.TRAIN else AlertType.BUS)
            }
    }

    fun getRouteAlert(id: String): List<RouteAlertsDTO> {
        val alertRoutes = ctaClient.get(CtaRequestType.ALERTS_ROUTE, buildAlertParam(id), AlertsRouteResponse::class.java)

        return if (alertRoutes.ctaAlerts.errorMessage != null) {
            Timber.e(alertRoutes.ctaAlerts.errorMessage.toString())
            listOf()
        } else
            alertRoutes.ctaAlerts.alert
                .map { alert ->
                    RouteAlertsDTO(
                        id = alert.alertId,
                        headLine = alert.headline,
                        description = alert.shortDescription.replace("\r\n", ""),
                        impact = alert.impact,
                        severityScore = alert.severityScore.toInt(),
                        start = formatDate(alert.eventStart),
                        end = formatDate(alert.eventEnd))
                }
                .sortedByDescending { it.severityScore }
    }

    private fun buildAlertsParam(): MultiValuedMap<String, String> {
        val params = ArrayListValuedHashMap<String, String>(1, 2)
        params.put("type", "rail")
        params.put("type", "bus")
        return params
    }


    private fun buildAlertParam(id: String): MultiValuedMap<String, String> {
        val params = ArrayListValuedHashMap<String, String>(1, 1)
        params.put("routeid", id)
        return params
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
