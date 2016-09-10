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
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import fr.cph.chicago.entity.enumeration.TrainLine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Eta entity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
@AllArgsConstructor
@Builder
@Data
public final class Eta implements Comparable<Eta>, Parcelable {
	/**
	 * The station
	 **/
	private Station station;
	/**
	 * The stop
	 **/
	private Stop stop;
	/**
	 * The bus number
	 **/
	private int runNumber;
	/**
	 * The route name
	 **/
	private TrainLine routeName;
	/**
	 * The destination station
	 **/
	private int destSt;
	/**
	 * The destination name
	 **/
	private String destName;
	/**
	 * The train route direction code
	 **/
	private int trainRouteDirectionCode;
	/**
	 * The prediction date
	 **/
	private Date predictionDate;
	/**
	 * The arrival departure date
	 **/
	private Date arrivalDepartureDate;
	/**
	 * Is approaching
	 **/
	private boolean isApp;
	/**
	 * Is scheduled
	 **/
	private boolean isSch;
	/** **/
	private boolean isFlt;
	/**
	 * Is delayed
	 **/
	private boolean isDly;
	/**
	 * Is flag
	 **/
	private String flags;
	/**
	 * The position
	 **/
	private Position position;
	/**
	 * Heading
	 **/
	private int heading;

	private Eta(@NonNull final Parcel in) {
		readFromParcel(in);
	}

    @NonNull
	private String getTimeLeft() {
		long time = arrivalDepartureDate.getTime() - predictionDate.getTime();
		return String.format(Locale.ENGLISH, "%d min", TimeUnit.MILLISECONDS.toMinutes(time));
	}

    @NonNull
	public final String getTimeLeftDueDelay() {
		String result;
		if (isDly()) {
			result = "Delay";
		} else {
			if (isApp()) {
				result = "Due";
			} else {
				result = getTimeLeft();
			}
		}
		return result;
	}

	public final int getDestSt() {
		return destSt;
	}

	public final void setDestSt(final int destSt) {
		this.destSt = destSt;
	}

    @NonNull
	public final String getDestName() {
		return destName;
	}

	public final void setDestName(@NonNull final String destName) {
		this.destName = destName;
	}

	@Override
	public final int compareTo(@NonNull final Eta another) {
		Long time1 = arrivalDepartureDate.getTime() - predictionDate.getTime();
		Long time2 = another.getArrivalDepartureDate().getTime() - another.getPredictionDate().getTime();
		return time1.compareTo(time2);
	}

	@Override
	public final String toString() {
		StringBuilder res = new StringBuilder();
		res.append("[Eta: [Station ").append(station.getName()).append("]");
		res.append("[Stop ").append(stop.getId()).append("]");
		res.append("[runNumber ").append(runNumber).append("]");
		res.append("[routeName ").append(routeName).append("]");
		res.append("[destSt ").append(destSt).append("]");
		res.append("[destName ").append(destName).append("]");
		res.append("[trainRouteDirectionCode ").append(trainRouteDirectionCode).append("]");
		res.append("[predictionDate ").append(predictionDate).append("]");
		res.append("[arrivalDepartureDate ").append(arrivalDepartureDate).append("]");
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
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeParcelable(station, flags);
		dest.writeParcelable(stop, flags);
		dest.writeInt(runNumber);
		dest.writeString(routeName.toTextString());
		dest.writeInt(destSt);
		dest.writeString(destName);
		dest.writeInt(trainRouteDirectionCode);
		dest.writeLong(predictionDate.getTime());
		dest.writeLong(arrivalDepartureDate.getTime());
		dest.writeString(String.valueOf(isApp));
		dest.writeString(String.valueOf(isSch));
		dest.writeString(String.valueOf(isFlt));
		dest.writeString(String.valueOf(isDly));
		dest.writeString(this.flags);
		dest.writeParcelable(position, flags);
		dest.writeInt(heading);
	}

	private void readFromParcel(@NonNull final Parcel in) {
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
		public Eta createFromParcel(final Parcel in) {
			return new Eta(in);
		}

		public Eta[] newArray(final int size) {
			return new Eta[size];
		}
	};
}
