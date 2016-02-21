package fr.cph.chicago.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.adapter.SearchAdapter;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.util.Util;

import java.util.List;

public class SearchActivity extends AppCompatActivity {

	private MenuItem searchItem;

	private SearchView searchView;

	private SearchAdapter searchAdapter;

	private List<BikeStation> bikeStations;

	@Override
	protected void onCreate(final Bundle state) {
		super.onCreate(state);
		ChicagoTracker.checkTrainData(this);
		ChicagoTracker.checkBusData(this);
		if (!this.isFinishing()) {
			setContentView(R.layout.search_layout);

			Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
			setSupportActionBar(toolbar);
			//getSupportActionBar().setLogo(R.drawable.ic_arrow_back_white_24dp);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);


//			if (Util.isNetworkAvailable()) {
//				searchAdapter = new SearchAdapter(this, container);
//				// FIXME possible bug without that.
//				bikeStations = getIntent().getExtras().getParcelableArrayList("bikeStations");
//				//handleIntent(getIntent());
//				//setListAdapter(searchAdapter);
//			} else {
//				Toast.makeText(ChicagoTracker.getAppContext(), "No network connection detected!", Toast.LENGTH_SHORT).show();
//			}

			// Associate searchable configuration with the SearchView
			SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

			searchView = new SearchView(getSupportActionBar().getThemedContext());
			searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
			searchView.setSubmitButtonEnabled(true);
			searchView.setIconifiedByDefault(true);
			searchView.setMaxWidth(1000);

			SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete) searchView
					.findViewById(android.support.v7.appcompat.R.id.search_src_text);

			// Collapse the search menu when the user hits the back key
			searchAutoComplete.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (!hasFocus)
						showSearch(false);
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		searchItem = menu.add(android.R.string.search_go);
		searchItem.setIcon(R.drawable.ic_search_white_24dp);
		MenuItemCompat.setActionView(searchItem, searchView);
		MenuItemCompat.setShowAsAction(searchItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS | MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		showSearch(false);
		Bundle extras = intent.getExtras();
		String userQuery = String.valueOf(extras.get(SearchManager.USER_QUERY));
		String query = String.valueOf(extras.get(SearchManager.QUERY));

		Toast.makeText(this, "query: " + query + " user_query: " + userQuery, Toast.LENGTH_SHORT).show();
	}

	protected void showSearch(boolean visible) {
		if (visible)
			MenuItemCompat.expandActionView(searchItem);
		else
			MenuItemCompat.collapseActionView(searchItem);
	}

	@Override
	public boolean onSearchRequested() {
		showSearch(true);
		// dont show the built-in search dialog
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}
}
