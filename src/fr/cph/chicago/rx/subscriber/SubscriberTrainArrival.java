package fr.cph.chicago.rx.subscriber;

import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.annimon.stream.Stream;

import java.util.Collections;
import java.util.List;

import fr.cph.chicago.activity.StationActivity;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.util.Util;
import rx.Subscriber;

public class SubscriberTrainArrival extends Subscriber<TrainArrival> {

    private static final String TAG = SubscriberTrainArrival.class.getSimpleName();

    private final StationActivity activity;
    private final SwipeRefreshLayout swipeRefreshLayout;

    public SubscriberTrainArrival(@NonNull final StationActivity activity, @NonNull final SwipeRefreshLayout swipeRefreshLayout) {
        this.activity = activity;
        this.swipeRefreshLayout = swipeRefreshLayout;
    }

    @Override
    public void onNext(final TrainArrival trainArrival) {
        Log.d(TAG, "Found train arrival: " + trainArrival);
        final List<Eta> etas;
        if (trainArrival != null) {
            etas = trainArrival.getEtas();
        } else {
            etas = Collections.emptyList();
        }
        activity.hideAllArrivalViews();
        Stream.of(etas).forEach(activity::drawAllArrivalsTrain);
    }

    @Override
    public void onError(final Throwable e) {
        Log.e(TAG, "Error while getting trains arrival time: " + e.getMessage(), e);
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
        Util.showNetworkErrorMessage(swipeRefreshLayout);
    }

    @Override
    public void onCompleted() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
