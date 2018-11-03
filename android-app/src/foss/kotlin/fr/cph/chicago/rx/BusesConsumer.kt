package fr.cph.chicago.rx

import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import com.mapbox.geojson.Feature
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.map.BusMapActivity
import fr.cph.chicago.core.adapter.BusMapSnippetAdapter
import fr.cph.chicago.core.model.BusArrival
import io.reactivex.functions.Consumer
import org.apache.commons.lang3.StringUtils
import java.lang.ref.WeakReference
import java.util.Date

class BusesConsumer(busMapActivity: BusMapActivity, private val feature: Feature, private val loadAll: Boolean, private val runNumber: String) : Consumer<List<BusArrival>>, AConsumer() {

    val activity: WeakReference<BusMapActivity> = WeakReference(busMapActivity)

    override fun accept(busArrivalsRes: List<BusArrival>) {
        val view = createView(feature, activity)
        val arrivals: ListView = view.findViewById(R.id.arrivals)
        val error: TextView = view.findViewById(R.id.error)

        var busArrivals = busArrivalsRes.toMutableList()
        if (!loadAll && busArrivals.size > 7) {
            busArrivals = busArrivals.subList(0, 6)
            val busArrival = BusArrival(Date(), "added bus", view.context.getString(R.string.bus_all_results), 0, 0, "", "", StringUtils.EMPTY, Date(), false)
            busArrivals.add(busArrival)
        }
        if (busArrivals.isNotEmpty()) {
            val container: RelativeLayout = view.findViewById(R.id.container)
            addParams(container, busArrivals.size)

            val ada = BusMapSnippetAdapter(busArrivals)
            arrivals.adapter = ada
            arrivals.visibility = ListView.VISIBLE
            error.visibility = TextView.GONE
        } else {
            arrivals.visibility = ListView.GONE
            error.visibility = TextView.VISIBLE
        }
        activity.get()?.update(feature, runNumber, view)
    }
}
