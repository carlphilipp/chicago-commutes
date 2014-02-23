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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import fr.cph.chicago.entity.enumeration.TrainLine;

/**
 * Station entity
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class Station implements Comparable<Station> {
	/** The id **/
	private Integer id;
	/** The name **/
	private String name;
	/** The stops list **/
	private List<Stop> stops;

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
	public final List<Stop> getStops() {
		return stops;
	}

	/**
	 * 
	 * @param stops
	 */
	public final void setStops(final List<Stop> stops) {
		this.stops = stops;
	}

	@Override
	public final String toString() {
		StringBuilder stb = new StringBuilder();
		stb.append("[Id=" + id);
		stb.append(";name=" + name);
		if (stops != null) {
			stb.append(";stops=" + stops);
		}
		if (getLines() != null) {
			stb.append(";lines=" + getLines());
		}
		stb.append("]");
		return stb.toString();
	}

	/**
	 * 
	 * @return
	 */
	public final Set<TrainLine> getLines() {
		if (stops != null) {
			Set<TrainLine> lines = new TreeSet<TrainLine>();
			for (Stop stop : stops) {
				for (TrainLine tl : stop.getLines()) {
					lines.add(tl);
				}
			}
			return lines;
		} else {
			return null;
		}
	}

	public final Map<TrainLine, List<Stop>> getStopByLines() {
		Map<TrainLine, List<Stop>> map = new TreeMap<TrainLine, List<Stop>>();
		List<Stop> stops = getStops();
		for (Stop stop : stops) {
			List<TrainLine> lines = stop.getLines();
			for (TrainLine tl : lines) {
				List<Stop> stopss;
				if (map.containsKey(tl)) {
					stopss = map.get(tl);
					stopss.add(stop);
				} else {
					stopss = new ArrayList<Stop>();
					stopss.add(stop);
					map.put(tl, stopss);
				}

			}
		}
		return map;
	}

	@Override
	public final int compareTo(final Station another) {
		return this.getName().compareTo(another.getName());
	}

	public List<Position> getStopsPosition() {
		List<Position> positions = new ArrayList<Position>();
		for (Stop stop : stops) {
			positions.add(stop.getPosition());
		}
		return positions;
	}

}
