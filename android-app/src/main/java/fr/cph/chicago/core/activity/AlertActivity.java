package fr.cph.chicago.core.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.cph.chicago.R;
import fr.cph.chicago.core.adapter.AlertRouteAdapter;
import fr.cph.chicago.entity.dto.RouteAlertsDTO;
import fr.cph.chicago.rx.ObservableUtil;
import io.reactivex.functions.Consumer;


public class AlertActivity extends Activity {

    private final AlertConsumer alertConsumer = new AlertConsumer();

    @BindView(R.id.activity_alerts_swipe_refresh_layout)
    SwipeRefreshLayout scrollView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.alert_route_list)
    ListView listView;

    private String routeId;
    private String title;


    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!this.isFinishing()) {
            setContentView(R.layout.activity_alert);
            ButterKnife.bind(this);
            final Bundle extras = getIntent().getExtras();
            routeId = extras.getString("routeId", "");
            title = extras.getString("title", "<Template title>");
            scrollView.setOnRefreshListener(this::refreshData);
            refreshData();
            setToolBar();
        }
    }

    private void setToolBar() {
        toolbar.inflateMenu(R.menu.main);
        toolbar.setOnMenuItemClickListener((item -> {
            scrollView.setRefreshing(true);
            refreshData();
            return false;
        }));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(4);
        }
        toolbar.setTitle(title);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setOnClickListener(v -> finish());
    }

    private void refreshData() {
        ObservableUtil.INSTANCE.createAlertRouteObservable(routeId).subscribe(alertConsumer);
    }

    private class AlertConsumer implements Consumer<List<RouteAlertsDTO>> {

        @Override
        public void accept(List<RouteAlertsDTO> routeAlertsDTOS) throws Exception {
            final AlertRouteAdapter ada = new AlertRouteAdapter(routeAlertsDTOS);
            listView.setAdapter(ada);
            if (scrollView.isRefreshing()) {
                scrollView.setRefreshing(false);
            }
        }
    }
}
