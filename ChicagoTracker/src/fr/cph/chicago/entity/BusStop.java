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

import java.io.Serializable;

/**
 * Bus stop entity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class BusStop implements Comparable<BusStop>, Parcelable, Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 0L;
	/** The id **/
	private Integer id;
	/** The name **/
	private String name;
	/** The position **/
	private Position position;

	/**
	 *
	 */
	public BusStop() {

	}

	/**
	 *
	 * @param in
	 */
	private BusStop(Parcel in) {
		readFromParcel(in);
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
	public final String getName() {
		return name;
	}

	/**
	 *
	 * @param name
	 */
	public final void setName(final String name) {
		this.name = name;
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

	@Override
	public final String toString() {
		return "[id:" + getId() + ";name:" + getName() + ";position:" + getPosition() + "]";
	}

	@Override
	public int compareTo(@NonNull final BusStop another) {
		Position position = another.getPosition();
		int latitude = getPosition().getLatitude().compareTo(position.getLatitude());
		return latitude == 0 ? getPosition().getLongitude().compareTo(position.getLongitude()) : latitude;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public final void writeToParcel(final Parcel dest, final int flags) {
		dest.writeInt(id);
		dest.writeString(name);
		dest.writeParcelable(position, flags);
	}

	private void readFromParcel(final Parcel in) {
		id = in.readInt();
		name = in.readString();
		position = in.readParcelable(Position.class.getClassLoader());
	}

	public static final Parcelable.Creator<BusStop> CREATOR = new Parcelable.Creator<BusStop>() {
		public BusStop createFromParcel(Parcel in) {
			return new BusStop(in);
		}

		public BusStop[] newArray(int size) {
			return new BusStop[size];
		}
	};
}
