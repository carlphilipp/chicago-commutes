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
import fr.cph.chicago.R;
import fr.cph.chicago.activity.BaseActivity;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.util.Util;

/**
 * Load Bus and train data into DataHolder. The data are load in a sequence mode. It means that if one of
 * the url contacted does not response, we will still process the other data, and won't throw any
 * exception
 *
 * @author Carl-Philipp Harmant
 */
public class LoadLocalDataTask extends AsyncTask<Void, String, Void> {

    private BaseActivity activity;
    private BusData busData;
    private TrainData trainData;

    public LoadLocalDataTask(@NonNull final BaseActivity activity) {
        this.activity = activity;
    }

    @Override
    protected final Void doInBackground(final Void... params) {
        // Load local CSV
        trainData = TrainData.getInstance();
        trainData.read();

        busData = BusData.getInstance();
        busData.readBusStops();
        return null;
    }

    @Override
    protected final void onPostExecute(final Void result) {
        // Put data into data holder
        final DataHolder dataHolder = DataHolder.getInstance();
        dataHolder.setBusData(busData);
        dataHolder.setTrainData(trainData);
        // Load favorites data
        Util.loadTrainFavorites(activity, BaseActivity.class);
        trackWithGoogleAnalytics();
    }

    private void trackWithGoogleAnalytics() {
        Util.trackAction(activity, R.string.analytics_category_req, R.string.analytics_action_get_train, R.string.analytics_action_get_train_arrivals, 0);
        Util.trackAction(activity, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.analytics_action_get_bus_arrival, 0);
    }
}
