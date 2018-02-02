package fr.cph.chicago.core.activity

import android.app.Activity
import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.TextView

import fr.cph.chicago.R
import fr.cph.chicago.client.GoogleStreetClient
import fr.cph.chicago.entity.Position
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

abstract class AbstractStationActivity : Activity() {

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
