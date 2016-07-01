/**
 * Copyright 2016 Carl-Philipp Harmant
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.Stop;
import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.entity.factory.StationFactory;
import fr.cph.chicago.entity.factory.StopFactory;

/**
 * Class that handle train data
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class TrainData {

    private static final String TAG = TrainData.class.getSimpleName();

    // https://data.cityofchicago.org/Transportation/CTA-System-Information-List-of-L-Stops/8pix-ypme
    private static final String TRAIN_FILE_PATH = "cta_L_stops_cph.csv";
    private static TrainData TRAIN_DATA;

    private final SparseArray<Station> stations;
    private final SparseArray<Stop> stops;
    private final CsvParser parser;
    private Map<TrainLine, List<Station>> stationsOrderByLineMap;

    private TrainData() {
        this.stations = new SparseArray<>();
        this.stops = new SparseArray<>();
        final CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setLineSeparator("\n");
        this.parser = new CsvParser(settings);
    }

    @NonNull
    public static TrainData getInstance() {
        if (TRAIN_DATA == null) {
            TRAIN_DATA = new TrainData();
        }
        return TRAIN_DATA;
    }

    /**
     * Read train data from CSV file.
     */
    public final void read(@NonNull final Context context) {
        if (stations.size() == 0 && stops.size() == 0) {
            try {
                final List<String[]> allRows = parser.parseAll((new InputStreamReader(context.getAssets().open(TRAIN_FILE_PATH))));
                for (int i = 1; i < allRows.size(); i++) {
                    final String[] row = allRows.get(i);
                    final int stopId = Integer.parseInt(row[0]); // STOP_ID
                    final TrainDirection direction = TrainDirection.fromString(row[1]); // DIRECTION_ID
                    final String stopName = row[2]; // STOP_NAME
                    final String stationName = row[3];// STATION_NAME
                    // String stationDescription = row[4];//STATION_DESCRIPTIVE_NAME
                    final int parentStopId = Integer.parseInt(row[5]);// MAP_ID (old PARENT_STOP_ID)
                    final boolean ada = Boolean.parseBoolean(row[6]);// ADA
                    final List<TrainLine> lines = new ArrayList<>();
                    final boolean red = Boolean.parseBoolean(row[7]);// Red
                    final boolean blue = Boolean.parseBoolean(row[8]);// Blue
                    final boolean green = Boolean.parseBoolean(row[9]);// G
                    final boolean brown = Boolean.parseBoolean(row[10]);// Brn
                    final boolean purple = Boolean.parseBoolean(row[11]);// P
                    final boolean purpleExp = Boolean.parseBoolean(row[12]);// Pexp
                    final boolean yellow = Boolean.parseBoolean(row[13]);// Y
                    final boolean pink = Boolean.parseBoolean(row[14]);// Pink
                    final boolean orange = Boolean.parseBoolean(row[15]);// Org
                    if (red) {
                        lines.add(TrainLine.RED);
                    }
                    if (blue) {
                        lines.add(TrainLine.BLUE);
                    }
                    if (brown) {
                        lines.add(TrainLine.BROWN);
                    }
                    if (green) {
                        lines.add(TrainLine.GREEN);
                    }
                    if (purple) {
                        // PURPLE_EXPRESS MOD
                        if (!lines.contains(TrainLine.PURPLE)) {
                            lines.add(TrainLine.PURPLE);
                        }
                    }
                    if (purpleExp) {
                        // PURPLE_EXPRESS MOD
                        if (!lines.contains(TrainLine.PURPLE)) {
                            lines.add(TrainLine.PURPLE);
                        }
                    }
                    if (yellow) {
                        lines.add(TrainLine.YELLOW);
                    }
                    if (pink) {
                        lines.add(TrainLine.PINK);
                    }
                    if (orange) {
                        lines.add(TrainLine.ORANGE);
                    }
                    final String location = row[16];// Location
                    final String locationTrunk = location.substring(1);
                    final String coordinates[] = locationTrunk.substring(0, locationTrunk.length() - 1).split(", ");
                    final double longitude = Double.parseDouble(coordinates[0]);
                    final double latitude = Double.parseDouble(coordinates[1]);

                    final Stop stop = StopFactory.buildStop(stopId, stopName, direction);
                    stop.setPosition(new Position(longitude, latitude));
                    final Station station = StationFactory.buildStation(parentStopId, stationName, new ArrayList<>());
                    stop.setAda(ada);
                    stop.setLines(lines);
                    stops.append(stopId, stop);

                    final Station currentStation = stations.get(parentStopId, null);
                    if (currentStation == null) {
                        station.setStops(Stream.of(stop).collect(Collectors.toList()));
                        stations.append(parentStopId, station);
                    } else {
                        currentStation.getStops().add(stop);
                    }
                }
                sort();
            } catch (final IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    /**
     * Get all stations
     *
     * @return a map containing all the stations ordered line
     */
    @NonNull
    public final Map<TrainLine, List<Station>> getAllStations() {
        return stationsOrderByLineMap;
    }

    /**
     * Get a list of station for a given line
     *
     * @param line the train line
     * @return a list of station
     */
    @NonNull
    public final List<Station> getStationsForLine(final TrainLine line) {
        return stationsOrderByLineMap.get(line);
    }

    /**
     * get a station
     *
     * @param id the id of the station
     * @return the station
     */
    @NonNull
    public final Optional<Station> getStation(final int id) {
        final Station station = stations.get(id);
        if (station == null) {
            return Optional.empty();
        } else {
            return Optional.of(station);
        }
    }

    public final boolean isStationNull() {
        return stations == null;
    }

    public final boolean isStopsNull() {
        return stops == null;
    }

    /**
     * Get a stop
     *
     * @param id the id of the stop
     * @return a stop
     */
    @Nullable
    public final Stop getStop(final Integer id) {
        if (stops.size() != 0) {
            return stops.get(id);
        }
        return null;

    }

    /**
     * Read near by station
     *
     * @param position the position
     * @return a list of station
     */
    @NonNull
    public final List<Station> readNearbyStation(final Position position) {

        final double dist = 0.004472;

        final double latitude = position.getLatitude();
        final double longitude = position.getLongitude();

        final double latMax = latitude + dist;
        final double latMin = latitude - dist;
        final double lonMax = longitude + dist;
        final double lonMin = longitude - dist;

        final List<Station> nearByStations = new ArrayList<>();
        for (int i = 0; i < stations.size(); i++) {
            final Station station = stations.valueAt(i);
            Stream.of(station.getStopsPosition())
                .filter(stopPosition -> {
                    final double trainLatitude = stopPosition.getLatitude();
                    final double trainLongitude = stopPosition.getLongitude();
                    return trainLatitude <= latMax && trainLatitude >= latMin && trainLongitude <= lonMax && trainLongitude >= lonMin;
                })
                .map(stopPosition -> station)
                // TODO understand why we limit to one here.
                .limit(1)
                .forEach(nearByStations::add);
        }
        return nearByStations;
    }

    @NonNull
    public final List<Position> readPattern(@NonNull final Context context, @NonNull final TrainLine line) {
        final List<Position> positions = new ArrayList<>();
        try {
            final List<String[]> allRows = parser.parseAll(new InputStreamReader(context.getAssets().open("train_pattern/" + line.toTextString() + "_pattern.csv")));
            Stream.of(allRows)
                .map(row -> {
                    final double longitude = Double.parseDouble(row[0]);
                    final double latitude = Double.parseDouble(row[1]);
                    final Position position = new Position();
                    position.setLatitude(latitude);
                    position.setLongitude(longitude);
                    return position;
                })
                .forEach(positions::add);
        } catch (final IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return positions;
    }

    /**
     * Order stations
     */
    private void sort() {
        stationsOrderByLineMap = new TreeMap<>();
        for (int i = 0; i < stations.size(); i++) {
            final Station station = stations.valueAt(i);
            final Set<TrainLine> trainLines = station.getLines();
            for (final TrainLine trainLine : trainLines) {
                if (stationsOrderByLineMap.containsKey(trainLine)) {
                    final List<Station> stations = stationsOrderByLineMap.get(trainLine);
                    stations.add(station);
                    Collections.sort(stations);
                } else {
                    final List<Station> stations = new ArrayList<>();
                    stationsOrderByLineMap.put(trainLine, stations);
                    stations.add(station);
                }
            }
        }
    }

    @NonNull
    public final SparseArray<Station> getStations() {
        return stations;
    }
}