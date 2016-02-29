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

package fr.cph.chicago.task;

import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.connection.DivvyConnect;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.json.Json;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.Xml;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

/**
 * CTA connect task
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
// TODO refactor
public class GlobalConnectTask extends AsyncTask<Void, Void, Boolean> {

	/**
	 * Tag
	 **/
	private static final String TAG = GlobalConnectTask.class.getSimpleName();
	/**
	 * Instance of the class where the we will callback
	 **/
	private Object instance;
	/**
	 * The class
	 **/
	private Class<?> clazz;
	/**
	 * Request type
	 **/
	private CtaRequestType requestType, requestType2;
	/**
	 * The params of the requests
	 **/
	private MultiValuedMap<String, String> params, params2;
	/**
	 * The XML parser
	 **/
	private Xml xml;
	/**
	 * The Json parser
	 **/
	private Json json;
	/**
	 * List of train arrivals
	 **/
	private SparseArray<TrainArrival> trainArrivals;
	/**
	 * Train data
	 **/
	private TrainData train;
	/**
	 * Train exception
	 **/
	private TrackerException trackerException;
	/**
	 * Bus exception
	 **/
	private TrackerException trackerBusException;
	/**
	 * Bike exception
	 **/
	private TrackerException trackerBikeException;
	/**
	 * Bus arrivals
	 **/
	private List<BusArrival> busArrivals;
	/**
	 * Bike stations
	 **/
	private List<BikeStation> bikeStations;
	/**
	 * Error train
	 */
	private boolean trainBoolean;
	/**
	 * Error bus
	 **/
	private boolean busBoolean;
	/**
	 * Error bike
	 **/
	private boolean bikeBoolean;
	/**
	 * Load trains or not
	 **/
	private boolean loadTrains;
	/**
	 * Load buses or not
	 **/
	private boolean loadBuses;
	/**
	 * Load bikes or not
	 **/
	private boolean loadBikes;
	/**
	 * Network available
	 **/
	private boolean networkAvailable;

	/**
	 * Constructor
	 *
	 * @param instance     Instance of the object
	 * @param classe       The class
	 * @param requestType  the request type
	 * @param params       the params
	 * @param requestType2 the request type
	 * @param params2      the params
	 * @throws ParserException the parser exception
	 */
	public GlobalConnectTask(final Object instance, final Class<?> classe, final CtaRequestType requestType,
			final MultiValuedMap<String, String> params, final CtaRequestType requestType2, final MultiValuedMap<String, String> params2,
			boolean loadTrains, boolean loadBuses, boolean loadBikes)
			throws ParserException {
		this.instance = instance;
		this.clazz = classe;
		this.requestType = requestType;
		this.params = params;
		this.train = DataHolder.getInstance().getTrainData();

		this.requestType2 = requestType2;
		this.params2 = params2;

		this.trainArrivals = new SparseArray<>();
		this.busArrivals = new ArrayList<>();
		this.bikeStations = new ArrayList<>();

		this.xml = new Xml();
		this.json = new Json();
		this.loadTrains = loadTrains;
		this.loadBuses = loadBuses;
		this.loadBikes = loadBikes;
	}

	@Override
	protected final Boolean doInBackground(final Void... connects) {
		trainBoolean = true;
		busBoolean = true;
		bikeBoolean = true;
		networkAvailable = Util.isNetworkAvailable();
		if (networkAvailable) {
			final CtaConnect ctaConnect = CtaConnect.getInstance();
			final DivvyConnect divvyConnect = DivvyConnect.getInstance();
			if (loadTrains) {
				try {
					for (final Entry<String, Collection<String>> entry : params.asMap().entrySet()) {
						final String key = entry.getKey();
						if (key.equals("mapid")) {
							final List<String> list = (List<String>) entry.getValue();
							if (list.size() < 5) {
								final String xmlResult = ctaConnect.connect(requestType, params);
								trainArrivals = xml.parseArrivals(xmlResult, train);
							} else {
								final int size = list.size();
								final SparseArray<TrainArrival> tempArrivals = new SparseArray<>();
								int start = 0;
								int end = 4;
								while (end < size + 1) {
									final List<String> subList = list.subList(start, end);
									final MultiValuedMap<String, String> paramsTemp = new ArrayListValuedHashMap<>();
									for (final String sub : subList) {
										paramsTemp.put(key, sub);
									}

									final String xmlResult = ctaConnect.connect(requestType, paramsTemp);
									final SparseArray<TrainArrival> temp = xml.parseArrivals(xmlResult, train);
									for (int j = 0; j < temp.size(); j++) {
										tempArrivals.put(temp.keyAt(j), temp.valueAt(j));
									}
									start = end;
									if (end + 3 >= size - 1 && end != size) {
										end = size;
									} else {
										end = end + 3;
									}
								}
								trainArrivals = tempArrivals;
							}
						}
					}

					// Apply filters
					int index = 0;
					while (index < trainArrivals.size()) {
						final TrainArrival arri = trainArrivals.valueAt(index++);
						final List<Eta> etas = arri.getEtas();
						// Sort Eta by arriving time
						Collections.sort(etas);
						// Copy data into new list to be able to avoid looping on a list that we want to modify
						final List<Eta> etas2 = new ArrayList<>();
						etas2.addAll(etas);
						int j = 0;
						for (int i = 0; i < etas2.size(); i++) {
							final Eta eta = etas2.get(i);
							final Station station = eta.getStation();
							final TrainLine line = eta.getRouteName();
							final TrainDirection direction = eta.getStop().getDirection();
							final boolean toRemove = Preferences.getTrainFilter(station.getId(), line, direction);
							if (!toRemove) {
								etas.remove(i - j++);
							}
						}
					}

				} catch (final ConnectException | ParserException e) {
					trainBoolean = false;
					this.trackerException = e;
				}
			}
			if (loadBuses) {
				try {
					final List<String> rts = new ArrayList<>();
					final List<String> stpids = new ArrayList<>();
					for (final Entry<String, Collection<String>> entry : params2.asMap().entrySet()) {
						final String key = entry.getKey();
						StringBuilder str = new StringBuilder();
						int i = 0;
						final List<String> values = (List<String>) entry.getValue();
						for (final String v : values) {
							str.append(v).append(",");
							if (i == 9 || i == values.size() - 1) {
								if (key.equals("rt")) {
									rts.add(str.toString());
								} else if (key.equals("stpid")) {
									stpids.add(str.toString());
								}
								str = new StringBuilder();
								i = -1;
							}
							i++;
						}
					}
					for (int i = 0; i < rts.size(); i++) {
						final MultiValuedMap<String, String> para = new ArrayListValuedHashMap<>();
						para.put("rt", rts.get(i));
						para.put("stpid", stpids.get(i));
						final String xmlResult = ctaConnect.connect(requestType2, para);
						busArrivals.addAll(xml.parseBusArrivals(xmlResult));
					}
				} catch (final ConnectException | ParserException e) {
					busBoolean = false;
					trackerBusException = e;
				}
			}
			if (loadBikes) {
				try {
					final String bikeContent = divvyConnect.connect();
					bikeStations = json.parseStations(bikeContent);
					Collections.sort(bikeStations, Util.BIKE_COMPARATOR_NAME);
				} catch (final ParserException | ConnectException e) {
					bikeBoolean = false;
					trackerBikeException = e;
				} finally {
					if (!(busBoolean && trainBoolean)) {
						if (params2.size() == 0 && busBoolean) {
							busBoolean = false;
						}
						if (params.size() == 0 && trainBoolean) {
							trainBoolean = false;
						}
					}
				}
			}
			return trainBoolean || busBoolean || bikeBoolean;
		} else {
			return Boolean.FALSE;
		}
	}

	@Override
	protected final void onProgressUpdate(final Void... progress) {

	}

	@Override
	protected final void onPostExecute(final Boolean success) {
		try {
			if (success) {
				clazz.getMethod("reloadData", SparseArray.class, List.class, List.class, Boolean.class, Boolean.class, Boolean.class, Boolean.class)
						.invoke(instance, trainArrivals, busArrivals, bikeStations, trainBoolean, busBoolean, bikeBoolean, networkAvailable);
			} else if (!networkAvailable) {
				clazz.getMethod("reloadData", SparseArray.class, List.class, List.class, Boolean.class, Boolean.class, Boolean.class, Boolean.class)
						.invoke(instance, trainArrivals, busArrivals, bikeStations, false, false, false, networkAvailable);
			} else {
				final TrackerException ex = trackerBusException == null ? (trackerBikeException == null ? trackerException : trackerBikeException)
						: trackerBusException;
				if (ex != null) {
					// because both can be null
					Log.e(TAG, ex.getMessage(), ex);
				}
				clazz.getMethod("displayError", TrackerException.class).invoke(instance, ex);
			}
		} catch (final Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		super.onPostExecute(success);
	}
}
