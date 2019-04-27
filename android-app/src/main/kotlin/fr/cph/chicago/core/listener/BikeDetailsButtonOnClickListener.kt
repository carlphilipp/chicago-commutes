package fr.cph.chicago.core.listener

import android.content.Intent
import android.os.Bundle
import android.view.View
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.station.BikeStationActivity
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.util.Util

class BikeDetailsButtonOnClickListener(private val bikeStation: BikeStation) : View.OnClickListener {

    override fun onClick(view: View?) {
        if (bikeStation.latitude != 0.0 && bikeStation.longitude != 0.0) {
            val intent = Intent(App.instance.baseContext, BikeStationActivity::class.java)
            val extras = Bundle()
            extras.putParcelable(App.instance.getString(R.string.bundle_bike_station), bikeStation)
            intent.putExtras(extras)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            App.instance.baseContext.startActivity(intent)
        } else {
            util.showNetworkErrorMessage(view!!)
        }
    }

    companion object {
        private val util = Util
    }
}
