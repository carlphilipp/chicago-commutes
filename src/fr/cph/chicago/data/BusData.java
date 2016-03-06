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
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.csv.BusStopCsvParser;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.xml.Xml;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static fr.cph.chicago.connection.CtaRequestType.BUS_ROUTES;
import static fr.cph.chicago.connection.CtaRequestType.BUS_STOP_LIST;

/**
 * Class that handle bus data. Singleton
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BusData {

	private static final String TAG = BusData.class.getSimpleName();

	private static BusData busData;

	private List<BusRoute> busRoutes;
	private List<BusStop> busStops;

	private BusStopCsvParser parser;

	private BusData() {
		this.busRoutes = new ArrayList<>();
		this.busStops = new ArrayList<>();
		this.parser = new BusStopCsvParser();
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
			busStops = parser.parse();
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
			long startTime = System.currentTimeMillis();
			final String xmlResult = connect.connect(BUS_ROUTES, params);
			long stopTime = System.currentTimeMillis();
			Log.e(TAG, "Load bus route online: " + (stopTime - startTime) + " ms");
			startTime = System.currentTimeMillis();
			busRoutes = xml.parseBusRoutes(xmlResult);
			stopTime = System.currentTimeMillis();
			Log.e(TAG, "Parse bus route: " + (stopTime - startTime) + " ms");
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
		final String xmlResult = connect.connect(BUS_STOP_LIST, params);
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
			public int compare(final BusStop lhs, final BusStop rhs) {
				return lhs.getName().compareTo(rhs.getName());
			}
		});
		return res;
	}
}
