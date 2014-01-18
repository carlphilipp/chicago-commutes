package fr.cph.chicago.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import fr.cph.chicago.R;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.adapter.TrainAdapter;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.entity.enumeration.TrainLine;

public class TrainStationActivity extends ListActivity {

	private TrainData data;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load data
		DataHolder dataHolder = DataHolder.getInstance();
		this.data = dataHolder.getTrainData();
		
		final TrainLine line = TrainLine.fromString(getIntent().getExtras().getString("line"));
		
		this.setTitle(line.toStringWithLine());
		
		setContentView(R.layout.activity_train_station);
		
		TrainAdapter ada = new TrainAdapter(line);
		setListAdapter(ada);
		ListView listView = getListView();
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
				Intent intent = new Intent(ChicagoTracker.getAppContext(), StationActivity.class);
				Bundle extras = new Bundle();
				extras.putInt("stationId", data.getStationsForLine(line).get(position).getId());
				intent.putExtras(extras);
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
