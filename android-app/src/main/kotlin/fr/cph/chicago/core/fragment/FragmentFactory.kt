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
