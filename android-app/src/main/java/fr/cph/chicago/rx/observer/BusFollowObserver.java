package fr.cph.chicago.rx.observer;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

import fr.cph.chicago.R;
import fr.cph.chicago.core.activity.BusMapActivity;
import fr.cph.chicago.core.adapter.BusMapSnippetAdapter;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.util.Util;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class BusFollowObserver implements Observer<List<BusArrival>> {

    private static final String TAG = BusFollowObserver.class.getSimpleName();

    private final BusMapActivity activity;
    private final View view;
    private final View layout;
    private final boolean loadAll;

    public BusFollowObserver(final BusMapActivity activity, final View layout, final View view, final boolean loadAll) {
        this.activity = activity;
        this.layout = layout;
        this.view = view;
        this.loadAll = loadAll;
    }

    @Override
    public void onSubscribe(Disposable d) {
    }

    @Override
    public void onNext(List<BusArrival> busArrivals) {
        if (!loadAll && busArrivals.size() > 7) {
            busArrivals = busArrivals.subList(0, 6);
            final BusArrival busArrival = new BusArrival(new Date(), "added bus", view.getContext().getString(R.string.bus_all_results), 0, 0, "", "", StringUtils.EMPTY, new Date(), false);
            busArrivals.add(busArrival);
        }
        final ListView arrivals = view.findViewById(R.id.arrivals);
        final TextView error = view.findViewById(R.id.error);
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
        Util.handleConnectOrParserException(throwable, null, layout, layout);
        Log.e(TAG, throwable.getMessage(), throwable);
    }

    @Override
    public void onComplete() {
    }
}
