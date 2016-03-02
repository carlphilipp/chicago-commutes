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

import android.annotation.SuppressLint;
import android.util.SparseArray;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.*;
import fr.cph.chicago.entity.enumeration.BusDirection;
import fr.cph.chicago.entity.enumeration.PredictionType;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.util.Util;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * XML parser
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
// TODO to refactor and optimize
public final class Xml {

	/**
	 * The parser
	 **/
	private XmlPullParser parser;
	/**
	 * The train date format
	 **/
	private SimpleDateFormat simpleDateFormatTrain;
	/**
	 * The bus date format
	 **/
	private SimpleDateFormat simpleDateFormatBus;

	/**
	 * @throws ParserException
	 */
	@SuppressLint("SimpleDateFormat")
	public Xml() throws ParserException {
		try {
			final XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
			parser = pullParserFactory.newPullParser();
			simpleDateFormatTrain = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
			simpleDateFormatBus = new SimpleDateFormat("yyyyMMdd HH:mm");
		} catch (XmlPullParserException e) {
			throw new ParserException(TrackerException.ERROR, e);
		}
	}

	/**
	 * Parse arrivals
	 *
	 * @param xml  the xml string
	 * @param data the train data
	 * @return a list of train arrival
	 * @throws ParserException the parser exception
	 */
	public final SparseArray<TrainArrival> parseArrivals(final String xml, final TrainData data) throws ParserException {
		InputStream is = null;
		SparseArray<TrainArrival> arrivals = null;
		try {
			is = new ByteArrayInputStream(xml.getBytes());
			parser.setInput(is, "UTF-8");
			int eventType = parser.getEventType();
			XmlArrivalTrainTag tag = null;
			Date tmst = null;
			Integer errCd = null;
			String errNum = null;
			String tagName = null;
			Integer staId = null;
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
					arrivals = new SparseArray<>();
				} else if (eventType == XmlPullParser.START_TAG) {
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
							staId = Integer.valueOf(text);
							final TrainArrival arri = arrivals.get(staId, new TrainArrival());
							arri.setErrorCode(errCd);
							arri.setErrorMessage(errNum);
							arri.setTimeStamp(tmst);
							List<Eta> etas = arri.getEtas();
							if (etas == null) {
								etas = new ArrayList<>();
								arri.setEtas(etas);
							}
							final Eta eta = new Eta();
							final Station station = data.getStation(Integer.valueOf(text));
							eta.setStation(station);
							etas.add(eta);

							arrivals.append(staId, arri);
							break;
						}
						case "stpId": {
							final TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								final Stop stop = data.getStop(Integer.valueOf(text));
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
								currentEta.setRunNumber(Integer.valueOf(text));
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
								final Integer i = Integer.valueOf(text);
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
							final TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								currentEta.setTrainRouteDirectionCode(Integer.valueOf(text));
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
								currentEta.setIsApp(BooleanUtils.toBoolean(Integer.valueOf(text)));
							}
							break;
						}
						case "isSch": {
							final TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								currentEta.setIsSch(BooleanUtils.toBoolean(Integer.valueOf(text)));
							}
							break;
						}
						case "isDly": {
							final TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								currentEta.setIsDly(BooleanUtils.toBoolean(Integer.valueOf(text)));
							}
							break;
						}
						case "isFlt": {
							final TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								currentEta.setIsFlt(BooleanUtils.toBoolean(Integer.valueOf(text)));
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
								position.setLatitude(Double.valueOf(text));
								currentEta.setPosition(position);
							}
							break;
						}
						case "lon": {
							final TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								final Position position = currentEta.getPosition();
								position.setLongitude(Double.valueOf(text));
							}
							break;
						}
						case "heading": {
							final TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								currentEta.setHeading(Integer.valueOf(text));
							}
							break;
						}
						}
						break;
					case TMST:
						tmst = simpleDateFormatTrain.parse(text);
						break;
					case ERRCD:
						errCd = Integer.valueOf(text);
						break;
					case ERRNM:
						errNum = text;
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
	public final List<BusRoute> parseBusRoutes(final String xml) throws ParserException {
		List<BusRoute> routes = new ArrayList<>();
		InputStream is = null;
		try {
			is = new ByteArrayInputStream(xml.getBytes());
			parser.setInput(is, "UTF-8");
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
			IOUtils.closeQuietly(is);
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
	public final BusDirections parseBusDirections(final String xml, final String id) throws ParserException {
		BusDirections directions = new BusDirections();
		directions.setId(id);
		InputStream is = null;
		try {
			is = new ByteArrayInputStream(xml.getBytes());
			parser.setInput(is, "UTF-8");
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
			IOUtils.closeQuietly(is);
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
	public final List<BusStop> parseBusBounds(final String xml) throws ParserException {
		List<BusStop> busStops = new ArrayList<>();
		String tagName = null;
		BusStop busStop = null;
		InputStream is = null;
		try {
			is = new ByteArrayInputStream(xml.getBytes());
			parser.setInput(is, "UTF-8");
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
						case "stpid":
							busStop = new BusStop();
							busStop.setId(Integer.valueOf(text));
							busStops.add(busStop);
							break;
						case "stpnm":
							assert busStop != null;
							busStop.setName(text);
							break;
						case "lat":
							final Position position = new Position();
							position.setLatitude(Double.valueOf(text));
							assert busStop != null;
							busStop.setPosition(position);
							break;
						case "lon":
							assert busStop != null;
							busStop.getPosition().setLongitude(Double.valueOf(text));
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
			IOUtils.closeQuietly(is);
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
	public final List<BusArrival> parseBusArrivals(String xml) throws ParserException {
		xml = xml.replaceAll("&", "&amp;");
		List<BusArrival> busArrivals = new ArrayList<>();
		String tagName = null;
		BusArrival busArrival = null;
		InputStream is = null;
		try {
			is = new ByteArrayInputStream(xml.getBytes());
			parser.setInput(is, "UTF-8");
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
							assert busArrival != null;
							busArrival.setPredictionType(PredictionType.fromString(text));
							break;
						case "stpnm":
							text = text.replaceAll("&amp;", "&");
							assert busArrival != null;
							busArrival.setStopName(text);
							break;
						case "stpid":
							if (busArrival != null) {
								busArrival.setStopId(Integer.valueOf(text));
							}
							break;
						case "vid":
							assert busArrival != null;
							busArrival.setBusId(Integer.valueOf(text));
							break;
						case "dstp":
							assert busArrival != null;
							busArrival.setDistanceToStop(Integer.valueOf(text));
							break;
						case "rt":
							if (busArrival != null) {
								busArrival.setRouteId(text);
							}
							break;
						case "rtdir":
							text = BusDirection.BusDirectionEnum.fromString(text).toString();
							assert busArrival != null;
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
							busArrival.setIsDly(BooleanUtils.toBoolean(text));
							break;
						}
					}
				}
				eventType = parser.next();
			}
		} catch (final XmlPullParserException | ParseException | IOException e) {
			throw new ParserException(TrackerException.ERROR, e);
		} finally {
			IOUtils.closeQuietly(is);
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
	public final List<BusPattern> parsePatterns(final String xml) throws ParserException {
		List<BusPattern> patterns = new ArrayList<>();
		String tagName = null;
		BusPattern pattern = null;
		PatternPoint patternPoint = null;
		Position position = null;
		InputStream is = null;
		try {
			is = new ByteArrayInputStream(xml.getBytes());
			parser.setInput(is, "UTF-8");
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					tagName = parser.getName();
					if (tagName.equals("ptr")) {
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
							pattern.setId(Integer.valueOf(text));
							patterns.add(pattern);
							break;
						case "ln":
							assert pattern != null;
							pattern.setLength(Double.valueOf(text));
							break;
						case "rtdir":
							text = BusDirection.BusDirectionEnum.fromString(text).toString();
							assert pattern != null;
							pattern.setDirection(text);
							break;
						case "pt":
							patternPoint = new PatternPoint();
							assert pattern != null;
							pattern.addPoint(patternPoint);
							break;
						case "seq":
							assert patternPoint != null;
							patternPoint.setSequence(Integer.valueOf(text));
							break;
						case "lat":
							position = new Position();
							assert patternPoint != null;
							patternPoint.setPosition(position);
							position.setLatitude(Double.valueOf(text));
							break;
						case "lon":
							assert position != null;
							position.setLongitude(Double.valueOf(text));
							break;
						case "typ":
							assert patternPoint != null;
							patternPoint.setType(text);
							break;
						case "stpid":
							assert patternPoint != null;
							patternPoint.setStopId(Integer.valueOf(text));
							break;
						case "stpnm":
							assert patternPoint != null;
							patternPoint.setStopName(text);
							break;
						case "pdist":
							assert patternPoint != null;
							patternPoint.setDistance(Double.valueOf(text));
							break;
						}
					}
				}
				eventType = parser.next();
			}
		} catch (final XmlPullParserException | IOException e) {
			throw new ParserException(TrackerException.ERROR, e);
		} finally {
			IOUtils.closeQuietly(is);
		}
		return patterns;
	}

	public final List<Bus> parseVehicles(final String xml) throws ParserException {
		List<Bus> buses = new ArrayList<>();
		String tagName = null;
		Bus bus = null;
		Position position = null;
		InputStream is = null;
		try {
			is = new ByteArrayInputStream(xml.getBytes());
			parser.setInput(is, "UTF-8");
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					tagName = parser.getName();
					if (tagName.equals("vehicle")) {
						bus = new Bus();
					} else if (tagName.equals("error")) {
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
							bus.setId(Integer.valueOf(text));
							buses.add(bus);
							break;
						case "tmstmp":
							break;
						case "lat":
							position = new Position();
							assert bus != null;
							bus.setPosition(position);
							position.setLatitude(Double.valueOf(text));
							break;
						case "lon":
							assert position != null;
							position.setLongitude(Double.valueOf(text));
							break;
						case "hdg":
							assert bus != null;
							bus.setHeading(Integer.valueOf(text));
							break;
						case "pid":
							assert bus != null;
							bus.setPatternId((Integer.valueOf(text)));
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

	public final List<Train> parseTrainsLocation(String xml) throws ParserException {
		List<Train> trains = new ArrayList<>();
		String tagName = null;
		Train train = null;
		Position position = null;
		InputStream is = null;
		try {
			is = new ByteArrayInputStream(xml.getBytes());
			parser.setInput(is, "UTF-8");
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					tagName = parser.getName();
					if (tagName.equals("train")) {
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
							train.setRouteNumber(Integer.valueOf(text));
							trains.add(train);
							break;
						case "destSt":
							assert train != null;
							train.setDestStation(Integer.valueOf(text));
							break;
						case "destNm":
							assert train != null;
							train.setDestName(text);
							break;
						case "lat":
							position = new Position();
							assert train != null;
							train.setPosition(position);
							position.setLatitude(Double.valueOf(text));
							break;
						case "lon":
							assert position != null;
							position.setLongitude(Double.valueOf(text));
							break;
						case "heading":
							assert train != null;
							train.setHeading(Integer.valueOf(text));
							break;
						case "isApp":
							assert train != null;
							train.setApp(Boolean.valueOf(text));
							break;
						case "isDly":
							assert train != null;
							train.setDly(Boolean.valueOf(text));
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

	public final List<Eta> parseTrainsFollow(final String xml, final TrainData data) throws ParserException {
		InputStream is = null;
		SparseArray<TrainArrival> arrivals = null;
		try {
			is = new ByteArrayInputStream(xml.getBytes());
			parser.setInput(is, "UTF-8");
			int eventType = parser.getEventType();
			XmlArrivalTrainTag tag = null;
			Date tmst = null;
			Integer errCd = null;
			String errNum = null;
			String tagName = null;
			Integer staId = null;
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
					arrivals = new SparseArray<>();
				} else if (eventType == XmlPullParser.START_TAG) {
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
							staId = Integer.valueOf(text);
							final TrainArrival arri = arrivals.get(staId, new TrainArrival());
							arri.setErrorCode(errCd);
							arri.setErrorMessage(errNum);
							arri.setTimeStamp(tmst);
							List<Eta> etas = arri.getEtas();
							if (etas == null) {
								etas = new ArrayList<>();
								arri.setEtas(etas);
							}
							final Eta eta = new Eta();
							final Station station = data.getStation(Integer.valueOf(text));
							eta.setStation(station);
							etas.add(eta);

							arrivals.append(staId, arri);
							break;
						}
						case "stpId": {
							final TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								final Stop stop = data.getStop(Integer.valueOf(text));
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
								currentEta.setRunNumber(Integer.valueOf(text));
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
								final Integer i = Integer.valueOf(text);
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
							final TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								currentEta.setTrainRouteDirectionCode(Integer.valueOf(text));
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
								currentEta.setIsApp(Util.textNumberToBoolean(text));
							}
							break;
						}
						case "isSch": {
							final TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								currentEta.setIsSch(Util.textNumberToBoolean(text));
							}
							break;
						}
						case "isDly": {
							final TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								currentEta.setIsDly(Util.textNumberToBoolean(text));
							}
							break;
						}
						case "isFlt": {
							final TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								final Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								currentEta.setIsFlt(Util.textNumberToBoolean(text));
							}
							break;
						}
						}
						break;
					case TMST:
						tmst = simpleDateFormatTrain.parse(text);
						break;
					case ERRCD:
						errCd = Integer.valueOf(text);
						break;
					case ERRNM:
						errNum = text;
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
