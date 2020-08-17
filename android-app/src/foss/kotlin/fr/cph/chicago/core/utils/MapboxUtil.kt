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

package fr.cph.chicago.core.utils

import android.content.res.Configuration
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style

fun setupMapbox(mapboxMap: MapboxMap, configuration: Configuration): MapboxMap {
    return with(mapboxMap) {
        uiSettings.isLogoEnabled = false
        uiSettings.isAttributionEnabled = false
        uiSettings.isRotateGesturesEnabled = false
        uiSettings.isTiltGesturesEnabled = false
        setStyle(getCurrentStyle(configuration))
        this
    }
}

fun getCurrentStyle(configuration: Configuration): String {
    return when (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_NO -> Style.LIGHT
        Configuration.UI_MODE_NIGHT_YES -> Style.DARK
        else -> Style.LIGHT
    }
}
