package fr.cph.chicago.rx

import android.view.View
import android.widget.ListView
import android.widget.TextView
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.TrainMapActivity
import fr.cph.chicago.core.adapter.TrainMapSnippetAdapter
import fr.cph.chicago.entity.Eta
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class TrainEtaObserver(view: View, private val trainMapActivity: TrainMapActivity) : Observer<List<Eta>> {

    private val arrivals: ListView = view.findViewById(R.id.arrivals)
    private val error: TextView = view.findViewById(R.id.error)

    override fun onNext(etas: List<Eta>) {
        // View can be null
        if (etas.isNotEmpty()) {
            val ada = TrainMapSnippetAdapter(etas)
            arrivals.adapter = ada
            arrivals.visibility = ListView.VISIBLE
            error.visibility = TextView.GONE
        } else {
            arrivals.visibility = ListView.GONE
            error.visibility = TextView.VISIBLE
        }
        trainMapActivity.refreshInfoWindow()
    }

    override fun onSubscribe(d: Disposable) {}

    override fun onError(e: Throwable) {}

    override fun onComplete() {}
}
