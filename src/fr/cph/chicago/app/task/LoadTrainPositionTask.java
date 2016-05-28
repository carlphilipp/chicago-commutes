/**
 * Copyright 2016 Carl-Philipp Harmant
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.app.task;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.InputStream;
import java.util.List;

import fr.cph.chicago.R;
import fr.cph.chicago.app.activity.TrainMapActivity;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.Train;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.parser.XmlParser;

import static fr.cph.chicago.connection.CtaRequestType.TRAIN_LOCATION;

public class LoadTrainPositionTask extends AsyncTask<Boolean, Void, List<Train>> {

    private static final String TAG = LoadTrainPositionTask.class.getSimpleName();

    private final TrainMapActivity activity;
    private final String line;
    private TrainData trainData;

    private boolean centerMap;
    private List<Position> positions;

    public LoadTrainPositionTask(@NonNull final TrainMapActivity activity, @NonNull final String line, @NonNull final TrainData trainData) {
        this.activity = activity;
        this.line = line;
        this.trainData = trainData;
    }

    @Override
    protected List<Train> doInBackground(Boolean... params) {
        centerMap = params[0];
        List<Train> trains = null;
        final CtaConnect connect = CtaConnect.getInstance();
        final MultiValuedMap<String, String> connectParam = new ArrayListValuedHashMap<>();
        connectParam.put(activity.getString(R.string.request_rt), line);
        try {
            final InputStream content = connect.connect(TRAIN_LOCATION, connectParam);
            final XmlParser xml = XmlParser.getInstance();
            trains = xml.parseTrainsLocation(content);
        } catch (final ConnectException | ParserException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Util.trackAction(activity, R.string.analytics_category_req, R.string.analytics_action_get_train, R.string.url_train_location, 0);
        if (trainData == null) {
            final DataHolder dataHolder = DataHolder.getInstance();
            trainData = dataHolder.getTrainData();
        }
        positions = trainData.readPattern(TrainLine.fromXmlString(line));
        return trains;
    }

    @Override
    protected final void onPostExecute(final List<Train> trains) {
        if (trains != null) {
            activity.drawTrains(trains);
            activity.drawLine(positions);
            if (trains.size() != 0) {
                if (centerMap) {
                    activity.centerMapOnTrain(trains);
                }
            } else {
                Util.showMessage(activity, R.string.message_no_train_found);
            }
        } else {
            Util.showMessage(activity, R.string.message_error_while_loading_data);
        }
    }
}
