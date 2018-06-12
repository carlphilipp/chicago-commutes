package fr.cph.chicago.core.listener

import android.content.Intent
import android.os.Bundle
import android.view.View
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.BikeStationActivity
import fr.cph.chicago.core.activity.MainActivity
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.util.Util

class BikeDetailsOnClickListener(private val activity: MainActivity, private val divvyStation: BikeStation) : View.OnClickListener {

    private val util = Util

    override fun onClick(v: View) {
        if (!util.isNetworkAvailable()) {
            util.showNetworkErrorMessage(activity)
        } else if (divvyStation.latitude != 0.0 && divvyStation.longitude != 0.0) {
            val intent = Intent(activity.applicationContext, BikeStationActivity::class.java)
            val extras = Bundle()
            extras.putParcelable(App.instance.getString(R.string.bundle_bike_station), divvyStation)
            intent.putExtras(extras)
            activity.startActivity(intent)
        } else {
            util.showMessage(activity, R.string.message_not_ready)
        }
    }
}
