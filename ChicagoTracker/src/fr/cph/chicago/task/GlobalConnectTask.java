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
import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

/**
 * CTA connect task
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class GlobalConnectTask extends AsyncTask<Void, Void, Boolean> {

	/** Tag **/
	private static final String TAG = "GlobalConnectTask";
	/** Instance of the class where the we will callback **/
	private Object mInstance;
	/** The class **/
	private Class<?> mClasse;
	/** Request type **/
	private CtaRequestType mRequestType, mRequestType2;
	/** The params of the requests **/
	private MultiMap<String, String> mParams, mParams2;
	/** The XML parser **/
	private Xml mXml;
	/** The Json parser **/
	private Json mJson;
	/** List of train arrivals **/
	private SparseArray<TrainArrival> mTrainArrivals;
	/** Train data **/
	private TrainData mData;
	/** Train exception **/
	private TrackerException mTrackerTrainException;
	/** Bus exception **/
	private TrackerException mTrackerBusException;
	/** Bike exception **/
	private TrackerException mTrackerBikeException;
	/** Bus arrivals **/
	private List<BusArrival> mBusArrivals;
	/** Bike stations **/
	private List<BikeStation> mBikeStations;
	/** Error train */
	private boolean mTrainBoolean;
	/** Error bus **/
	private boolean mBusBoolean;
	/** Error bike **/
	private boolean mBikeBoolean;
	/** Load trains or not **/
	private boolean mLoadTrains;
	/** Load buses or not **/
	private boolean mLoadBuses;
	/** Load bikes or not **/
	private boolean mLoadBikes;
	/** **/
	private boolean mNetworkAvailable;

	/**
	 * Constructor
	 *
	 * @param instance
	 *            Instance of the object
	 * @param classe
	 *            The class
	 * @param requestType
	 *            the request type
	 * @param params
	 *            the params
	 * @param requestType2
	 *            the request type
	 * @param params2
	 *            the params
	 * @throws ParserException
	 *             the parser exception
	 */
	public GlobalConnectTask(final Object instance, final Class<?> classe, final CtaRequestType requestType, final MultiMap<String, String> params,
			final CtaRequestType requestType2, final MultiMap<String, String> params2, boolean loadTrains, boolean loadBuses, boolean loadBikes)
			throws ParserException {
		this.mInstance = instance;
		this.mClasse = classe;
		this.mRequestType = requestType;
		this.mParams = params;
		this.mData = DataHolder.getInstance().getTrainData();

		this.mRequestType2 = requestType2;
		this.mParams2 = params2;

		this.mTrainArrivals = new SparseArray<>();
		this.mBusArrivals = new ArrayList<>();
		this.mBikeStations = new ArrayList<>();

		this.mXml = new Xml();
		this.mJson = new Json();
		this.mLoadTrains = loadTrains;
		this.mLoadBuses = loadBuses;
		this.mLoadBikes = loadBikes;
	}

	@Override
	protected final Boolean doInBackground(final Void... connects) {
		mTrainBoolean = true;
		mBusBoolean = true;
		mBikeBoolean = true;
		mNetworkAvailable = Util.isNetworkAvailable();
		if (mNetworkAvailable) {
			CtaConnect ctaConnect = CtaConnect.getInstance();
			DivvyConnect divvyConnect = DivvyConnect.getInstance();
			if (mLoadTrains) {
				try {
					for (Entry<String, Object> entry : mParams.entrySet()) {
						String key = entry.getKey();
						if (key.equals("mapid")) {
							Object value = entry.getValue();
							if (value instanceof String) {
								String xmlResult = ctaConnect.connect(mRequestType, mParams);
								this.mTrainArrivals = mXml.parseArrivals(xmlResult, mData);
							} else if (value instanceof List) {
								@SuppressWarnings("unchecked")
								List<String> list = (List<String>) value;
								if (list.size() < 5) {
									String xmlResult = ctaConnect.connect(mRequestType, mParams);
									this.mTrainArrivals = mXml.parseArrivals(xmlResult, mData);
								} else {
									int size = list.size();
									SparseArray<TrainArrival> tempArrivals = new SparseArray<TrainArrival>();
									int start = 0;
									int end = 4;
									while (end < size + 1) {
										List<String> subList = list.subList(start, end);
										MultiMap<String, String> paramsTemp = new MultiValueMap<String, String>();
										for (String sub : subList) {
											paramsTemp.put(key, sub);
										}

										String xmlResult = ctaConnect.connect(mRequestType, paramsTemp);
										SparseArray<TrainArrival> temp = mXml.parseArrivals(xmlResult, mData);
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
									this.mTrainArrivals = tempArrivals;
								}
							}
						}
					}

					// Apply filters
					int index = 0;
					while (index < mTrainArrivals.size()) {
						TrainArrival arri = mTrainArrivals.valueAt(index++);
						List<Eta> etas = arri.getEtas();
						// Sort Eta by arriving time
						Collections.sort(etas);
						// Copy data into new list to be able to avoid looping on a list that we want to
						// modify
						List<Eta> etas2 = new ArrayList<>();
						etas2.addAll(etas);
						int j = 0;
						Eta eta = null;
						Station station = null;
						TrainLine line = null;
						TrainDirection direction = null;
						for (int i = 0; i < etas2.size(); i++) {
							eta = etas2.get(i);
							station = eta.getStation();
							line = eta.getRouteName();
							direction = eta.getStop().getDirection();
							boolean toRemove = Preferences.getTrainFilter(station.getId(), line, direction);
							if (!toRemove) {
								etas.remove(i - j++);
							}
						}
					}

				} catch (ConnectException | ParserException e) {
					mTrainBoolean = false;
					this.mTrackerTrainException = e;
				}
			}
			if (mLoadBuses) {
				try {
					List<String> rts = new ArrayList<>();
					List<String> stpids = new ArrayList<>();
					for (Entry<String, Object> entry : mParams2.entrySet()) {
						String key = entry.getKey();
						StringBuilder str = new StringBuilder();
						int i = 0;
						@SuppressWarnings("unchecked")
						List<String> values = (ArrayList<String>) entry.getValue();
						for (String v : values) {
							str.append(v + ",");
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
						MultiMap<String, String> para = new MultiValueMap<>();
						para.put("rt", rts.get(i));
						para.put("stpid", stpids.get(i));
						String xmlResult = ctaConnect.connect(mRequestType2, para);
						this.mBusArrivals.addAll(mXml.parseBusArrivals(xmlResult));
					}
				} catch (ConnectException | ParserException e) {
					mBusBoolean = false;
					this.mTrackerBusException = e;
				}
			}
			if (mLoadBikes) {
				try {
					String bikeContent = divvyConnect.connect();
					this.mBikeStations = mJson.parseStations(bikeContent);
					Collections.sort(this.mBikeStations, Util.BIKE_COMPARATOR_NAME);
				} catch (ParserException | ConnectException e) {
					mBikeBoolean = false;
					this.mTrackerBikeException = e;
				} finally {
					if (!(mBusBoolean && mTrainBoolean)) {
						if (mParams2.size() == 0 && mBusBoolean) {
							mBusBoolean = false;
						}
						if (mParams.size() == 0 && mTrainBoolean) {
							mTrainBoolean = false;
						}
					}
				}
			}
			return mTrainBoolean || mBusBoolean || mBikeBoolean;
		} else {
			return mNetworkAvailable;
		}
	}

	@Override
	protected final void onProgressUpdate(final Void... progress) {

	}

	@Override
	protected final void onPostExecute(final Boolean success) {
		try {
			if (success) {
				mClasse.getMethod("reloadData", SparseArray.class, List.class, List.class, Boolean.class, Boolean.class, Boolean.class, Boolean.class)
						.invoke(
								mInstance, this.mTrainArrivals, this.mBusArrivals, this.mBikeStations, this.mTrainBoolean, this.mBusBoolean,
								this.mBikeBoolean,
								this.mNetworkAvailable);
			} else if (!mNetworkAvailable) {
				mClasse.getMethod("reloadData", SparseArray.class, List.class, List.class, Boolean.class, Boolean.class, Boolean.class, Boolean.class)
						.invoke(
								mInstance, this.mTrainArrivals, this.mBusArrivals, this.mBikeStations, false, false, false, this.mNetworkAvailable);
			} else {
				TrackerException ex = mTrackerBusException == null ? (mTrackerBikeException == null ? mTrackerTrainException : mTrackerBikeException)
						: mTrackerBusException;
				if (ex != null) {
					// because both can be null
					Log.e(TAG, ex.getMessage(), ex);
				}
				mClasse.getMethod("displayError", TrackerException.class).invoke(mInstance, ex);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		super.onPostExecute(success);
	}
}
