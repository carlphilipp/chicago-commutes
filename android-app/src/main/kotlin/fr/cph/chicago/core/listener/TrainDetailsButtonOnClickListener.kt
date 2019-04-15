package fr.cph.chicago.core.listener

import android.content.Intent
import android.os.Bundle
import android.view.View
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.station.TrainStationActivity

class TrainDetailsButtonOnClickListener(private val stationId: Int) : View.OnClickListener {

    override fun onClick(v: View?) {
        // Start train station activity
        val extras = Bundle()
        val intent = Intent(App.instance.applicationContext, TrainStationActivity::class.java)
        extras.putInt(App.instance.getString(R.string.bundle_train_stationId), stationId)
        intent.putExtras(extras)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        App.instance.startActivity(intent)
    }
}
