package fr.cph.chicago.listener;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.cph.chicago.App;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.BusMapActivity;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.entity.BusDetailsDTO;

public class BusMapOnClickListener implements View.OnClickListener {

    private final MainActivity activity;
    private final List<BusDetailsDTO> busDetailsDTOs;
    private final String busRouteId;

    public BusMapOnClickListener(final MainActivity activity, final List<BusDetailsDTO> busDetailsDTOs, final String busRouteId){
        this.activity = activity;
        this.busDetailsDTOs = busDetailsDTOs;
        this.busRouteId = busRouteId;
    }

    @Override
    public void onClick(final View v) {
        final Set<String> bounds = new HashSet<>();
        for (final BusDetailsDTO busDetail : busDetailsDTOs) {
            bounds.add(busDetail.getBound());
        }
        final Intent intent = new Intent(App.getContext(), BusMapActivity.class);
        final Bundle extras = new Bundle();
        extras.putString(activity.getString(R.string.bundle_bus_route_id), busRouteId);
        extras.putStringArray(activity.getString(R.string.bundle_bus_bounds), bounds.toArray(new String[bounds.size()]));
        intent.putExtras(extras);
        activity.startActivity(intent);
    }
}
