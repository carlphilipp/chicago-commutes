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

/**
 * Class that handle train data
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class TrainData {
	/** Tag **/
	private static final String TAG = "TrainData";
	/** List of stations **/
	private SparseArray<Station> mStations;
	/** List of stations ordered by name **/
	private List<Station> mStationsOrderByName;
	/** List of stations ordered by line and name **/
	private List<Station> mStationsOrderByLine;
	/** Map of stations ordered by line and map **/
	private Map<TrainLine, List<Station>> mStationsOrderByLineMap;
	/** List of stops **/
	private SparseArray<Stop> mStops;

	/**
	 * Constructor
	 */
	public TrainData() {
		this.mStations = new SparseArray<Station>();
		this.mStops = new SparseArray<Stop>();
	}

	/**
	 * Read train data from CSV file.
	 */
	public final void read() {
		if (mStations.size() == 0 && mStops.size() == 0) {
			try {
				CSVReader reader = new CSVReader(new InputStreamReader(ChicagoTracker.getAppContext().getAssets().open("cta_L_stops_cph.csv")));
				reader.readNext();
				String[] row = null;
				while ((row = reader.readNext()) != null) {
					Integer stopId = Integer.valueOf(row[0]); // STOP_ID
					TrainDirection direction = TrainDirection.fromString(row[1]); // DIRECTION_ID
					String stopName = row[2]; // STOP_NAME
					String stationName = row[3];// STATION_NAME
					// String stationDescription = row[4];//STATION_DESCRIPTIVE_NAME
					Integer parentStopId = Integer.valueOf(row[5]);// MAP_ID (old PARENT_STOP_ID)
					Boolean ada = Boolean.valueOf(row[6]);// ADA
					List<TrainLine> lines = new ArrayList<TrainLine>();
					String red = row[7];// Red
					String blue = row[8];// Blue
					String green = row[9];// G
					String brown = row[10];// Brn
					String purple = row[11];// P
					String purpleExp = row[12];// Pexp
					String yellow = row[13];// Y
					String pink = row[14];// Pink
					String orange = row[15];// Org
					if (red.equals("TRUE")) {
						lines.add(TrainLine.RED);
					}
					if (blue.equals("TRUE")) {
						lines.add(TrainLine.BLUE);
					}
					if (brown.equals("TRUE")) {
						lines.add(TrainLine.BROWN);
					}
					if (green.equals("TRUE")) {
						lines.add(TrainLine.GREEN);
					}
					if (purple.equals("TRUE")) {
						// PURPLE_EXPRESS MOD
						if (!lines.contains(TrainLine.PURPLE)) {
							lines.add(TrainLine.PURPLE);
						}
					}
					if (purpleExp.equals("TRUE")) {
						// PURPLE_EXPRESS MOD
						if (!lines.contains(TrainLine.PURPLE)) {
							lines.add(TrainLine.PURPLE);
						}
					}
					if (yellow.equals("TRUE")) {
						lines.add(TrainLine.YELLOW);
					}
					if (pink.equals("TRUE")) {
						lines.add(TrainLine.PINK);
					}
					if (orange.equals("TRUE")) {
						lines.add(TrainLine.ORANGE);
					}
					String location = row[16];// Location
					String locationTrunk = location.substring(1);
					String coordinates[] = locationTrunk.substring(0, locationTrunk.length() - 1).split(", ");
					Double longitude = Double.valueOf(coordinates[0]);
					Double latitude = Double.valueOf(coordinates[1]);

					Stop stop = StopFactory.buildStop(stopId, stopName, direction);
					stop.setPosition(new Position(longitude, latitude));
					Station station = StationFactory.buildStation(parentStopId, stationName, null);
					// stop.setStation(station);
					stop.setAda(ada);
					stop.setLines(lines);
					mStops.append(stopId, stop);

					Station currentStation = mStations.get(parentStopId, null);
					if (currentStation == null) {
						List<Stop> st = new ArrayList<Stop>();
						st.add(stop);
						station.setStops(st);
						mStations.append(parentStopId, station);
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
	 * Get all stations
	 *
	 * @return a map containing all the stations ordered line
	 */
	public final Map<TrainLine, List<Station>> getAllStations() {
		return mStationsOrderByLineMap;
	}

	/**
	 * Get a list of station for a given line
	 *
	 * @param line
	 *            the train line
	 * @return a list of station
	 */
	public final List<Station> getStationsForLine(final TrainLine line) {
		List<Station> res = mStationsOrderByLineMap.get(line);
		return res;
	}

	/**
	 * get a station
	 *
	 * @param id
	 *            the id of the station
	 * @return
	 */
	public final Station getStation(final Integer id) {
		if (mStations.size() != 0) {
			return mStations.get(id);
		} else {
			return null;
		}
	}

	/**
	 * Get a station with its position in the list
	 *
	 * @param position
	 *            the position of the station in the list
	 * @return a station
	 */
	public final Station getStationByPosition(final int position) {
		if (mStations.size() != 0 && position <= mStations.size()) {
			return mStations.valueAt(position);
		} else {
			return null;
		}
	}

	/**
	 * Get a station with its position in the ordered by name list
	 *
	 * @param position
	 *            the position
	 * @return a station
	 */
	public final Station getStationByPositionAndName(final int position) {
		if (mStationsOrderByName.size() != 0 && position <= mStationsOrderByName.size()) {
			return mStationsOrderByName.get(position);
		} else {
			return null;
		}
	}

	/**
	 * Get station by position and line
	 *
	 * @param position
	 *            the position
	 * @return a station
	 */
	public final Station getStationByPositionAndLine(final int position) {
		if (mStationsOrderByLine.size() != 0 && position <= mStationsOrderByLine.size()) {
			return mStationsOrderByLine.get(position);
		} else {
			return null;
		}
	}

	/**
	 * Get stations size
	 *
	 * @return the size of the stations list
	 */
	public final int getStationsSize() {
		return mStations.size();
	}

	public final boolean isStationNull() {
		return mStations == null;
	}

	/**
	 * Get station size from the ordered by line
	 *
	 * @return the size
	 */
	public final int getStationsSizeByLine() {
		return mStationsOrderByLine.size();
	}

	/**
	 * Get station by name
	 *
	 * @param name
	 *            the name of the station
	 * @return a station
	 */
	public final Station getStationByName(final String name) {
		int index = 0;
		while (index < mStations.size()) {
			Station station = mStations.valueAt(index++);
			if (station.getName().equals(name)) {
				return station;
			}
		}
		return null;
	}

	public final boolean isStopsNull() {
		return mStops == null;
	}

	/**
	 * Get a stop
	 *
	 * @param id
	 *            the id of the stop
	 * @return a stop
	 */
	public final Stop getStop(final Integer id) {
		if (mStops.size() != 0) {
			return mStops.get(id);
		} else {
			return null;
		}
	}

	/**
	 * Get a stop from the list
	 *
	 * @param position
	 *            the position of the stop in the list
	 * @return a stop
	 */
	public final Stop getStopByPosition(final int position) {
		if (mStops.size() != 0) {
			return mStops.valueAt(position);
		} else {
			return null;
		}
	}

	/**
	 * Get the size of the stops found
	 *
	 * @return a size
	 */
	public final int getStopsSize() {
		return mStops.size();
	}

	/**
	 * Get stop by desc
	 *
	 * @param desc
	 *            the desription of stop
	 * @return a stop
	 */
	public final Stop getStopByDesc(final String desc) {
		int index = 0;
		while (index < mStops.size()) {
			Stop stop = mStops.valueAt(index++);
			if (stop.getDescription().equals(desc) || stop.getDescription().split(" ")[0].equals(desc)) {
				return stop;
			}
		}
		return null;
	}

	/**
	 * Read near by station
	 *
	 * @param position
	 *            the position
	 * @return a list of station
	 */
	public final List<Station> readNearbyStation(final Position position) {

		final double dist = 0.004472;

		List<Station> res = new ArrayList<Station>();
		double latitude = position.getLatitude();
		double longitude = position.getLongitude();

		double latMax = latitude + dist;
		double latMin = latitude - dist;
		double lonMax = longitude + dist;
		double lonMin = longitude - dist;

		for (Station station : mStationsOrderByName) {
			for (Position stopPosition : station.getStopsPosition()) {
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

	public final List<Position> readPattern(final TrainLine line) {
		List<Position> positions = new ArrayList<Position>();
		try {
			CSVReader reader = new CSVReader(new InputStreamReader(ChicagoTracker.getAppContext().getAssets()
					.open("train_pattern/" + line.toTextString() + "_pattern.csv")));
			String[] row = null;
			while ((row = reader.readNext()) != null) {
				double longitude = Double.valueOf(row[0]);
				double latitude = Double.valueOf(row[1]);
				Position position = new Position();
				position.setLatitude(latitude);
				position.setLongitude(longitude);
				positions.add(position);
			}
			reader.close();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return positions;
	}

	/**
	 * Order stations
	 */
	private final void order() {
		List<Station> vals = new ArrayList<Station>();
		for (int i = 0; i < mStations.size(); i++) {
			vals.add(mStations.valueAt(i));
		}
		Collections.sort(vals);
		mStationsOrderByName = new ArrayList<Station>();
		mStationsOrderByLineMap = new TreeMap<TrainLine, List<Station>>();
		for (Station station : vals) {
			mStationsOrderByName.add(station);
		}
		for (Station station : vals) {
			Set<TrainLine> tls = station.getLines();
			Iterator<TrainLine> iterator = tls.iterator();
			while (iterator.hasNext()) {
				TrainLine tl = iterator.next();
				List<Station> stations = null;
				if (mStationsOrderByLineMap.containsKey(tl)) {
					stations = mStationsOrderByLineMap.get(tl);
				} else {
					stations = new ArrayList<Station>();
					mStationsOrderByLineMap.put(tl, stations);
				}
				stations.add(station);
				Collections.sort(stations);
			}
		}
		mStationsOrderByLine = new ArrayList<Station>();
		for (Entry<TrainLine, List<Station>> e : mStationsOrderByLineMap.entrySet()) {
			List<Station> temp = e.getValue();
			mStationsOrderByLine.addAll(temp);
		}
	}

	public SparseArray<Station> getStations() {
		return mStations;
	}
}
