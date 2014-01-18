package fr.cph.chicago.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.xml.Xml;

public class CtaConnectTask extends AsyncTask<Void, Void, Boolean> {

	/** Tag **/
	private static final String TAG = "CtaConnectTask";

	private Class<?> classe;
	private CtaRequestType requestType;
	private MultiMap<String, String> params;
	private Xml xml;
	private SparseArray<TrainArrival> arrivals;
	private TrainData data;
	private Activity activity;

	public CtaConnectTask(final Class<?> classe, final CtaRequestType requestType, final MultiMap<String, String> params, final TrainData data,
			Activity activity) throws XmlPullParserException {
		this.classe = classe;
		this.requestType = requestType;
		this.params = params;
		this.xml = new Xml();
		this.data = data;
		this.arrivals = new SparseArray<TrainArrival>();

		// to remove, for test only
		this.activity = activity;
	}

	@Override
	protected Boolean doInBackground(Void... connects) {
		CtaConnect connect = CtaConnect.getInstance();
		try {
			for (Entry<String, Object> entry : params.entrySet()) {
				String key = entry.getKey();
				if (key.equals("mapid")) {
					Object value = entry.getValue();
					if (value instanceof String) {
						String xmlResult = connect.connect(requestType, params);
						this.arrivals = xml.parseArrivals(xmlResult, data);
					} else if (value instanceof List) {
						@SuppressWarnings("unchecked")
						List<String> list = (List<String>) value;
						if (list.size() < 5) {
							String xmlResult = connect.connect(requestType, params);
							this.arrivals = xml.parseArrivals(xmlResult, data);
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
							this.arrivals = tempArrivals;
						}
					}
				}
			}

			// Apply filters
			int index = 0;
			while (index < arrivals.size()) {
				TrainArrival arri = arrivals.valueAt(index++);
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
					boolean toRemove = Preferences.getFilter(station.getId(), line, direction);
					if (!toRemove) {
						etas.remove(i - j++);
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	protected void onProgressUpdate(Void... progress) {

	}

	@Override
	protected void onPostExecute(final Boolean success) {
		try {
			classe.getMethod("reloadData", SparseArray.class).invoke(null, this.arrivals);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		super.onPostExecute(success);
	}

	public String connectTest() {
		StringBuilder derp = new StringBuilder();
		try {
			InputStreamReader ipsr = new InputStreamReader(activity.getAssets().open("test3.xml"));
			BufferedReader reader = new BufferedReader(ipsr);
			String line = null;

			while ((line = reader.readLine()) != null) {
				derp.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return derp.toString();
	}

}
