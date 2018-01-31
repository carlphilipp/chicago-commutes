package fr.cph.chicago.core.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import butterknife.ButterKnife;
import fr.cph.chicago.R;
import fr.cph.chicago.rx.ObservableUtil;


public class AlertActivity extends Activity {

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!this.isFinishing()) {
            setContentView(R.layout.activity_bus);
            ButterKnife.bind(this);
            final Bundle extras = getIntent().getExtras();
            final String routeId = extras.getString("routeId");
            Log.i("DERP", "Route id: " + routeId);
            ObservableUtil.INSTANCE.createAlertRouteObservable(routeId)
                .subscribe(routeAlertsDTOS -> {
                    Log.i("DERP", "Result: " + routeAlertsDTOS);
                });
        }
    }
}
