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

package fr.cph.chicago.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;

import android.os.AsyncTask;
import android.util.SparseArray;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.xml.Xml;

public class CtaConnectTask extends AsyncTask<Void, Void, Boolean> {

	/** Tag **/
	private static final String TAG = "CtaConnectTask";

	private Class<?> classe;
	private CtaRequestType requestType, requestType2;
	private MultiMap<String, String> params, params2;
	private Xml xml;
	private SparseArray<TrainArrival> trainArrivals;
	private TrainData data;
	private TrackerException trackerException;
	private List<BusArrival> busArrivals;

	public CtaConnectTask(final Class<?> classe, final CtaRequestType requestType, final MultiMap<String, String> params,
			final CtaRequestType requestType2, final MultiMap<String, String> params2

	) throws ParserException {

		this.classe = classe;
		this.requestType = requestType;
		this.params = params;
		this.data = DataHolder.getInstance().getTrainData();

		this.requestType2 = requestType2;
		this.params2 = params2;

		this.trainArrivals = new SparseArray<TrainArrival>();
		this.busArrivals = new ArrayList<BusArrival>();

		this.xml = new Xml();
	}

	@Override
	protected final Boolean doInBackground(final Void... connects) {
		CtaConnect connect = CtaConnect.getInstance();
		try {
			for (Entry<String, Object> entry : params.entrySet()) {
				String key = entry.getKey();
				if (key.equals("mapid")) {
					Object value = entry.getValue();
					if (value instanceof String) {
						String xmlResult = connect.connect(requestType, params);
						this.trainArrivals = xml.parseArrivals(xmlResult, data);
					} else if (value instanceof List) {
						@SuppressWarnings("unchecked")
						List<String> list = (List<String>) value;
						if (list.size() < 5) {
							String xmlResult = connect.connect(requestType, params);
							this.trainArrivals = xml.parseArrivals(xmlResult, data);
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

								String xmlResult = connect.connect(requestType, paramsTemp);
								SparseArray<TrainArrival> temp = xml.parseArrivals(xmlResult, data);
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
							this.trainArrivals = tempArrivals;
						}
					}
				}
			}

			// Apply filters
			int index = 0;
			while (index < trainArrivals.size()) {
				TrainArrival arri = trainArrivals.valueAt(index++);
				List<Eta> etas = arri.getEtas();
				// Sort Eta by arriving time
				Collections.sort(etas);
				// Copy data into new list to be able to avoid looping on a list that we want to modify
				List<Eta> etas2 = new ArrayList<Eta>();
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

			List<String> rts = new ArrayList<String>();
			List<String> stpids = new ArrayList<String>();
			for (Entry<String, Object> entry : params2.entrySet()) {

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
				MultiMap<String, String> para = new MultiValueMap<String, String>();
				para.put("rt", rts.get(i));
				para.put("stpid", stpids.get(i));
				String xmlResult = connect.connect(requestType2, para);
				this.busArrivals.addAll(xml.parseBusArrivals(xmlResult));
			}
		} catch (ConnectException e) {
			this.trackerException = e;
		} catch (ParserException e) {
			this.trackerException = e;
		}
		return true;
	}

	@Override
	protected final void onProgressUpdate(final Void... progress) {

	}

	@Override
	protected final void onPostExecute(final Boolean success) {
		try {
			if (trackerException == null) {
				classe.getMethod("reloadData", SparseArray.class, List.class).invoke(null, this.trainArrivals, this.busArrivals);
			} else {
				// call static function
				classe.getMethod("displayError", TrackerException.class).invoke(null, trackerException);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onPostExecute(success);
	}
}
