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

package fr.cph.chicago.core.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.SearchActivity
import fr.cph.chicago.core.listener.BikeStationOnClickListener
import fr.cph.chicago.core.listener.TrainOnClickListener
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.rx.BusDirectionObserver
import fr.cph.chicago.rx.ObservableUtil
import fr.cph.chicago.util.LayoutUtil
import fr.cph.chicago.util.Util

/**
 * Adapter that will handle search
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class SearchAdapter(private val activity: SearchActivity) : BaseAdapter() {

    private val context: Context = activity.applicationContext

    private var trains: List<TrainStation> = listOf()
    private var busRoutes: List<BusRoute> = listOf()
    private var divvyStations: List<BikeStation> = listOf()

    override fun getCount(): Int {
        return trains.size + busRoutes.size + divvyStations.size
    }

    override fun getItem(position: Int): Any {
        val result: Any
        result = when {
            position < trains.size -> trains[position]
            position < trains.size + busRoutes.size -> busRoutes[position - trains.size]
            else -> divvyStations[position - (trains.size + busRoutes.size)]
        }
        return result
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val vi = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = vi.inflate(R.layout.list_search, parent, false)

        val routeName: TextView = view.findViewById(R.id.station_name)

        when {
            position < trains.size -> {
                val station = getItem(position) as TrainStation
                routeName.text = station.name

                val stationColorView: LinearLayout = view.findViewById(R.id.station_color)

                station.lines
                    .map { layoutUtil.createColoredRoundForMultiple(it) }
                    .forEach { stationColorView.addView(it) }

                view.setOnClickListener(TrainOnClickListener(parent.context, station.id, station.lines))
            }
            position < trains.size + busRoutes.size -> {
                val busRoute = getItem(position) as BusRoute

                val icon: ImageView = view.findViewById(R.id.icon)
                icon.setImageDrawable(ContextCompat.getDrawable(parent.context, R.drawable.ic_directions_bus_white_24dp))

                val name = "${busRoute.id} ${busRoute.name}"
                routeName.text = name

                val loadingTextView: TextView = view.findViewById(R.id.loading_text_view)
                view.setOnClickListener { _ ->
                    loadingTextView.visibility = LinearLayout.VISIBLE
                    observableUtil.createBusDirectionsObservable(busRoute.id)
                        .doOnError { throwable ->
                            util.handleConnectOrParserException(throwable, activity, null, loadingTextView)
                            Log.e(TAG, throwable.message, throwable)
                        }.subscribe(BusDirectionObserver(App.instance.screenWidth, parent, loadingTextView, busRoute))
                }
            }
            else -> {
                val bikeStation = getItem(position) as BikeStation

                val icon: ImageView = view.findViewById(R.id.icon)
                icon.setImageDrawable(ContextCompat.getDrawable(parent.context, R.drawable.ic_directions_bike_white_24dp))

                routeName.text = bikeStation.name

                view.setOnClickListener(BikeStationOnClickListener(bikeStation))
            }
        }
        return view
    }

    fun updateData(trains: List<TrainStation>, buses: List<BusRoute>, divvies: List<BikeStation>) {
        this.trains = trains
        this.busRoutes = buses
        this.divvyStations = divvies
    }

    companion object {
        private val TAG = SearchAdapter::class.java.simpleName
        private val util = Util
        private val observableUtil = ObservableUtil
        private val layoutUtil = LayoutUtil
    }
}
