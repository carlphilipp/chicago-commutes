package fr.cph.chicago.rx.observer;

import android.util.Log;
import android.view.View;

import java.util.List;

import fr.cph.chicago.R;
import fr.cph.chicago.core.activity.BusMapActivity;
import fr.cph.chicago.entity.Bus;
import fr.cph.chicago.util.Util;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class BusObserver implements Observer<List<Bus>> {

    private static final String TAG = BusObserver.class.getSimpleName();

    private final BusMapActivity activity;
    private final boolean centerMap;
    private final View view;

    public BusObserver(final BusMapActivity activity, final boolean centerMap, final View view) {
        this.activity = activity;
        this.centerMap = centerMap;
        this.view = view;
    }

    @Override
    public void onSubscribe(Disposable d) {
    }

    @Override
    public void onNext(final List<Bus> buses) {
        if (buses != null) {
            activity.drawBuses(buses);
            if (buses.size() != 0) {
                if (centerMap) {
                    activity.centerMapOnBus(buses);
                }
            } else {
                Util.INSTANCE.showMessage(view, R.string.message_no_bus_found);
            }
        } else {
            Util.INSTANCE.showMessage(view, R.string.message_error_while_loading_data);
        }
    }

    @Override
    public void onError(final Throwable throwable) {
        Util.INSTANCE.handleConnectOrParserException(throwable, null, view, view);
        Log.e(TAG, throwable.getMessage(), throwable);
    }

    @Override
    public void onComplete() {
    }
}
