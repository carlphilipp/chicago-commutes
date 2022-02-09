/**
 * Copyright 2021 Carl-Philipp Harmant
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
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.MaterialToolbar
import fr.cph.chicago.R
import fr.cph.chicago.core.listener.GoogleStreetOnClickListener
import fr.cph.chicago.core.listener.OpenMapDirectionOnClickListener
import fr.cph.chicago.core.listener.OpenMapOnClickListener
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.databinding.ActivityBikeStationBinding
import fr.cph.chicago.redux.AddBikeFavoriteAction
import fr.cph.chicago.redux.BikeStationAction
import fr.cph.chicago.redux.RemoveBikeFavoriteAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.store
import fr.cph.chicago.util.Color
import org.rekotlin.StoreSubscriber
import timber.log.Timber

/**
 * Activity the list of train stations
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BikeStationActivity : StationActivity(), StoreSubscriber<State> {

    private lateinit var bikeStation: BikeStation
    private lateinit var binding: ActivityBikeStationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBikeStationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView(
            swipeRefreshLayout = binding.activityStationSwipeRefreshLayout,
            streetViewImage = binding.header.streetViewImage,
            streetViewProgressBar = binding.header.streetViewProgressBar,
            streetViewText = binding.header.streetViewText,
            favoritesImage = binding.header.favorites.favoritesImage,
            mapImage = binding.header.favorites.mapImage,
            favoritesImageContainer = binding.header.favorites.favoritesImageContainer
        )

        bikeStation = intent.extras?.getParcelable(getString(R.string.bundle_bike_station))
            ?: BikeStation.buildUnknownStation()
        position = Position(bikeStation.latitude, bikeStation.longitude)

        loadGoogleStreetImage(position)

        handleFavorite()

        binding.bikeStationValue.text = bikeStation.address
        streetViewImage.setOnClickListener(GoogleStreetOnClickListener(position.latitude, position.longitude))
        binding.header.favorites.mapContainer.setOnClickListener(OpenMapOnClickListener(position.latitude, position.longitude))
        binding.header.favorites.walkContainer.setOnClickListener(OpenMapDirectionOnClickListener(position.latitude, position.longitude))

        drawData()
        buildToolbar(binding.included.toolbar)
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
        Timber.d("New state")
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
                    favoritesImage.drawable.colorFilter = mapImage.drawable.colorFilter
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

    override fun buildToolbar(toolbar: Toolbar) {
        super.buildToolbar(toolbar)
        toolbar.title = bikeStation.name
    }

    private fun drawData() {
        if (bikeStation.availableBikes == -1) {
            binding.availableBikes.text = "?"
            binding.availableBikes.setTextColor(Color.orange)
        } else {
            binding.availableBikes.text = util.formatBikesDocksValues(bikeStation.availableBikes)
            val color = if (bikeStation.availableBikes == 0) Color.red else Color.green
            binding.availableBikes.setTextColor(color)
        }
        if (bikeStation.availableDocks == -1) {
            binding.availableDocks.text = "?"
            binding.availableDocks.setTextColor(Color.orange)
        } else {
            binding.availableDocks.text = util.formatBikesDocksValues(bikeStation.availableDocks)
            val color = if (bikeStation.availableDocks == 0) Color.red else Color.green
            binding.availableDocks.setTextColor(color)
        }
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        bikeStation = savedInstanceState.getParcelable(getString(R.string.bundle_bike_station))
            ?: BikeStation.buildUnknownStation()
        position = Position(bikeStation.latitude, bikeStation.longitude)
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        if (::bikeStation.isInitialized) savedInstanceState.putParcelable(getString(R.string.bundle_bike_station), bikeStation)
        super.onSaveInstanceState(savedInstanceState)
    }

    /**
     * Is favorite or not ?
     *
     * @return if the bike station is favorite
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
}
