package fr.cph.chicago.rx.observer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.util.List;

import fr.cph.chicago.R;
import fr.cph.chicago.core.App;
import fr.cph.chicago.core.activity.BusBoundActivity;
import fr.cph.chicago.core.activity.BusMapActivity;
import fr.cph.chicago.core.adapter.PopupBusAdapter;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.enumeration.BusDirection;
import fr.cph.chicago.util.Util;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class BusDirectionObserver implements Observer<BusDirections> {

    private static final String TAG = BusDirectionObserver.class.getSimpleName();

    private final ViewGroup parent;
    private final BusRoute busRoute;
    private final View convertView;

    public BusDirectionObserver(@NonNull final ViewGroup parent, @NonNull final View convertView, @NonNull final BusRoute busRoute) {
        this.busRoute = busRoute;
        this.convertView = convertView;
        this.parent = parent;
    }

    @Override
    public void onSubscribe(Disposable d) {
    }

    @Override
    public void onNext(final BusDirections busDirections) {
        if (busDirections != null) {
            final List<BusDirection> lBusDirections = busDirections.getLBusDirection();
            final List<String> data = Stream.of(lBusDirections)
                .map(busDir -> busDir.getBusDirectionEnum().toString())
                .collect(Collectors.toList());
            data.add(parent.getContext().getString(R.string.message_see_all_buses_on_line) + busDirections.getId());

            final LayoutInflater vi = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View popupView = vi.inflate(R.layout.popup_bus, parent, false);
            final ListView listView = (ListView) popupView.findViewById(R.id.details);
            final PopupBusAdapter ada = new PopupBusAdapter(parent.getContext().getApplicationContext(), data);
            listView.setAdapter(ada);

            final AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
            builder.setAdapter(ada, (dialog, pos) -> {
                final Bundle extras = new Bundle();
                if (pos != data.size() - 1) {
                    final Intent intent = new Intent(parent.getContext(), BusBoundActivity.class);
                    extras.putString(parent.getContext().getString(R.string.bundle_bus_route_id), busRoute.getId());
                    extras.putString(parent.getContext().getString(R.string.bundle_bus_route_name), busRoute.getName());
                    extras.putString(parent.getContext().getString(R.string.bundle_bus_bound), lBusDirections.get(pos).getBusTextReceived());
                    extras.putString(parent.getContext().getString(R.string.bundle_bus_bound_title), lBusDirections.get(pos).getBusDirectionEnum().toString());
                    intent.putExtras(extras);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    parent.getContext().getApplicationContext().startActivity(intent);
                } else {
                    final String[] busDirectionArray = new String[lBusDirections.size()];
                    int i = 0;
                    for (final BusDirection busDir : lBusDirections) {
                        busDirectionArray[i++] = busDir.getBusDirectionEnum().toString();
                    }
                    final Intent intent = new Intent(parent.getContext(), BusMapActivity.class);
                    extras.putString(parent.getContext().getString(R.string.bundle_bus_route_id), busDirections.getId());
                    extras.putStringArray(parent.getContext().getString(R.string.bundle_bus_bounds), busDirectionArray);
                    intent.putExtras(extras);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    parent.getContext().getApplicationContext().startActivity(intent);
                }
            });
            builder.setOnCancelListener(dialog -> convertView.setVisibility(LinearLayout.GONE));
            final AlertDialog dialog = builder.create();
            dialog.show();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout((int) (App.getScreenWidth() * 0.7), ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
    }

    @Override
    public void onError(final Throwable throwable) {
        Util.showOopsSomethingWentWrong(convertView);
        convertView.setVisibility(LinearLayout.GONE);
        Log.e(TAG, throwable.getMessage(), throwable);
    }

    @Override
    public void onComplete() {
        convertView.setVisibility(LinearLayout.GONE);
    }
}
