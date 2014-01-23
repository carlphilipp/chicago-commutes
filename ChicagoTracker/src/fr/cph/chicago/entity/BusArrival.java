package fr.cph.chicago.entity;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import fr.cph.chicago.entity.enumeration.PredictionType;

public class BusArrival {
	private Date timeStamp;
	private String errorMessage;
	private PredictionType predictionType;
	private String stopName;
	private Integer stopId;
	private Integer busId;
	private Integer distanceToStop; // feets
	private String routeId;
	private String routeDirection;
	private String busDestination;
	private Date predictionTime;
	private Boolean isDly;

	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public PredictionType getPredictionType() {
		return predictionType;
	}

	public void setPredictionType(PredictionType predictionType) {
		this.predictionType = predictionType;
	}

	public String getStopName() {
		return stopName;
	}

	public void setStopName(String stopName) {
		this.stopName = stopName;
	}

	public Integer getStopId() {
		return stopId;
	}

	public void setStopId(Integer stopId) {
		this.stopId = stopId;
	}

	public Integer getBusId() {
		return busId;
	}

	public void setBusId(Integer busId) {
		this.busId = busId;
	}

	public Integer getDistanceToStop() {
		return distanceToStop;
	}

	public void setDistanceToStop(Integer distanceToStop) {
		this.distanceToStop = distanceToStop;
	}

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}

	public String getRouteDirection() {
		return routeDirection;
	}

	public void setRouteDirection(String routeDirection) {
		this.routeDirection = routeDirection;
	}

	public String getBusDestination() {
		return busDestination;
	}

	public void setBusDestination(String busDestination) {
		this.busDestination = busDestination;
	}

	public Date getPredictionTime() {
		return predictionTime;
	}

	public void setPredictionTime(Date predictionTime) {
		this.predictionTime = predictionTime;
	}

	public Boolean getIsDly() {
		return isDly;
	}

	public void setIsDly(Boolean isDly) {
		this.isDly = isDly;
	}

	public String getTimeLeft() {
		long time = predictionTime.getTime() - timeStamp.getTime();
		return String.format(Locale.ENGLISH, "%d min", TimeUnit.MILLISECONDS.toMinutes(time));
	}

}
