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

import android.os.Parcel;
import android.os.Parcelable;
import fr.cph.chicago.entity.enumeration.PredictionType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Bus Arrival entity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class BusArrival implements Parcelable {
	/** **/
	public static String NO_SERVICE = "No service scheduled";
	/** Timestamp **/
	private Date timeStamp;
	/** Error message **/
	private String errorMessage;
	/** TYpe of prediction **/
	private PredictionType predictionType;
	/** Stop name **/
	private String stopName;
	/** Stop id **/
	private Integer stopId;
	/** Bus id **/
	private Integer busId;
	/** Distance to stop **/
	private Integer distanceToStop; // feets
	/** Route id **/
	private String routeId;
	/** Route direction **/
	private String routeDirection;
	/** Bus destination **/
	private String busDestination;
	/** Prediction time **/
	private Date predictionTime;
	/** Is delayed **/
	private Boolean isDly = false;

	/**
	 *
	 */
	public BusArrival() {

	}

	/**
	 *
	 * @param in
	 */
	private BusArrival(Parcel in) {
		readFromParcel(in);
	}

	/**
	 *
	 * @return
	 */
	public final Date getTimeStamp() {
		return timeStamp;
	}

	/**
	 *
	 * @param timeStamp
	 */
	public final void setTimeStamp(final Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 *
	 * @return
	 */
	public final String getErrorMessage() {
		return errorMessage;
	}

	/**
	 *
	 * @param errorMessage
	 */
	public final void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 *
	 * @return
	 */
	public final PredictionType getPredictionType() {
		return predictionType;
	}

	/**
	 *
	 * @param predictionType
	 */
	public final void setPredictionType(final PredictionType predictionType) {
		this.predictionType = predictionType;
	}

	/**
	 *
	 * @return
	 */
	public final String getStopName() {
		return stopName;
	}

	/**
	 *
	 * @param stopName
	 */
	public final void setStopName(final String stopName) {
		this.stopName = stopName;
	}

	/**
	 *
	 * @return
	 */
	public final Integer getStopId() {
		return stopId;
	}

	/**
	 *
	 * @param stopId
	 */
	public final void setStopId(final Integer stopId) {
		this.stopId = stopId;
	}

	/**
	 *
	 * @return
	 */
	public final Integer getBusId() {
		return busId;
	}

	/**
	 *
	 * @param busId
	 */
	public final void setBusId(final Integer busId) {
		this.busId = busId;
	}

	/**
	 *
	 * @return
	 */
	public final Integer getDistanceToStop() {
		return distanceToStop;
	}

	/**
	 *
	 * @param distanceToStop
	 */
	public final void setDistanceToStop(final Integer distanceToStop) {
		this.distanceToStop = distanceToStop;
	}

	/**
	 *
	 * @return
	 */
	public final String getRouteId() {
		return routeId;
	}

	/**
	 *
	 * @param routeId
	 */
	public final void setRouteId(final String routeId) {
		this.routeId = routeId;
	}

	/**
	 *
	 * @return
	 */
	public final String getRouteDirection() {
		return routeDirection;
	}

	/**
	 *
	 * @param routeDirection
	 */
	public final void setRouteDirection(final String routeDirection) {
		this.routeDirection = routeDirection;
	}

	/**
	 *
	 * @return
	 */
	public final String getBusDestination() {
		return busDestination;
	}

	/**
	 *
	 * @param busDestination
	 */
	public final void setBusDestination(final String busDestination) {
		this.busDestination = busDestination;
	}

	/**
	 *
	 * @return
	 */
	public final Date getPredictionTime() {
		return predictionTime;
	}

	/**
	 *
	 * @param predictionTime
	 */
	public final void setPredictionTime(final Date predictionTime) {
		this.predictionTime = predictionTime;
	}

	/**
	 *
	 * @return
	 */
	public final Boolean getIsDly() {
		return isDly;
	}

	/**
	 *
	 * @param isDly
	 */
	public final void setIsDly(final Boolean isDly) {
		this.isDly = isDly;
	}

	/**
	 *
	 * @return
	 */
	public final String getTimeLeft() {
		if (predictionTime != null && timeStamp != null) {
			long time = predictionTime.getTime() - timeStamp.getTime();
			return String.format(Locale.ENGLISH, "%d min", TimeUnit.MILLISECONDS.toMinutes(time));
		} else {
			return NO_SERVICE;
		}
	}

	/**
	 *
	 * @return
	 */
	public final String getTimeLeftDueDelay() {
		String result;
		if (getIsDly()) {
			result = "Delay";
		} else {
			if (getTimeLeft().equals("0 min")) {
				result = "Due";
			} else {
				result = getTimeLeft();
			}
		}
		return result;
	}

	@Override
	public final int hashCode() {
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
	public final boolean equals(final Object obj) {
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

	@Override
	public final int describeContents() {
		return 0;
	}

	@Override
	public final void writeToParcel(final Parcel dest, final int flags) {
		dest.writeLong(timeStamp.getTime());
		dest.writeString(errorMessage);
		dest.writeString(predictionType.toString());
		dest.writeString(stopName);
		dest.writeInt(stopId);
		dest.writeInt(busId);
		dest.writeInt(distanceToStop);
		dest.writeString(routeId);
		dest.writeString(routeDirection);
		dest.writeString(busDestination);
		dest.writeLong(predictionTime.getTime());
		dest.writeString(isDly.toString());
	}

	private void readFromParcel(final Parcel in) {
		timeStamp = new Date(in.readLong());
		errorMessage = in.readString();
		predictionType = PredictionType.fromString(in.readString());
		stopName = in.readString();
		stopId = in.readInt();
		busId = in.readInt();
		distanceToStop = in.readInt();
		routeId = in.readString();
		routeDirection = in.readString();
		busDestination = in.readString();
		predictionTime = new Date(in.readLong());
		isDly = Boolean.valueOf(in.readString());
	}

	public static final Parcelable.Creator<BusArrival> CREATOR = new Parcelable.Creator<BusArrival>() {
		public BusArrival createFromParcel(Parcel in) {
			return new BusArrival(in);
		}

		public BusArrival[] newArray(int size) {
			return new BusArrival[size];
		}
	};

	public static List<BusArrival> getRealBusArrival(List<BusArrival> arrivals) {
		List<BusArrival> res = new ArrayList<>();
		for (BusArrival arrival : arrivals) {
			if (!arrival.getTimeLeft().equals(NO_SERVICE)) {
				res.add(arrival);
			}
		}
		return res;
	}
}
