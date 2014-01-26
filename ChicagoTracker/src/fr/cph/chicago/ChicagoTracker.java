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

import android.app.Application;
import android.content.Context;

public class ChicagoTracker extends Application {

	private static Context context;
	private static Date lastTrainUpdate = null;
	public static final String PREFERENCE_FAVORITES = "ChicagoTrackerFavorites";
	public static final String PREFERENCE_FAVORITES_TRAIN = "ChicagoTrackerFavoritesTrain";
	public static final String PREFERENCE_FAVORITES_BUS = "ChicagoTrackerFavoritesBus";

	public void onCreate() {
		super.onCreate();
		ChicagoTracker.context = getApplicationContext();
	}

	public static Context getAppContext() {
		return ChicagoTracker.context;
	}

	public static void modifyLastUpdate(Date date) {
		lastTrainUpdate = date;
	}

	public static Date getLastTrainUpdate() {
		return lastTrainUpdate;
	}

}
