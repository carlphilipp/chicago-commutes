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

package fr.cph.chicago.entity.factory;

import java.util.List;

import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.Stop;

/**
 * 
 * @author carl
 * 
 */
public class StationFactory {
	/**
	 * 
	 */
	private StationFactory() {

	}

	/**
	 * 
	 * @param id
	 * @param name
	 * @param stops
	 * @return
	 */
	public static final Station buildStation(final Integer id, final String name, final List<Stop> stops) {
		Station station = new Station();
		station.setId(id);
		station.setName(name);
		station.setStops(stops);
		return station;
	}
}
