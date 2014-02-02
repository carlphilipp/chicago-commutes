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

package fr.cph.chicago.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;

import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.xml.Xml;

public class BusData {

	private List<BusRoute> routes;

	private static BusData busData;

	public static BusData getInstance() {
		if (busData == null) {
			busData = new BusData();
		}
		return busData;
	}

	private BusData() {
		routes = new ArrayList<BusRoute>();
	}

	public final List<BusRoute> read() throws ParserException, ConnectException {
		if (routes.size() == 0) {
			MultiMap<String, String> params = new MultiValueMap<String, String>();
			CtaConnect connect = CtaConnect.getInstance();
			Xml xml = new Xml();
			String xmlResult = connect.connect(CtaRequestType.BUS_ROUTES, params);
			routes = xml.parseBusRoutes(xmlResult);
		}
		return routes;
	}

	public final int getRouteSize() {
		return routes.size();
	}

	public final BusRoute getRoute(final int position) {
		return routes.get(position);
	}

	public final BusRoute getRoute(final String routeId) {
		BusRoute result = null;
		for (BusRoute br : routes) {
			if (br.getId().equals(routeId)) {
				result = br;
				break;
			}
		}
		return result;
	}

	public final List<BusStop> readBusStop(final String stopId, final String bound) throws ConnectException, ParserException {
		CtaConnect connect = CtaConnect.getInstance();
		MultiMap<String, String> params2 = new MultiValueMap<String, String>();
		params2.put("rt", stopId);
		params2.put("dir", bound);
		List<BusStop> busStops = null;
		String xmlResult = connect.connect(CtaRequestType.BUS_STOP_LIST, params2);
		Xml xml = new Xml();
		busStops = xml.parseBusBounds(xmlResult);
		return busStops;
	}
}
