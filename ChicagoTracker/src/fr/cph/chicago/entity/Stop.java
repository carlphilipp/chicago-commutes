/**
 * Copyright 2016 Carl-Philipp Harmant
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
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;

/**
 * Station entity
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class Stop implements Comparable<Stop>, Parcelable {
	/** The id **/
	private Integer id;
	/** The description **/
	private String description;
	/** The train direction **/
	private TrainDirection direction;
	/** The position **/
	private Position position;
	/** Ada **/
	private Boolean ada;
	/** The list of train line **/
	private List<TrainLine> lines;

	/**
	 * 
	 */
	public Stop() {

	}

	/**
	 * 
	 * @param in
	 */
	private Stop(Parcel in) {
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
	public final String getDescription() {
		return description;
	}

	/**
	 * 
	 * @param description
	 */
	public final void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * 
	 * @return
	 */
	public final TrainDirection getDirection() {
		return direction;
	}

	/**
	 * 
	 * @param direction
	 */
	public final void setDirection(final TrainDirection direction) {
		this.direction = direction;
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

	/**
	 * 
	 * @return
	 */
	public final List<TrainLine> getLines() {
		return lines;
	}

	/**
	 * 
	 * @param lines
	 */
	public final void setLines(final List<TrainLine> lines) {
		this.lines = lines;
	}

	/**
	 * 
	 * @return
	 */
	public final Boolean getAda() {
		return ada;
	}

	/**
	 * 
	 * @param ada
	 */
	public final void setAda(final Boolean ada) {
		this.ada = ada;
	}

	@Override
	public final String toString() {
		StringBuilder stb = new StringBuilder();
		stb.append("[Id=" + id);
		if (description != null) {
			stb.append(";description=" + description);
		}
		if (direction != null) {
			stb.append(";direction=" + direction);
		}
		if (position != null) {
			stb.append(";position=" + position);
		}
		if (ada != null) {
			stb.append(";ada=" + ada);
		}
		if (lines != null) {
			stb.append(";lines=" + lines);
		}
		stb.append("]");
		return stb.toString();
	}

	@Override
	public final int compareTo(final Stop anotherStop) {
		return this.direction.compareTo(anotherStop.getDirection());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(description);
		dest.writeString(direction.toTextString());
		dest.writeParcelable(position, PARCELABLE_WRITE_RETURN_VALUE);
		dest.writeString(ada.toString());
		List<String> linesString = new ArrayList<String>();
		for (TrainLine line : lines) {
			linesString.add(line.toTextString());
		}
		dest.writeStringList(linesString);
	}

	private void readFromParcel(Parcel in) {
		id = in.readInt();
		description = in.readString();
		direction = TrainDirection.fromString(in.readString());
		position = in.readParcelable(Position.class.getClassLoader());
		ada = Boolean.valueOf(in.readString());
		List<String> linesString = new ArrayList<String>();
		in.readStringList(linesString);
		lines = new ArrayList<TrainLine>();
		for (String line : linesString) {
			lines.add(TrainLine.fromXmlString(line));
		}
	}

	public static final Parcelable.Creator<Stop> CREATOR = new Parcelable.Creator<Stop>() {
		public Stop createFromParcel(Parcel in) {
			return new Stop(in);
		}

		public Stop[] newArray(int size) {
			return new Stop[size];
		}
	};
}
