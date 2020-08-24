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

package fr.cph.chicago.core.fragment

import androidx.fragment.app.Fragment
import fr.cph.chicago.R

fun buildFragment(fragmentId: Int): Fragment {
    return when (fragmentId) {
        R.id.navigation_train -> TrainFragment.newInstance(R.id.navigation_train)
        R.id.navigation_bus -> BusFragment.newInstance(R.id.navigation_bus)
        R.id.navigation_bike -> BikeFragment.newInstance(R.id.navigation_bike)
        R.id.navigation_nearby -> NearbyFragment.newInstance(R.id.navigation_nearby)
        R.id.navigation_cta_map -> CtaMapFragment.newInstance(R.id.navigation_cta_map)
        R.id.navigation_alert_cta -> AlertFragment.newInstance(R.id.navigation_alert_cta)
        R.id.navigation_settings -> SettingsFragment.newInstance(R.id.navigation_settings)
        else -> FavoritesFragment.newInstance(R.id.navigation_favorites)
    }
}
