/**
 * Copyright 2016 Carl-Philipp Harmant
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Alert entity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class Alert {
	/** The id **/
	private Integer id;
	/** The headline **/
	private String headline;
	/** The short description **/
	private String shortDescription;
	/** The full description **/
	private String fullDescription;
	/** The severity score **/
	private Integer severityScore;
	/** The severity color **/
	private String severityColor;
	/** The severity css **/
	private String severityCSS;
	/** THe impact **/
	private String impact;
	/** Beginning of the alert **/
	private Date eventStart;
	/** End of the alert **/
	private Date eventEnd;
	/** Tbd ? **/
	private Integer tbd;
	/** Level of alert **/
	private Integer majorAlert;
	/** Alert URL **/
	private String alertUrl;
	/** List of service impacted **/
	private List<Service> impactedServices;

	/**
	 * Constructor
	 */
	public Alert() {
		this.impactedServices = new ArrayList<>();
	}

	/**
	 *
	 * @return the id
	 */
	public final Integer getId() {
		return id;
	}

	/**
	 *
	 * @param id
	 *            the id
	 */
	public final void setId(final Integer id) {
		this.id = id;
	}

	/**
	 *
	 * @return the headline
	 */
	public final String getHeadline() {
		return headline;
	}

	/**
	 *
	 * @param headline
	 */
	public final void setHeadline(final String headline) {
		this.headline = headline;
	}

	/**
	 *
	 * @return
	 */
	public final String getShortDescription() {
		return shortDescription;
	}

	/**
	 *
	 * @param shortDescription
	 */
	public final void setShortDescription(final String shortDescription) {
		this.shortDescription = shortDescription;
	}

	/**
	 *
	 * @return
	 */
	public final String getFullDescription() {
		return fullDescription;
	}

	/**
	 *
	 * @param fullDescription
	 */
	public final void setFullDescription(final String fullDescription) {
		this.fullDescription = fullDescription;
	}

	/**
	 *
	 * @return
	 */
	public final Integer getSeverityScore() {
		return severityScore;
	}

	/**
	 *
	 * @param severityScore
	 */
	public final void setSeverityScore(final Integer severityScore) {
		this.severityScore = severityScore;
	}

	/**
	 *
	 * @return
	 */
	public final String getSeverityColor() {
		return severityColor;
	}

	/**
	 *
	 * @param severityColor
	 */
	public final void setSeverityColor(final String severityColor) {
		this.severityColor = severityColor;
	}

	/**
	 *
	 * @return
	 */
	public final String getSeverityCSS() {
		return severityCSS;
	}

	/**
	 *
	 * @param severityCSS
	 */
	public final void setSeverityCSS(final String severityCSS) {
		this.severityCSS = severityCSS;
	}

	public final String getImpact() {
		return impact;
	}

	/**
	 *
	 * @param impact
	 */
	public final void setImpact(final String impact) {
		this.impact = impact;
	}

	/**
	 *
	 * @return
	 */
	public final Date getEventStart() {
		return eventStart;
	}

	/**
	 *
	 * @param eventStart
	 */
	public final void setEventStart(final Date eventStart) {
		this.eventStart = eventStart;
	}

	/**
	 *
	 * @return
	 */
	public final Date getEventEnd() {
		return eventEnd;
	}

	/**
	 *
	 * @param eventEnd
	 */
	public final void setEventEnd(final Date eventEnd) {
		this.eventEnd = eventEnd;
	}

	/**
	 *
	 * @return
	 */
	public final Integer getTbd() {
		return tbd;
	}

	/**
	 *
	 * @param tbd
	 */
	public final void setTbd(final Integer tbd) {
		this.tbd = tbd;
	}

	/**
	 *
	 * @return
	 */
	public final Integer getMajorAlert() {
		return majorAlert;
	}

	/**
	 *
	 * @param majorAlert
	 */
	public final void setMajorAlert(Integer majorAlert) {
		this.majorAlert = majorAlert;
	}

	/**
	 *
	 * @return
	 */
	public final String getAlertUrl() {
		return alertUrl;
	}

	/**
	 *
	 * @param alertUrl
	 */
	public final void setAlertUrl(final String alertUrl) {
		this.alertUrl = alertUrl;
	}

	/**
	 *
	 * @return
	 */
	public final List<Service> getImpactedServices() {
		return impactedServices;
	}

	/**
	 *
	 * @param impactedService
	 */
	public final void addService(final Service impactedService) {
		impactedServices.add(impactedService);
	}

}
