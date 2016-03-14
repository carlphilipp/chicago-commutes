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
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.SparseArray;

import org.apache.commons.collections4.MultiValuedMap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.StationActivity;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.XmlParser;

import static fr.cph.chicago.connection.CtaRequestType.TRAIN_ARRIVALS;

/// TODO optimize onPostExecute method and do not import useless things like  SwipeRefreshLayout
public class LoadTrainArrivalDataTask extends AsyncTask<MultiValuedMap<String, String>, Void, TrainArrival> {

    private static final String TAG = LoadTrainArrivalDataTask.class.getSimpleName();

    private TrainData trainData;
    private StationActivity activity;
    private TrackerException trackerException;
    private SwipeRefreshLayout swipeRefreshLayout;

    public LoadTrainArrivalDataTask(final StationActivity activity, final TrainData trainData, final SwipeRefreshLayout swipeRefreshLayout) {
        this.activity = activity;
        this.trainData = trainData;
        this.swipeRefreshLayout = swipeRefreshLayout;
    }

    @SafeVarargs
    @Override
    protected final TrainArrival doInBackground(final MultiValuedMap<String, String>... params) {
        // Get menu item and put it to loading mod
        publishProgress((Void[]) null);
        SparseArray<TrainArrival> arrivals = new SparseArray<>();
        final CtaConnect connect = CtaConnect.getInstance();
        try {
            final XmlParser xml = XmlParser.getInstance();
            final InputStream xmlResult = connect.connect(TRAIN_ARRIVALS, params[0]);
            //String xmlResult = connectTest();
            arrivals = xml.parseArrivals(xmlResult, trainData);
            // Apply filters
            int index = 0;
            while (index < arrivals.size()) {
                final TrainArrival arri = arrivals.valueAt(index++);
                final List<Eta> etas = arri.getEtas();
                // Sort Eta by arriving time
                Collections.sort(etas);
                // Copy data into new list to be able to avoid looping on a list that we want to modify
                final List<Eta> etasCopy = new ArrayList<>();
                etasCopy.addAll(etas);
                int j = 0;
                for (int i = 0; i < etasCopy.size(); i++) {
                    final Eta eta = etasCopy.get(i);
                    final Station station = eta.getStation();
                    final TrainLine line = eta.getRouteName();
                    final TrainDirection direction = eta.getStop().getDirection();
                    final boolean toRemove = Preferences.getTrainFilter(station.getId(), line, direction);
                    if (!toRemove) {
                        etas.remove(i - j++);
                    }
                }
            }
        } catch (final ParserException | ConnectException e) {
            Log.e(TAG, e.getMessage(), e);
            trackerException = e;
        }
        Util.trackAction(activity, R.string.analytics_category_req, R.string.analytics_action_get_train, R.string.analytics_action_get_train_arrivals, 0);
        if (arrivals.size() == 1) {
            final String id = ((List<String>) params[0].get(activity.getString(R.string.request_map_id))).get(0);
            return arrivals.get(Integer.valueOf(id));
        } else {
            return null;
        }
    }

    @Override
    protected final void onProgressUpdate(final Void... values) {
    }

    @Override
    protected final void onPostExecute(@Nullable final TrainArrival trainArrival) {
        if (trackerException == null) {
            List<Eta> etas;
            if (trainArrival != null) {
                etas = trainArrival.getEtas();
            } else {
                etas = Collections.emptyList();
            }
            activity.hideAllArrivalViews();
            for (final Eta eta : etas) {
                activity.drawAllArrivalsTrain(eta);
            }
        } else {
            ChicagoTracker.displayError(activity, trackerException);
        }
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
