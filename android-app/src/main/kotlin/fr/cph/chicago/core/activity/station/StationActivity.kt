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
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import fr.cph.chicago.client.GoogleStreetClient
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.butterknife.ButterKnifeActivity
import fr.cph.chicago.core.model.Position
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

abstract class StationActivity(contentView: Int) : ButterKnifeActivity(contentView) {

    @SuppressLint("CheckResult")
    fun loadGoogleStreetImage(position: Position, streetViewImage: ImageView, streetViewProgressBar: ProgressBar) {
        Observable.fromCallable { GoogleStreetClient.connect(position.latitude, position.longitude) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally { streetViewProgressBar.visibility = View.GONE }
            .subscribe(
                { drawable ->
                    streetViewImage.setImageDrawable(drawable)
                    streetViewImage.scaleType = ImageView.ScaleType.CENTER_CROP
                },
                { error ->
                    Timber.e(error, "Error while loading street view image")
                    failStreetViewImage(streetViewImage)
                }
            )
    }

    protected fun failStreetViewImage(streetViewImage: ImageView) {
        val placeHolder = App.instance.streetViewPlaceHolder
        streetViewImage.setImageDrawable(placeHolder)
        streetViewImage.scaleType = ImageView.ScaleType.CENTER
    }

    protected abstract fun isFavorite(): Boolean
}
