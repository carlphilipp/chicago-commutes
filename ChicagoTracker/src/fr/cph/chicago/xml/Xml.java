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

package fr.cph.chicago.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.BooleanUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.annotation.SuppressLint;
import android.util.SparseArray;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.Alert;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.Pattern;
import fr.cph.chicago.entity.PatternPoint;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.Service;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.Stop;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.enumeration.BusDirection;
import fr.cph.chicago.entity.enumeration.PredictionType;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;

/**
 * XML parser
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class Xml {
	/** The parser **/
	private XmlPullParser parser;
	/** The train date format **/
	private SimpleDateFormat dfTrain;
	/** The bus date format **/
	private SimpleDateFormat dfBus;

	/**
	 * 
	 * @throws ParserException
	 */
	@SuppressLint("SimpleDateFormat")
	public Xml() throws ParserException {
		try {
			XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
			parser = pullParserFactory.newPullParser();
			dfTrain = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
			dfBus = new SimpleDateFormat("yyyyMMdd HH:mm");
		} catch (XmlPullParserException e) {
			throw new ParserException(TrackerException.ERROR, e);
		}
	}

	/**
	 * Parse arrivals
	 * 
	 * @param xml
	 *            the xml string
	 * @param data
	 *            the train data
	 * @return a list of train arrival
	 * @throws ParserException
	 *             the parser exception
	 */
	public final SparseArray<TrainArrival> parseArrivals(final String xml, final TrainData data) throws ParserException {
		InputStream is = new ByteArrayInputStream(xml.getBytes());
		SparseArray<TrainArrival> arrivals = null;
		try {
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
					arrivals = new SparseArray<TrainArrival>();
				} else if (eventType == XmlPullParser.START_TAG) {
					tagName = parser.getName();
					if (tagName.equals("tmst")) {
						tag = XmlArrivalTrainTag.TMST;
					} else if (tagName.equals("errCd")) {
						tag = XmlArrivalTrainTag.ERRCD;
					} else if (tagName.equals("errNm")) {
						tag = XmlArrivalTrainTag.ERRNM;
					} else if (tagName.equals("eta")) {
						tag = XmlArrivalTrainTag.ETA;
					} else {
						tag = XmlArrivalTrainTag.OTHER;
					}
				} else if (eventType == XmlPullParser.END_TAG) {
					tag = null;
				} else if (eventType == XmlPullParser.TEXT) {
					String text = parser.getText();
					switch (tag) {
					case ETA:
						break;
					case OTHER:
						if (tagName.equals("staId")) {
							staId = Integer.valueOf(text);
							TrainArrival arri = arrivals.get(staId, new TrainArrival());
							arri.setErrorCode(errCd);
							arri.setErrorMessage(errNum);
							arri.setTimeStamp(tmst);
							List<Eta> etas = arri.getEtas();
							if (etas == null) {
								etas = new ArrayList<Eta>();
								arri.setEtas(etas);
							}
							Eta eta = new Eta();
							Station station = data.getStation(Integer.valueOf(text));
							eta.setStation(station);
							etas.add(eta);

							arrivals.append(staId, arri);
						} else if (tagName.equals("stpId")) {
							TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								Stop stop = data.getStop(Integer.valueOf(text));
								currentEta.setStop(stop);
							}
						} else if (tagName.equals("staNm")) {
							TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								Station station = currentEta.getStation();
								station.setName(text);
							}

						} else if (tagName.equals("stpDe")) {
							TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								Stop stop = currentEta.getStop();
								stop.setDescription(text);
							}
						} else if (tagName.equals("rn")) {
							TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								currentEta.setRunNumber(Integer.valueOf(text));
							}
						} else if (tagName.equals("rt")) {
							TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								TrainLine line = TrainLine.fromXmlString(text);
								currentEta.setRouteName(line);
							}
						} else if (tagName.equals("destSt")) {
							TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								Integer i = Integer.valueOf(text);
								currentEta.setDestSt(i);
							}
						} else if (tagName.equals("destNm")) {
							TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								currentEta.setDestName(text);
							}
						} else if (tagName.equals("trDr")) {
							TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								currentEta.setTrainRouteDirectionCode(Integer.valueOf(text));
							}
						} else if (tagName.equals("prdt")) {
							TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								currentEta.setPredictionDate(dfTrain.parse(text));
							}
						} else if (tagName.equals("arrT")) {
							TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								currentEta.setArrivalDepartureDate(dfTrain.parse(text));
							}
						} else if (tagName.equals("isApp")) {
							TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								currentEta.setIsApp(BooleanUtils.toBoolean(Integer.valueOf(text)));
							}
						} else if (tagName.equals("isSch")) {
							TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								currentEta.setIsSch(BooleanUtils.toBoolean(Integer.valueOf(text)));
							}
						} else if (tagName.equals("isDly")) {
							TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								currentEta.setIsDly(BooleanUtils.toBoolean(Integer.valueOf(text)));
							}
						} else if (tagName.equals("isFlt")) {
							TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								currentEta.setIsFlt(BooleanUtils.toBoolean(Integer.valueOf(text)));
							}
						} else if (tagName.equals("flags")) {

						} else if (tagName.equals("lat")) {
							TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								Position position = new Position();
								position.setLatitude(Double.valueOf(text));
								currentEta.setPosition(position);
							}
						} else if (tagName.equals("lon")) {
							TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								Position position = currentEta.getPosition();
								position.setLongitude(Double.valueOf(text));
							}
						} else if (tagName.equals("heading")) {
							TrainArrival arri = arrivals.get(staId, null);
							if (arri != null) {
								Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
								currentEta.setHeading(Integer.valueOf(text));
							}
						}
						break;
					case TMST:
						tmst = dfTrain.parse(text);
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
		} catch (XmlPullParserException e) {
			throw new ParserException(TrackerException.ERROR, e);
		} catch (ParseException e) {
			throw new ParserException(TrackerException.ERROR, e);
		} catch (IOException e) {
			throw new ParserException(TrackerException.ERROR, e);
		}
		return arrivals;
	}

	/**
	 * Parse bus route
	 * 
	 * @param xml
	 *            the xml to parse
	 * @return a list of bus routes
	 * @throws ParserException
	 *             a parser exception
	 */
	public final List<BusRoute> parseBusRoutes(final String xml) throws ParserException {
		List<BusRoute> routes = null;
		try {
			InputStream is = new ByteArrayInputStream(xml.getBytes());
			parser.setInput(is, "UTF-8");
			int eventType = parser.getEventType();
			XmlArrivalBusTag tag = null;
			String tagName = null;
			BusRoute busRoute = null;
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
					routes = new ArrayList<BusRoute>();
				} else if (eventType == XmlPullParser.START_TAG) {
					tagName = parser.getName();
					if (tagName.equals("route")) {
						tag = XmlArrivalBusTag.ROUTE;
					} else if (tagName.equals("rt")) {
						tag = XmlArrivalBusTag.RT;
					} else if (tagName.equals("rtnm")) {
						tag = XmlArrivalBusTag.RTNM;
					} else if (tagName.equals("bustime-response") || tagName.equals("SCRIPT")) {
						tag = XmlArrivalBusTag.OTHER;
					}
				} else if (eventType == XmlPullParser.END_TAG) {
					tag = XmlArrivalBusTag.OTHER;
				} else if (eventType == XmlPullParser.TEXT) {
					String text = parser.getText();
					if (tag != null) {
						switch (tag) {
						case ROUTE:
							busRoute = new BusRoute();
							routes.add(busRoute);
							break;
						case RT:
							busRoute.setId(text);
							break;
						case RTNM:
							busRoute.setName(text);
							break;
						default:
							break;
						}
					}
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			throw new ParserException(TrackerException.ERROR, e);
		} catch (IOException e) {
			throw new ParserException(TrackerException.ERROR, e);
		}
		return routes;
	}

	/**
	 * Parse bus directions
	 * 
	 * @param xml
	 *            the xml to parse
	 * @param id
	 *            the id
	 * @return a bus directions
	 * @throws ParserException
	 *             a parser exception
	 */
	public final BusDirections parseBusDirections(final String xml, final String id) throws ParserException {
		BusDirections directions = null;
		try {
			InputStream is = new ByteArrayInputStream(xml.getBytes());
			parser.setInput(is, "UTF-8");
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
					directions = new BusDirections();
					directions.setId(id);
				} else if (eventType == XmlPullParser.START_TAG) {
					// TODO something ?
				} else if (eventType == XmlPullParser.END_TAG) {
					// TODO something ?
				} else if (eventType == XmlPullParser.TEXT) {
					String text = parser.getText();
					BusDirection busDirection = BusDirection.fromString(text);
					if (busDirection != null) {
						directions.addBusDirection(busDirection);
					}
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			throw new ParserException(TrackerException.ERROR, e);
		} catch (IOException e) {
			throw new ParserException(TrackerException.ERROR, e);
		}
		return directions;
	}

	/**
	 * Parse bus bounds
	 * 
	 * @param xml
	 *            the xml to parse
	 * @return a list of bus stop
	 * @throws ParserException
	 *             a parser exception
	 */
	public final List<BusStop> parseBusBounds(final String xml) throws ParserException {
		List<BusStop> busStops = null;
		String tagName = null;
		BusStop busStop = null;

		try {
			InputStream is = new ByteArrayInputStream(xml.getBytes());
			parser.setInput(is, "UTF-8");
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
					busStops = new ArrayList<BusStop>();
				} else if (eventType == XmlPullParser.START_TAG) {
					tagName = parser.getName();
				} else if (eventType == XmlPullParser.END_TAG) {
					tagName = null;
				} else if (eventType == XmlPullParser.TEXT) {
					String text = parser.getText();
					if (tagName != null) {
						if (tagName.equals("stpid")) {
							busStop = new BusStop();
							busStop.setId(Integer.valueOf(text));
							busStops.add(busStop);
						} else if (tagName.equals("stpnm")) {
							busStop.setName(text);
						} else if (tagName.equals("lat")) {
							Position position = new Position();
							position.setLatitude(Double.valueOf(text));
							busStop.setPosition(position);
						} else if (tagName.equals("lon")) {
							busStop.getPosition().setLongitude(Double.valueOf(text));
						}
					}
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			throw new ParserException(TrackerException.ERROR, e);
		} catch (IOException e) {
			throw new ParserException(TrackerException.ERROR, e);
		}
		return busStops;
	}

	/**
	 * Parse bus arrivals
	 * 
	 * @param xml
	 *            the xml to parse
	 * @return a list of bus arrivals
	 * @throws ParserException
	 *             a parser exception
	 */
	public final List<BusArrival> parseBusArrivals(String xml) throws ParserException {
		xml = xml.replaceAll("&", "&amp;");
		List<BusArrival> busArrivals = null;
		String tagName = null;
		BusArrival busArrival = null;
		InputStream is = new ByteArrayInputStream(xml.getBytes());
		try {
			parser.setInput(is, "UTF-8");
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
					busArrivals = new ArrayList<BusArrival>();
				} else if (eventType == XmlPullParser.START_TAG) {
					tagName = parser.getName();
				} else if (eventType == XmlPullParser.END_TAG) {
					tagName = null;
				} else if (eventType == XmlPullParser.TEXT) {
					String text = parser.getText();
					if (tagName != null) {
						if (tagName.equals("tmstmp")) {
							busArrival = new BusArrival();
							busArrival.setTimeStamp(dfBus.parse(text));
							busArrivals.add(busArrival);
						} else if (tagName.equals("typ")) {
							busArrival.setPredictionType(PredictionType.fromString(text));
						} else if (tagName.equals("stpnm")) {
							text = text.replaceAll("&amp;", "&");
							busArrival.setStopName(text);
						} else if (tagName.equals("stpid")) {
							if (busArrival != null) {
								busArrival.setStopId(Integer.valueOf(text));
							}
						} else if (tagName.equals("vid")) {
							busArrival.setBusId(Integer.valueOf(text));
						} else if (tagName.equals("dstp")) {
							busArrival.setDistanceToStop(Integer.valueOf(text));
						} else if (tagName.equals("rt")) {
							if (busArrival != null) {
								busArrival.setRouteId(text);
							}
						} else if (tagName.equals("rtdir")) {
							text = BusDirection.fromString(text).toString();
							busArrival.setRouteDirection(text);
						} else if (tagName.equals("des")) {
							busArrival.setBusDestination(text);
						} else if (tagName.equals("prdtm")) {
							busArrival.setPredictionTime(dfBus.parse(text));
						} else if (tagName.equals("dly")) {
							busArrival.setIsDly(BooleanUtils.toBoolean(text));
						}
					}
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			throw new ParserException(TrackerException.ERROR, e);
		} catch (ParseException e) {
			throw new ParserException(TrackerException.ERROR, e);
		} catch (IOException e) {
			throw new ParserException(TrackerException.ERROR, e);
		}
		return busArrivals;
	}

	/**
	 * Parse alert general
	 * 
	 * @param xml
	 *            the xml to parse
	 * @return a list of alert
	 * @throws ParserException
	 *             a parser exception
	 */
	public final List<Alert> parseAlertGeneral(final String xml) throws ParserException {
		List<Alert> alerts = null;
		String tagName = null;
		Alert alert = null;
		Service service = null;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.US);
		InputStream is = new ByteArrayInputStream(xml.getBytes());
		try {
			parser.setInput(is, "UTF-8");
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
					alerts = new ArrayList<Alert>();
				} else if (eventType == XmlPullParser.START_TAG) {
					tagName = parser.getName();
					if (tagName.equals("Alert")) {
						alert = new Alert();
					}
				} else if (eventType == XmlPullParser.END_TAG) {
					tagName = null;
					if (parser.getName().equals("Service")) {
						service = null;
					}
				} else if (eventType == XmlPullParser.TEXT) {
					String text = parser.getText();
					if (tagName != null) {
						if (tagName.equals("AlertId")) {
							alert.setId(Integer.valueOf(text));
							alerts.add(alert);
						} else if (tagName.equals("Headline")) {
							alert.setHeadline(text);
						} else if (tagName.equals("ShortDescription")) {
							alert.setShortDescription(text.trim());
						} else if (tagName.equals("FullDescription")) {
							alert.setFullDescription(text);
						} else if (tagName.equals("SeverityScore")) {
							alert.setSeverityScore(Integer.valueOf(text));
						} else if (tagName.equals("SeverityColor")) {
							alert.setSeverityColor(text);
						} else if (tagName.equals("SeverityCSS")) {
							alert.setSeverityCSS(text);
						} else if (tagName.equals("Impact")) {
							alert.setImpact(text);
						} else if (tagName.equals("EventStart")) {
							Date parsedDate = formatter.parse(text);
							alert.setEventStart(parsedDate);
						} else if (tagName.equals("EventEnd")) {
							Date parsedDate = formatter.parse(text);
							alert.setEventEnd(parsedDate);
						} else if (tagName.equals("TBD")) {
							alert.setTbd(Integer.valueOf(text));
						} else if (tagName.equals("MajorAlert")) {
							alert.setMajorAlert(Integer.valueOf(text));
						} else if (tagName.equals("AlertURL")) {
							alert.setAlertUrl(text);
						} else if (tagName.equals("ServiceType")) {
							service = new Service();
							alert.addService(service);
							service.setType(text);
						} else if (tagName.equals("ServiceTypeDescription")) {
							service.setTypeDescription(text);
						} else if (tagName.equals("ServiceName")) {
							service.setName(text);
						} else if (tagName.equals("ServiceId")) {
							service.setId(text);
						} else if (tagName.equals("ServiceBackColor")) {
							service.setBackColor(text);
						} else if (tagName.equals("ServiceTextColor")) {
							service.setTextColor(text);
						} else if (tagName.equals("ServiceURL")) {
							service.setUrl(text);
						}
					}
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			throw new ParserException(TrackerException.ERROR, e);
		} catch (ParseException e) {
			throw new ParserException(TrackerException.ERROR, e);
		} catch (IOException e) {
			throw new ParserException(TrackerException.ERROR, e);
		}
		return alerts;
	}
	
	/**
	 * Parse alert general
	 * 
	 * @param xml
	 *            the xml to parse
	 * @return a list of alert
	 * @throws ParserException
	 *             a parser exception
	 */
	public final List<Pattern> parsePatterns(final String xml) throws ParserException {
		List<Pattern> patterns = null;
		String tagName = null;
		Pattern pattern = null;
		PatternPoint patternPoint = null;
		Position position = null;
		InputStream is = new ByteArrayInputStream(xml.getBytes());
		try {
			parser.setInput(is, "UTF-8");
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
					patterns = new ArrayList<Pattern>();
				} else if (eventType == XmlPullParser.START_TAG) {
					tagName = parser.getName();
					if (tagName.equals("ptr")) {
						pattern = new Pattern();
					}
				} else if (eventType == XmlPullParser.END_TAG) {
					tagName = null;
				} else if (eventType == XmlPullParser.TEXT) {
					String text = parser.getText();
					if (tagName != null) {
						if (tagName.equals("pid")) {
							pattern.setId(Integer.valueOf(text));
							patterns.add(pattern);
						} else if (tagName.equals("ln")) {
							pattern.setLength(Double.valueOf(text));
						} else if (tagName.equals("rtdir")) {
							text = BusDirection.fromString(text).toString();
							pattern.setDirection(text);
						} else if (tagName.equals("pt")) {
							patternPoint = new PatternPoint();
							pattern.addPoint(patternPoint);
						} else if (tagName.equals("seq")) {
							patternPoint.setSequence(Integer.valueOf(text));
						} else if (tagName.equals("lat")) {
							position = new Position();
							patternPoint.setPosition(position);
							position.setLatitude(Double.valueOf(text));
						} else if (tagName.equals("lon")) {
							position.setLongitude(Double.valueOf(text));
						} else if (tagName.equals("typ")) {
							patternPoint.setType(text);
						} else if (tagName.equals("stpid")) {
							patternPoint.setStopId(Integer.valueOf(text));
						} else if (tagName.equals("stpnm")) {
							patternPoint.setStopName(text);
						} else if (tagName.equals("pdist")) {
							patternPoint.setDistance(Double.valueOf(text));
						}
					}
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			throw new ParserException(TrackerException.ERROR, e);
		} catch (IOException e) {
			throw new ParserException(TrackerException.ERROR, e);
		}
		return patterns;
	}
}
