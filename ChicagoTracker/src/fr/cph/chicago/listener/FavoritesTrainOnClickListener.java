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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.StationActivity;
import fr.cph.chicago.activity.TrainMapActivity;
import fr.cph.chicago.adapter.PopupTrainAdapter;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Favorites train on click listener
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class FavoritesTrainOnClickListener implements OnClickListener {
	/**
	 * The main activity
	 **/
	private Activity activity;
	/**
	 * The station id
	 **/
	private int stationId;
	/**
	 * Train lines
	 **/
	private Set<TrainLine> trainLines;

	public FavoritesTrainOnClickListener(final Activity activity, final int stationId, final Set<TrainLine> trainLines) {
		this.activity = activity;
		this.stationId = stationId;
		this.trainLines = trainLines;
	}

	@Override
	public void onClick(final View view) {
		if (!Util.isNetworkAvailable()) {
			Toast.makeText(activity, "No network connection detected!", Toast.LENGTH_LONG).show();
		} else {
			final List<String> values = new ArrayList<>();
			final List<Integer> colors = new ArrayList<>();
			values.add("Open details");
			for (final TrainLine line : trainLines) {
				values.add(line.toString() + " line - All trains");
				colors.add(line.getColor());
			}
			final PopupTrainAdapter ada = new PopupTrainAdapter(activity, values, colors);

			final List<TrainLine> lines = new ArrayList<>();
			lines.addAll(trainLines);

			final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setAdapter(ada, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(final DialogInterface dialog, final int position) {
					final Bundle extras = new Bundle();
					if (position == 0) {
						final Intent intent = new Intent(ChicagoTracker.getContext(), StationActivity.class);
						extras.putInt(activity.getResources().getString(R.string.bundle_train_stationId), stationId);
						intent.putExtras(extras);
						activity.startActivity(intent);
					} else {
						final Intent intent = new Intent(ChicagoTracker.getContext(), TrainMapActivity.class);
						extras.putString(activity.getResources().getString(R.string.bundle_train_line), lines.get(position - 1).toTextString());
						intent.putExtras(extras);
						activity.startActivity(intent);
					}
				}
			});

			final AlertDialog dialog = builder.create();
			dialog.show();
		}
	}
}
