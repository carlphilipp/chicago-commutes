package fr.cph.chicago.rx.subscriber;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import fr.cph.chicago.App;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.BusMapActivity;
import fr.cph.chicago.adapter.BusMapSnippetAdapter;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.util.Util;
import rx.Subscriber;

public class BusFollowSubscriber extends Subscriber<List<BusArrival>> {

    private final BusMapActivity activity;
    private final View view;
    final boolean loadAll;

    public BusFollowSubscriber(final BusMapActivity activity, final View view, final boolean loadAll) {
        this.activity = activity;
        this.view = view;
        this.loadAll = loadAll;
    }

    @Override
    public void onNext(@NonNull List<BusArrival> onNext) {
        if (!loadAll && onNext.size() > 7) {
            onNext = onNext.subList(0, 6);
            final BusArrival arrival = new BusArrival();
            arrival.setStopName(App.getContext().getString(R.string.bus_all_results));
            arrival.setDly(false);
            onNext.add(arrival);
        }
        final ListView arrivals = (ListView) view.findViewById(R.id.arrivals);
        final TextView error = (TextView) view.findViewById(R.id.error);
        if (onNext.size() != 0) {
            final BusMapSnippetAdapter ada = new BusMapSnippetAdapter(activity, onNext);
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
        Util.showOopsSomethingWentWrong(view);
    }

    @Override
    public void onCompleted() {

    }
}
