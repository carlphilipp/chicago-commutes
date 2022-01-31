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

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.MaterialToolbar
import fr.cph.chicago.R
import fr.cph.chicago.client.GoogleStreetClient
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.util.Color
import fr.cph.chicago.util.Util
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import timber.log.Timber

abstract class StationActivity : AppCompatActivity() {

    companion object {
        private const val TAG_ERROR = "error"
        private const val TAG_DEFAULT = "default"
        private const val TAG_STREET_VIEW = "streetview"
        private val googleStreetClient = GoogleStreetClient

        @JvmStatic
        protected val preferenceService = PreferenceService

        @JvmStatic
        protected val util = Util
    }

    protected lateinit var swipeRefreshLayout: SwipeRefreshLayout
    protected var position: Position = Position()
    protected var applyFavorite: Boolean = false
    protected lateinit var streetViewImage: ImageView
    protected lateinit var streetViewProgressBar: ProgressBar
    private lateinit var streetViewText: TextView
    protected lateinit var favoritesImage: ImageView
    protected lateinit var mapImage: ImageView
    protected lateinit var toolbar: MaterialToolbar

    fun setupView(
        swipeRefreshLayout: SwipeRefreshLayout,
        streetViewImage: ImageView,
        streetViewProgressBar: ProgressBar,
        streetViewText: TextView,
        favoritesImage: ImageView,
        mapImage: ImageView,
        favoritesImageContainer: LinearLayout) {
        this.swipeRefreshLayout = swipeRefreshLayout.apply {
            setOnRefreshListener { refresh() }
            setColorSchemeColors(preferenceService.getColorSchemeColors(resources.configuration))
            setProgressBackgroundColorSchemeResource(preferenceService.getProgressBackgroundColorSchemeResource(resources.configuration))
        }
        this.streetViewImage = streetViewImage
        this.streetViewProgressBar = streetViewProgressBar
        this.streetViewText = streetViewText
        this.favoritesImage = favoritesImage
        this.mapImage = mapImage
        favoritesImageContainer.setOnClickListener{ switchFavorite() }
    }

    @SuppressLint("CheckResult")
    fun loadGoogleStreetImage(position: Position) {
        if (streetViewImage.tag == TAG_DEFAULT || streetViewImage.tag == TAG_ERROR) {
            googleStreetClient.getImage(position.latitude, position.longitude)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { streetViewProgressBar.visibility = View.GONE }
                .subscribe(
                    { drawable ->
                        streetViewImage.setImageDrawable(drawable)
                        streetViewImage.tag = TAG_STREET_VIEW
                        streetViewImage.scaleType = ImageView.ScaleType.CENTER_CROP
                        streetViewText.visibility = View.VISIBLE
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
        } else {
            favoritesImage.drawable.colorFilter = mapImage.drawable.colorFilter
        }
    }

    protected open fun refresh() {
        swipeRefreshLayout.isRefreshing = true
        swipeRefreshLayout.setColorSchemeColors(util.randomColor)
    }

    protected fun stopRefreshing() {
        if (swipeRefreshLayout.isRefreshing) {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    protected open fun switchFavorite() {
        applyFavorite = true
    }

    protected open fun buildToolbar(toolbar: MaterialToolbar) {
        this.toolbar = toolbar
        toolbar.inflateMenu(R.menu.main)
        toolbar.setOnMenuItemClickListener { refresh(); false }
        toolbar.elevation = 4f
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setOnClickListener { finish() }
    }
}
