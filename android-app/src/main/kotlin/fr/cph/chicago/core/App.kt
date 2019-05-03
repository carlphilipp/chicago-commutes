/**
 * Copyright 2019 Carl-Philipp Harmant
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

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.view.WindowManager
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.ErrorActivity
import fr.cph.chicago.redux.ApiKeysAction
import fr.cph.chicago.redux.store
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber
import timber.log.Timber.DebugTree


/**
 * Main class that extends Application. Mainly used to get the context from anywhere in the app.
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class App : Application() {

    var refresh: Boolean = false

    val screenWidth: Int by lazy {
        screenSize[0]
    }

    val lineWidthGoogleMap: Float by lazy {
        if (screenWidth > 1080) 7f else if (screenWidth > 480) 4f else 2f
    }

    val lineWidthMapBox: Float by lazy {
        if (screenWidth > 1080) 2f else if (screenWidth > 480) 1f else 2f
    }

    val streetViewPlaceHolder: Drawable by lazy {
        resources.getDrawable(R.drawable.placeholder_street_view, this.theme)
    }

    override fun onCreate() {
        super.onCreate()

        Timber.plant(DebugTree())
        instance = this

        RxJavaPlugins.setErrorHandler { throwable ->
            Timber.e(throwable, "RxError not handled")
            startErrorActivity()
        }

        val action = ApiKeysAction(
            ctaTrainKey = applicationContext.getString(R.string.cta_train_key),
            ctaBusKey = applicationContext.getString(R.string.cta_bus_key),
            googleStreetKey = applicationContext.getString(R.string.google_maps_api_key))
        store.dispatch(action)
    }

    private val screenSize: IntArray by lazy {
        val wm = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val point = Point()
        display.getSize(point)
        intArrayOf(point.x, point.y)
    }

    companion object {
        lateinit var instance: App

        private fun startErrorActivity() {
            val context = instance.applicationContext
            val intent = Intent(context, ErrorActivity::class.java)
            intent.putExtra(context.getString(R.string.bundle_error), "Something went wrong")
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
