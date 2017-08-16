/**
 * Copyright 2017 Carl-Philipp Harmant
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

package fr.cph.chicago.core.listener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import fr.cph.chicago.R;
import fr.cph.chicago.core.App;
import fr.cph.chicago.core.activity.StationActivity;
import fr.cph.chicago.core.activity.TrainMapActivity;
import fr.cph.chicago.core.adapter.PopupTrainAdapter;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.util.Util;

/**
 * FavoritesData train on click listener
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class TrainOnClickListener implements OnClickListener {

    private final Context context;
    /**
     * The station id
     **/
    private final int stationId;
    /**
     * Train lines
     **/
    private final Set<TrainLine> trainLines;

    public TrainOnClickListener(final Context context, final int stationId, final Set<TrainLine> trainLines) {
        this.context = context;
        this.stationId = stationId;
        this.trainLines = trainLines;
    }

    @Override
    public void onClick(final View view) {
        if (!Util.INSTANCE.isNetworkAvailable(view.getContext())) {
            Util.INSTANCE.showNetworkErrorMessage(view);
        } else {
            final List<String> values = new ArrayList<>();
            final List<Integer> colors = new ArrayList<>();
            values.add(view.getContext().getString(R.string.message_open_details));
            for (final TrainLine line : trainLines) {
                values.add(line.toString() + " line - See trains");
                if (line != TrainLine.YELLOW) {
                    colors.add(line.getColor());
                } else {
                    colors.add(ContextCompat.getColor(view.getContext(), R.color.yellowLine));
                }
            }
            final PopupTrainAdapter ada = new PopupTrainAdapter(view.getContext(), values, colors);

            final List<TrainLine> lines = new ArrayList<>();
            lines.addAll(trainLines);

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setAdapter(ada, (dialog, position) -> {
                final Bundle extras = new Bundle();
                if (position == 0) {
                    // Start station activity
                    final Intent intent = new Intent(view.getContext(), StationActivity.class);
                    extras.putInt(view.getContext().getString(R.string.bundle_train_stationId), stationId);
                    intent.putExtras(extras);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    view.getContext().startActivity(intent);
                } else {
                    // Follow all trains from given line on google map view
                    final Intent intent = new Intent(view.getContext(), TrainMapActivity.class);
                    extras.putString(view.getContext().getString(R.string.bundle_train_line), lines.get(position - 1).toTextString());
                    intent.putExtras(extras);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    view.getContext().startActivity(intent);
                }
            });

            final AlertDialog dialog = builder.create();
            dialog.show();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout((int) (App.Companion.getScreenWidth() * 0.7), ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
    }
}
