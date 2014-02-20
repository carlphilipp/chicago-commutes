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

/**
 * 
 * @author carl
 * 
 */
public final class BusStop implements Comparable<BusStop> {
	/** **/
	private Integer id;
	/** **/
	private String name;
	/** **/
	private Position position;

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
	public int compareTo(BusStop another) {
		Position position = another.getPosition();
		int latitude = getPosition().getLatitude().compareTo(position.getLatitude());
		return latitude == 0 ? getPosition().getLongitude().compareTo(position.getLongitude()) : latitude;
	}

}
