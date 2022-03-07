package fr.cph.chicago.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat.startActivity
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.BikeStationComposable
import fr.cph.chicago.core.activity.BusStationActivity
import fr.cph.chicago.core.activity.TrainStationActivity
import fr.cph.chicago.core.activity.BusMapActivity
import fr.cph.chicago.core.activity.TrainMapActivity
import fr.cph.chicago.core.activity.settings.DisplayActivity
import fr.cph.chicago.core.activity.settings.ThemeChooserActivity
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusDirections
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.dto.BusDetailsDTO
import fr.cph.chicago.core.model.enumeration.TrainLine

// Trains
fun startTrainStationActivity(context: Context, trainStation: TrainStation) {
    val extras = Bundle()
    val intent = Intent(context, TrainStationActivity::class.java)
    extras.putString(context.getString(R.string.bundle_train_stationId), trainStation.id)
    intent.putExtras(extras)
    startActivity(context, intent)
}

fun startTrainMapActivity(context: Context, trainLine: TrainLine) {
    val extras = Bundle()
    val intent = Intent(context, TrainMapActivity::class.java)
    extras.putString(context.getString(R.string.bundle_train_line), trainLine.toTextString())
    intent.putExtras(extras)
    startActivity(context, intent)
}

// Buses
fun startBusDetailActivity(context: Context, busDetailsDTO: BusDetailsDTO) {
    val intent = Intent(context, BusStationActivity::class.java)
    val extras = with(Bundle()) {
        putString(context.getString(R.string.bundle_bus_stop_id), busDetailsDTO.stopId.toString())
        putString(context.getString(R.string.bundle_bus_route_id), busDetailsDTO.busRouteId)
        putString(context.getString(R.string.bundle_bus_route_name), busDetailsDTO.routeName)
        putString(context.getString(R.string.bundle_bus_bound), busDetailsDTO.bound)
        putString(context.getString(R.string.bundle_bus_bound_title), busDetailsDTO.boundTitle)
        putString(context.getString(R.string.bundle_bus_stop_name), busDetailsDTO.stopName)
        this
    }

    intent.putExtras(extras)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(context, intent)
}

fun startBusMapActivity(context: Context, busDirections: BusDirections) {
    startBusMapActivity(
        context = context,
        busRouteId = busDirections.id,
    )
}

fun startBusMapActivity(context: Context, busRouteId: String) {
    val extras = Bundle()
    val intent = Intent(context, BusMapActivity::class.java)
    extras.putString(context.getString(R.string.bundle_bus_route_id), busRouteId)
    intent.putExtras(extras)
    startActivity(context, intent)
}

// Bikes
fun startBikeStationActivity(context: Context, bikeStation: BikeStation) {
    val intent = Intent(context, BikeStationComposable::class.java)
    val extras = Bundle()
    extras.putParcelable(context.getString(R.string.bundle_bike_station), bikeStation)
    intent.putExtras(extras)
    startActivity(context, intent)
}

// Settings
fun startSettingsActivity(context: Context, clazz: Class<*>) {
    val intent = Intent(context, clazz)
    startActivity(context, intent)
}


private fun startActivity(context: Context, intent: Intent) {
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(context, intent, null)
}
