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

package fr.cph.chicago.listener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import fr.cph.chicago.App;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.StationActivity;

public class TrainStationOnClickListener implements View.OnClickListener {

    final private Activity activity;
    final private int stationId;

    public TrainStationOnClickListener(final Activity activity, final int stationId) {
        this.activity = activity;
        this.stationId = stationId;
    }

    @Override
    public void onClick(final View v) {
        // Start station activity
        final Bundle extras = new Bundle();
        final Intent intent = new Intent(App.getContext(), StationActivity.class);
        extras.putInt(activity.getString(R.string.bundle_train_stationId), stationId);
        intent.putExtras(extras);
        activity.startActivity(intent);
    }
}
