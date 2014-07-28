package fr.cph.chicago.entity;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author carl
 * 
 */
public final class BusPattern implements Parcelable {
	/** The pattern id **/
	private Integer id;
	/** The length in feet **/
	private Double length;
	/** The direction **/
	private String direction;
	/** The list of points **/
	private List<PatternPoint> points;

	/**
	 * 
	 */
	public BusPattern() {
		this.points = new ArrayList<PatternPoint>();
	}

	/**
	 * 
	 * @param in
	 */
	private BusPattern(Parcel in) {
		readFromParcel(in);
	}

	/**
	 * @return
	 */
	public final Integer getId() {
		return id;
	}

	/**
	 * @param id
	 */
	public final void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return
	 */
	public final Double getLength() {
		return length;
	}

	/**
	 * @param length
	 */
	public final void setLength(Double length) {
		this.length = length;
	}

	/**
	 * @return
	 */
	public final String getDirection() {
		return direction;
	}

	/**
	 * @param direction
	 */
	public final void setDirection(String direction) {
		this.direction = direction;
	}

	/**
	 * @return
	 */
	public final List<PatternPoint> getPoints() {
		return points;
	}

	/**
	 * @param points
	 */
	public final void setPoints(List<PatternPoint> points) {
		this.points = points;
	}
	
	public final void addPoint(PatternPoint patternPoint){
		this.points.add(patternPoint);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeDouble(length);
		dest.writeString(direction);
		dest.writeList(points);
	}

	private void readFromParcel(Parcel in) {
		id = in.readInt();
		length = in.readDouble();
		direction = in.readString();
		in.readList(points, PatternPoint.class.getClassLoader());
	}

	public static final Parcelable.Creator<BusPattern> CREATOR = new Parcelable.Creator<BusPattern>() {
		public BusPattern createFromParcel(Parcel in) {
			return new BusPattern(in);
		}

		public BusPattern[] newArray(int size) {
			return new BusPattern[size];
		}
	};

}
