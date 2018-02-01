package fr.cph.chicago.core.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.cph.chicago.R;
import fr.cph.chicago.core.adapter.AlertRouteAdapter;
import fr.cph.chicago.rx.ObservableUtil;


public class AlertActivity extends Activity {

    @BindView(R.id.alert_route_list)
    ListView listView;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!this.isFinishing()) {
            setContentView(R.layout.activity_alert);
            ButterKnife.bind(this);
            final Bundle extras = getIntent().getExtras();
            final String routeId = extras.getString("routeId", "");
            ObservableUtil.INSTANCE.createAlertRouteObservable(routeId)
                .subscribe(routeAlertsDTOS -> {
                    Log.i("AlertActivity", "Result: " + routeAlertsDTOS);
                    final AlertRouteAdapter ada = new AlertRouteAdapter(routeAlertsDTOS);
                    listView.setAdapter(ada);
                });
        }
    }
}
