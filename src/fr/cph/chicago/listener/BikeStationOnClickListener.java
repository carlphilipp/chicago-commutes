package fr.cph.chicago.listener;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import fr.cph.chicago.App;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.BikeStationActivity;
import fr.cph.chicago.entity.BikeStation;

public class BikeStationOnClickListener implements View.OnClickListener {

    private final BikeStation station;

    public BikeStationOnClickListener(final BikeStation station) {
        this.station = station;
    }

    @Override
    public void onClick(final View v) {
        final Intent intent = new Intent(App.getContext(), BikeStationActivity.class);
        final Bundle extras = new Bundle();
        extras.putParcelable(App.getContext().getString(R.string.bundle_bike_station), station);
        intent.putExtras(extras);
        App.getContext().startActivity(intent);
    }
}
