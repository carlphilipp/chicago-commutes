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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import fr.cph.chicago.entity.enumeration.TrainLine;

/**
 * Train Arrival entity
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class TrainArrival implements Parcelable {
	/** The timestamp **/
	private Date timeStamp;
	/** The error code **/
	private Integer errorCode;
	/** The error message **/
	private String errorMessage;
	/** A list of Eta **/
	private List<Eta> etas;

	/**
	 * 
	 */
	public TrainArrival() {

	}

	/**
	 * 
	 * @param in
	 */
	private TrainArrival(Parcel in) {
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
	public final Integer getErrorCode() {
		return errorCode;
	}

	/**
	 * 
	 * @param errorCode
	 */
	public final void setErrorCode(final Integer errorCode) {
		this.errorCode = errorCode;
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
	public final List<Eta> getEtas() {
		return etas;
	}

	/**
	 * 
	 * @param etas
	 */
	public final void setEtas(final List<Eta> etas) {
		this.etas = etas;
	}

	/**
	 * 
	 * @param line
	 * @return
	 */
	public final List<Eta> getEtas(final TrainLine line) {
		List<Eta> etas = new ArrayList<Eta>();
		for (Eta eta : getEtas()) {
			if (eta.getRouteName() == line) {
				etas.add(eta);
			}
		}
		return etas;
	}

	@Override
	public final int describeContents() {
		return 0;
	}

	@Override
	public final void writeToParcel(final Parcel dest, final int flags) {
		dest.writeLong(timeStamp.getTime());
		dest.writeInt(errorCode);
		dest.writeString(errorMessage);
		dest.writeList(etas);
	}

	private void readFromParcel(final Parcel in) {
		timeStamp = new Date(in.readLong());
		errorCode = in.readInt();
		errorMessage = in.readString();
		etas = new ArrayList<Eta>();
		in.readList(etas, Eta.class.getClassLoader());
	}

	public static final Parcelable.Creator<TrainArrival> CREATOR = new Parcelable.Creator<TrainArrival>() {
		public TrainArrival createFromParcel(Parcel in) {
			return new TrainArrival(in);
		}

		public TrainArrival[] newArray(int size) {
			return new TrainArrival[size];
		}
	};
}
