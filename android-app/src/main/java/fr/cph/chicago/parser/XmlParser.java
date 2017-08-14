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

package fr.cph.chicago.parser;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.Bus;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusPattern;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.PatternPoint;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.Stop;
import fr.cph.chicago.entity.Train;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.enumeration.BusDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;

/**
 * XML parser
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
// TODO to refactor and optimize
public enum XmlParser {

    INSTANCE;

    private static final String TAG = XmlParser.class.getSimpleName();

    private XmlPullParser parser;
    private SimpleDateFormat simpleDateFormatTrain;
    private SimpleDateFormat simpleDateFormatBus;

    private void setUp() {
        try {
            final XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
            parser = pullParserFactory.newPullParser();
            simpleDateFormatTrain = new SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.US);
            simpleDateFormatBus = new SimpleDateFormat("yyyyMMdd HH:mm", Locale.US);
        } catch (final XmlPullParserException e) {
            Log.e(TAG, TrackerException.ERROR, e);
        }
    }

    XmlParser() {
        setUp();
    }

    /**
     * Parse arrivals
     *
     * @param is   the xml string
     * @param data the train data
     * @return a list of train arrival
     * @throws ParserException the parser exception
     */
    @SuppressWarnings("ConstantConditions")
    @NonNull
    public final synchronized SparseArray<TrainArrival> parseArrivals(@NonNull final InputStream is, @NonNull final TrainData data) throws ParserException {
        SparseArray<TrainArrival> arrivals = new SparseArray<>();
        try {
            parser.setInput(is, "UTF-8");
            int eventType = parser.getEventType();
            String tagName = null;

            Integer stationId = null;
            Integer stopId = null;
            String stationName = null;
            String stopDestination = null;
            TrainLine routeName = null;
            String destinationName = null;
            Date predictionDate = null;
            Date arrivalDepartureDate = null;
            boolean isApp = false;
            boolean isDly = false;
            double latitude = 0;
            double longitude = 0;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
                } else if (eventType == XmlPullParser.END_TAG) {
                    final String etaName = parser.getName();
                    if (StringUtils.isNotBlank(etaName) && "eta".equals(etaName)) {
                        final Station station = data.getStation(stationId).orElse(new Station());
                        station.setName(stationName);
                        final Stop stop = data.getStop(stopId).orElse(new Stop());
                        stop.setDescription(stopDestination);
                        final Position position = new Position(latitude, longitude);
                        destinationName = ("See train".equalsIgnoreCase(destinationName) && stop.getDescription().contains("Loop") && routeName == TrainLine.GREEN)
                            || ("See train".equalsIgnoreCase(destinationName) && stop.getDescription().contains("Loop") && routeName == TrainLine.BROWN)
                            || ("Loop, Midway".equalsIgnoreCase(destinationName) && routeName == TrainLine.BROWN)
                            ? "Loop" : destinationName;
                        final Eta eta = new Eta(
                            station,
                            stop,
                            routeName,
                            destinationName,
                            predictionDate,
                            arrivalDepartureDate,
                            isApp,
                            isDly,
                            position
                        );
                        final TrainArrival arri = arrivals.get(stationId, new TrainArrival());
                        List<Eta> etas = arri.getEtas();
                        if (etas == null) {
                            etas = new ArrayList<>();
                            arri.setEtas(etas);
                        }
                        etas.add(eta);
                        arrivals.append(stationId, arri);
                    }
                    tagName = null;
                } else if (eventType == XmlPullParser.TEXT) {
                    String text = parser.getText();
                    if (tagName != null) {
                        switch (tagName) {
                            case "staId": {
                                stationId = Integer.parseInt(text);
                                break;
                            }
                            case "stpId": {
                                stopId = Integer.parseInt(text);
                                break;
                            }
                            case "staNm": {
                                stationName = text;
                                break;
                            }
                            case "stpDe": {
                                stopDestination = text;
                                break;
                            }
                            case "rt": {
                                routeName = TrainLine.fromXmlString(text);
                                break;
                            }
                            case "destNm": {
                                destinationName = text;
                                break;
                            }
                            case "prdt": {
                                predictionDate = simpleDateFormatTrain.parse(text);
                                break;
                            }
                            case "arrT": {
                                arrivalDepartureDate = simpleDateFormatTrain.parse(text);
                                break;
                            }
                            case "isApp": {
                                isApp = Boolean.parseBoolean(text);
                                break;
                            }
                            case "isDly": {
                                isDly = Boolean.parseBoolean(text);
                                break;
                            }
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException | ParseException | IOException e) {
            throw new ParserException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        return arrivals;
    }

    /**
     * Parse bus route
     *
     * @param xml the xml to parse
     * @return a list of bus routes
     * @throws ParserException a parser exception
     */
    @NonNull
    public final synchronized List<BusRoute> parseBusRoutes(@NonNull final InputStream xml) throws ParserException {
        final List<BusRoute> routes = new ArrayList<>();
        try {
            parser.setInput(xml, "UTF-8");
            int eventType = parser.getEventType();
            String tagName = null;
            String routeId = null;
            String routeName = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
                } else if (eventType == XmlPullParser.END_TAG) {
                    final String route = parser.getName();
                    if (StringUtils.isNotBlank(route) && "route".equals(route)) {
                        assert routeId != null;
                        assert routeName != null;
                        final BusRoute busRoute = new BusRoute(routeId, routeName);
                        routes.add(busRoute);
                    }
                    tagName = null;
                } else if (eventType == XmlPullParser.TEXT) {
                    if (tagName != null) {
                        final String text = parser.getText();
                        switch (tagName) {
                            case "rt":
                                routeId = text;
                                break;
                            case "rtnm":
                                routeName = text;
                                break;
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (final IOException | XmlPullParserException e) {
            throw new ParserException(e);
        } finally {
            IOUtils.closeQuietly(xml);
        }
        return routes;
    }

    /**
     * Parse bus directions
     *
     * @param xml the xml to parse
     * @param id  the line id
     * @return a bus directions
     * @throws ParserException a parser exception
     */
    @NonNull
    public final synchronized BusDirections parseBusDirections(@NonNull final InputStream xml, @NonNull final String id) throws ParserException {
        final BusDirections directions = new BusDirections(id);
        try {
            parser.setInput(xml, "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.TEXT) {
                    final String text = parser.getText();
                    final BusDirection busDirection = new BusDirection(text);
                    if (busDirection.isOk()) {
                        directions.addBusDirection(busDirection);
                    }
                }
                eventType = parser.next();
            }
        } catch (final IOException | XmlPullParserException e) {
            throw new ParserException(e);
        } finally {
            IOUtils.closeQuietly(xml);
        }
        return directions;
    }

    /**
     * Parse bus bounds
     *
     * @param xml the xml to parse
     * @return a list of bus stop
     * @throws ParserException a parser exception
     */
    @NonNull
    public final synchronized List<BusStop> parseBusBounds(@NonNull final InputStream xml) throws ParserException {
        final List<BusStop> busStops = new ArrayList<>();
        String tagName = null;
        try {
            parser.setInput(xml, "UTF-8");
            Integer stopId = null;
            String stopName = null;
            Double latitude = null;
            Double longitude = null;
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
                } else if (eventType == XmlPullParser.END_TAG) {
                    final String stop = parser.getName();
                    if (StringUtils.isNotBlank(stop) && "stop".equals(stop)) {
                        final BusStop busArrival = new BusStop(stopId, stopName, stopName, new Position(latitude, longitude));
                        busStops.add(busArrival);
                    }
                    tagName = null;
                } else if (eventType == XmlPullParser.TEXT) {
                    final String text = parser.getText();
                    if (tagName != null) {
                        switch (tagName) {
                            case "stpid":
                                stopId = Integer.parseInt(text);
                                break;
                            case "stpnm":
                                stopName = text;
                                break;
                            case "lat":
                                latitude = Double.parseDouble(text);
                                break;
                            case "lon":
                                longitude = Double.parseDouble(text);
                                break;
                            case "msg":
                                throw new ParserException(text);
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (final IOException | XmlPullParserException e) {
            throw new ParserException(e);
        } finally {
            IOUtils.closeQuietly(xml);
        }
        return busStops;
    }

    /**
     * Parse bus arrivals
     *
     * @param xml the xml to parse
     * @return a list of bus arrivals
     * @throws ParserException a parser exception
     */
    @NonNull
    public final synchronized List<BusArrival> parseBusArrivals(@NonNull final InputStream xml) throws ParserException {
        final List<BusArrival> busArrivals = new ArrayList<>();
        String tagName = null;
        try {
            parser.setInput(xml, "UTF-8");
            int eventType = parser.getEventType();
            Date timeStamp = null;
            String stopName = null;
            Integer stopId = null;
            Integer busId = null;
            String routeId = null;
            String routeDirection = null;
            String busDestination = null;
            Date predictionTime = null;
            Boolean isDelay = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
                } else if (eventType == XmlPullParser.END_TAG) {
                    final String prd = parser.getName();
                    if (StringUtils.isNotBlank(prd) && "prd".equals(prd)) {
                        final BusArrival busArrival = new BusArrival(timeStamp, StringUtils.EMPTY, stopName, stopId, busId, routeId, routeDirection, busDestination, predictionTime, isDelay != null ? isDelay : false);
                        busArrivals.add(busArrival);
                    }
                    tagName = null;
                } else if (eventType == XmlPullParser.TEXT) {
                    if (tagName != null) {
                        final String text = parser.getText();
                        switch (tagName) {
                            case "tmstmp":
                                timeStamp = simpleDateFormatBus.parse(text);
                                break;
                            case "typ":
                                break;
                            case "stpnm":
                                stopName = text;
                                break;
                            case "stpid":
                                stopId = Integer.parseInt(text);
                                break;
                            case "vid":
                                busId = Integer.parseInt(text);
                                break;
                            case "dstp":
                                break;
                            case "rt":
                                routeId = text;
                                break;
                            case "rtdir":
                                routeDirection = BusDirection.BusDirectionEnum.fromString(text).toString();
                                break;
                            case "des":
                                busDestination = text;
                                break;
                            case "prdtm":
                                predictionTime = simpleDateFormatBus.parse(text);
                                break;
                            case "dly":
                                isDelay = BooleanUtils.toBoolean(text);
                                break;
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (final XmlPullParserException | ParseException | IOException e) {
            throw new ParserException(e);
        } finally {
            IOUtils.closeQuietly(xml);
        }
        return busArrivals;
    }

    /**
     * Parse alert general
     *
     * @param xml the xml to parse
     * @return a list of alert
     * @throws ParserException a parser exception
     */
    @NonNull
    public final synchronized List<BusPattern> parsePatterns(@NonNull final InputStream xml) throws ParserException {
        final List<BusPattern> patterns = new ArrayList<>();
        String tagName = null;
        BusPattern pattern = null;
        PatternPoint patternPoint = null;
        Position position = null;
        try {
            parser.setInput(xml, "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
                    if ("ptr".equals(tagName)) {
                        pattern = new BusPattern();
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    tagName = null;
                } else if (eventType == XmlPullParser.TEXT) {
                    String text = parser.getText();
                    if (tagName != null) {
                        switch (tagName) {
                            case "pid":
                                assert pattern != null;
                                pattern.setId(Integer.parseInt(text));
                                patterns.add(pattern);
                                break;
                            case "ln":
                                assert pattern != null;
                                pattern.setLength(Double.parseDouble(text));
                                break;
                            case "rtdir":
                                assert pattern != null;
                                text = BusDirection.BusDirectionEnum.fromString(text).toString();
                                pattern.setDirection(text);
                                break;
                            case "pt":
                                assert pattern != null;
                                patternPoint = new PatternPoint();
                                pattern.addPoint(patternPoint);
                                break;
                            case "seq":
                                assert patternPoint != null;
                                patternPoint.setSequence(Integer.parseInt(text));
                                break;
                            case "lat":
                                position = new Position();
                                assert patternPoint != null;
                                patternPoint.setPosition(position);
                                position.setLatitude(Double.parseDouble(text));
                                break;
                            case "lon":
                                assert position != null;
                                position.setLongitude(Double.parseDouble(text));
                                break;
                            case "typ":
                                assert patternPoint != null;
                                patternPoint.setType(text);
                                break;
                            case "stpid":
                                assert patternPoint != null;
                                patternPoint.setStopId(Integer.parseInt(text));
                                break;
                            case "stpnm":
                                assert patternPoint != null;
                                patternPoint.setStopName(text);
                                break;
                            case "pdist":
                                assert patternPoint != null;
                                patternPoint.setDistance(Double.parseDouble(text));
                                break;
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (final XmlPullParserException | IOException e) {
            throw new ParserException(e);
        } finally {
            IOUtils.closeQuietly(xml);
        }
        return patterns;
    }

    @NonNull
    public final synchronized List<Bus> parseVehicles(@NonNull final InputStream is) throws ParserException {
        final List<Bus> buses = new ArrayList<>();
        String tagName = null;
        try {
            Integer busId = null;
            Double latitude = null;
            Double longitude = null;
            Integer heading = null;
            String destination = null;

            parser.setInput(is, "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
                } else if (eventType == XmlPullParser.END_TAG) {
                    final String vehicle = parser.getName();
                    if (StringUtils.isNotBlank(vehicle) && "vehicle".equals(vehicle)) {
                        final Position position = new Position(latitude, longitude);
                        final Bus bus = new Bus(busId, position, heading, destination);
                        buses.add(bus);
                    }
                    tagName = null;
                } else if (eventType == XmlPullParser.TEXT) {
                    String text = parser.getText();
                    if (tagName != null) {
                        switch (tagName) {
                            case "vid":
                                busId = Integer.parseInt(text);
                                break;
                            case "lat":
                                latitude = Double.parseDouble(text);
                                break;
                            case "lon":
                                longitude = Double.parseDouble(text);
                                break;
                            case "hdg":
                                heading = Integer.parseInt(text);
                                break;
                            case "des":
                                destination = text;
                                break;
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            throw new ParserException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        return buses;
    }

    @NonNull
    public final synchronized List<Train> parseTrainsLocation(@NonNull final InputStream is) throws ParserException {
        final List<Train> trains = new ArrayList<>();
        String tagName = null;
        Train train = null;
        Position position = null;
        try {
            parser.setInput(is, "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
                    if ("train".equals(tagName)) {
                        train = new Train();
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    tagName = null;
                } else if (eventType == XmlPullParser.TEXT) {
                    String text = parser.getText();
                    if (tagName != null) {
                        switch (tagName) {
                            case "rn":
                                assert train != null;
                                train.setRouteNumber(Integer.parseInt(text));
                                trains.add(train);
                                break;
                            case "destSt":
                                //assert train != null;
                                //train.setDestStation(Integer.parseInt(text));
                                break;
                            case "destNm":
                                assert train != null;
                                train.setDestName(text);
                                break;
                            case "lat":
                                position = new Position();
                                assert train != null;
                                train.setPosition(position);
                                position.setLatitude(Double.parseDouble(text));
                                break;
                            case "lon":
                                assert position != null;
                                position.setLongitude(Double.parseDouble(text));
                                break;
                            case "heading":
                                assert train != null;
                                train.setHeading(Integer.parseInt(text));
                                break;
                            case "isApp":
                                //assert train != null;
                                //train.setApp(Boolean.valueOf(text));
                                break;
                            case "isDelay":
                                //assert train != null;
                                //train.setDly(Boolean.valueOf(text));
                                break;
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            throw new ParserException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        return trains;
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    public final synchronized List<Eta> parseTrainsFollow(@NonNull final InputStream is, @NonNull final TrainData data) throws ParserException {
        final SparseArray<TrainArrival> arrivals = parseArrivals(is, data);
        final List<Eta> res = new ArrayList<>();
        int index = 0;
        while (index < arrivals.size()) {
            final TrainArrival arri = arrivals.valueAt(index++);
            final List<Eta> etas = arri.getEtas();
            if (etas != null && etas.size() != 0) {
                res.add(etas.get(0));
            }
        }
        Collections.sort(res);
        return res;
    }
}
