package fr.cph.chicago.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.annotation.SuppressLint;
import android.util.SparseArray;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.Stop;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.enumeration.BusDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;

public class Xml {

	/** Tag **/
	private static final String TAG = "Xml";

	private XmlPullParser parser;
	private SimpleDateFormat df;

	@SuppressLint("SimpleDateFormat")
	public Xml() throws XmlPullParserException {
		XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
		parser = pullParserFactory.newPullParser();
		df = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
	}

	public SparseArray<TrainArrival> parseArrivals(String xml, TrainData data) throws XmlPullParserException, IOException, ParseException {
		InputStream is = new ByteArrayInputStream(xml.getBytes());
		parser.setInput(is, "UTF-8");
		int eventType = parser.getEventType();

		SparseArray<TrainArrival> arrivals = null;
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
							currentEta.setPredictionDate(df.parse(text));
						}
					} else if (tagName.equals("arrT")) {
						TrainArrival arri = arrivals.get(staId, null);
						if (arri != null) {
							Eta currentEta = arri.getEtas().get(arri.getEtas().size() - 1);
							currentEta.setArrivalDepartureDate(df.parse(text));
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
					tmst = df.parse(text);
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
		return arrivals;
	}

	public List<BusRoute> parseBusRoutes(String xml) throws XmlPullParserException, IOException, ParseException {
		InputStream is = new ByteArrayInputStream(xml.getBytes());
		parser.setInput(is, "UTF-8");
		int eventType = parser.getEventType();

		List<BusRoute> routes = null;
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
			eventType = parser.next();
		}
		return routes;
	}

	public BusDirections parseBusDirections(String xml, String id) throws XmlPullParserException, IOException {
		InputStream is = new ByteArrayInputStream(xml.getBytes());
		parser.setInput(is, "UTF-8");
		int eventType = parser.getEventType();
		BusDirections directions = null;
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
				if(busDirection != null){
					directions.addBusDirection(busDirection);
				}
			}
			eventType = parser.next();
		}
		return directions;
	}
}
