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

import java.util.List;

import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;

/**
 * 
 * @author carl
 * 
 */
public class Stop implements Comparable<Stop> {
	/** **/
	private Integer id;
	/** **/
	private String description;
	/** **/
	private TrainDirection direction;
	/** **/
	private Position position;
	/** **/
	private Boolean ada;
	/** **/
	private List<TrainLine> lines;

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
}
