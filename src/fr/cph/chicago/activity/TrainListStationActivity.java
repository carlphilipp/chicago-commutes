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

package fr.cph.chicago.activity;

import android.app.ListActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import fr.cph.chicago.R;
import fr.cph.chicago.adapter.TrainAdapter;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.util.Util;

/**
 * Activity the list of train stations
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class TrainListStationActivity extends ListActivity {

    private TrainLine trainLine;
    private String lineParam;

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!this.isFinishing()) {
            // Load data
            if (savedInstanceState != null) {
                lineParam = savedInstanceState.getString(getString(R.string.bundle_train_line));
            } else {
                lineParam = getIntent().getExtras().getString(getString(R.string.bundle_train_line));
            }
            trainLine = TrainLine.fromString(lineParam);
            setTitle(trainLine.toStringWithLine());

            setContentView(R.layout.activity_train_station);

            final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            Util.setWindowsColor(this, toolbar, trainLine);
            toolbar.setTitle(trainLine.toStringWithLine());

            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
            toolbar.setOnClickListener(v -> finish());

            final TrainAdapter ada = new TrainAdapter(trainLine, this);
            setListAdapter(ada);
        }
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        lineParam = savedInstanceState.getString(getString(R.string.bundle_train_line));
        trainLine = TrainLine.fromString(lineParam);
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putString(getString(R.string.bundle_train_line), lineParam);
        super.onSaveInstanceState(savedInstanceState);
    }
}
