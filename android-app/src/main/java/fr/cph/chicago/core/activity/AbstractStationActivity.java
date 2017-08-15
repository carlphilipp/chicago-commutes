package fr.cph.chicago.core.activity;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.TextView;

import com.annimon.stream.Optional;

import fr.cph.chicago.R;
import fr.cph.chicago.client.GoogleStreetClient;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public abstract class AbstractStationActivity extends Activity {

    private Observable<Optional<Drawable>> googleMapImageObservable;

    final void createGoogleStreetObservable(final double latitude, final double longitude) {
        googleMapImageObservable = Observable.create(
            (ObservableEmitter<Optional<Drawable>> observableOnSubscribe) -> {
                if (!observableOnSubscribe.isDisposed()) {
                    observableOnSubscribe.onNext(GoogleStreetClient.Companion.getINSTANCE().connect(latitude, longitude));
                    observableOnSubscribe.onComplete();
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    final void subscribeToGoogleStreet(final ImageView streetViewImage, final TextView streetViewText) {
        googleMapImageObservable.subscribe(
            drawable -> {
                streetViewImage.setImageDrawable(drawable.orElse(null));
                streetViewText.setText(AbstractStationActivity.this.getApplicationContext().getString(R.string.station_activity_street_view));
            }
        );
    }

    protected abstract boolean isFavorite();
}
