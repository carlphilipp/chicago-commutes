/**
 * Copyright 2017 Carl-Philipp Harmant
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
import fr.cph.chicago.entity.BikeStation
import fr.cph.chicago.entity.BusRoute
import fr.cph.chicago.entity.Station
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

    private var trains: List<Station>? = null
    private var busRoutes: List<BusRoute>? = null
    private var bikeStations: List<BikeStation>? = null

    override fun getCount(): Int {
        return trains!!.size + busRoutes!!.size + bikeStations!!.size
    }

    override fun getItem(position: Int): Any {
        val `object`: Any
        if (position < trains!!.size) {
            `object` = trains!![position]
        } else if (position < trains!!.size + busRoutes!!.size) {
            `object` = busRoutes!![position - trains!!.size]
        } else {
            `object` = bikeStations!![position - (trains!!.size + busRoutes!!.size)]
        }
        return `object`
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val vi = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = vi.inflate(R.layout.list_search, parent, false)

        val routeName: TextView = view.findViewById(R.id.station_name)

        if (position < trains!!.size) {
            val station = getItem(position) as Station
            routeName.text = station.name

            val stationColorView: LinearLayout = view.findViewById(R.id.station_color)

            station.lines
                .map { LayoutUtil.createColoredRoundForMultiple(context, it) }
                .forEach { stationColorView.addView(it) }

            view.setOnClickListener(TrainOnClickListener(parent.context, activity, station.id, station.lines))
        } else if (position < trains!!.size + busRoutes!!.size) {
            val busRoute = getItem(position) as BusRoute

            val icon: ImageView = view.findViewById(R.id.icon)
            icon.setImageDrawable(ContextCompat.getDrawable(parent.context, R.drawable.ic_directions_bus_white_24dp))

            val name = busRoute.id + " " + busRoute.name
            routeName.text = name

            val loadingTextView: TextView = view.findViewById(R.id.loading_text_view)
            view.setOnClickListener { _ ->
                loadingTextView.visibility = LinearLayout.VISIBLE
                ObservableUtil.createBusDirectionsObservable(parent.context, busRoute.id)
                    .doOnError { throwable ->
                        Util.handleConnectOrParserException(throwable, activity, null, loadingTextView)
                        Log.e(TAG, throwable.message, throwable)
                    }.subscribe(BusDirectionObserver(activity.application as App, parent, loadingTextView, busRoute))
            }
        } else {
            val bikeStation = getItem(position) as BikeStation

            val icon: ImageView = view.findViewById(R.id.icon)
            icon.setImageDrawable(ContextCompat.getDrawable(parent.context, R.drawable.ic_directions_bike_white_24dp))

            routeName.text = bikeStation.name

            view.setOnClickListener(BikeStationOnClickListener(bikeStation))
        }
        return view
    }

    /**
     * Update data
     *
     * @param trains the list of train stations
     * @param buses  the list of bus routes
     * @param bikes  the list of bikes
     */
    fun updateData(trains: List<Station>, buses: List<BusRoute>, bikes: List<BikeStation>) {
        this.trains = trains
        this.busRoutes = buses
        this.bikeStations = bikes
    }

    companion object {
        private val TAG = SearchAdapter::class.java.simpleName
    }
}
