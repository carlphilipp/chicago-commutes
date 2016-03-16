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

import java.io.Serializable;

import lombok.Data;

/**
 * The position
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
@Data
public class Position implements Parcelable, Serializable {
	/**
	 * Serializable
	 **/
	private static final long serialVersionUID = 0L;
	/**
	 * The latitude
	 **/
	private double latitude;
	/**
	 * The longitude
	 **/
	private double longitude;

	/**
	 * Public constructor
	 */
	public Position() {
	}

	/**
	 * Public constructor
	 *
	 * @param latitude  the latitude
	 * @param longitude the longitude
	 */
	public Position(final double latitude, final double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	private Position(final Parcel in) {
		readFromParcel(in);
	}

	@Override
	public final String toString() {
		return "[latitude=" + latitude + ";longitude=" + longitude + "]";
	}

	@Override
	public final int describeContents() {
		return 0;
	}

	@Override
	public final void writeToParcel(final Parcel dest, final int flags) {
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
	}

	private void readFromParcel(final Parcel in) {
		latitude = in.readDouble();
		longitude = in.readDouble();
	}

	public static final Parcelable.Creator<Position> CREATOR = new Parcelable.Creator<Position>() {
		public Position createFromParcel(Parcel in) {
			return new Position(in);
		}

		public Position[] newArray(int size) {
			return new Position[size];
		}
	};
}
