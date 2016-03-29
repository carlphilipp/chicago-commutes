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
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import fr.cph.chicago.connection.CtaConnect;
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
import fr.cph.chicago.json.JsonParser;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.XmlParser;

import static fr.cph.chicago.connection.CtaRequestType.BUS_ARRIVALS;
import static fr.cph.chicago.connection.CtaRequestType.TRAIN_ARRIVALS;


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

    private Object instance;
    private Class<?> clazz;
    private MultiValuedMap<String, String> trainParams, busParams;
    private XmlParser xmlParser;
    private JsonParser jsonParser;

    private SparseArray<TrainArrival> trainArrivals;
    private List<BusArrival> busArrivals;
    private List<BikeStation> bikeStations;

    private TrainData train;
    private TrackerException trackerException, trackerBusException, trackerBikeException;

    private boolean networkAvailable;

    /**
     * Constructor
     *
     * @param instance    Instance of the object
     * @param clazz       The class
     * @param trainParams the trainParams
     * @param busParams   the trainParams
     * @throws ParserException the parser exception
     */
    // TODO remove some of the trainParams as always the same are used
    public GlobalConnectTask(@NonNull final Object instance,
                             @NonNull final Class<?> clazz,
                             @NonNull final MultiValuedMap<String, String> trainParams,
                             @NonNull final MultiValuedMap<String, String> busParams) {
        this.instance = instance;
        this.clazz = clazz;
        this.trainParams = trainParams;
        this.busParams = busParams;

        this.train = DataHolder.getInstance().getTrainData();

        this.trainArrivals = new SparseArray<>();
        this.busArrivals = new ArrayList<>();
        this.bikeStations = new ArrayList<>();

        this.xmlParser = XmlParser.getInstance();
        this.jsonParser = JsonParser.getInstance();
    }

    @Override
    protected final Boolean doInBackground(final Void... connects) {
        networkAvailable = Util.isNetworkAvailable();
        if (networkAvailable) {
            loadTrains();
            loadBuses();
            loadBikes();
            return true;
        } else {
            return false;
        }
    }

    private void loadTrains() {
        try {
            final CtaConnect ctaConnect = CtaConnect.getInstance();
            for (final Entry<String, Collection<String>> entry : trainParams.asMap().entrySet()) {
                final String key = entry.getKey();
                if ("mapid".equals(key)) {
                    final List<String> list = (List<String>) entry.getValue();
                    if (list.size() < 5) {
                        final InputStream xmlResult = ctaConnect.connect(TRAIN_ARRIVALS, trainParams);
                        trainArrivals = xmlParser.parseArrivals(xmlResult, train);
                    } else {
                        final int size = list.size();
                        int start = 0;
                        int end = 4;
                        while (end < size + 1) {
                            final List<String> subList = list.subList(start, end);
                            final MultiValuedMap<String, String> paramsTemp = new ArrayListValuedHashMap<>();
                            for (final String sub : subList) {
                                paramsTemp.put(key, sub);
                            }
                            final InputStream xmlResult = ctaConnect.connect(TRAIN_ARRIVALS, paramsTemp);
                            final SparseArray<TrainArrival> temp = xmlParser.parseArrivals(xmlResult, train);
                            for (int j = 0; j < temp.size(); j++) {
                                trainArrivals.put(temp.keyAt(j), temp.valueAt(j));
                            }
                            start = end;
                            if (end + 3 >= size - 1 && end != size) {
                                end = size;
                            } else {
                                end = end + 3;
                            }
                        }
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
            this.trackerException = e;
        }
    }

    private void loadBuses() {
        // Load bus
        try {
            final CtaConnect ctaConnect = CtaConnect.getInstance();
            final List<String> rts = new ArrayList<>();
            final List<String> stpids = new ArrayList<>();
            for (final Entry<String, Collection<String>> entry : busParams.asMap().entrySet()) {
                final String key = entry.getKey();
                StringBuilder str = new StringBuilder();
                int i = 0;
                final List<String> values = (List<String>) entry.getValue();
                for (final String v : values) {
                    str.append(v).append(",");
                    if (i == 9 || i == values.size() - 1) {
                        if ("rt".equals(key)) {
                            rts.add(str.toString());
                        } else if ("stpid".equals(key)) {
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
                final InputStream xmlResult = ctaConnect.connect(BUS_ARRIVALS, para);
                busArrivals.addAll(xmlParser.parseBusArrivals(xmlResult));
            }
        } catch (final ConnectException | ParserException e) {
            trackerBusException = e;
        }
    }

    private void loadBikes() {
        try {
            final DivvyConnect divvyConnect = DivvyConnect.getInstance();
            final InputStream bikeContent = divvyConnect.connect();
            bikeStations = jsonParser.parseStations(bikeContent);
            Collections.sort(bikeStations, Util.BIKE_COMPARATOR_NAME);
        } catch (final ParserException | ConnectException e) {
            trackerBikeException = e;
        }
    }

    @Override
    protected final void onProgressUpdate(final Void... progress) {

    }

    @Override
    protected final void onPostExecute(final Boolean success) {
        try {
            if (success || !networkAvailable) {
                clazz.getMethod("reloadData", SparseArray.class, List.class, List.class, Boolean.class).invoke(instance, trainArrivals, busArrivals, bikeStations, networkAvailable);
            } else {
                final TrackerException ex = trackerBusException == null ? (trackerBikeException == null ? trackerException : trackerBikeException) : trackerBusException;
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
