package fr.cph.chicago.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat.startActivity
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.BusMapActivity
import fr.cph.chicago.core.model.BusDirections

// Buses
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

// Settings
fun startSettingsActivity(context: Context, clazz: Class<*>) {
    val intent = Intent(context, clazz)
    startActivity(context, intent)
}


private fun startActivity(context: Context, intent: Intent) {
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(context, intent, null)
}
