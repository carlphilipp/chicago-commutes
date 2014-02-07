/**
 * Copyright 2014 Carl-Philipp Harmant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.widget.FrameLayout;
import fr.cph.chicago.activity.ErrorActivity;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.exception.TrackerException;

public class ChicagoTracker extends Application {

	private static Context context;
	private static Date lastTrainUpdate = null;
	public static final String PREFERENCE_FAVORITES = "ChicagoTrackerFavorites";
	public static final String PREFERENCE_FAVORITES_TRAIN = "ChicagoTrackerFavoritesTrain";
	public static final String PREFERENCE_FAVORITES_BUS = "ChicagoTrackerFavoritesBus";
	private static SparseArray<TrainArrival> trainArrivals;
	private static List<BusArrival> busArrivals;

	public static FrameLayout container;

	public final void onCreate() {
		super.onCreate();
		ChicagoTracker.context = getApplicationContext();
	}

	public static Context getAppContext() {
		return ChicagoTracker.context;
	}

	public static void modifyLastUpdate(final Date date) {
		lastTrainUpdate = date;
	}

	public static Date getLastTrainUpdate() {
		return lastTrainUpdate;
	}

	public static void displayError(Activity activity, TrackerException ex) {
		Intent intent = new Intent(activity, ErrorActivity.class);
		Bundle extras = new Bundle();
		extras.putString("error", ex.getMessage());
		intent.putExtras(extras);
		activity.finish();
		activity.startActivity(intent);
	}

	public static SparseArray<TrainArrival> getTrainArrivals() {
		return trainArrivals;
	}

	public static void setTrainArrivals(SparseArray<TrainArrival> trainArrivals) {
		ChicagoTracker.trainArrivals = trainArrivals;
	}

	public static List<BusArrival> getBusArrivals() {
		return busArrivals;
	}

	public static void setBusArrivals(List<BusArrival> busArrivals) {
		ChicagoTracker.busArrivals = busArrivals;
	}
}
