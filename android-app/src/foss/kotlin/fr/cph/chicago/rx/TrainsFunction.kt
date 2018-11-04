package fr.cph.chicago.rx

import android.view.View
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import com.mapbox.geojson.Feature
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.map.TrainMapActivity
import fr.cph.chicago.core.adapter.TrainMapSnippetAdapter
import fr.cph.chicago.core.model.TrainEta
import io.reactivex.functions.Function
import java.lang.ref.WeakReference

class TrainsFunction(trainMapActivity: TrainMapActivity, private val feature: Feature) : Function<List<TrainEta>, View>, AConsumer() {

    val activity: WeakReference<TrainMapActivity> = WeakReference(trainMapActivity)

    override fun apply(trains: List<TrainEta>): View {
        val view = createView(feature, activity)
        val arrivals: ListView = view.findViewById(R.id.arrivals)
        val error: TextView = view.findViewById(R.id.error)

        if (trains.isNotEmpty()) {
            val container: RelativeLayout = view.findViewById(R.id.container)
            addParams(container, trains.size)

            val ada = TrainMapSnippetAdapter(trains)
            arrivals.adapter = ada
            arrivals.visibility = ListView.VISIBLE
            error.visibility = TextView.GONE
        } else {
            arrivals.visibility = ListView.GONE
            error.visibility = TextView.VISIBLE
        }
        return view
    }
}
