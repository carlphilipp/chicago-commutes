/**
 * Copyright 2014 Carl-Philipp Harmant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
	private Boolean isDly = false;

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

	public String getTimeLeftDueDelay() {
		String result;
		if (getIsDly()) {
			result = "Delay";
		} else {
			result = getTimeLeft();
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((busDestination == null) ? 0 : busDestination.hashCode());
		result = prime * result + ((busId == null) ? 0 : busId.hashCode());
		result = prime * result + ((distanceToStop == null) ? 0 : distanceToStop.hashCode());
		result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
		result = prime * result + ((isDly == null) ? 0 : isDly.hashCode());
		result = prime * result + ((predictionTime == null) ? 0 : predictionTime.hashCode());
		result = prime * result + ((predictionType == null) ? 0 : predictionType.hashCode());
		result = prime * result + ((routeDirection == null) ? 0 : routeDirection.hashCode());
		result = prime * result + ((routeId == null) ? 0 : routeId.hashCode());
		result = prime * result + ((stopId == null) ? 0 : stopId.hashCode());
		result = prime * result + ((stopName == null) ? 0 : stopName.hashCode());
		result = prime * result + ((timeStamp == null) ? 0 : timeStamp.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BusArrival other = (BusArrival) obj;
		if (busDestination == null) {
			if (other.busDestination != null)
				return false;
		} else if (!busDestination.equals(other.busDestination))
			return false;
		if (busId == null) {
			if (other.busId != null)
				return false;
		} else if (!busId.equals(other.busId))
			return false;
		if (distanceToStop == null) {
			if (other.distanceToStop != null)
				return false;
		} else if (!distanceToStop.equals(other.distanceToStop))
			return false;
		if (errorMessage == null) {
			if (other.errorMessage != null)
				return false;
		} else if (!errorMessage.equals(other.errorMessage))
			return false;
		if (isDly == null) {
			if (other.isDly != null)
				return false;
		} else if (!isDly.equals(other.isDly))
			return false;
		if (predictionTime == null) {
			if (other.predictionTime != null)
				return false;
		} else if (!predictionTime.equals(other.predictionTime))
			return false;
		if (predictionType != other.predictionType)
			return false;
		if (routeDirection == null) {
			if (other.routeDirection != null)
				return false;
		} else if (!routeDirection.equals(other.routeDirection))
			return false;
		if (routeId == null) {
			if (other.routeId != null)
				return false;
		} else if (!routeId.equals(other.routeId))
			return false;
		if (stopId == null) {
			if (other.stopId != null)
				return false;
		} else if (!stopId.equals(other.stopId))
			return false;
		if (stopName == null) {
			if (other.stopName != null)
				return false;
		} else if (!stopName.equals(other.stopName))
			return false;
		if (timeStamp == null) {
			if (other.timeStamp != null)
				return false;
		} else if (!timeStamp.equals(other.timeStamp))
			return false;
		return true;
	}

}
