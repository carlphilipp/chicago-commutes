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

package fr.cph.chicago.core.activity

import android.os.Build
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.Toolbar
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindColor
import butterknife.BindString
import butterknife.BindView
import butterknife.ButterKnife
import fr.cph.chicago.R
import fr.cph.chicago.core.listener.GoogleMapDirectionOnClickListener
import fr.cph.chicago.core.listener.GoogleMapOnClickListener
import fr.cph.chicago.core.listener.GoogleStreetOnClickListener
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.rx.BikeAllBikeStationsObserver
import fr.cph.chicago.rx.ObservableUtil
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.util.LayoutUtil
import fr.cph.chicago.util.Util

/**
 * Activity the list of train stations
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BikeStationActivity : AbstractStationActivity() {

    @BindView(R.id.activity_station_swipe_refresh_layout)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.activity_favorite_star)
    lateinit var favoritesImage: ImageView
    @BindView(R.id.activity_bike_station_streetview_image)
    lateinit var streetViewImage: ImageView
    @BindView(R.id.activity_bike_station_steetview_text)
    lateinit var streetViewText: TextView
    @BindView(R.id.activity_map_image)
    lateinit var mapImage: ImageView
    @BindView(R.id.activity_map_direction)
    lateinit var directionImage: ImageView
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
    @BindView(R.id.favorites_bikes_list)
    lateinit var container: LinearLayout

    @BindString(R.string.bundle_bike_station)
    lateinit var bundleBikeStation: String

    private val observableUtil: ObservableUtil = ObservableUtil
    private val preferenceService: PreferenceService = PreferenceService

    @JvmField
    @BindColor(R.color.grey_5)
    internal var grey_5: Int = 0
    @JvmField
    @BindColor(R.color.red)
    internal var red: Int = 0
    @JvmField
    @BindColor(R.color.green)
    internal var green: Int = 0
    @JvmField
    @BindColor(R.color.yellowLineDark)
    internal var yellowLineDark: Int = 0

    private lateinit var divvyStation: BikeStation
    private var isFavorite: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this.isFinishing) {
            setContentView(R.layout.activity_bike_station)
            ButterKnife.bind(this)
            divvyStation = intent.extras.getParcelable(bundleBikeStation)
            val latitude = divvyStation.latitude
            val longitude = divvyStation.longitude

            swipeRefreshLayout.setOnRefreshListener {
                observableUtil.createAllBikeStationsObservable()
                    .subscribe(BikeAllBikeStationsObserver(this, divvyStation.id, swipeRefreshLayout))
            }

            isFavorite = isFavorite()

            // Call google street api to load image
            loadGoogleStreetImage(Position(latitude, longitude), streetViewImage, streetViewText)

            mapImage.setColorFilter(grey_5)
            directionImage.setColorFilter(grey_5)

            favoritesImage.setColorFilter(if (isFavorite) yellowLineDark else grey_5)

            favoritesImageContainer.setOnClickListener { _ -> switchFavorite() }
            bikeStationValue.text = divvyStation.address
            streetViewImage.setOnClickListener(GoogleStreetOnClickListener(latitude, longitude))
            mapContainer.setOnClickListener(GoogleMapOnClickListener(latitude, longitude))
            walkContainer.setOnClickListener(GoogleMapDirectionOnClickListener(latitude, longitude))

            drawData()
            setToolBar()
        }
    }

    private fun setToolBar() {
        toolbar.inflateMenu(R.menu.main)
        toolbar.setOnMenuItemClickListener { _ ->
            swipeRefreshLayout.isRefreshing = true
            observableUtil.createAllBikeStationsObservable()
                .subscribe(BikeAllBikeStationsObserver(this@BikeStationActivity, divvyStation.id, swipeRefreshLayout))
            false
        }
        Util.setWindowsColor(this, toolbar, TrainLine.NA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.elevation = 4f
        }
        toolbar.title = divvyStation.name
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setOnClickListener { _ -> finish() }
    }

    private fun drawData() {
        val bikeResultLayout = LayoutUtil.buildBikeStationLayout(divvyStation)
        container.addView(bikeResultLayout)
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        divvyStation = savedInstanceState.getParcelable(bundleBikeStation)
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putParcelable(bundleBikeStation, divvyStation)
        super.onSaveInstanceState(savedInstanceState)
    }

    /**
     * Is favorite or not ?
     *
     * @return if the train station is favorite
     */
    override fun isFavorite(): Boolean {
        return preferenceService.isBikeStationFavorite(divvyStation.id)
    }

    fun refreshStation(station: BikeStation) {
        this.divvyStation = station
        drawData()
    }

    /**
     * Add/remove favorites
     */
    private fun switchFavorite() {
        isFavorite = if (isFavorite) {
            preferenceService.removeFromBikeFavorites(divvyStation.id, swipeRefreshLayout)
            favoritesImage.setColorFilter(grey_5)
            false
        } else {
            preferenceService.addToBikeFavorites(divvyStation.id, swipeRefreshLayout)
            preferenceService.addBikeRouteNameMapping(Integer.toString(divvyStation.id), divvyStation.name)
            favoritesImage.setColorFilter(yellowLineDark)
            true
        }
    }
}
