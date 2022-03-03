package fr.cph.chicago.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat.startActivity
import fr.cph.chicago.R
import fr.cph.chicago.core.composable.BikeStationComposable
import fr.cph.chicago.core.composable.BusStationComposable
import fr.cph.chicago.core.composable.TrainStationComposable
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.dto.BusDetailsDTO

fun startTrainStationActivity(context: Context, trainStation: TrainStation) {
    val extras = Bundle()
    val intent = Intent(context, TrainStationComposable::class.java)
    extras.putString(context.getString(R.string.bundle_train_stationId), trainStation.id)
    intent.putExtras(extras)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(context, intent, null)
}

fun startBusDetailActivity(context: Context, busDetailsDTO: BusDetailsDTO) {
    val intent = Intent(context, BusStationComposable::class.java)
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
    startActivity(context, intent, null)
}

fun startBikeStationActivity(context: Context, bikeStation: BikeStation) {
    val intent = Intent(context, BikeStationComposable::class.java)
    val extras = Bundle()
    extras.putParcelable(context.getString(R.string.bundle_bike_station), bikeStation)
    intent.putExtras(extras)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(context, intent, null)
}
