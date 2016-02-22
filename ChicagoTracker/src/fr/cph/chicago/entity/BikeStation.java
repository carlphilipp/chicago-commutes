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

import java.util.ArrayList;
import java.util.List;

/**
 * Bike station entity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class BikeStation implements Parcelable {
	/** Station id **/
	private int id;
	/** Station name **/
	private String name;
	/** Available docks **/
	private Integer availableDocks;
	/** Total docks **/
	private Integer totalDocks;
	/** The position **/
	private Position position;
	/** Status value **/
	private String statusValue;
	/** Status key **/
	private String statusKey;
	/** Available bikes **/
	private Integer availableBikes;
	/** Street address 1 **/
	private String stAddress1;
	/** Street address 2 **/
	private String stAddress2;
	/** City **/
	private String city;
	/** Postal code **/
	private String postalCode;
	/** Location **/
	private String location;
	/** Altitude **/
	private String altitude;
	/** Test station **/
	private boolean testStation;
	/** Last communication time **/
	private String lastCommunicationTime;
	/** Land mark **/
	private int landMark;

	public BikeStation() {

	}

	/**
	 *
	 * @param in
	 */
	private BikeStation(Parcel in) {
		readFromParcel(in);
	}

	public final int getId() {
		return id;
	}

	public final void setId(final int id) {
		this.id = id;
	}

	public final String getName() {
		return name;
	}

	public final void setName(final String name) {
		this.name = name;
	}

	public final Integer getAvailableDocks() {
		return availableDocks;
	}

	public final void setAvailableDocks(final Integer availableDocks) {
		this.availableDocks = availableDocks;
	}

	public final Integer getTotalDocks() {
		return totalDocks;
	}

	public final void setTotalDocks(final Integer totalDocks) {
		this.totalDocks = totalDocks;
	}

	public final Position getPosition() {
		return position;
	}

	public final void setPosition(final Position position) {
		this.position = position;
	}

	public final String getStatusValue() {
		return statusValue;
	}

	public final void setStatusValue(final String statusValue) {
		this.statusValue = statusValue;
	}

	public final String getStatusKey() {
		return statusKey;
	}

	public final void setStatusKey(final String statusKey) {
		this.statusKey = statusKey;
	}

	public final Integer getAvailableBikes() {
		return availableBikes;
	}

	public final void setAvailableBikes(final Integer availableBikes) {
		this.availableBikes = availableBikes;
	}

	public final String getStAddress1() {
		return stAddress1;
	}

	public final void setStAddress1(final String stAddress1) {
		this.stAddress1 = stAddress1;
	}

	public final String getStAddress2() {
		return stAddress2;
	}

	public final void setStAddress2(final String stAddress2) {
		this.stAddress2 = stAddress2;
	}

	public final String getCity() {
		return city;
	}

	public final void setCity(final String city) {
		this.city = city;
	}

	public final String getPostalCode() {
		return postalCode;
	}

	public final void setPostalCode(final String postalCode) {
		this.postalCode = postalCode;
	}

	public final String getLocation() {
		return location;
	}

	public final void setLocation(final String location) {
		this.location = location;
	}

	public final String getAltitude() {
		return altitude;
	}

	public final void setAltitude(final String altitude) {
		this.altitude = altitude;
	}

	public final boolean isTestStation() {
		return testStation;
	}

	public final void setTestStation(final boolean testStation) {
		this.testStation = testStation;
	}

	public final String getLastCommunicationTime() {
		return lastCommunicationTime;
	}

	public final void setLastCommunicationTime(final String lastCommunicationTime) {
		this.lastCommunicationTime = lastCommunicationTime;
	}

	public final int getLandMark() {
		return landMark;
	}

	public final void setLandMark(final int landMark) {
		this.landMark = landMark;
	}

	@Override
	public final String toString() {
		return "[" + id + " " + name + " " + availableBikes + "/" + totalDocks + "]";
	}

	@Override
	public final int describeContents() {
		return 0;
	}

	@Override
	public final void writeToParcel(final Parcel dest, final int flags) {
		dest.writeInt(id);
		dest.writeString(name);
		dest.writeInt(availableDocks);
		dest.writeInt(totalDocks);
		dest.writeParcelable(position, PARCELABLE_WRITE_RETURN_VALUE);
		dest.writeString(statusValue);
		dest.writeString(statusKey);
		dest.writeInt(availableBikes);
		dest.writeString(stAddress1);
		dest.writeString(stAddress2);
		dest.writeString(city);
		dest.writeString(postalCode);
		dest.writeString(location);
		dest.writeString(altitude);
		dest.writeString(String.valueOf(testStation));
		dest.writeString(lastCommunicationTime);
		dest.writeInt(landMark);
	}

	private void readFromParcel(final Parcel in) {
		id = in.readInt();
		name = in.readString();
		availableDocks = in.readInt();
		totalDocks = in.readInt();
		position = in.readParcelable(Position.class.getClassLoader());
		statusValue = in.readString();
		statusKey = in.readString();
		availableBikes = in.readInt();
		stAddress1 = in.readString();
		stAddress2 = in.readString();
		city = in.readString();
		postalCode = in.readString();
		location = in.readString();
		altitude = in.readString();
		testStation = Boolean.valueOf(in.readString());
		lastCommunicationTime = in.readString();
		landMark = in.readInt();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((altitude == null) ? 0 : altitude.hashCode());
		result = prime * result + availableBikes;
		result = prime * result + availableDocks;
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + id;
		result = prime * result + landMark;
		result = prime * result + ((lastCommunicationTime == null) ? 0 : lastCommunicationTime.hashCode());
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		result = prime * result + ((postalCode == null) ? 0 : postalCode.hashCode());
		result = prime * result + ((stAddress1 == null) ? 0 : stAddress1.hashCode());
		result = prime * result + ((stAddress2 == null) ? 0 : stAddress2.hashCode());
		result = prime * result + ((statusKey == null) ? 0 : statusKey.hashCode());
		result = prime * result + ((statusValue == null) ? 0 : statusValue.hashCode());
		result = prime * result + (testStation ? 1231 : 1237);
		result = prime * result + totalDocks;
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
		BikeStation other = (BikeStation) obj;
		return id == other.id;
	}

	public static final Parcelable.Creator<BikeStation> CREATOR = new Parcelable.Creator<BikeStation>() {
		public BikeStation createFromParcel(Parcel in) {
			return new BikeStation(in);
		}

		public BikeStation[] newArray(int size) {
			return new BikeStation[size];
		}
	};

	public static List<BikeStation> readNearbyStation(final List<BikeStation> bikeStations, final Position position) {
		final double dist = 0.004472;
		final double latitude = position.getLatitude();
		final double longitude = position.getLongitude();

		final double latMax = latitude + dist;
		final double latMin = latitude - dist;
		final double lonMax = longitude + dist;
		final double lonMin = longitude - dist;

		final List<BikeStation> bikeStationsRes = new ArrayList<>();
		for (final BikeStation station : bikeStations) {
			final double trainLatitude = station.getPosition().getLatitude();
			final double trainLongitude = station.getPosition().getLongitude();
			if (trainLatitude <= latMax && trainLatitude >= latMin && trainLongitude <= lonMax && trainLongitude >= lonMin) {
				bikeStationsRes.add(station);
			}
		}
		return bikeStationsRes;
	}
}
