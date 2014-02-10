package fr.cph.chicago.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.adapter.SearchAdapter;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.enumeration.TrainLine;

public class SearchActivity extends ListActivity {

	private Menu menu;
	private SearchAdapter ada;

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		ada = new SearchAdapter(this);
		handleIntent(getIntent());
		setListAdapter(ada);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		this.menu = menu;
		getMenuInflater().inflate(R.menu.main, menu);
		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		return super.onCreateOptionsMenu(menu);
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			Toast.makeText(this.getBaseContext(), query, Toast.LENGTH_LONG).show();

			DataHolder dataHolder = DataHolder.getInstance();
			BusData busData = dataHolder.getBusData();
			TrainData trainData = dataHolder.getTrainData();

			List<Station> found = new ArrayList<Station>();

			for (Entry<TrainLine, List<Station>> e : trainData.getAllStations().entrySet()) {
				for (Station station : e.getValue()) {
					boolean res = StringUtils.containsIgnoreCase(station.getName(), query);
					if (res) {
						if(!found.contains(station)){
							found.add(station);
						}
					}
				}
			}
			ada.setTrains(found);
			ada.notifyDataSetChanged();
		}
	}
}
