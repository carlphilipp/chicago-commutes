package fr.cph.chicago.rx.subscriber;

import android.util.Log;
import android.widget.RelativeLayout;

import java.util.List;

import fr.cph.chicago.R;
import fr.cph.chicago.app.activity.BusMapActivity;
import fr.cph.chicago.entity.Bus;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.util.Util;
import rx.Subscriber;

public class BusSubscriber extends Subscriber<List<Bus>> {

    private static final String TAG = BusSubscriber.class.getSimpleName();

    private final BusMapActivity activity;
    private final boolean centerMap;
    private final RelativeLayout layout;

    public BusSubscriber(final BusMapActivity activity, final boolean centerMap, final RelativeLayout layout) {
        this.activity = activity;
        this.centerMap = centerMap;
        this.layout = layout;
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
                Util.showMessage(layout, R.string.message_no_bus_found);
            }
        } else {
            Util.showMessage(layout, R.string.message_error_while_loading_data);
        }
    }

    @Override
    public void onError(final Throwable throwable) {
        if (throwable.getCause() instanceof ConnectException) {
            Util.showNetworkErrorMessage(layout);
        } else {
            Util.showOopsSomethingWentWrong(layout);
        }
        Log.e(TAG, throwable.getMessage(), throwable);
    }

    @Override
    public void onCompleted() {
    }
}
