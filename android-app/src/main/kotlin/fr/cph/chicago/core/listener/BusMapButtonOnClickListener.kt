package fr.cph.chicago.core.listener

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.map.BusMapActivity
import fr.cph.chicago.core.model.BusRoute

class BusMapButtonOnClickListener(
    private val context: Context,
    private val busRoute: BusRoute,
    private val bounds: Set<String>) : View.OnClickListener {

    override fun onClick(v: View?) {
        val intent = Intent(context, BusMapActivity::class.java)
        val extras = Bundle()
        extras.putString(context.getString(R.string.bundle_bus_route_id), busRoute.id)
        extras.putStringArray(context.getString(R.string.bundle_bus_bounds), bounds.toTypedArray())
        intent.putExtras(extras)
        context.startActivity(intent)
    }
}
