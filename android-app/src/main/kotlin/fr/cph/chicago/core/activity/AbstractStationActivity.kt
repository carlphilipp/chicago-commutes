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

import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.TextView
import fr.cph.chicago.R
import fr.cph.chicago.client.GoogleStreetClient
import fr.cph.chicago.core.model.Position
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

abstract class AbstractStationActivity(contentView: Int) : ButterKnifeActivity(contentView) {

    fun loadGoogleStreetImage(position: Position, streetViewImage: ImageView, streetViewText: TextView) {
        Observable.create { observableOnSubscribe: ObservableEmitter<Drawable> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(GoogleStreetClient.connect(position.latitude, position.longitude))
                observableOnSubscribe.onComplete()
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { drawable ->
                streetViewImage.setImageDrawable(drawable)
                streetViewText.text = this@AbstractStationActivity.applicationContext.getString(R.string.station_activity_street_view)
            }
    }

    protected abstract fun isFavorite(): Boolean
}
