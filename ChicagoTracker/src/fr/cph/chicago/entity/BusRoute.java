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

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Bus route entity
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class BusRoute implements Parcelable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 0L;
	/** The id **/
	private String id;
	/** The name **/
	private String name;

	/**
	 * 
	 */
	public BusRoute(){
		
	}
	/**
	 * 
	 * @param in
	 */
	private BusRoute(Parcel in) {
		readFromParcel(in);
	}
	
	/**
	 * 
	 * @return
	 */
	public final String getId() {
		return id;
	}

	/**
	 * 
	 * @param id
	 */
	public final void setId(final String id) {
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
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public final void writeToParcel(final Parcel dest, final int flags) {
		dest.writeString(id);
		dest.writeString(name);
	}

	private void readFromParcel(final Parcel in) {
		id = in.readString();
		name = in.readString();
	}

	public static final Parcelable.Creator<BusRoute> CREATOR = new Parcelable.Creator<BusRoute>() {
		public BusRoute createFromParcel(Parcel in) {
			return new BusRoute(in);
		}

		public BusRoute[] newArray(int size) {
			return new BusRoute[size];
		}
	};
}
