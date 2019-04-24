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

import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindString
import butterknife.BindView
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.listener.GoogleMapDirectionOnClickListener
import fr.cph.chicago.core.listener.GoogleMapOnClickListener
import fr.cph.chicago.core.listener.GoogleStreetOnClickListener
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.rx.BikeAllBikeStationsObserver
import fr.cph.chicago.rx.ObservableUtil
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.util.Color
import fr.cph.chicago.util.Util

/**
 * Activity the list of train stations
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BikeStationActivity : StationActivity(R.layout.activity_bike_station) {

    @BindView(R.id.activity_station_swipe_refresh_layout)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.activity_favorite_star)
    lateinit var favoritesImage: ImageView
    @BindView(R.id.activity_station_streetview_image)
    lateinit var streetViewImage: ImageView
    @BindView(R.id.street_view_progress_bar)
    lateinit var streetViewProgressBar: ProgressBar
    @BindView(R.id.activity_map_image)
    lateinit var mapImage: ImageView
    @BindView(R.id.map_container)
    lateinit var mapContainer: LinearLayout
    @BindView(R.id.walk_container)
    lateinit var walkContainer: LinearLayout
    @BindView(R.id.favorites_container)
    lateinit var favoritesImageContainer: LinearLayout
    @BindView(R.id.activity_bike_station_value)
    lateinit var bikeStationValue: TextView
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.activity_bike_available_bike_value)
    lateinit var availableBikes: TextView
    @BindView(R.id.activity_bike_available_docks_value)
    lateinit var availableDocks: TextView

    @BindString(R.string.bundle_bike_station)
    lateinit var bundleBikeStation: String

    private val bikeStationsObservable = ObservableUtil.createAllBikeStationsObs()
    private val preferenceService: PreferenceService = PreferenceService

    private lateinit var bikeStation: BikeStation
    private var isFavorite: Boolean = false

    override fun create(savedInstanceState: Bundle?) {
        bikeStation = intent.extras?.getParcelable(bundleBikeStation)
            ?: BikeStation.buildUnknownStation()
        val latitude = bikeStation.latitude
        val longitude = bikeStation.longitude

        swipeRefreshLayout.setOnRefreshListener {
            bikeStationsObservable.subscribe(BikeAllBikeStationsObserver(this, bikeStation.id))
            // FIXME: Identify if it's the place holder or not. This is not great
            if (streetViewImage.scaleType == ImageView.ScaleType.CENTER) {
                loadGoogleStreetImage(Position(latitude, longitude), streetViewImage, streetViewProgressBar)
            }
        }

        isFavorite = isFavorite()

        // Call google street api to load image
        loadGoogleStreetImage(Position(latitude, longitude), streetViewImage, streetViewProgressBar)

        if (isFavorite) {
            favoritesImage.setColorFilter(Color.yellowLineDark)
        }

        favoritesImageContainer.setOnClickListener { switchFavorite() }
        bikeStationValue.text = bikeStation.address
        streetViewImage.setOnClickListener(GoogleStreetOnClickListener(latitude, longitude))
        mapContainer.setOnClickListener(GoogleMapOnClickListener(latitude, longitude))
        walkContainer.setOnClickListener(GoogleMapDirectionOnClickListener(latitude, longitude))

        drawData()
        setToolBar()
    }

    private fun setToolBar() {
        toolbar.inflateMenu(R.menu.main)
        toolbar.setOnMenuItemClickListener {
            swipeRefreshLayout.isRefreshing = true
            bikeStationsObservable.subscribe(BikeAllBikeStationsObserver(this@BikeStationActivity, bikeStation.id))
            false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.elevation = 4f
        }
        toolbar.title = bikeStation.name
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setOnClickListener { finish() }
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
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putParcelable(bundleBikeStation, bikeStation)
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

    fun refreshStation(station: BikeStation) {
        this.bikeStation = station
        drawData()
    }

    /**
     * Add/remove favorites
     */
    private fun switchFavorite() {
        isFavorite = if (isFavorite) {
            preferenceService.removeFromBikeFavorites(bikeStation.id, swipeRefreshLayout)
            favoritesImage.colorFilter = mapImage.colorFilter
            false
        } else {
            preferenceService.addToBikeFavorites(bikeStation.id, swipeRefreshLayout)
            preferenceService.addBikeRouteNameMapping(bikeStation.id.toString(), bikeStation.name)
            favoritesImage.setColorFilter(Color.yellowLineDark)
            App.instance.refresh = true
            true
        }
    }
}
