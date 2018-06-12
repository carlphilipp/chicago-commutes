package fr.cph.chicago.core.listener

import android.content.Intent
import android.os.Bundle
import android.view.View
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.BusMapActivity
import fr.cph.chicago.core.activity.MainActivity
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.util.Util

class BusMapButtonOnClickListener(
    private val activity: MainActivity,
    private val busRoute: BusRoute,
    private val bounds: Set<String>) : View.OnClickListener {

    private val util = Util

    override fun onClick(v: View) {
        if (!util.isNetworkAvailable()) {
            util.showNetworkErrorMessage(activity)
        } else {
            val intent = Intent(App.instance.applicationContext, BusMapActivity::class.java)
            val extras = Bundle()
            extras.putString(App.instance.getString(R.string.bundle_bus_route_id), busRoute.id)
            extras.putStringArray(App.instance.getString(R.string.bundle_bus_bounds), bounds.toTypedArray())
            intent.putExtras(extras)
            activity.startActivity(intent)
        }
    }
}
