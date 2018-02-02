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
import fr.cph.chicago.entity.BikeStation
import fr.cph.chicago.entity.Position
import fr.cph.chicago.entity.enumeration.TrainLine
import fr.cph.chicago.rx.BikeAllBikeStationsObserver
import fr.cph.chicago.rx.ObservableUtil
import fr.cph.chicago.service.PreferenceService
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
    @BindString(R.string.bike_available_bikes)
    lateinit var bikeAvailableBikes: String
    @BindString(R.string.bike_available_docks)
    lateinit var bikeAvailableDocks: String

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

    private var bikeStation: BikeStation? = null
    private var isFavorite: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this.isFinishing) {
            setContentView(R.layout.activity_bike_station)
            ButterKnife.bind(this)
            bikeStation = intent.extras!!.getParcelable(bundleBikeStation)
            if (bikeStation != null) {
                val latitude = bikeStation!!.latitude
                val longitude = bikeStation!!.longitude

                swipeRefreshLayout.setOnRefreshListener {
                    observableUtil.createAllBikeStationsObservable()
                        .subscribe(BikeAllBikeStationsObserver(this, bikeStation!!.id, swipeRefreshLayout))
                }

                isFavorite = isFavorite()

                // Call google street api to load image
                loadGoogleStreetImage(Position(latitude, longitude), streetViewImage, streetViewText)

                mapImage.setColorFilter(grey_5)
                directionImage.setColorFilter(grey_5)

                if (isFavorite) {
                    favoritesImage.setColorFilter(yellowLineDark)
                } else {
                    favoritesImage.setColorFilter(grey_5)
                }

                favoritesImageContainer.setOnClickListener { _ -> switchFavorite() }
                bikeStationValue.text = bikeStation!!.stAddress1
                streetViewImage.setOnClickListener(GoogleStreetOnClickListener(latitude, longitude))
                mapContainer.setOnClickListener(GoogleMapOnClickListener(latitude, longitude))
                walkContainer.setOnClickListener(GoogleMapDirectionOnClickListener(latitude, longitude))

                drawData()
            }
            setToolBar()
        }
    }

    private fun setToolBar() {
        toolbar.inflateMenu(R.menu.main)
        toolbar.setOnMenuItemClickListener { _ ->
            swipeRefreshLayout.isRefreshing = true
            observableUtil.createAllBikeStationsObservable()
                .subscribe(BikeAllBikeStationsObserver(this@BikeStationActivity, bikeStation!!.id, swipeRefreshLayout))
            false
        }
        Util.setWindowsColor(this, toolbar, TrainLine.NA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.elevation = 4f
        }
        toolbar.title = bikeStation!!.name
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setOnClickListener { _ -> finish() }
    }

    private fun drawData() {
        val context = applicationContext

        val availableLayout = LinearLayout(context)
        val availableBikes = LinearLayout(context)
        val availableDocks = LinearLayout(context)

        val availableBike = TextView(context)
        val availableDock = TextView(context)
        val amountBike = TextView(context)
        val amountDock = TextView(context)

        container.removeAllViews()
        container.orientation = LinearLayout.HORIZONTAL
        availableLayout.orientation = LinearLayout.VERTICAL
        availableBikes.orientation = LinearLayout.HORIZONTAL
        availableBike.text = bikeAvailableBikes
        availableBike.setTextColor(grey_5)
        availableBikes.addView(availableBike)
        amountBike.text = bikeStation!!.availableBikes.toString()
        if (bikeStation!!.availableBikes == 0) {
            amountBike.setTextColor(red)
        } else {
            amountBike.setTextColor(green)
        }
        availableBikes.addView(amountBike)
        availableLayout.addView(availableBikes)
        availableDocks.orientation = LinearLayout.HORIZONTAL
        availableDock.text = bikeAvailableDocks
        availableDock.setTextColor(grey_5)
        availableDocks.addView(availableDock)
        amountDock.text = bikeStation!!.availableDocks.toString()
        if (bikeStation!!.availableDocks == 0) {
            amountDock.setTextColor(red)
        } else {
            amountDock.setTextColor(green)
        }
        availableDocks.addView(amountDock)
        availableLayout.addView(availableDocks)
        container.addView(availableLayout)
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        bikeStation = savedInstanceState.getParcelable(bundleBikeStation)
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putParcelable(bundleBikeStation, bikeStation)
        super.onSaveInstanceState(savedInstanceState)
    }

    /**
     * Is favorite or not ?
     *
     * @return if the station is favorite
     */
    override fun isFavorite(): Boolean {
        return preferenceService.isBikeStationFavorite(bikeStation!!.id)
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
            preferenceService.removeFromBikeFavorites(bikeStation!!.id, swipeRefreshLayout)
            favoritesImage.setColorFilter(grey_5)
            false
        } else {
            preferenceService.addToBikeFavorites(bikeStation!!.id, swipeRefreshLayout)
            preferenceService.addBikeRouteNameMapping(Integer.toString(bikeStation!!.id), bikeStation!!.name)
            favoritesImage.setColorFilter(yellowLineDark)
            true
        }
    }
}
