/**
 * Copyright 2016 Carl-Philipp Harmant
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.connection.DivvyConnect;
import fr.cph.chicago.data.AlertData;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.fragment.AlertFragment;
import fr.cph.chicago.fragment.BikeFragment;
import fr.cph.chicago.fragment.BusFragment;
import fr.cph.chicago.fragment.FavoritesFragment;
import fr.cph.chicago.fragment.MapFragment;
import fr.cph.chicago.fragment.NearbyFragment;
import fr.cph.chicago.fragment.SettingsFragment;
import fr.cph.chicago.fragment.TrainFragment;
import fr.cph.chicago.fragment.drawer.NavigationDrawerFragment;
import fr.cph.chicago.json.Json;
import fr.cph.chicago.task.GlobalConnectTask;
import fr.cph.chicago.util.Util;
import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Activity that is the using loading fragments as needed.
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks {
	/**
	 * Tag
	 **/
	private static final String TAG = "MainActivity";
	/**
	 * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
	 **/
	private NavigationDrawerFragment navigationDrawerFragment;
	/**
	 * Favorites fragment
	 **/
	private FavoritesFragment favoritesFragment;
	/**
	 * Train fragment
	 **/
	private TrainFragment trainFragment;
	/**
	 * Bus Fragment
	 **/
	private BusFragment busFragment;
	/**
	 * Bike Fragment
	 **/
	private BikeFragment bikeFragment;
	/**
	 * Nearby fragment
	 **/
	private NearbyFragment nearbyFragment;
	/**
	 * Alert Fragment
	 **/
	private AlertFragment alertFragment;
	/**
	 * Map fragment
	 **/
	private MapFragment mapFragment;
	/**
	 * Settings fragment
	 **/
	private SettingsFragment settingsFragment;
	/**
	 * Title
	 **/
	private CharSequence title;
	/**
	 * Menu
	 **/
	private Menu menu;
	/**
	 * Current position
	 **/
	private int currentPosition;
	/**
	 * Drawer favorites
	 **/
	private static final int POSITION_FAVORITES = 0;
	/**
	 * Drawer Train
	 **/
	private static final int POSITION_TRAIN = 1;
	/**
	 * Drawer Bus
	 **/
	private static final int POSITION_BUS = 2;
	/**
	 * Drawer Divvy
	 **/
	private static final int POSITION_DIVVY = 3;
	/**
	 * Drawer Nearby
	 **/
	private static final int POSITION_NEARBY = 4;
	/**
	 * Drawer Alerts
	 **/
	private static final int POSITION_ALERTS = 5;
	/**
	 * Drawer Map
	 **/
	private static final int POSITION_MAP = 6;
	/**
	 * Drawer Settings
	 **/
	private static final int POSITION_SETTINGS = 7;

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(new CalligraphyContextWrapper(newBase));
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!isFinishing()) {
			// Handle onStop event where bus data has been switched to null
			if (savedInstanceState != null) {
				boolean first = savedInstanceState.getBoolean("first", false);
				if (!first) {
					DataHolder dataHolder = DataHolder.getInstance();
					BusData busData = BusData.getInstance();
					if (busData.readAllBusStops() == null || busData.readAllBusStops().size() == 0) {
						busData.readBusStops();
						dataHolder.setBusData(busData);
					}
					TrainData trainData = new TrainData();
					if (trainData.isStationNull() || trainData.isStopsNull()) {
						trainData.read();
						dataHolder.setTrainData(trainData);
					}
				}
			}

			new LoadData().execute();

			setContentView(R.layout.activity_main);

			ChicagoTracker.container = (FrameLayout) findViewById(R.id.container);
			ChicagoTracker.container.getForeground().setAlpha(0);

			navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
			title = getTitle();
			// Set up the drawer.
			navigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

			// Preventing keyboard from moving background when showing up
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

			displayUpdatePanel();
		}
	}

	@Override
	public final void onResume() {
		super.onResume();
	}

	@Override
	public final void onNavigationDrawerItemSelected(final int position) {
		int oldPosition = currentPosition;
		currentPosition = position;
		// update the main content by replacing fragments
		final FragmentManager fragmentManager = getFragmentManager();
		final FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		switch (position) {
		case POSITION_FAVORITES:
			if (favoritesFragment == null) {
				favoritesFragment = FavoritesFragment.newInstance(position + 1);
			}
			if (!this.isFinishing()) {
				ft.replace(R.id.container, favoritesFragment).commit();
			}
			break;
		case POSITION_TRAIN:
			if (trainFragment == null) {
				trainFragment = TrainFragment.newInstance(position + 1);
			}
			if (!this.isFinishing()) {
				ft.replace(R.id.container, trainFragment).commit();
			}
			break;
		case POSITION_BUS:
			if (busFragment == null) {
				busFragment = BusFragment.newInstance(position + 1);
			}
			if (!this.isFinishing()) {
				ft.replace(R.id.container, busFragment).commit();
			}
			break;
		case POSITION_DIVVY:
			if (bikeFragment == null) {
				bikeFragment = BikeFragment.newInstance(position + 1);
			}
			if (!this.isFinishing()) {
				ft.replace(R.id.container, bikeFragment).commit();
			}
			break;
		case POSITION_NEARBY:
			if (nearbyFragment == null) {
				nearbyFragment = NearbyFragment.newInstance(position + 1);
				if (!this.isFinishing()) {
					ft.replace(R.id.container, nearbyFragment).commit();
				}
			} else {
				if (oldPosition == 4) {
					fragmentManager.beginTransaction().commit();
				} else {
					if (!this.isFinishing()) {
						ft.replace(R.id.container, nearbyFragment).commit();
					}
				}
			}
			break;
		case POSITION_ALERTS:
			if (alertFragment == null) {
				alertFragment = AlertFragment.newInstance(position + 1);
			}
			if (!this.isFinishing()) {
				ft.replace(R.id.container, alertFragment).commit();
			}
			break;
		case POSITION_MAP:
			if (mapFragment == null) {
				mapFragment = MapFragment.newInstance(position + 1);
			}
			if (!this.isFinishing()) {
				ft.replace(R.id.container, mapFragment).commit();
			}
			break;
		case POSITION_SETTINGS:
			if (settingsFragment == null) {
				settingsFragment = SettingsFragment.newInstance(position + 1);
			}
			if (!this.isFinishing()) {
				ft.replace(R.id.container, settingsFragment).commit();
			}
			break;
		}
	}

	/**
	 * Change title
	 *
	 * @param number the tab number
	 */
	public final void onSectionAttached(final int number) {
		switch (number) {
		case POSITION_FAVORITES + 1:
			title = getString(R.string.favorites);
			break;
		case POSITION_TRAIN + 1:
			title = getString(R.string.train);
			break;
		case POSITION_BUS + 1:
			title = getString(R.string.bus);
			break;
		case POSITION_DIVVY + 1:
			title = getString(R.string.divvy);
			break;
		case POSITION_NEARBY + 1:
			title = getString(R.string.nearby);
			break;
		case POSITION_ALERTS + 1:
			title = getString(R.string.alerts);
			break;
		case POSITION_MAP + 1:
			title = getString(R.string.map);
			break;
		case POSITION_SETTINGS + 1:
			title = getString(R.string.settings);
			break;
		}
		restoreActionBar();
	}

	/**
	 * Restore action bar
	 */
	public final void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(title);
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

	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			if (currentPosition != POSITION_DIVVY && currentPosition != POSITION_NEARBY && currentPosition != POSITION_ALERTS) {
				MenuItem menuItem = item;
				menuItem.setActionView(R.layout.progressbar);
				menuItem.expandActionView();

				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
				boolean loadTrain = sharedPref.getBoolean("cta_train", true);
				boolean loadBus = sharedPref.getBoolean("cta_bus", true);
				boolean loadBike = sharedPref.getBoolean("divvy_bike", true);
				boolean loadAlert = sharedPref.getBoolean("cta_alert", true);

				MultiMap<String, String> params = new MultiValueMap<String, String>();
				List<Integer> trainFavorites = Preferences.getTrainFavorites(ChicagoTracker.PREFERENCE_FAVORITES_TRAIN);
				for (Integer fav : trainFavorites) {
					params.put("mapid", String.valueOf(fav));
				}
				MultiMap<String, String> params2 = new MultiValueMap<String, String>();
				List<String> busFavorites = Preferences.getBusFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BUS);
				for (String str : busFavorites) {
					String[] fav = Util.decodeBusFavorite(str);
					params2.put("rt", fav[0]);
					params2.put("stpid", fav[1]);
				}
				GlobalConnectTask task;
				try {
					task = new GlobalConnectTask(favoritesFragment, FavoritesFragment.class, CtaRequestType.TRAIN_ARRIVALS, params,
							CtaRequestType.BUS_ARRIVALS, params2, loadTrain, loadBus, loadBike);
					task.execute((Void) null);
				} catch (ParserException e) {
					ChicagoTracker.displayError(this, e);
					return true;
				}
				// Google analytics
				if (loadTrain) {
					Util.trackAction(MainActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_train,
							R.string.analytics_action_get_train_arrivals, 0);
				}
				if (loadBus) {
					Util.trackAction(MainActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus,
							R.string.analytics_action_get_bus_arrival, 0);
				}
				if (loadBike) {
					Util.trackAction(MainActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_divvy,
							R.string.analytics_action_get_divvy_all, 0);
				}
				// Check if bus/bike or alert data are not loaded. If not, load them.
				// Can happen when the app has been loaded without any data connection
				boolean loadData = false;
				DataHolder dataHolder = DataHolder.getInstance();

				BusData busData = dataHolder.getBusData();
				AlertData alertData = dataHolder.getAlertData();

				Bundle bundle = getIntent().getExtras();
				List<BikeStation> bikeStations = bundle.getParcelableArrayList("bikeStations");

				if (loadBus && busData.getRoutes() != null && busData.getRoutes().size() == 0) {
					loadData = true;
				}
				if (!loadData && loadAlert && alertData.getAlerts() != null && alertData.getAlerts().size() == 0) {
					loadData = true;
				}
				if (!loadData && loadBike && bikeStations == null) {
					loadData = true;
				}
				if (loadData) {
					startRefreshAnimation();
					new LoadData().execute();
				}
				Util.trackAction(this, R.string.analytics_category_ui, R.string.analytics_action_press, R.string.analytics_action_refresh_fav, 0);
			} else if (currentPosition == POSITION_NEARBY) {
				nearbyFragment.reloadData();
				Util.trackAction(this, R.string.analytics_category_ui, R.string.analytics_action_press, R.string.analytics_action_refresh_nearby, 0);
			}
			return false;
		case R.id.action_search:
			Util.trackAction(this, R.string.analytics_category_ui, R.string.analytics_action_press, R.string.analytics_action_search, 0);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public final void onBackPressed() {
		if (currentPosition != POSITION_FAVORITES) {
			navigationDrawerFragment.selectItem(0, true);
		} else {
			DataHolder.getInstance().setBusData(null);
			DataHolder.getInstance().setTrainData(null);
			DataHolder.getInstance().setAlertData(null);
			finish();
		}
	}

	/**
	 * Load animation in menu
	 */
	public final void startRefreshAnimation() {
		if (menu != null) {
			MenuItem refreshMenuItem = menu.findItem(R.id.action_refresh);
			refreshMenuItem.setActionView(R.layout.progressbar);
			refreshMenuItem.expandActionView();
		}
	}

	/**
	 * Stop animation in menu
	 */
	public final void stopRefreshAnimation() {
		if (menu != null) {
			MenuItem refreshMenuItem = menu.findItem(R.id.action_refresh);
			refreshMenuItem.collapseActionView();
			refreshMenuItem.setActionView(null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		this.setIntent(intent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#startActivity(android.content.Intent)
	 */
	@Override
	public void startActivity(Intent intent) {
		// check if search intent
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			ArrayList<BikeStation> bikeStations = getIntent().getExtras().getParcelableArrayList("bikeStations");
			intent.putParcelableArrayListExtra("bikeStations", bikeStations);
		}
		super.startActivity(intent);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("first", false);
		super.onSaveInstanceState(outState);
	}

	private void displayUpdatePanel() {
		try {
			String versionName = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			String versionNamePreferences = sharedPref.getString("version.name", null);
			if (versionNamePreferences == null || !versionNamePreferences.equals(versionName)) {
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putString("version.name", versionName);
				editor.commit();

				final Dialog dialog = new Dialog(this);
				dialog.setContentView(R.layout.update);
				dialog.setTitle("Update");

				InputStreamReader is = new InputStreamReader(ChicagoTracker.getAppContext().getAssets().open("update.txt"));
				BufferedReader br = new BufferedReader(is);
				String read = br.readLine();
				StringBuilder sb = new StringBuilder();
				while (read != null) {
					sb.append(read).append("\n");
					read = br.readLine();
				}

				TextView text = (TextView) dialog.findViewById(R.id.updateText);
				text.setText(sb.toString());

				Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
				dialogButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});

				Display display = getWindowManager().getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);
				int width = size.x;
				int newWidth = width - (width * 20 / 100);
				int height = size.y;
				int newHeight = height - (height * 20 / 100);

				dialog.getWindow().setLayout(newWidth, newHeight);

				dialog.show();
			}

		} catch (NameNotFoundException | IOException e) {
			Log.w(TAG, e.getMessage(), e);
		}
	}

	public final class LoadData extends AsyncTask<Void, Void, Void> {
		/**
		 * Bus data
		 **/
		private BusData busData;
		/**
		 * Alert data
		 **/
		private AlertData alertData;
		/**
		 * Bike stations
		 **/
		private List<BikeStation> bikeStations;
		/**
		 * Load bikes
		 **/
		private boolean loadBike;

		@Override
		protected final Void doInBackground(final Void... params) {

			DataHolder dataHolder = DataHolder.getInstance();
			this.busData = dataHolder.getBusData();

			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
			boolean loadBus = sharedPref.getBoolean("cta_bus", true);
			boolean loadAlert = false;
			this.loadBike = false;

			if (sharedPref.contains("cta_alert")) {
				loadAlert = sharedPref.getBoolean("cta_alert", true);
			} else {
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putBoolean("cta_alert", false);
				editor.commit();
			}
			if (sharedPref.contains("divvy_bike")) {
				this.loadBike = sharedPref.getBoolean("divvy_bike", true);
			} else {
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putBoolean("divvy_bike", false);
				editor.commit();
			}

			// Load bus API data
			if (loadBus) {
				try {
					this.busData.loadBusRoutes();
					Util.trackAction(MainActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus,
							R.string.analytics_action_get_bus_routes, 0);
					publishProgress();
				} catch (final ParserException | ConnectException e) {
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(MainActivity.this, "Bus error: " + e.getMessage(), Toast.LENGTH_LONG).show();
						}
					});
					Log.e(TAG, e.getMessage(), e);
				}
			}

			// Load alert API data
			if (loadAlert) {
				try {
					this.alertData = AlertData.getInstance();
					this.alertData.loadGeneralAlerts();
					Util.trackAction(MainActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_alert,
							R.string.analytics_action_get_alert_general, 0);
					publishProgress();
				} catch (ParserException | ConnectException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}

			// Load divvy
			this.bikeStations = new ArrayList<>();
			if (loadBike) {
				try {
					Json json = new Json();
					DivvyConnect divvyConnect = DivvyConnect.getInstance();
					String bikeContent = divvyConnect.connect();
					this.bikeStations = json.parseStations(bikeContent);
					Collections.sort(this.bikeStations, Util.BIKE_COMPARATOR_NAME);
					Util.trackAction(MainActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_divvy,
							R.string.analytics_action_get_divvy_all, 0);
					publishProgress();
				} catch (ConnectException | ParserException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
			return null;
		}

		@Override
		protected final void onProgressUpdate(Void... progress) {
			startRefreshAnimation();
		}

		@Override
		protected final void onPostExecute(final Void result) {
			// Put data into data holder
			DataHolder dataHolder = DataHolder.getInstance();
			dataHolder.setBusData(busData);
			dataHolder.setAlertData(alertData);

			if (loadBike) {
				getIntent().putParcelableArrayListExtra("bikeStations", (ArrayList<BikeStation>) bikeStations);
				onNewIntent(getIntent());
				if (favoritesFragment != null) {
					favoritesFragment.setBikeStations(bikeStations);
				}
			}
			if (currentPosition == POSITION_BUS && busFragment != null) {
				busFragment.update();
			}
			if (currentPosition == POSITION_ALERTS && alertFragment != null) {
				alertFragment.loadList();
			}
			stopRefreshAnimation();
		}
	}
}
