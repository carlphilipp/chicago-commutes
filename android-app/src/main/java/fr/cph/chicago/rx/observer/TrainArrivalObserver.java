package fr.cph.chicago.rx.observer;

import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.annimon.stream.Stream;

import fr.cph.chicago.core.activity.StationActivity;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.util.Util;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class TrainArrivalObserver implements Observer<TrainArrival> {

    private static final String TAG = TrainArrivalObserver.class.getSimpleName();

    private final StationActivity activity;
    private final SwipeRefreshLayout swipeRefreshLayout;

    public TrainArrivalObserver(@NonNull final StationActivity activity, @NonNull final SwipeRefreshLayout swipeRefreshLayout) {
        this.activity = activity;
        this.swipeRefreshLayout = swipeRefreshLayout;
    }

    @Override
    public void onSubscribe(Disposable d) {
    }

    @Override
    public void onNext(final TrainArrival trainArrival) {
        activity.hideAllArrivalViews();
        Stream.of(trainArrival.getEtas()).forEach(activity::drawAllArrivalsTrain);
    }

    @Override
    public void onError(final Throwable e) {
        Log.e(TAG, "Error while getting trains arrival time: " + e.getMessage(), e);
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
        Util.INSTANCE.showNetworkErrorMessage(swipeRefreshLayout);
    }

    @Override
    public void onComplete() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
