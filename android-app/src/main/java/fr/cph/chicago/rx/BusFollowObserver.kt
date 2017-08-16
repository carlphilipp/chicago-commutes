package fr.cph.chicago.rx

import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.TextView
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.BusMapActivity
import fr.cph.chicago.core.adapter.BusMapSnippetAdapter
import fr.cph.chicago.entity.BusArrival
import fr.cph.chicago.util.Util
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.apache.commons.lang3.StringUtils
import java.util.*

class BusFollowObserver(private val activity: BusMapActivity, private val layout: View, private val view: View, private val loadAll: Boolean) : Observer<MutableList<BusArrival>> {

    override fun onSubscribe(d: Disposable) {}

    override fun onNext(busArrivals: MutableList<BusArrival>) {
        var busArrivals = busArrivals
        if (!loadAll && busArrivals.size > 7) {
            busArrivals = busArrivals.subList(0, 6)
            val busArrival = BusArrival(Date(), "added bus", view.context.getString(R.string.bus_all_results), 0, 0, "", "", StringUtils.EMPTY, Date(), false)
            busArrivals.add(busArrival)
        }
        val arrivals = view.findViewById<ListView>(R.id.arrivals)
        val error = view.findViewById<TextView>(R.id.error)
        if (busArrivals.size != 0) {
            val ada = BusMapSnippetAdapter(activity, busArrivals)
            arrivals.adapter = ada
            arrivals.visibility = ListView.VISIBLE
            error.visibility = TextView.GONE
        } else {
            arrivals.visibility = ListView.GONE
            error.visibility = TextView.VISIBLE
        }
        activity.refreshInfoWindow()
    }

    override fun onError(throwable: Throwable) {
        Util.handleConnectOrParserException(throwable, null, layout, layout)
        Log.e(TAG, throwable.message, throwable)
    }

    override fun onComplete() {}

    companion object {
        private val TAG = BusFollowObserver::class.java.simpleName
    }
}
