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

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.*
import fr.cph.chicago.core.listener.BusStopOnClickListener
import fr.cph.chicago.core.listener.GoogleMapOnClickListener
import fr.cph.chicago.data.FavoritesData
import fr.cph.chicago.entity.*
import fr.cph.chicago.entity.dto.BusDetailsDTO
import fr.cph.chicago.entity.enumeration.BusDirection
import fr.cph.chicago.entity.enumeration.TrainLine
import fr.cph.chicago.util.LayoutUtil
import fr.cph.chicago.util.Util
import java.util.Calendar
import java.util.Date

/**
 * Adapter that will handle favoritesData
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
// TODO to analyze and refactor
class FavoritesAdapter(private val activity: MainActivity) : RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder>() {

    private val util = Util
    private val favoritesData = FavoritesData
    private val context: Context = activity.applicationContext
    private val layoutUtil = LayoutUtil
    private lateinit var lastUpdate: String

    class FavoritesViewHolder(view: View, val parent: ViewGroup) : RecyclerView.ViewHolder(view) {
        val mainLayout: LinearLayout = view.findViewById(R.id.favorites_arrival_layout)
        val lastUpdateTextView: TextView = view.findViewById(R.id.last_update)
        val stationNameTextView: TextView = view.findViewById(R.id.favorites_station_name)
        val favoriteImage: ImageView = view.findViewById(R.id.favorites_icon)
        val detailsButton: Button = view.findViewById(R.id.details_button)
        val mapButton: Button = view.findViewById(R.id.view_map_button)

        init {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                this.mainLayout.background = ContextCompat.getDrawable(parent.context, R.drawable.any_selector)
            }
            this.stationNameTextView.setLines(1)
            this.stationNameTextView.ellipsize = TextUtils.TruncateAt.END
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesViewHolder {
        // create a new view
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_favorites_train, parent, false)
        return FavoritesViewHolder(v, parent)
    }

    override fun onBindViewHolder(holder: FavoritesViewHolder, position: Int) {
        resetData(holder)
        val model = favoritesData.getObject(position)
        holder.lastUpdateTextView.text = lastUpdate
        when (model) {
            is Station -> handleStation(holder, model)
            is BusRoute -> handleBusRoute(holder, model)
            else -> {
                val bikeStation = model as BikeStation
                handleBikeStation(holder, bikeStation)
            }
        }
    }

    private fun resetData(holder: FavoritesViewHolder) {
        holder.mainLayout.removeAllViews()
    }

    private fun handleStation(holder: FavoritesViewHolder, station: Station) {
        val stationId = station.id
        val trainLines = station.lines

        holder.favoriteImage.setImageResource(R.drawable.ic_train_white_24dp)
        holder.stationNameTextView.text = station.name
        holder.detailsButton.setOnClickListener { _ ->
            if (!util.isNetworkAvailable()) {
                util.showNetworkErrorMessage(activity)
            } else {
                // Start station activity
                val extras = Bundle()
                val intent = Intent(context, TrainStationActivity::class.java)
                extras.putInt(activity.getString(R.string.bundle_train_stationId), stationId)
                intent.putExtras(extras)
                activity.startActivity(intent)
            }
        }

        holder.mapButton.text = activity.getString(R.string.favorites_view_trains)
        holder.mapButton.setOnClickListener { _ ->
            if (!util.isNetworkAvailable()) {
                util.showNetworkErrorMessage(activity)
            } else {
                if (trainLines.size == 1) {
                    startActivity(trainLines.iterator().next())
                } else {
                    val colors = mutableListOf<Int>()
                    val values = trainLines
                        .flatMap { line ->
                            val color = if (line !== TrainLine.YELLOW) line.color else ContextCompat.getColor(context, R.color.yellowLine)
                            colors.add(color)
                            listOf(line.toStringWithLine())
                        }.toList()

                    val ada = PopupFavoritesTrainAdapter(activity, values, colors)

                    val lines = mutableListOf<TrainLine>()
                    lines.addAll(trainLines)

                    val builder = AlertDialog.Builder(activity)
                    builder.setAdapter(ada) { _, position -> startActivity(lines[position]) }

                    val dialog = builder.create()
                    dialog.show()
                    if (dialog.window != null) {
                        dialog.window.setLayout(((activity.application as App).screenWidth * 0.7).toInt(), LayoutParams.WRAP_CONTENT)
                    }
                }
            }
        }

        trainLines.forEach { trainLine ->
            var newLine = true
            val etas = favoritesData.getTrainArrivalByLine(stationId, trainLine)
            for ((i, entry) in etas.entries.withIndex()) {
                val containParams = layoutUtil.getInsideParams(newLine, i == etas.size - 1)
                val container = layoutUtil.createTrainArrivalsLayout(containParams, entry, trainLine)

                holder.mainLayout.addView(container)

                newLine = false
            }
        }
    }

    private fun startActivity(trainLine: TrainLine) {
        val extras = Bundle()
        val intent = Intent(context, TrainMapActivity::class.java)
        extras.putString(activity.getString(R.string.bundle_train_line), trainLine.toTextString())
        intent.putExtras(extras)
        activity.startActivity(intent)
    }

    private fun handleBusRoute(holder: FavoritesViewHolder, busRoute: BusRoute) {
        holder.stationNameTextView.text = busRoute.id
        holder.favoriteImage.setImageResource(R.drawable.ic_directions_bus_white_24dp)

        val busDetailsDTOs = mutableListOf<BusDetailsDTO>()

        val busArrivalDTO = favoritesData.getBusArrivalsMapped(busRoute.id)
        val entrySet = busArrivalDTO.entries

        for ((stopName, boundMap) in entrySet) {
            val stopNameTrimmed = util.trimBusStopNameIfNeeded(stopName)

            var newLine = true
            var i = 0

            for ((key, value) in boundMap) {

                // Build data for button outside of the loop
                val (_, _, _, stopId, _, routeId, boundTitle) = value[0]
                val busDirectionEnum: BusDirection = BusDirection.fromString(boundTitle)
                val busDetails = BusDetailsDTO(
                    routeId,
                    busDirectionEnum.shortUpperCase,
                    boundTitle,
                    Integer.toString(stopId),
                    busRoute.name,
                    stopName
                )
                busDetailsDTOs.add(busDetails)

                // Build UI
                val containParams = layoutUtil.getInsideParams(newLine, i == boundMap.size - 1)
                val container = layoutUtil.createBusArrivalsLayout(containParams, stopNameTrimmed, BusDirection.fromString(key), value as MutableList<out BusArrival>)

                holder.mainLayout.addView(container)

                newLine = false
                i++
            }
        }

        holder.mapButton.text = activity.getString(R.string.favorites_view_buses)
        holder.detailsButton.setOnClickListener(BusStopOnClickListener(activity, holder.parent, busDetailsDTOs))
        holder.mapButton.setOnClickListener { _ ->
            if (!util.isNetworkAvailable()) {
                util.showNetworkErrorMessage(activity)
            } else {
                val bounds = busDetailsDTOs.map { it.bound }.toSet()
                val intent = Intent(activity.applicationContext, BusMapActivity::class.java)
                val extras = Bundle()
                extras.putString(activity.getString(R.string.bundle_bus_route_id), busRoute.id)
                extras.putStringArray(activity.getString(R.string.bundle_bus_bounds), bounds.toTypedArray())
                intent.putExtras(extras)
                activity.startActivity(intent)
            }
        }
    }

    private fun handleBikeStation(holder: FavoritesViewHolder, bikeStation: BikeStation) {
        holder.stationNameTextView.text = bikeStation.name
        holder.favoriteImage.setImageResource(R.drawable.ic_directions_bike_white_24dp)

        holder.detailsButton.setOnClickListener { _ ->
            if (!util.isNetworkAvailable()) {
                util.showNetworkErrorMessage(activity)
            } else if (bikeStation.latitude != 0.0 && bikeStation.longitude != 0.0) {
                val intent = Intent(activity.applicationContext, BikeStationActivity::class.java)
                val extras = Bundle()
                extras.putParcelable(activity.getString(R.string.bundle_bike_station), bikeStation)
                intent.putExtras(extras)
                activity.startActivity(intent)
            } else {
                util.showMessage(activity, R.string.message_not_ready)
            }
        }

        holder.mapButton.text = activity.getString(R.string.favorites_view_station)
        holder.mapButton.setOnClickListener(GoogleMapOnClickListener(bikeStation.latitude, bikeStation.longitude))

        val bikeResultLayout = layoutUtil.createBikeLayout(bikeStation)

        holder.mainLayout.addView(bikeResultLayout)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return favoritesData.size()
    }

    fun refreshFavorites() {
        favoritesData.refreshFavorites()
    }

    /**
     * Refresh date update
     */
    fun resetLastUpdate() {
        (activity.application as App).lastUpdate = Calendar.getInstance().time
    }

    /**
     * Refresh updated view
     */
    fun updateModel() {
        lastUpdate = lastUpdateInMinutes
        notifyDataSetChanged()
    }

    fun updateTrainArrivalsAndBusArrivals(trainArrivals: SparseArray<TrainArrival>, busArrivals: List<BusArrival>) {
        favoritesData.updateTrainArrivals(trainArrivals)
        favoritesData.updateBusArrivals(busArrivals)
    }

    fun updateBikeStations(bikeStations: List<BikeStation>) {
        favoritesData.updateBikeStations(bikeStations)
    }

    /**
     * Get last update in minutes
     *
     * @return a string
     */
    private val lastUpdateInMinutes: String
        get() {
            val lastUpdateInMinutes = StringBuilder()
            val currentDate = Calendar.getInstance().time
            val diff = getTimeDifference((activity.application as App).lastUpdate, currentDate)
            val hours = diff[0]
            val minutes = diff[1]
            if (hours == 0L && minutes == 0L) {
                lastUpdateInMinutes.append(activity.getString(R.string.time_now))
            } else {
                if (hours == 0L) {
                    lastUpdateInMinutes.append(minutes).append(activity.getString(R.string.time_min))
                } else {
                    lastUpdateInMinutes.append(hours).append(activity.getString(R.string.time_hour)).append(minutes).append(activity.getString(R.string.time_min))
                }
            }
            return lastUpdateInMinutes.toString()
        }

    /**
     * Get time difference between 2 dates
     *
     * @param date1 the date one
     * @param date2 the date two
     * @return a tab containing in 0 the hour and in 1 the minutes
     */
    private fun getTimeDifference(date1: Date, date2: Date): LongArray {
        val result = LongArray(2)
        val cal = Calendar.getInstance()
        cal.time = date1
        val t1 = cal.timeInMillis
        cal.time = date2
        var diff = Math.abs(cal.timeInMillis - t1)
        val day = 1000 * 60 * 60 * 24
        val hour = day / 24
        val minute = hour / 60
        diff %= day.toLong()
        val h = diff / hour
        diff %= hour.toLong()
        val m = diff / minute
        result[0] = h
        result[1] = m
        return result
    }
}
