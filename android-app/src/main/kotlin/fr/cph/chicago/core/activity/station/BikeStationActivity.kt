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

package fr.cph.chicago.core.activity.station

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindString
import butterknife.BindView
import fr.cph.chicago.R
import fr.cph.chicago.core.listener.GoogleMapDirectionOnClickListener
import fr.cph.chicago.core.listener.GoogleMapOnClickListener
import fr.cph.chicago.core.listener.GoogleStreetOnClickListener
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.redux.AddBikeFavoriteAction
import fr.cph.chicago.redux.BikeStationAction
import fr.cph.chicago.redux.RemoveBikeFavoriteAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.store
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.util.Color
import fr.cph.chicago.util.Util
import org.rekotlin.StoreSubscriber
import timber.log.Timber

/**
 * Activity the list of train stations
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BikeStationActivity : StationActivity(R.layout.activity_bike_station), StoreSubscriber<State> {

    @BindView(R.id.map_container)
    lateinit var mapContainer: LinearLayout
    @BindView(R.id.walk_container)
    lateinit var walkContainer: LinearLayout
    @BindView(R.id.activity_bike_station_value)
    lateinit var bikeStationValue: TextView
    @BindView(R.id.activity_bike_available_bike_value)
    lateinit var availableBikes: TextView
    @BindView(R.id.activity_bike_available_docks_value)
    lateinit var availableDocks: TextView

    @BindString(R.string.bundle_bike_station)
    lateinit var bundleBikeStation: String

    private lateinit var bikeStation: BikeStation

    override fun create(savedInstanceState: Bundle?) {
        bikeStation = intent.extras?.getParcelable(bundleBikeStation)
            ?: BikeStation.buildUnknownStation()
        position = Position(bikeStation.latitude, bikeStation.longitude)

        loadGoogleStreetImage(position)

        handleFavorite()

        bikeStationValue.text = bikeStation.address
        streetViewImage.setOnClickListener(GoogleStreetOnClickListener(position.latitude, position.longitude))
        mapContainer.setOnClickListener(GoogleMapOnClickListener(position.latitude, position.longitude))
        walkContainer.setOnClickListener(GoogleMapDirectionOnClickListener(position.latitude, position.longitude))

        drawData()
        setToolbar()
    }

    override fun onPause() {
        super.onPause()
        store.unsubscribe(this)
    }

    override fun onResume() {
        super.onResume()
        store.subscribe(this)
    }

    override fun newState(state: State) {
        when (state.bikeStationsStatus) {
            Status.FAILURE, Status.FULL_FAILURE -> util.showSnackBar(swipeRefreshLayout, state.bikeStationsErrorMessage)
            Status.ADD_FAVORITES -> {
                if (applyFavorite) {
                    util.showSnackBar(swipeRefreshLayout, R.string.message_add_fav, true)
                    applyFavorite = false
                    favoritesImage.setColorFilter(Color.yellowLineDark)
                }
            }
            Status.REMOVE_FAVORITES -> {
                if (applyFavorite) {
                    util.showSnackBar(swipeRefreshLayout, R.string.message_remove_fav, true)
                    applyFavorite = false
                    favoritesImage.colorFilter = mapImage.colorFilter
                }
            }
            else -> {
                state.bikeStations
                    .filter { station -> bikeStation.id == station.id }
                    .elementAtOrElse(0) { BikeStation.buildDefaultBikeStationWithName("error") }
                    .also { station ->
                        if (station.name != "error") {
                            refreshStation(station)
                            intent.extras?.putParcelable(getString(R.string.bundle_bike_station), station)
                        } else {
                            Timber.w("Train station id [%s] not found", bikeStation.id)
                            util.showOopsSomethingWentWrong(swipeRefreshLayout)
                        }
                    }
            }
        }
        stopRefreshing()
    }

    override fun refresh() {
        super.refresh()
        store.dispatch(BikeStationAction())
        loadGoogleStreetImage(position)
    }

    override fun setToolbar() {
        super.setToolbar()
        toolbar.title = bikeStation.name
    }

    private fun drawData() {
        if (bikeStation.availableBikes == -1) {
            availableBikes.text = "?"
            availableBikes.setTextColor(Color.orange)
        } else {
            availableBikes.text = Util.formatBikesDocksValues(bikeStation.availableBikes)
            val color = if (bikeStation.availableBikes == 0) Color.red else Color.green
            availableBikes.setTextColor(color)
        }
        if (bikeStation.availableDocks == -1) {
            availableDocks.text = "?"
            availableDocks.setTextColor(Color.orange)
        } else {
            availableDocks.text = Util.formatBikesDocksValues(bikeStation.availableDocks)
            val color = if (bikeStation.availableDocks == 0) Color.red else Color.green
            availableDocks.setTextColor(color)
        }
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        bikeStation = savedInstanceState.getParcelable(bundleBikeStation)
            ?: BikeStation.buildUnknownStation()
        position = Position(bikeStation.latitude, bikeStation.longitude)
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        if (::bikeStation.isInitialized) savedInstanceState.putParcelable(bundleBikeStation, bikeStation)
        super.onSaveInstanceState(savedInstanceState)
    }

    /**
     * Is favorite or not ?
     *
     * @return if the train station is favorite
     */
    override fun isFavorite(): Boolean {
        return preferenceService.isBikeStationFavorite(bikeStation.id)
    }

    private fun refreshStation(station: BikeStation) {
        this.bikeStation = station
        drawData()
    }

    /**
     * Add/remove favorites
     */
    override fun switchFavorite() {
        super.switchFavorite()
        if (isFavorite()) {
            store.dispatch(RemoveBikeFavoriteAction(bikeStation.id))
        } else {
            store.dispatch(AddBikeFavoriteAction(bikeStation.id, bikeStation.name))
        }
    }

    companion object {
        private val util = Util
        private val preferenceService = PreferenceService
    }
}
