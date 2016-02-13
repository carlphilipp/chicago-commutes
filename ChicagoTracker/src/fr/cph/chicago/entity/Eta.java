/**
 * Copyright 2016 Carl-Philipp Harmant
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

import android.os.Parcel;
import android.os.Parcelable;
import fr.cph.chicago.entity.enumeration.TrainLine;

/**
 * Eta entity
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class Eta implements Comparable<Eta>, Parcelable {
	/** The station **/
	private Station station;
	/** The stop **/
	private Stop stop;
	/** The bus number **/
	private Integer runNumber;
	/** The route name **/
	private TrainLine routeName;
	/** The destination station **/
	private Integer destSt;
	/** The destination name **/
	private String destName;
	/** The train route direction code **/
	private Integer trainRouteDirectionCode;
	/** The prediction date **/
	private Date predictionDate;
	/** The arrival departure date **/
	private Date arrivalDepartureDate;
	/** Is approaching **/
	private Boolean isApp;
	/** Is scheduled **/
	private Boolean isSch;
	/** **/
	private Boolean isFlt;
	/** Is delayed **/
	private Boolean isDly;
	/** Is flag **/
	private String flags;
	/** The position **/
	private Position position;
	/** Heading **/
	private Integer heading;

	/**
	 * 
	 */
	public Eta() {

	}

	/**
	 * 
	 * @param in
	 */
	private Eta(Parcel in) {
		readFromParcel(in);
	}

	/**
	 * 
	 * @return
	 */
	public final Station getStation() {
		return station;
	}

	/**
	 * 
	 * @param station
	 */
	public final void setStation(final Station station) {
		this.station = station;
	}

	/**
	 * 
	 * @return
	 */
	public final Stop getStop() {
		return stop;
	}

	/**
	 * 
	 * @param stop
	 */
	public final void setStop(final Stop stop) {
		this.stop = stop;
	}

	/**
	 * 
	 * @return
	 */
	public final Integer getRunNumber() {
		return runNumber;
	}

	/**
	 * 
	 * @param runNumber
	 */
	public final void setRunNumber(final Integer runNumber) {
		this.runNumber = runNumber;
	}

	/**
	 * 
	 * @return
	 */
	public final TrainLine getRouteName() {
		return routeName;
	}

	/**
	 * 
	 * @param routeName
	 */
	public final void setRouteName(final TrainLine routeName) {
		this.routeName = routeName;
	}

	/**
	 * 
	 * @return
	 */
	public final Integer getTrainRouteDirectionCode() {
		return trainRouteDirectionCode;
	}

	/**
	 * 
	 * @param trainRouteDirectionCode
	 */
	public final void setTrainRouteDirectionCode(final Integer trainRouteDirectionCode) {
		this.trainRouteDirectionCode = trainRouteDirectionCode;
	}

	/**
	 * 
	 * @return
	 */
	public final Date getPredictionDate() {
		return predictionDate;
	}

	/**
	 * 
	 * @param predictionDate
	 */
	public final void setPredictionDate(final Date predictionDate) {
		this.predictionDate = predictionDate;
	}

	/**
	 * 
	 * @return
	 */
	public final Date getArrivalDepartureDate() {
		return arrivalDepartureDate;
	}

	/**
	 * 
	 * @param arrivalDepartureDate
	 */
	public final void setArrivalDepartureDate(final Date arrivalDepartureDate) {
		this.arrivalDepartureDate = arrivalDepartureDate;
	}

	/**
	 * 
	 * @return
	 */
	public final Boolean getIsApp() {
		return isApp;
	}

	/**
	 * 
	 * @param isApp
	 */
	public final void setIsApp(final Boolean isApp) {
		this.isApp = isApp;
	}

	/**
	 * 
	 * @return
	 */
	public final Boolean getIsSch() {
		return isSch;
	}

	/**
	 * 
	 * @param isSch
	 */
	public final void setIsSch(final Boolean isSch) {
		this.isSch = isSch;
	}

	/**
	 * 
	 * @return
	 */
	public final Boolean getIsFlt() {
		return isFlt;
	}

	/**
	 * 
	 * @param isFlt
	 */
	public final void setIsFlt(final Boolean isFlt) {
		this.isFlt = isFlt;
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
	public final String getFlags() {
		return flags;
	}

	/**
	 * 
	 * @param flags
	 */
	public final void setFlags(final String flags) {
		this.flags = flags;
	}

	/**
	 * 
	 * @return
	 */
	public final Position getPosition() {
		return position;
	}

	/**
	 * 
	 * @param position
	 */
	public final void setPosition(final Position position) {
		this.position = position;
	}

	/**
	 * 
	 * @return
	 */
	public final Integer getHeading() {
		return heading;
	}

	/**
	 * 
	 * @param heading
	 */
	public final void setHeading(final Integer heading) {
		this.heading = heading;
	}

	/**
	 * 
	 * @return
	 */
	public final String getTimeLeft() {
		long time = arrivalDepartureDate.getTime() - predictionDate.getTime();
		return String.format(Locale.ENGLISH, "%d min", TimeUnit.MILLISECONDS.toMinutes(time));
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
			if (getIsApp()) {
				result = "Due";
			} else {
				result = getTimeLeft();
			}
		}
		return result;
	}

	/**
	 * 
	 * @return
	 */
	public final Integer getDestSt() {
		return destSt;
	}

	/**
	 * 
	 * @param destSt
	 */
	public final void setDestSt(final Integer destSt) {
		this.destSt = destSt;
	}

	/**
	 * 
	 * @return
	 */
	public final String getDestName() {
		return destName;
	}

	/**
	 * 
	 * @param destName
	 */
	public final void setDestName(final String destName) {
		this.destName = destName;
	}

	@Override
	public final int compareTo(final Eta another) {
		Long time1 = arrivalDepartureDate.getTime() - predictionDate.getTime();
		Long time2 = another.getArrivalDepartureDate().getTime() - another.getPredictionDate().getTime();
		return time1.compareTo(time2);
	}

	@Override
	public final String toString() {
		StringBuilder res = new StringBuilder();
		res.append("[Eta: [Station " + station.getName() + "]");
		res.append("[Stop " + stop.getId() + "]");
		res.append("[runNumber " + runNumber + "]");
		res.append("[routeName " + routeName + "]");
		res.append("[destSt " + destSt + "]");
		res.append("[destName " + destName + "]");
		res.append("[trainRouteDirectionCode " + trainRouteDirectionCode + "]");
		res.append("[predictionDate " + predictionDate + "]");
		res.append("[arrivalDepartureDate " + arrivalDepartureDate + "]");
		// res.append("[isApp " + isApp + "]");
		// res.append("[isFlt " + isFlt + "]");
		// res.append("[isDly " + isDly + "]");
		// res.append("[flags " + flags + "]");
		// res.append("[position " + position + "]");
		// res.append("[heading " + heading + "]");
		res.append("]");
		return res.toString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(station, flags);
		dest.writeParcelable(stop, flags);
		dest.writeInt(runNumber);
		dest.writeString(routeName.toTextString());
		dest.writeInt(destSt);
		dest.writeString(destName);
		dest.writeInt(trainRouteDirectionCode);
		dest.writeLong(predictionDate.getTime());
		dest.writeLong(arrivalDepartureDate.getTime());
		dest.writeString(isApp.toString());
		dest.writeString(isSch.toString());
		dest.writeString(isFlt.toString());
		dest.writeString(isDly.toString());
		dest.writeString(this.flags);
		dest.writeParcelable(position, flags);
		if (heading != null) {
			dest.writeInt(heading);
		} else {
			dest.writeInt(0);
		}
	}

	private void readFromParcel(Parcel in) {
		station = in.readParcelable(Station.class.getClassLoader());
		stop = in.readParcelable(Stop.class.getClassLoader());
		runNumber = in.readInt();
		routeName = TrainLine.fromXmlString(in.readString());
		destSt = in.readInt();
		destName = in.readString();
		trainRouteDirectionCode = in.readInt();
		predictionDate = new Date(in.readLong());
		arrivalDepartureDate = new Date(in.readLong());
		isApp = Boolean.valueOf(in.readString());
		isSch = Boolean.valueOf(in.readString());
		isFlt = Boolean.valueOf(in.readString());
		isDly = Boolean.valueOf(in.readString());
		this.flags = in.readString();
		position = in.readParcelable(Position.class.getClassLoader());
		heading = in.readInt();
	}

	public static final Parcelable.Creator<Eta> CREATOR = new Parcelable.Creator<Eta>() {
		public Eta createFromParcel(Parcel in) {
			return new Eta(in);
		}

		public Eta[] newArray(int size) {
			return new Eta[size];
		}
	};
}
