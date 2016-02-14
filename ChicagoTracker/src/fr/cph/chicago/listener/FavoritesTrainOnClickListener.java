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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
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
	/** The main activity **/
	private Activity mActivity;
	/** The layout that is used to display a fade black background **/
	private FrameLayout mFirstLayout;
	/** The station id **/
	private int mStationId;
	/** Train lines **/
	private Set<TrainLine> mTrainLines;

	/**
	 * @param activity
	 * @param firstLayout
	 * @param stationId
	 * @param trainLines
	 */
	public FavoritesTrainOnClickListener(final Activity activity, final FrameLayout firstLayout, final int stationId,
			final Set<TrainLine> trainLines) {
		this.mActivity = activity;
		this.mFirstLayout = firstLayout;
		this.mStationId = stationId;
		this.mTrainLines = trainLines;
	}

	@Override
	public void onClick(final View view) {
		if (!Util.isNetworkAvailable()) {
			Toast.makeText(mActivity, "No network connection detected!", Toast.LENGTH_LONG).show();
		} else {
			LayoutInflater layoutInflater = (LayoutInflater) mActivity.getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View popupView = layoutInflater.inflate(R.layout.popup_train, null);

			final int[] screenSize = Util.getScreenSize();

			final PopupWindow popup = new PopupWindow(popupView, (int) (screenSize[0] * 0.7), LayoutParams.WRAP_CONTENT);
			popup.setFocusable(true);
			popup.setBackgroundDrawable(ChicagoTracker.getAppContext().getResources().getDrawable(R.drawable.any_selector));
			mFirstLayout.getForeground().setAlpha(210);

			ListView listView = (ListView) popupView.findViewById(R.id.details);
			final List<String> values = new ArrayList<String>();
			final List<Integer> colors = new ArrayList<Integer>();
			values.add("Open details");
			for (TrainLine line : mTrainLines) {
				values.add(line.toString() + " line - All trains");
				colors.add(line.getColor());
			}
			PopupTrainAdapter ada = new PopupTrainAdapter(mActivity, values, colors);
			listView.setAdapter(ada);
			final List<TrainLine> lines = new ArrayList<TrainLine>();
			lines.addAll(mTrainLines);

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if (position == 0) {
						Intent intent = new Intent(ChicagoTracker.getAppContext(), StationActivity.class);
						Bundle extras = new Bundle();
						extras.putInt("stationId", mStationId);
						intent.putExtras(extras);
						mActivity.startActivity(intent);
						mActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
						popup.dismiss();
					} else {
						Intent intent = new Intent(ChicagoTracker.getAppContext(), TrainMapActivity.class);
						Bundle extras = new Bundle();
						extras.putString("line", lines.get(position - 1).toTextString());
						intent.putExtras(extras);
						mActivity.startActivity(intent);
						mActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
						popup.dismiss();
					}
				}
			});
			popup.setFocusable(true);
			popup.setBackgroundDrawable(ChicagoTracker.getAppContext().getResources().getDrawable(R.drawable.any_selector));
			mFirstLayout.getForeground().setAlpha(210);
			popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
				@Override
				public void onDismiss() {
					mFirstLayout.getForeground().setAlpha(0);
				}
			});
			popup.showAtLocation(mFirstLayout, Gravity.CENTER, 0, 0);
		}
	}
}
