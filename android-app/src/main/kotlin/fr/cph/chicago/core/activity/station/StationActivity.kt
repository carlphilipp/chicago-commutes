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

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import fr.cph.chicago.R
import fr.cph.chicago.client.GoogleStreetClient
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.butterknife.ButterKnifeActivity
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.util.Color
import fr.cph.chicago.util.Util
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

abstract class StationActivity(contentView: Int) : ButterKnifeActivity(contentView) {

    companion object {
        private const val TAG_ERROR = "error"
        private const val TAG_DEFAULT = "default"
        private const val TAG_STREET_VIEW = "streetview"
        private val googleStreetClient = GoogleStreetClient
        @JvmStatic
        protected val util = Util
    }

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.activity_station_swipe_refresh_layout)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.favorites_container)
    lateinit var favoritesImageContainer: LinearLayout
    @BindView(R.id.activity_station_streetview_image)
    lateinit var streetViewImage: ImageView
    @BindView(R.id.street_view_progress_bar)
    lateinit var streetViewProgressBar: ProgressBar
    @BindView(R.id.activity_favorite_star)
    lateinit var favoritesImage: ImageView
    @BindView(R.id.activity_map_image)
    lateinit var mapImage: ImageView

    protected var position: Position = Position()
    protected var applyFavorite: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        swipeRefreshLayout.setOnRefreshListener { refresh() }
        favoritesImageContainer.setOnClickListener { switchFavorite() }
        favoritesImageContainer.setOnClickListener { switchFavorite() }
    }

    @SuppressLint("CheckResult")
    fun loadGoogleStreetImage(position: Position) {
        if (streetViewImage.tag == TAG_DEFAULT || streetViewImage.tag == TAG_ERROR) {
            googleStreetClient.connect(position.latitude, position.longitude)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { streetViewProgressBar.visibility = View.GONE }
                .subscribe(
                    { drawable ->
                        streetViewImage.setImageDrawable(drawable)
                        streetViewImage.tag = TAG_STREET_VIEW
                        streetViewImage.scaleType = ImageView.ScaleType.CENTER_CROP
                    },
                    { error ->
                        Timber.e(error, "Error while loading street view image")
                        failStreetViewImage(streetViewImage)
                    }
                )
        }
    }

    protected fun failStreetViewImage(streetViewImage: ImageView) {
        val placeHolder = App.instance.streetViewPlaceHolder
        streetViewImage.setImageDrawable(placeHolder)
        streetViewImage.scaleType = ImageView.ScaleType.CENTER
        streetViewImage.tag = TAG_ERROR
    }

    protected abstract fun isFavorite(): Boolean

    protected fun handleFavorite() {
        if (isFavorite()) {
            favoritesImage.setColorFilter(Color.yellowLineDark)
        }
    }

    protected open fun refresh() {
        swipeRefreshLayout.isRefreshing = true
    }

    protected fun stopRefreshing() {
        if (swipeRefreshLayout.isRefreshing) {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    protected open fun switchFavorite() {
        applyFavorite = true
    }

    protected open fun setToolbar() {
        toolbar.inflateMenu(R.menu.main)
        toolbar.setOnMenuItemClickListener { refresh(); false }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.elevation = 4f
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setOnClickListener { finish() }
    }
}
