package fr.cph.chicago.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 * @author carl
 * 
 */
public class Alert {
	/** **/
	private Integer id;
	/** **/
	private String headline;
	/** **/
	private String shortDescription;
	/** **/
	private String fullDescription;
	/** **/
	private Integer severityScore;
	/** **/
	private String severityColor;
	/** **/
	private String severityCSS;
	/** **/
	private String impact;
	/** **/
	private Date eventStart;
	/** **/
	private Date eventEnd;
	/** **/
	private Integer tbd;
	/** **/
	private Integer majorAlert;
	/** **/
	private String alertUrl;
	/** **/
	private List<Service> impactedServices;

	/**
	 * 
	 */
	public Alert() {
		this.impactedServices = new ArrayList<Service>();
	}

	/**
	 * 
	 * @return
	 */
	public final Integer getId() {
		return id;
	}

	/**
	 * 
	 * @param id
	 */
	public final void setId(final Integer id) {
		this.id = id;
	}

	/**
	 * 
	 * @return
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
