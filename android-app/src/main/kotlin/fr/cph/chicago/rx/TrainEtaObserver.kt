/**
 * Copyright 2018 Carl-Philipp Harmant
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
