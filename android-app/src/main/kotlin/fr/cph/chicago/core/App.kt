/**
 * Copyright 2021 Carl-Philipp Harmant
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
import android.graphics.Rect
import android.os.Build
import android.view.WindowManager
import com.google.android.material.color.DynamicColors
import fr.cph.chicago.core.activity.ErrorActivity
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import kotlinx.coroutines.CoroutineExceptionHandler
import timber.log.Timber
import timber.log.Timber.DebugTree

/**
 * Main class that extends Application. Mainly used to get the context from anywhere in the app.
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class App : Application() {

    companion object {
        lateinit var instance: App

        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            Timber.e(exception, "Unexpected exception caught in coroutine")
            startErrorActivity()
        }

        fun startErrorActivity() {
            val context = instance.applicationContext
            val intent = Intent(context, ErrorActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    val lineWidthGoogleMap: Float by lazy {
        if (screenWidth > 1080) 7f else if (screenWidth > 480) 4f else 2f
    }

    override fun onCreate() {
        instance = this
        // Enable dynamic color if available
        DynamicColors.applyToActivitiesIfAvailable(this);
        // Setup Timber
        Timber.plant(DebugTree())
        // Setup RxJava default error handling
        RxJavaPlugins.setErrorHandler { throwable ->
            Timber.e(throwable, "RxError not handled")
            startErrorActivity()
        }

        super.onCreate()
    }

    private val screenWidth: Int by lazy {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                val windowManager: WindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val rect: Rect = windowManager.currentWindowMetrics.bounds
                rect.width()
            }
            else -> {
                val wm = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = wm.defaultDisplay
                val point = Point()
                display.getSize(point)
                point.x
            }
        }
    }
}
