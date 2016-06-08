package fr.cph.chicago.rx.subscriber;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import fr.cph.chicago.R;
import fr.cph.chicago.core.activity.BusMapActivity;
import fr.cph.chicago.core.adapter.BusMapSnippetAdapter;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.util.Util;
import rx.Subscriber;

public class BusFollowSubscriber extends Subscriber<List<BusArrival>> {

    private static final String TAG = BusFollowSubscriber.class.getSimpleName();

    private final BusMapActivity activity;
    private final View view;
    private final View layout;
    private final boolean loadAll;

    public BusFollowSubscriber(final BusMapActivity activity, final View layout, final View view, final boolean loadAll) {
        this.activity = activity;
        this.layout = layout;
        this.view = view;
        this.loadAll = loadAll;
    }

    @Override
    public void onNext(List<BusArrival> busArrivals) {
        if (!loadAll && busArrivals.size() > 7) {
            busArrivals = busArrivals.subList(0, 6);
            final BusArrival arrival = new BusArrival();
            arrival.setStopName(view.getContext().getString(R.string.bus_all_results));
            arrival.setDly(false);
            busArrivals.add(arrival);
        }
        final ListView arrivals = (ListView) view.findViewById(R.id.arrivals);
        final TextView error = (TextView) view.findViewById(R.id.error);
        if (busArrivals.size() != 0) {
            final BusMapSnippetAdapter ada = new BusMapSnippetAdapter(activity, busArrivals);
            arrivals.setAdapter(ada);
            arrivals.setVisibility(ListView.VISIBLE);
            error.setVisibility(TextView.GONE);
        } else {
            arrivals.setVisibility(ListView.GONE);
            error.setVisibility(TextView.VISIBLE);
        }
        activity.refreshInfoWindow();
    }

    @Override
    public void onError(@NonNull final Throwable throwable) {
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
