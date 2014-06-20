package fr.cph.chicago.entity;

import android.os.Parcel;
import android.os.Parcelable;

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
	private int availableDocks;
	/** Total docks **/
	private int totalDocks;
	/** The position **/
	private Position position;
	/** Status value **/
	private String statusValue;
	/** Status key **/
	private String statusKey;
	/** Available bikes **/
	private int availableBikes;
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

	public static final Parcelable.Creator<BikeStation> CREATOR = new Parcelable.Creator<BikeStation>() {
		public BikeStation createFromParcel(Parcel in) {
			return new BikeStation(in);
		}

		public BikeStation[] newArray(int size) {
			return new BikeStation[size];
		}
	};
}
