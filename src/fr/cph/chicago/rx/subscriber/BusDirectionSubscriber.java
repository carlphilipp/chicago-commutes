package fr.cph.chicago.rx.subscriber;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.util.List;

import fr.cph.chicago.R;
import fr.cph.chicago.app.activity.BusBoundActivity;
import fr.cph.chicago.app.activity.BusMapActivity;
import fr.cph.chicago.app.adapter.PopupBusAdapter;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.enumeration.BusDirection;
import fr.cph.chicago.util.Util;
import rx.Subscriber;

public class BusDirectionSubscriber extends Subscriber<BusDirections> {

    private final Activity activity;
    private final ViewGroup parent;
    private final BusRoute busRoute;
    private final View convertView;

    public BusDirectionSubscriber(@NonNull final Activity activity, @NonNull final ViewGroup parent, @NonNull final View convertView, @NonNull final BusRoute busRoute) {
        this.busRoute = busRoute;
        this.activity = activity;
        this.convertView = convertView;
        this.parent = parent;
    }

    @Override
    public void onNext(final BusDirections onNext) {
        if (onNext != null) {
            final List<BusDirection> busDirections = onNext.getLBusDirection();
            final List<String> data = Stream.of(busDirections)
                .map(busDir -> busDir.getBusDirectionEnum().toString())
                .collect(Collectors.toList());
            data.add(activity.getString(R.string.message_see_all_buses_on_line) + onNext.getId());

            final View popupView = activity.getLayoutInflater().inflate(R.layout.popup_bus, parent, false);
            final ListView listView = (ListView) popupView.findViewById(R.id.details);
            final PopupBusAdapter ada = new PopupBusAdapter(activity, data);
            listView.setAdapter(ada);

            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setAdapter(ada, (dialog, pos) -> {
                final Bundle extras = new Bundle();
                if (pos != data.size() - 1) {
                    final Intent intent = new Intent(activity, BusBoundActivity.class);
                    extras.putString(activity.getString(R.string.bundle_bus_route_id), busRoute.getId());
                    extras.putString(activity.getString(R.string.bundle_bus_route_name), busRoute.getName());
                    extras.putString(activity.getString(R.string.bundle_bus_bound), busDirections.get(pos).getBusTextReceived());
                    extras.putString(activity.getString(R.string.bundle_bus_bound_title), busDirections.get(pos).getBusDirectionEnum().toString());
                    intent.putExtras(extras);
                    activity.startActivity(intent);
                } else {
                    final String[] busDirectionArray = new String[busDirections.size()];
                    int i = 0;
                    for (final BusDirection busDir : busDirections) {
                        busDirectionArray[i++] = busDir.getBusDirectionEnum().toString();
                    }
                    final Intent intent = new Intent(activity, BusMapActivity.class);
                    extras.putString(activity.getString(R.string.bundle_bus_route_id), onNext.getId());
                    extras.putStringArray(activity.getString(R.string.bundle_bus_bounds), busDirectionArray);
                    intent.putExtras(extras);
                    activity.startActivity(intent);
                }
            });
            builder.setOnCancelListener(dialog -> convertView.setVisibility(LinearLayout.GONE));
            final int[] screenSize = Util.getScreenSize(activity.getApplicationContext());
            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getWindow().setLayout((int) (screenSize[0] * 0.7), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onError(final Throwable throwable) {
        Util.showOopsSomethingWentWrong(convertView);
        convertView.setVisibility(LinearLayout.GONE);
    }

    @Override
    public void onCompleted() {
        convertView.setVisibility(LinearLayout.GONE);
    }
}
