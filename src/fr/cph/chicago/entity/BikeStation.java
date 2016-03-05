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
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Bike station entity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class BikeStation implements Parcelable {
	/**
	 * Station id
	 **/
	@JsonProperty("id")
	private int id;
	/**
	 * Station name
	 **/
	@JsonProperty("stationName")
	private String name;
	/**
	 * Available docks
	 **/
	@JsonProperty("availableDocks")
	private int availableDocks;
	/**
	 * Total docks
	 **/
	@JsonProperty("totalDocks")
	private int totalDocks;
	/**
	 * The latitude
	 **/
	@JsonProperty("latitude")
	private double latitude;
	/**
	 * The longitude
	 **/
	@JsonProperty("longitude")
	private double longitude;
	/**
	 * Status value
	 **/
	@JsonProperty("statusValue")
	private String statusValue;
	/**
	 * Status key
	 **/
	@JsonProperty("statusKey")
	private String statusKey;
	/**
	 * Available bikes
	 **/
	@JsonProperty("availableBikes")
	private int availableBikes;
	/**
	 * Street address 1
	 **/
	@JsonProperty("stAddress1")
	private String stAddress1;
	/**
	 * Street address 2
	 **/
	@JsonProperty("stAddress2")
	private String stAddress2;
	/**
	 * City
	 **/
	@JsonProperty("city")
	private String city;
	/**
	 * Postal code
	 **/
	@JsonProperty("postalCode")
	private String postalCode;
	/**
	 * Location
	 **/
	@JsonProperty("location")
	private String location;
	/**
	 * Altitude
	 **/
	@JsonProperty("altitude")
	private String altitude;
	/**
	 * Test station
	 **/
	@JsonProperty("testStation")
	private boolean testStation;
	/**
	 * Last communication time
	 **/
	@JsonProperty("lastCommunicationTime")
	private String lastCommunicationTime;
	/**
	 * Land mark
	 **/
	@JsonProperty("landMark")
	private int landMark;

	public BikeStation() {

	}

	/**
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

	public final int getAvailableDocks() {
		return availableDocks;
	}

	public final void setAvailableDocks(final int availableDocks) {
		this.availableDocks = availableDocks;
	}

	public final int getTotalDocks() {
		return totalDocks;
	}

	public final void setTotalDocks(final int totalDocks) {
		this.totalDocks = totalDocks;
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

	public final int getAvailableBikes() {
		return availableBikes;
	}

	public final void setAvailableBikes(final int availableBikes) {
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
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
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
		latitude = in.readDouble();
		longitude = in.readDouble();
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

	@Override
	public int hashCode() {
		int result;
		long temp;
		result = id;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + availableDocks;
		result = 31 * result + totalDocks;
		temp = Double.doubleToLongBits(latitude);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(longitude);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		result = 31 * result + (statusValue != null ? statusValue.hashCode() : 0);
		result = 31 * result + (statusKey != null ? statusKey.hashCode() : 0);
		result = 31 * result + availableBikes;
		result = 31 * result + (stAddress1 != null ? stAddress1.hashCode() : 0);
		result = 31 * result + (stAddress2 != null ? stAddress2.hashCode() : 0);
		result = 31 * result + (city != null ? city.hashCode() : 0);
		result = 31 * result + (postalCode != null ? postalCode.hashCode() : 0);
		result = 31 * result + (location != null ? location.hashCode() : 0);
		result = 31 * result + (altitude != null ? altitude.hashCode() : 0);
		result = 31 * result + (testStation ? 1 : 0);
		result = 31 * result + (lastCommunicationTime != null ? lastCommunicationTime.hashCode() : 0);
		result = 31 * result + landMark;
		return result;
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
			final double bikeLatitude = station.getLatitude();
			final double bikeLongitude = station.getLongitude();
			if (bikeLatitude <= latMax && bikeLatitude >= latMin && bikeLongitude <= lonMax && bikeLongitude >= lonMin) {
				bikeStationsRes.add(station);
			}
		}
		return bikeStationsRes;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
}
