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

package fr.cph.chicago.data;

import android.util.Log;
import au.com.bytecode.opencsv.CSVReader;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.xml.Xml;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Class that handle bus data. Singleton
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BusData {
	/**
	 * Tag
	 **/
	private static final String TAG = BusData.class.getSimpleName();
	/**
	 * Singleton
	 **/
	private static BusData busData;
	/**
	 * List of bus routes
	 **/
	private List<BusRoute> busRoutes;
	/**
	 * List of bus stop
	 **/
	private List<BusStop> busStops;

	/**
	 * Private constuctor
	 */
	private BusData() {
		this.busRoutes = new ArrayList<>();
		this.busStops = new ArrayList<>();
	}

	/**
	 * Get instance of the class
	 *
	 * @return a bus data instance
	 */
	public static BusData getInstance() {
		if (busData == null) {
			busData = new BusData();
		}
		return busData;
	}

	/**
	 * Method that read bus stops from CSV
	 *
	 * @return a list of bus stops
	 */
	public final List<BusStop> readBusStops() {
		if (busStops.size() == 0) {
			try {
				final CSVReader reader = new CSVReader(new InputStreamReader(ChicagoTracker.getAppContext().getAssets().open("stops.txt")));
				reader.readNext();
				String[] row;
				while ((row = reader.readNext()) != null) {
					// int locationType = Integer.valueOf(row[6]);// location_type
					final Integer stopId = Integer.valueOf(row[0]); // stop_id
					if (stopId < 30000) {
						// String stopCode = TrainDirection.fromString(row[1]); // stop_code
						final String stopName = row[2]; // stop_name
						// String stopDesc = row[3]; // stop_desc
						final Double latitude = Double.valueOf(row[4]);// stop_lat
						final Double longitude = Double.valueOf(row[5]);// stop_lon

						final BusStop busStop = new BusStop();
						busStop.setId(stopId);
						busStop.setName(stopName);
						final Position position = new Position();
						position.setLatitude(latitude);
						position.setLongitude(longitude);
						busStop.setPosition(position);

						busStops.add(busStop);
					} else {
						break;
					}
				}
				reader.close();
				order();
			} catch (final IOException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		return busStops;
	}

	/**
	 * Load bus routes from CTA API
	 *
	 * @return a list of bus route
	 * @throws ParserException  a parser exception
	 * @throws ConnectException a connect exception
	 */
	public final List<BusRoute> loadBusRoutes() throws ParserException, ConnectException {
		if (busRoutes.size() == 0) {
			final MultiValuedMap<String, String> params = new ArrayListValuedHashMap<>();
			final CtaConnect connect = CtaConnect.getInstance();
			final Xml xml = new Xml();
			final String xmlResult = connect.connect(CtaRequestType.BUS_ROUTES, params);
			busRoutes = xml.parseBusRoutes(xmlResult);
		}
		return busRoutes;
	}

	/**
	 * Get bus routes
	 *
	 * @return a list of bus route
	 */
	public final List<BusRoute> getRoutes() {
		return busRoutes;
	}

	/**
	 * Get number of route
	 *
	 * @return a number
	 */
	public final int getRouteSize() {
		return busRoutes.size();
	}

	/**
	 * Get a route
	 *
	 * @param position the position in the list
	 * @return a bus route
	 */
	public final BusRoute getRoute(final int position) {
		return busRoutes.get(position);
	}

	/**
	 * Get a route
	 *
	 * @param routeId the id of the bus route
	 * @return a bus route
	 */
	public final BusRoute getRoute(final String routeId) {
		BusRoute result = null;
		for (final BusRoute br : busRoutes) {
			if (br.getId().equals(routeId)) {
				result = br;
				break;
			}
		}
		return result;
	}

	public final boolean containsRoute(final String routeId) {
		for (final BusRoute br : busRoutes) {
			if (br.getId().equals(routeId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Load from CTA API a bus stop list
	 *
	 * @param stopId the stop id
	 * @param bound  the direction
	 * @return a bus stop list
	 * @throws ConnectException a connect exception
	 * @throws ParserException  a parser exception
	 */
	public final List<BusStop> loadBusStop(final String stopId, final String bound) throws ConnectException, ParserException {
		final CtaConnect connect = CtaConnect.getInstance();
		final MultiValuedMap<String, String> params = new ArrayListValuedHashMap<>();
		params.put("rt", stopId);
		params.put("dir", bound);
		final String xmlResult = connect.connect(CtaRequestType.BUS_STOP_LIST, params);
		final Xml xml = new Xml();
		return xml.parseBusBounds(xmlResult);
	}

	/**
	 * Get all bus stops from CSV
	 *
	 * @return a list of bus stops
	 */
	public final List<BusStop> readAllBusStops() {
		return this.busStops;
	}

	/**
	 * Get one bus from CSV
	 *
	 * @param id the id of the bus
	 * @return a bus stop
	 */
	public final BusStop readOneBus(final int id) {
		BusStop res = null;
		for (final BusStop busStop : busStops) {
			if (busStop.getId() == id) {
				res = busStop;
				break;
			}
		}
		return res;
	}

	/**
	 * Get a list of bus stop within a a distance and position
	 *
	 * @param position the position
	 * @return a list of bus stop
	 */
	public final List<BusStop> readNearbyStops(final Position position) {

		final double dist = 0.004472;

		final List<BusStop> res = new ArrayList<>();
		final double latitude = position.getLatitude();
		final double longitude = position.getLongitude();

		final double latMax = latitude + dist;
		final double latMin = latitude - dist;
		final double lonMax = longitude + dist;
		final double lonMin = longitude - dist;

		for (final BusStop busStop : busStops) {
			final double busLatitude = busStop.getPosition().getLatitude();
			final double busLongitude = busStop.getPosition().getLongitude();
			if (busLatitude <= latMax && busLatitude >= latMin && busLongitude <= lonMax && busLongitude >= lonMin) {
				res.add(busStop);
			}

		}
		Collections.sort(res, new Comparator<BusStop>() {

			@Override
			public int compare(BusStop lhs, BusStop rhs) {
				return lhs.getName().compareTo(rhs.getName());
			}
		});
		return res;
	}

	/**
	 * Sort stops list
	 */
	private void order() {
		if (busStops.size() != 0) {
			Collections.sort(busStops);
		}
	}
}
