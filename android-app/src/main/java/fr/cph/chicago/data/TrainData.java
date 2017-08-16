/**
 * Copyright 2017 Carl-Philipp Harmant
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
import android.util.Log;
import android.util.SparseArray;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import org.apache.commons.io.IOUtils;

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

/**
 * Class that handle train data
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public enum TrainData {
    INSTANCE;

    private static final String TAG = TrainData.class.getSimpleName();

    private static final double DEFAULT_RANGE = 0.008;

    // https://data.cityofchicago.org/Transportation/CTA-System-Information-List-of-L-Stops/8pix-ypme
    private static final String TRAIN_FILE_PATH = "train_stops.csv";

    private final SparseArray<Station> stations;
    private final SparseArray<Stop> stops;
    private final CsvParser parser;
    private Map<TrainLine, List<Station>> stationsOrderByLineMap;

    TrainData() {
        this.stations = new SparseArray<>();
        this.stops = new SparseArray<>();
        final CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setLineSeparator("\n");
        this.parser = new CsvParser(settings);
    }

    /**
     * Read train data from CSV file.
     */
    public final void read(@NonNull final Context context) {
        if (stations.size() == 0 && stops.size() == 0) {
            InputStreamReader inputStreamReader = null;
            try {
                inputStreamReader = new InputStreamReader(context.getAssets().open(TRAIN_FILE_PATH));
                final List<String[]> allRows = parser.parseAll(inputStreamReader);
                for (int i = 1; i < allRows.size(); i++) {
                    final String[] row = allRows.get(i);
                    final int stopId = Integer.parseInt(row[0]); // STOP_ID
                    final TrainDirection direction = TrainDirection.Companion.fromString(row[1]); // DIRECTION_ID
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

                    final Station station = new Station(parentStopId, stationName, Collections.emptyList());
                    final Stop stop = new Stop(stopId, stopName, direction, new Position(longitude, latitude), ada, lines);
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
            } finally {
                IOUtils.closeQuietly(inputStreamReader);
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
    @NonNull
    public final Optional<Stop> getStop(final Integer id) {
        if (stops.size() != 0) {
            return Optional.ofNullable(stops.get(id));
        }
        return Optional.empty();
    }

    /**
     * Read near by station
     *
     * @param position the position
     * @return a list of station
     */
    @NonNull
    public final List<Station> readNearbyStation(final Position position) {
        final double latitude = position.getLatitude();
        final double longitude = position.getLongitude();

        final double latMax = latitude + DEFAULT_RANGE;
        final double latMin = latitude - DEFAULT_RANGE;
        final double lonMax = longitude + DEFAULT_RANGE;
        final double lonMin = longitude - DEFAULT_RANGE;

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
                .findFirst()
                .ifPresent(nearByStations::add);
        }
        return nearByStations;
    }

    @NonNull
    public final List<Position> readPattern(@NonNull final Context context, @NonNull final TrainLine line) {
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(context.getAssets().open("train_pattern/" + line.toTextString() + "_pattern.csv"));
            final List<String[]> allRows = parser.parseAll(inputStreamReader);
            return Stream.of(allRows)
                .map(row -> {
                    final double longitude = Double.parseDouble(row[0]);
                    final double latitude = Double.parseDouble(row[1]);
                    final Position position = new Position();
                    position.setLatitude(latitude);
                    position.setLongitude(longitude);
                    return position;
                })
                .collect(Collectors.toList());
        } catch (final IOException e) {
            Log.e(TAG, e.getMessage(), e);
            return new ArrayList<>();
        } finally {
            IOUtils.closeQuietly(inputStreamReader);
        }
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
}
