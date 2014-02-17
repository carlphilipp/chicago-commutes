package fr.cph.chicago.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Alert {
	private Integer id;
	private String headline;
	private String shortDescription;
	private String fullDescription;
	private Integer severityScore;
	private String severityColor;
	private String severityCSS;
	private String impact;
	private Date eventStart;
	private Date eventEnd;
	private Integer tbd;
	private Integer majorAlert;
	private String alertUrl;
	private List<Service> impactedServices;

	public Alert() {
		this.impactedServices = new ArrayList<Service>();
	}

	public final Integer getId() {
		return id;
	}

	public final void setId(Integer id) {
		this.id = id;
	}

	public final String getHeadline() {
		return headline;
	}

	public final void setHeadline(String headline) {
		this.headline = headline;
	}

	public final String getShortDescription() {
		return shortDescription;
	}

	public final void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public final String getFullDescription() {
		return fullDescription;
	}

	public final void setFullDescription(String fullDescription) {
		this.fullDescription = fullDescription;
	}

	public final Integer getSeverityScore() {
		return severityScore;
	}

	public final void setSeverityScore(Integer severityScore) {
		this.severityScore = severityScore;
	}

	public final String getSeverityColor() {
		return severityColor;
	}

	public final void setSeverityColor(String severityColor) {
		this.severityColor = severityColor;
	}

	public final String getSeverityCSS() {
		return severityCSS;
	}

	public final void setSeverityCSS(String severityCSS) {
		this.severityCSS = severityCSS;
	}

	public final String getImpact() {
		return impact;
	}

	public final void setImpact(String impact) {
		this.impact = impact;
	}

	public final Date getEventStart() {
		return eventStart;
	}

	public final void setEventStart(Date eventStart) {
		this.eventStart = eventStart;
	}

	public final Date getEventEnd() {
		return eventEnd;
	}

	public final void setEventEnd(Date eventEnd) {
		this.eventEnd = eventEnd;
	}

	public final Integer getTbd() {
		return tbd;
	}

	public final void setTbd(Integer tbd) {
		this.tbd = tbd;
	}

	public final Integer getMajorAlert() {
		return majorAlert;
	}

	public final void setMajorAlert(Integer majorAlert) {
		this.majorAlert = majorAlert;
	}

	public final String getAlertUrl() {
		return alertUrl;
	}

	public final void setAlertUrl(String alertUrl) {
		this.alertUrl = alertUrl;
	}

	public final List<Service> getImpactedServices() {
		return impactedServices;
	}

	public final void addService(Service impactedService) {
		impactedServices.add(impactedService);
	}

}
