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

package fr.cph.chicago.xml;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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
import fr.cph.chicago.util.Util;

/**
 * XML parser
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
// TODO to refactor and optimize
public final class XmlParser {

    private static final String TAG = XmlParser.class.getSimpleName();

    private static XmlParser instance;
    private XmlPullParser parser;
    private SimpleDateFormat simpleDateFormatTrain;
    private SimpleDateFormat simpleDateFormatBus;

    public static XmlParser getInstance() {
        if (instance == null) {
            instance = new XmlParser();
        }
        return instance;
    }

    private XmlParser() {
        try {
            final XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
            parser = pullParserFactory.newPullParser();
            simpleDateFormatTrain = new SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.US);
            simpleDateFormatBus = new SimpleDateFormat("yyyyMMdd HH:mm", Locale.US);
        } catch (final XmlPullParserException e) {
            Log.e(TAG, TrackerException.ERROR, e);
        }
    }

    /**
     * Parse arrivals
     *
     * @param is        the xml string
     * @param trainData the train data
     * @return a list of train arrival
     * @throws ParserException the parser exception
     */
    @NonNull
    public final SparseArray<TrainArrival> parseArrivals(@NonNull final InputStream is, @NonNull final TrainData trainData) throws ParserException {
        final SparseArray<TrainArrival> arrivals = new SparseArray<>();
        try {
            parser.setInput(is, "UTF-8");
            int eventType = parser.getEventType();
            XmlArrivalTrainTag tag = null;
            //Date tmst = null;
            //Integer errCd = null;
            //String errNum = null;
            String tagName = null;
            Integer staId = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
                    switch (tagName) {
                        case "tmst":
                            tag = XmlArrivalTrainTag.TMST;
                            break;
                        case "errCd":
                            tag = XmlArrivalTrainTag.ERRCD;
                            break;
                        case "errNm":
                            tag = XmlArrivalTrainTag.ERRNM;
                            break;
                        case "eta":
                            tag = XmlArrivalTrainTag.ETA;
                            break;
                        default:
                            tag = XmlArrivalTrainTag.OTHER;
                            break;
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    tag = null;
                } else if (eventType == XmlPullParser.TEXT) {
                    final String text = parser.getText();
                    switch (tag) {
                        case ETA:
                            break;
                        case OTHER:
                            switch (tagName) {
                                case "staId": {
                                    staId = Integer.parseInt(text);
                                    final TrainArrival arri = arrivals.get(staId, new TrainArrival());
                                    //arri.setErrorCode(errCd);
                                    //arri.setErrorMessage(errNum);
                                    //arri.setTimeStamp(tmst);
                                    List<Eta> etas = arri.getEtas();
                                    if (etas == null) {
                                        etas = new ArrayList<>();
                                        arri.setEtas(etas);
                                    }
                                    final Eta eta = new Eta();
                                    final Station station = trainData.getStation(staId);
                                    eta.setStation(station);
                                    etas.add(eta);

                                    arrivals.append(staId, arri);
                                    break;
                                }
                                case "stpId": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        final Stop stop = trainData.getStop(Integer.parseInt(text));
                                        currentEta.setStop(stop);
                                    }
                                    break;
                                }
                                case "staNm": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        currentEta.getStation().setName(text);
                                    }
                                    break;
                                }
                                case "stpDe": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        currentEta.getStop().setDescription(text);
                                    }
                                    break;
                                }
                                case "rn": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        currentEta.setRunNumber(Integer.parseInt(text));
                                    }
                                    break;
                                }
                                case "rt": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        currentEta.setRouteName(TrainLine.fromXmlString(text));
                                    }
                                    break;
                                }
                                case "destSt": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        final Integer i = Integer.parseInt(text);
                                        currentEta.setDestSt(i);
                                    }
                                    break;
                                }
                                case "destNm": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        if ("See train".equalsIgnoreCase(text) && currentEta.getStop().getDescription().contains("Loop") && currentEta.getRouteName() == TrainLine.GREEN) {
                                            currentEta.setDestName("Loop");
                                        } else if ("See train".equalsIgnoreCase(text) && currentEta.getStop().getDescription().contains("Loop") && currentEta.getRouteName() == TrainLine.BROWN) {
                                            currentEta.setDestName("Loop");
                                        } else if ("Loop, Midway".equalsIgnoreCase(text) && currentEta.getRouteName() == TrainLine.BROWN) {
                                            currentEta.setDestName("Loop");
                                        } else {
                                            currentEta.setDestName(text);
                                        }
                                    }
                                    break;
                                }
                                case "trDr": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        currentEta.setTrainRouteDirectionCode(Integer.parseInt(text));
                                    }
                                    break;
                                }
                                case "prdt": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        currentEta.setPredictionDate(simpleDateFormatTrain.parse(text));
                                    }
                                    break;
                                }
                                case "arrT": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        currentEta.setArrivalDepartureDate(simpleDateFormatTrain.parse(text));
                                    }
                                    break;
                                }
                                case "isApp": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        currentEta.setApp(BooleanUtils.toBoolean(Integer.parseInt(text)));
                                    }
                                    break;
                                }
                                case "isSch": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        currentEta.setSch(BooleanUtils.toBoolean(Integer.parseInt(text)));
                                    }
                                    break;
                                }
                                case "isDly": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        currentEta.setDly(BooleanUtils.toBoolean(Integer.parseInt(text)));
                                    }
                                    break;
                                }
                                case "isFlt": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        currentEta.setFlt(BooleanUtils.toBoolean(Integer.parseInt(text)));
                                    }
                                    break;
                                }
                                case "flags":
                                    break;
                                case "lat": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        final Position position = new Position();
                                        position.setLatitude(Double.parseDouble(text));
                                        currentEta.setPosition(position);
                                    }
                                    break;
                                }
                                case "lon": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        final Position position = currentEta.getPosition();
                                        position.setLongitude(Double.parseDouble(text));
                                    }
                                    break;
                                }
                                case "heading": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        currentEta.setHeading(Integer.parseInt(text));
                                    }
                                    break;
                                }
                            }
                            break;
                        case TMST:
                            //tmst = simpleDateFormatTrain.parse(text);
                            break;
                        case ERRCD:
                            //errCd = Integer.parseInt(text);
                            break;
                        case ERRNM:
                            //errNum = text;
                            break;
                        default:
                            break;
                    }
                }
                eventType = parser.next();
            }
        } catch (final XmlPullParserException | ParseException | IOException e) {
            throw new ParserException(TrackerException.ERROR, e);
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
    public final List<BusRoute> parseBusRoutes(@NonNull final InputStream xml) throws ParserException {
        final List<BusRoute> routes = new ArrayList<>();
        try {
            parser.setInput(xml, "UTF-8");
            int eventType = parser.getEventType();
            XmlArrivalBusTag tag = null;
            String tagName;
            BusRoute busRoute = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
                    switch (tagName) {
                        case "route":
                            tag = XmlArrivalBusTag.ROUTE;
                            break;
                        case "rt":
                            tag = XmlArrivalBusTag.RT;
                            break;
                        case "rtnm":
                            tag = XmlArrivalBusTag.RTNM;
                            break;
                        case "bustime-response":
                        case "SCRIPT":
                            tag = XmlArrivalBusTag.OTHER;
                            break;
                        case "msg":
                            tag = XmlArrivalBusTag.ERROR;
                            break;
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    tag = XmlArrivalBusTag.OTHER;
                } else if (eventType == XmlPullParser.TEXT) {
                    final String text = parser.getText();
                    if (tag != null) {
                        switch (tag) {
                            case ROUTE:
                                busRoute = new BusRoute();
                                routes.add(busRoute);
                                break;
                            case RT:
                                assert busRoute != null;
                                busRoute.setId(text);
                                break;
                            case RTNM:
                                assert busRoute != null;
                                busRoute.setName(text);
                                break;
                            case ERROR:
                                throw new ParserException(text);
                            default:
                                break;
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (final IOException | XmlPullParserException e) {
            throw new ParserException(TrackerException.ERROR, e);
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
    public final BusDirections parseBusDirections(@NonNull final InputStream xml, @NonNull final String id) throws ParserException {
        final BusDirections directions = new BusDirections();
        directions.setId(id);
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
            throw new ParserException(TrackerException.ERROR, e);
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
    public final List<BusStop> parseBusBounds(@NonNull final InputStream xml) throws ParserException {
        final List<BusStop> busStops = new ArrayList<>();
        String tagName = null;
        BusStop busStop = null;
        try {
            parser.setInput(xml, "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
                } else if (eventType == XmlPullParser.END_TAG) {
                    tagName = null;
                } else if (eventType == XmlPullParser.TEXT) {
                    final String text = parser.getText();
                    if (tagName != null) {
                        switch (tagName) {
                            case "stpid":
                                busStop = new BusStop();
                                busStop.setId(Integer.parseInt(text));
                                busStops.add(busStop);
                                break;
                            case "stpnm":
                                assert busStop != null;
                                busStop.setName(text);
                                break;
                            case "lat":
                                final Position position = new Position();
                                position.setLatitude(Double.parseDouble(text));
                                assert busStop != null;
                                busStop.setPosition(position);
                                break;
                            case "lon":
                                assert busStop != null;
                                busStop.getPosition().setLongitude(Double.parseDouble(text));
                                break;
                            case "msg":
                                throw new ParserException(text);
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (final IOException | XmlPullParserException e) {
            throw new ParserException(TrackerException.ERROR, e);
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
    public final List<BusArrival> parseBusArrivals(@NonNull final InputStream xml) throws ParserException {
        final List<BusArrival> busArrivals = new ArrayList<>();
        String tagName = null;
        BusArrival busArrival = null;
        try {
            parser.setInput(xml, "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
                } else if (eventType == XmlPullParser.END_TAG) {
                    tagName = null;
                } else if (eventType == XmlPullParser.TEXT) {
                    String text = parser.getText();
                    if (tagName != null) {
                        switch (tagName) {
                            case "tmstmp":
                                busArrival = new BusArrival();
                                busArrival.setTimeStamp(simpleDateFormatBus.parse(text));
                                busArrivals.add(busArrival);
                                break;
                            case "typ":
                                //assert busArrival != null;
                                //busArrival.setPredictionType(PredictionType.fromString(text));
                                break;
                            case "stpnm":
                                assert busArrival != null;
                                // to check if needed
                                //text = text.replaceAll("&amp;", "&");
                                busArrival.setStopName(text);
                                break;
                            case "stpid":
                                if (busArrival != null) {
                                    busArrival.setStopId(Integer.parseInt(text));
                                }
                                break;
                            case "vid":
                                assert busArrival != null;
                                busArrival.setBusId(Integer.parseInt(text));
                                break;
                            case "dstp":
                                //assert busArrival != null;
                                //busArrival.setDistanceToStop(Integer.parseInt(text));
                                break;
                            case "rt":
                                if (busArrival != null) {
                                    busArrival.setRouteId(text);
                                }
                                break;
                            case "rtdir":
                                assert busArrival != null;
                                text = BusDirection.BusDirectionEnum.fromString(text).toString();
                                busArrival.setRouteDirection(text);
                                break;
                            case "des":
                                assert busArrival != null;
                                busArrival.setBusDestination(text);
                                break;
                            case "prdtm":
                                assert busArrival != null;
                                busArrival.setPredictionTime(simpleDateFormatBus.parse(text));
                                break;
                            case "dly":
                                assert busArrival != null;
                                // to check
                                busArrival.setDly(BooleanUtils.toBoolean(text));
                                break;
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (final XmlPullParserException | ParseException | IOException e) {
            throw new ParserException(TrackerException.ERROR, e);
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
    public final List<BusPattern> parsePatterns(@NonNull final InputStream xml) throws ParserException {
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
            throw new ParserException(TrackerException.ERROR, e);
        } finally {
            IOUtils.closeQuietly(xml);
        }
        return patterns;
    }

    @NonNull
    public final List<Bus> parseVehicles(@NonNull final InputStream is) throws ParserException {
        final List<Bus> buses = new ArrayList<>();
        String tagName = null;
        Bus bus = null;
        Position position = null;
        try {
            parser.setInput(is, "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
                    if ("vehicle".equals(tagName)) {
                        bus = new Bus();
                    } else if ("error".equals(tagName)) {
                        eventType = XmlPullParser.END_DOCUMENT;
                        break;
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    tagName = null;
                } else if (eventType == XmlPullParser.TEXT) {
                    String text = parser.getText();
                    if (tagName != null) {
                        switch (tagName) {
                            case "vid":
                                assert bus != null;
                                bus.setId(Integer.parseInt(text));
                                buses.add(bus);
                                break;
                            case "tmstmp":
                                break;
                            case "lat":
                                position = new Position();
                                assert bus != null;
                                bus.setPosition(position);
                                position.setLatitude(Double.parseDouble(text));
                                break;
                            case "lon":
                                assert position != null;
                                position.setLongitude(Double.parseDouble(text));
                                break;
                            case "hdg":
                                assert bus != null;
                                bus.setHeading(Integer.parseInt(text));
                                break;
                            case "pid":
                                assert bus != null;
                                bus.setPatternId((Integer.parseInt(text)));
                                break;
                            case "rt":
                                assert bus != null;
                                bus.setRouteId(text);
                                break;
                            case "des":
                                assert bus != null;
                                bus.setDestination(text);
                                break;
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            throw new ParserException(TrackerException.ERROR, e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        return buses;
    }

    @NonNull
    public final List<Train> parseTrainsLocation(@NonNull final InputStream is) throws ParserException {
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
                            case "isDly":
                                //assert train != null;
                                //train.setDly(Boolean.valueOf(text));
                                break;
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            throw new ParserException(TrackerException.ERROR, e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        return trains;
    }

    @NonNull
    public final List<Eta> parseTrainsFollow(@NonNull final InputStream is, @NonNull final TrainData data) throws ParserException {
        SparseArray<TrainArrival> arrivals = new SparseArray<>();
        try {
            parser.setInput(is, "UTF-8");
            int eventType = parser.getEventType();
            XmlArrivalTrainTag tag = null;
            //			Date tmst = null;
            //			Integer errCd = null;
            //			String errNum = null;
            String tagName = null;
            Integer staId = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
                    switch (tagName) {
                        case "tmst":
                            tag = XmlArrivalTrainTag.TMST;
                            break;
                        case "errCd":
                            tag = XmlArrivalTrainTag.ERRCD;
                            break;
                        case "errNm":
                            tag = XmlArrivalTrainTag.ERRNM;
                            break;
                        case "eta":
                            tag = XmlArrivalTrainTag.ETA;
                            break;
                        default:
                            tag = XmlArrivalTrainTag.OTHER;
                            break;
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    tag = null;
                } else if (eventType == XmlPullParser.TEXT) {
                    String text = parser.getText();
                    switch (tag) {
                        case ETA:
                            break;
                        case OTHER:
                            switch (tagName) {
                                case "staId": {
                                    staId = Integer.parseInt(text);
                                    final TrainArrival arri = arrivals.get(staId, new TrainArrival());
                                    //arri.setErrorCode(errCd);
                                    //arri.setErrorMessage(errNum);
                                    //arri.setTimeStamp(tmst);
                                    List<Eta> etas = arri.getEtas();
                                    if (etas == null) {
                                        etas = new ArrayList<>();
                                        arri.setEtas(etas);
                                    }
                                    final Eta eta = new Eta();
                                    final Station station = data.getStation(Integer.parseInt(text));
                                    eta.setStation(station);
                                    etas.add(eta);

                                    arrivals.append(staId, arri);
                                    break;
                                }
                                case "stpId": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        final Stop stop = data.getStop(Integer.parseInt(text));
                                        currentEta.setStop(stop);
                                    }
                                    break;
                                }
                                case "staNm": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        final Station station = currentEta.getStation();
                                        station.setName(text);
                                    }
                                    break;
                                }
                                case "stpDe": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        final Stop stop = currentEta.getStop();
                                        stop.setDescription(text);
                                    }
                                    break;
                                }
                                case "rn": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        currentEta.setRunNumber(Integer.parseInt(text));
                                    }
                                    break;
                                }
                                case "rt": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        final TrainLine line = TrainLine.fromXmlString(text);
                                        currentEta.setRouteName(line);
                                    }
                                    break;
                                }
                                case "destSt": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        final Integer i = Integer.parseInt(text);
                                        currentEta.setDestSt(i);
                                    }
                                    break;
                                }
                                case "destNm": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        currentEta.setDestName(text);
                                    }
                                    break;
                                }
                                case "trDr": {
                                    //							final TrainArrival arri = arrivals.get(staId, null);
                                    //							if (arri != null) {
                                    //								final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                    //								currentEta.setTrainRouteDirectionCode(Integer.parseInt(text));
                                    //							}
                                    break;
                                }
                                case "prdt": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        currentEta.setPredictionDate(simpleDateFormatTrain.parse(text));
                                    }
                                    break;
                                }
                                case "arrT": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        currentEta.setArrivalDepartureDate(simpleDateFormatTrain.parse(text));
                                    }
                                    break;
                                }
                                case "isApp": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        currentEta.setApp(Util.textNumberToBoolean(text));
                                    }
                                    break;
                                }
                                case "isSch": {
                                    //							final TrainArrival arri = arrivals.get(staId, null);
                                    //							if (arri != null) {
                                    //								final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                    //								currentEta.setIsSch(Util.textNumberToBoolean(text));
                                    //							}
                                    break;
                                }
                                case "isDly": {
                                    final TrainArrival arri = arrivals.get(staId, null);
                                    if (arri != null) {
                                        final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                        currentEta.setDly(Util.textNumberToBoolean(text));
                                    }
                                    break;
                                }
                                case "isFlt": {
                                    //							final TrainArrival arri = arrivals.get(staId, null);
                                    //							if (arri != null) {
                                    //								final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
                                    //								currentEta.setIsFlt(Util.textNumberToBoolean(text));
                                    //							}
                                    break;
                                }
                            }
                            break;
                        case TMST:
                            //tmst = simpleDateFormatTrain.parse(text);
                            break;
                        case ERRCD:
                            //errCd = Integer.parseInt(text);
                            break;
                        case ERRNM:
                            //errNum = text;
                            break;
                        default:
                            break;
                    }
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException | ParseException | IOException e) {
            throw new ParserException(TrackerException.ERROR, e);
        } finally {
            IOUtils.closeQuietly(is);
        }
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
