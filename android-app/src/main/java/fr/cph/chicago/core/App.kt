/**
 * Copyright 2017 Carl-Philipp Harmant
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.core

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.BaseActivity
import fr.cph.chicago.data.DataHolder
import fr.cph.chicago.util.Util
import java.util.*

/**
 * Main class that extends Application. Mainly used to get the context from anywhere in the app.
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class App : Application() {
    companion object {

        /**
         * Last update of favorites
         */
        var lastUpdate: Date? = null
        private var tracker: Tracker? = null


        var screenWidth: Int = 0
        var lineWidth: Float = 0.toFloat()
        var ctaTrainKey: String? = null
        var ctaBusKey: String? = null
        var googleStreetKey: String? = null

        fun checkTrainData(activity: Activity): Boolean {
            if (DataHolder.trainData == null) {
                startErrorActivity(activity)
                return false
            }
            return true
        }

        fun checkBusData(activity: Activity) {
            if (DataHolder.busData == null) {
                startErrorActivity(activity)
            }
        }

        private fun startErrorActivity(activity: Activity) {
            val intent = Intent(activity, BaseActivity::class.java)
            intent.putExtra(activity.getString(R.string.bundle_error), true)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(intent)
            activity.finish()
        }

        fun getTracker(context: Context): Tracker {
            if (tracker == null) {
                val analytics = GoogleAnalytics.getInstance(context)
                val key = context.getString(R.string.google_analytics_key)
                tracker = analytics.newTracker(key)
                tracker!!.enableAutoActivityTracking(true)
            }
            return tracker as Tracker
        }

        fun setupContextData(context: Context) {
            val screenSize = Util.getScreenSize(context)
            screenWidth = screenSize[0]
            lineWidth = if (screenWidth > 1080) 7f else if (screenWidth > 480) 4f else 2f
            ctaTrainKey = context.getString(R.string.cta_train_key)
            ctaBusKey = context.getString(R.string.cta_bus_key)
            googleStreetKey = context.getString(R.string.google_maps_api_key)
        }
    }
}
