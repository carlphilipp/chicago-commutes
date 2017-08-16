package fr.cph.chicago.core.listener

import android.content.Intent
import android.os.Bundle
import android.view.View

import fr.cph.chicago.R
import fr.cph.chicago.core.activity.BikeStationActivity
import fr.cph.chicago.entity.BikeStation

class BikeStationOnClickListener(private val station: BikeStation) : View.OnClickListener {

    override fun onClick(view: View) {
        val intent = Intent(view.context, BikeStationActivity::class.java)
        val extras = Bundle()
        extras.putParcelable(view.context.getString(R.string.bundle_bike_station), station)
        intent.putExtras(extras)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        view.context.startActivity(intent)
    }
}
