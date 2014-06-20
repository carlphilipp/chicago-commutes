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

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import fr.cph.chicago.activity.BaseActivity;
import fr.cph.chicago.activity.ErrorActivity;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.exception.TrackerException;

/**
 * 
 * Main class that extends Application. Mainly used to get the context from anywhere in the app.
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class ChicagoTracker extends Application {
	/** Preference string that is used to in shared preference of the phone **/
	public static final String PREFERENCE_FAVORITES = "ChicagoTrackerFavorites";
	/** Train preference string **/
	public static final String PREFERENCE_FAVORITES_TRAIN = "ChicagoTrackerFavoritesTrain";
	/** Bus preference string **/
	public static final String PREFERENCE_FAVORITES_BUS = "ChicagoTrackerFavoritesBus";
	/** Bike preference string **/
	public static final String PREFERENCE_FAVORITES_BIKE = "ChicagoTrackerFavoritesBike";
	/** Application context **/
	private static Context context;
	/** Last update of favorites **/
	private static Date lastUpdate;
	/** Container that is used to get a faded black background **/
	public static FrameLayout container;

	@Override
	public final void onCreate() {
		super.onCreate();
		ChicagoTracker.context = getApplicationContext();
	}

	/**
	 * Get Application context
	 * 
	 * @return the context
	 */
	public static Context getAppContext() {
		return ChicagoTracker.context;
	}

	/**
	 * Modify last update date.
	 * 
	 * @param date
	 *            the last update of favorites
	 */
	public static void modifyLastUpdate(final Date date) {
		lastUpdate = date;
	}

	/**
	 * Get last update
	 * 
	 * @return the last update
	 */
	public static Date getLastUpdate() {
		return lastUpdate;
	}

	/**
	 * Function that is used all over the application. Display error message and provide a way to
	 * retry
	 * 
	 * @param activity
	 *            that is needed to lunch a new one
	 * @param ex
	 *            the kind of exception. Used to display the error message
	 */
	public static void displayError(Activity activity, TrackerException ex) {
		Intent intent = new Intent(activity, ErrorActivity.class);
		Bundle extras = new Bundle();
		extras.putString("error", ex.getMessage());
		intent.putExtras(extras);
		activity.finish();
		activity.startActivity(intent);
	}

	public static void checkData(Activity mActivity) {
		if (DataHolder.getInstance().getBusData() == null || DataHolder.getInstance().getTrainData() == null
				|| DataHolder.getInstance().getAlertData() == null) {
			Intent intent = new Intent(mActivity, BaseActivity.class);
			intent.putExtra("error", true);
			mActivity.startActivity(intent);
			mActivity.finish();
		}
	}
}
