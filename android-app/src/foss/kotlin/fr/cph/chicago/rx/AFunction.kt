/**
 * Copyright 2020 Carl-Philipp Harmant
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

package fr.cph.chicago.rx

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.mapbox.geojson.Feature
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.map.PROPERTY_DESTINATION
import fr.cph.chicago.util.Util
import java.lang.ref.WeakReference

abstract class AFunction {

    companion object {
        private val util = Util
    }

    protected fun addParams(container: RelativeLayout, size: Int) {
        val params = container.layoutParams
        params.width = util.convertDpToPixel(200)
        params.height = (util.convertDpToPixel(21) * size) + util.convertDpToPixel(30)
        container.layoutParams = params
    }

    protected fun createView(feature: Feature, activity: WeakReference<out Activity>): View {
        val inflater = LayoutInflater.from(activity.get())
        val view = inflater.inflate(R.layout.marker_mapbox, null) // FIXME: Should not pass null
        val destination = feature.getStringProperty(PROPERTY_DESTINATION)
        val title = view.findViewById<TextView>(R.id.title)
        title.text = destination
        return view
    }
}
