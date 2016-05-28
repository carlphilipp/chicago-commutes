package fr.cph.chicago.app.listener;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import fr.cph.chicago.app.App;
import fr.cph.chicago.R;
import fr.cph.chicago.app.activity.BikeStationActivity;
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
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        App.getContext().startActivity(intent);
    }
}
