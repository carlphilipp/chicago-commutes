/**
 * Copyright 2019 Carl-Philipp Harmant
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

import android.text.TextUtils
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.RecyclerView
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.MainActivity
import fr.cph.chicago.core.listener.BikeDetailsButtonOnClickListener
import fr.cph.chicago.core.listener.BusMapButtonOnClickListener
import fr.cph.chicago.core.listener.BusStopOnClickListener
import fr.cph.chicago.core.listener.GoogleMapOnClickListener
import fr.cph.chicago.core.listener.TrainDetailsButtonOnClickListener
import fr.cph.chicago.core.listener.TrainMapButtonOnClickListener
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusArrival
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.Favorites
import fr.cph.chicago.core.model.TrainArrival
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.dto.BusDetailsDTO
import fr.cph.chicago.core.model.enumeration.BusDirection
import fr.cph.chicago.redux.AppState
import fr.cph.chicago.redux.CounterActionDecrease
import fr.cph.chicago.redux.CounterActionIncrease
import fr.cph.chicago.redux.mainStore
import fr.cph.chicago.util.LayoutUtil
import fr.cph.chicago.util.TimeUtil
import fr.cph.chicago.util.Util
import org.rekotlin.StoreSubscriber
import java.util.Calendar

/**
 * Adapter that will handle favorites
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class FavoritesAdapter(private val activity: MainActivity) : RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder>() {

    private val textAppearance: Int = Util.getAttribute(activity, R.attr.textAppearance)

    private val util = Util
    private val favorites = Favorites
    private val layoutUtil = LayoutUtil
    private lateinit var lastUpdate: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_favorites, parent, false)
        return FavoritesViewHolder(view, parent)
    }

    override fun onBindViewHolder(holder: FavoritesViewHolder, position: Int) {
        holder.mainLayout.removeAllViews()
        val model = favorites.getObject(position)
        holder.lastUpdateTextView.text = lastUpdate

        when (model) {
            is TrainStation -> handleTrainStation(textAppearance, holder, model)
            is BusRoute -> handleBusRoute(textAppearance, holder, model)
            else -> handleBikeStation(textAppearance, holder, model as BikeStation)
        }
    }

    class FavoritesViewHolder(view: View, val parent: ViewGroup) : RecyclerView.ViewHolder(view), StoreSubscriber<AppState> {
        val mainLayout: LinearLayout = view.findViewById(R.id.favorites_arrival_layout)
        val lastUpdateTextView: TextView = view.findViewById(R.id.last_update)
        val stationNameTextView: TextView = view.findViewById(R.id.favorites_station_name)
        val favoriteImage: ImageView = view.findViewById(R.id.favorites_icon)
        val detailsButton: Button = view.findViewById(R.id.details_button)
        val mapButton: Button = view.findViewById(R.id.view_map_button)

        val counter: TextView = view.findViewById(R.id.counter)
        val decButton: Button = view.findViewById(R.id.dec)
        val incButton: Button = view.findViewById(R.id.inc)

        init {
            this.stationNameTextView.setLines(1)
            this.stationNameTextView.ellipsize = TextUtils.TruncateAt.END
            this.decButton.setOnClickListener {
                mainStore.dispatch(CounterActionDecrease())
            }
            this.incButton.setOnClickListener {
                mainStore.dispatch(CounterActionIncrease())
            }
            mainStore.subscribe(this)
        }

        override fun newState(state: AppState) {
            counter.text = "${state.counter}"
        }
    }

    private fun handleTrainStation(@StyleRes textAppearance: Int, holder: FavoritesViewHolder, trainStation: TrainStation) {
        holder.favoriteImage.setImageResource(R.drawable.ic_train_white_24dp)
        holder.stationNameTextView.text = trainStation.name
        holder.detailsButton.setOnClickListener(TrainDetailsButtonOnClickListener(trainStation.id))
        holder.detailsButton.isEnabled = true

        holder.mapButton.text = App.instance.getString(R.string.favorites_view_trains)
        holder.mapButton.setOnClickListener(TrainMapButtonOnClickListener(activity, trainStation.lines))

        trainStation.lines.forEach { trainLine ->
            var newLine = true
            val etas = favorites.getTrainArrivalByLine(trainStation.id, trainLine)
            for ((i, entry) in etas.entries.withIndex()) {
                val containParams = layoutUtil.getInsideParams(newLine, i == etas.size - 1)
                val container = layoutUtil.createTrainArrivalsLayout(textAppearance, containParams, entry, trainLine)

                holder.mainLayout.addView(container)

                newLine = false
            }
        }
    }

    private fun handleBusRoute(@StyleRes textAppearance: Int, holder: FavoritesViewHolder, busRoute: BusRoute) {
        holder.stationNameTextView.text = busRoute.id
        holder.favoriteImage.setImageResource(R.drawable.ic_directions_bus_white_24dp)

        val busDetailsDTOs = mutableListOf<BusDetailsDTO>()

        val busArrivalDTO = favorites.getBusArrivalsMapped(busRoute.id)
        val entrySet = busArrivalDTO.entries

        for ((stopName, boundMap) in entrySet) {
            val stopNameTrimmed = util.trimBusStopNameIfNeeded(stopName)

            var newLine = true
            var i = 0

            for ((key, value) in boundMap) {

                // Build data for button outside of the loop
                val (_, _, _, stopId, _, routeId, boundTitle) = value.iterator().next()
                val busDirectionEnum: BusDirection = BusDirection.fromString(boundTitle)
                val busDetails = BusDetailsDTO(
                    routeId,
                    busDirectionEnum.shortUpperCase,
                    boundTitle,
                    stopId,
                    busRoute.name,
                    stopName
                )
                busDetailsDTOs.add(busDetails)

                // Build UI
                val containParams = layoutUtil.getInsideParams(newLine, i == boundMap.size - 1)
                val container = layoutUtil.createFavoritesBusArrivalsLayout(textAppearance, containParams, stopNameTrimmed, BusDirection.fromString(key), value as MutableList<out BusArrival>)

                holder.mainLayout.addView(container)

                newLine = false
                i++
            }
        }

        holder.mapButton.text = App.instance.getString(R.string.favorites_view_buses)
        holder.detailsButton.setOnClickListener(BusStopOnClickListener(activity, holder.parent, busDetailsDTOs))
        holder.detailsButton.isEnabled = true
        holder.mapButton.setOnClickListener(BusMapButtonOnClickListener(activity, busRoute, busDetailsDTOs.map { it.bound }.toSet()))
    }

    private fun handleBikeStation(@StyleRes textAppearance: Int, holder: FavoritesViewHolder, divvyStation: BikeStation) {
        holder.stationNameTextView.text = divvyStation.name
        holder.favoriteImage.setImageResource(R.drawable.ic_directions_bike_white_24dp)

        holder.detailsButton.isEnabled = divvyStation.latitude != 0.0 && divvyStation.longitude != 0.0
        holder.detailsButton.setOnClickListener(BikeDetailsButtonOnClickListener(activity, divvyStation))

        holder.mapButton.text = App.instance.getString(R.string.favorites_view_station)
        holder.mapButton.setOnClickListener(GoogleMapOnClickListener(divvyStation.latitude, divvyStation.longitude))

        val bikeResultLayout = layoutUtil.buildBikeFavoritesLayout(textAppearance, divvyStation)

        holder.mainLayout.addView(bikeResultLayout)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return favorites.size()
    }

    fun refreshFavorites() {
        favorites.refreshFavorites()
    }

    /**
     * Refresh date update
     */
    fun resetLastUpdate() {
        App.instance.lastUpdate = Calendar.getInstance().time
    }

    /**
     * Refresh updated view
     */
    fun updateModel() {
        lastUpdate = TimeUtil.lastUpdateInMinutes()
        notifyDataSetChanged()
    }

    fun updateData(trainArrivals: SparseArray<TrainArrival>, busArrivals: List<BusArrival>, divvyStations: List<BikeStation>) {
        favorites.updateData(trainArrivals, busArrivals, divvyStations)
        favorites.updateBikeStations(divvyStations)
    }

    fun updateBikeStations(divvyStations: List<BikeStation>) {
        favorites.updateBikeStations(divvyStations)
    }
}
