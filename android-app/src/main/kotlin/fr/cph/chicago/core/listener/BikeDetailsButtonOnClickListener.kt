package fr.cph.chicago.core.listener

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.station.BikeStationActivity
import fr.cph.chicago.core.model.BikeStation

class BikeDetailsButtonOnClickListener(private val activity: Activity, private val divvyStation: BikeStation) : NetworkCheckListener(activity) {

    override fun onClick() {
        if (divvyStation.latitude != 0.0 && divvyStation.longitude != 0.0) {
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
