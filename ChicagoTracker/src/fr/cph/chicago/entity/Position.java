package fr.cph.chicago.entity;

public class Position {

	private Double latitude;
	private Double longitude;

	public Position() {
	}

	public Position(final Double latitude, final Double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	@Override
	public String toString() {
		return "[lattitude=" + latitude + ";longitude=" + longitude + "]";
	}

}
