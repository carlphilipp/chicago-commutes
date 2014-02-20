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

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import android.util.Log;
import android.util.SparseArray;
import au.com.bytecode.opencsv.CSVReader;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.Stop;
import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.entity.factory.StationFactory;
import fr.cph.chicago.entity.factory.StopFactory;

/**
 * 
 * @author carl
 *
 */
public class TrainData {

	/** Tag **/
	private static final String TAG = "TrainData";
	/** **/
	private SparseArray<Station> stations;
	/** **/
	private List<Station> stationsOrderByName;
	/** **/
	private List<Station> stationsOrderByLine;
	/** **/
	private Map<TrainLine, List<Station>> stationsOrderByLineMap;
	/** **/
	private SparseArray<Stop> stops;

	/**
	 * 
	 */
	public TrainData() {
		this.stations = new SparseArray<Station>();
		this.stops = new SparseArray<Stop>();
	}

	/**
	 * 
	 */
	public final void read() {
		if (stations.size() == 0 && stops.size() == 0) {
			try {
				CSVReader reader = new CSVReader(new InputStreamReader(ChicagoTracker.getAppContext().getAssets().open("cta_L_stops_cph.csv")));
				reader.readNext();
				String[] row = null;
				while ((row = reader.readNext()) != null) {
					Integer stopId = Integer.valueOf(row[0]); // STOP_ID
					TrainDirection direction = TrainDirection.fromString(row[1]); // DIRECTION_ID
					String stopName = row[2]; // STOP_NAME
					Double latitude = Double.valueOf(row[3]);// LON
					Double longitude = Double.valueOf(row[4]);// LAT
					String stationName = row[5];// STATION_NAME
					// String stationDescription = row[6];//STATION_DESCRIPTIVE_NAME
					Integer parentStopId = Integer.valueOf(row[7]);// PARENT_STOP_ID
					Boolean ada = Boolean.valueOf(row[8]);// ADA
					List<TrainLine> lines = new ArrayList<TrainLine>();
					String red = row[9];// Red
					String blue = row[10];// Blue
					String brown = row[11];// Brn
					String green = row[12];// G
					String purple = row[13];// P
					String purpleExp = row[14];// Pexp
					String yellow = row[15];// Y
					String pink = row[16];// Pink
					String orange = row[17];// Org
					if (red.equals("1")) {
						lines.add(TrainLine.RED);
					}
					if (blue.equals("1")) {
						lines.add(TrainLine.BLUE);
					}
					if (brown.equals("1")) {
						lines.add(TrainLine.BROWN);
					}
					if (green.equals("1")) {
						lines.add(TrainLine.GREEN);
					}
					if (purple.equals("1")) {
						// PURPLE_EXPRESS MOD
						if (!lines.contains(TrainLine.PURPLE)) {
							lines.add(TrainLine.PURPLE);
						}
					}
					if (purpleExp.equals("1")) {
						// PURPLE_EXPRESS MOD
						if (!lines.contains(TrainLine.PURPLE)) {
							lines.add(TrainLine.PURPLE);
						}
					}
					if (yellow.equals("1")) {
						lines.add(TrainLine.YELLOW);
					}
					if (pink.equals("1")) {
						lines.add(TrainLine.PINK);
					}
					if (orange.equals("1")) {
						lines.add(TrainLine.ORANGE);
					}

					Stop stop = StopFactory.buildStop(stopId, stopName, direction);
					stop.setPosition(new Position(longitude, latitude));
					Station station = StationFactory.buildStation(parentStopId, stationName, null);
					// stop.setStation(station);
					stop.setAda(ada);
					stop.setLines(lines);
					stops.append(stopId, stop);

					Station currentStation = stations.get(parentStopId, null);
					if (currentStation == null) {
						List<Stop> st = new ArrayList<Stop>();
						st.add(stop);
						station.setStops(st);
						stations.append(parentStopId, station);
					} else {
						currentStation.getStops().add(stop);
					}
				}
				reader.close();
				order();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}

	/**
	 * 
	 */
	private final void order() {
		List<Station> vals = new ArrayList<Station>();
		for (int i = 0; i < stations.size(); i++) {
			vals.add(stations.valueAt(i));
		}
		Collections.sort(vals);
		stationsOrderByName = new ArrayList<Station>();
		stationsOrderByLineMap = new TreeMap<TrainLine, List<Station>>();
		for (Station station : vals) {
			stationsOrderByName.add(station);
		}
		for (Station station : vals) {
			Set<TrainLine> tls = station.getLines();
			Iterator<TrainLine> iterator = tls.iterator();
			while (iterator.hasNext()) {
				TrainLine tl = iterator.next();
				List<Station> stations = null;
				if (stationsOrderByLineMap.containsKey(tl)) {
					stations = stationsOrderByLineMap.get(tl);
				} else {
					stations = new ArrayList<Station>();
					stationsOrderByLineMap.put(tl, stations);
				}
				stations.add(station);
				Collections.sort(stations);
			}
		}
		stationsOrderByLine = new ArrayList<Station>();
		for (Entry<TrainLine, List<Station>> e : stationsOrderByLineMap.entrySet()) {
			List<Station> temp = e.getValue();
			stationsOrderByLine.addAll(temp);
		}
	}

	/**
	 * 
	 * @return
	 */
	public final Map<TrainLine, List<Station>> getAllStations() {
		return stationsOrderByLineMap;
	}

	/**
	 * 
	 * @param line
	 * @return
	 */
	public final List<Station> getStationsForLine(final TrainLine line) {
		List<Station> res = stationsOrderByLineMap.get(line);
		return res;
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public final Stop getStop(final Integer id) {
		if (stops.size() != 0) {
			return stops.get(id);
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @param position
	 * @return
	 */
	public final Stop getStopByPosition(final int position) {
		if (stops.size() != 0) {
			return stops.valueAt(position);
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @return
	 */
	public final int getStopsSize() {
		return stops.size();
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public final Station getStation(final Integer id) {
		if (stations.size() != 0) {
			return stations.get(id);
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @param position
	 * @return
	 */
	public final Station getStationByPosition(final int position) {
		if (stations.size() != 0 && position <= stations.size()) {
			return stations.valueAt(position);
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @param position
	 * @return
	 */
	public final Station getStationByPositionAndName(final int position) {
		if (stationsOrderByName.size() != 0 && position <= stationsOrderByName.size()) {
			return stationsOrderByName.get(position);
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @param position
	 * @return
	 */
	public final Station getStationByPositionAndLine(final int position) {
		if (stationsOrderByLine.size() != 0 && position <= stationsOrderByLine.size()) {
			return stationsOrderByLine.get(position);
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @return
	 */
	public final int getStationsSize() {
		return stations.size();
	}

	/**
	 * 
	 * @return
	 */
	public final int getStationsSizeByLine() {
		return stationsOrderByLine.size();
	}

	/**
	 * 
	 * @param desc
	 * @return
	 */
	public final Stop getStopByDesc(final String desc) {
		int index = 0;
		while (index < stops.size()) {
			Stop stop = stops.valueAt(index++);
			if (stop.getDescription().equals(desc) || stop.getDescription().split(" ")[0].equals(desc)) {
				return stop;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public final Station getStationByName(final String name) {
		int index = 0;
		while (index < stations.size()) {
			Station station = stations.valueAt(index++);
			if (station.getName().equals(name)) {
				return station;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param position
	 * @return
	 */
	public final List<Station> readNearbyStation(final Position position) {
		/** **/
		final double dist = 0.004472;

		List<Station> res = new ArrayList<Station>();
		double latitude = position.getLatitude();
		double longitude = position.getLongitude();

		double latMax = latitude + dist;
		double latMin = latitude - dist;
		double lonMax = longitude + dist;
		double lonMin = longitude - dist;

		for (Station station : stationsOrderByName) {
			for(Position stopPosition : station.getStopsPosition()){
				double trainLatitude = stopPosition.getLatitude();
				double trainLongitude = stopPosition.getLongitude();
				if (trainLatitude <= latMax && trainLatitude >= latMin && trainLongitude <= lonMax && trainLongitude >= lonMin) {
					res.add(station);
					break;
				}
			}
		}
		return res;
	}
}
