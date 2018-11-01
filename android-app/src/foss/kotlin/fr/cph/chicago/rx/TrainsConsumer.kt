package fr.cph.chicago.rx

import android.view.LayoutInflater
import android.view.View
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import com.mapbox.geojson.Feature
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.map.TrainMapActivity
import fr.cph.chicago.core.adapter.TrainMapSnippetAdapter
import fr.cph.chicago.core.model.TrainEta
import fr.cph.chicago.util.Util
import io.reactivex.functions.Consumer
import java.lang.ref.WeakReference

class TrainsConsumer(trainMapActivity: TrainMapActivity, private val feature: Feature, private val runNumber: String) : Consumer<List<TrainEta>> {

    val activity: WeakReference<TrainMapActivity> = WeakReference(trainMapActivity)

    override fun accept(trains: List<TrainEta>) {
        val view = createView(feature)
        val arrivals: ListView = view.findViewById(R.id.arrivals)
        val error: TextView = view.findViewById(R.id.error)

        if (trains.isNotEmpty()) {
            val container: RelativeLayout = view.findViewById(R.id.container)

            val params = container.layoutParams
            params.width = Util.convertDpToPixel(200)
            params.height = (Util.convertDpToPixel(21) * trains.size) + Util.convertDpToPixel(30)
            container.layoutParams = params

            val ada = TrainMapSnippetAdapter(trains)
            arrivals.adapter = ada
            arrivals.visibility = ListView.VISIBLE
            error.visibility = TextView.GONE
        } else {
            arrivals.visibility = ListView.GONE
            error.visibility = TextView.VISIBLE
        }
        activity.get()?.update(feature, runNumber, view)
    }

    private fun createView(feature: Feature): View {
        val inflater = LayoutInflater.from(activity.get())
        val view = inflater.inflate(R.layout.marker_mapbox, null)
        val destination = feature.getStringProperty(TrainMapActivity.PROPERTY_DESTINATION)
        val title = view.findViewById(R.id.title) as TextView
        title.text = destination
        return view
    }
}
