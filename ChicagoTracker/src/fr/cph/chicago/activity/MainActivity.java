/**
 * Copyright 2014 Carl-Philipp Harmant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.activity;

import java.util.List;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.xmlpull.v1.XmlPullParserException;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.fragment.BusFragment;
import fr.cph.chicago.fragment.FavoritesFragment;
import fr.cph.chicago.fragment.NavigationDrawerFragment;
import fr.cph.chicago.fragment.TrainFragment;
import fr.cph.chicago.task.CtaConnectTask2;
import fr.cph.chicago.util.Util;

;

public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

	/** Tag **/
	private static final String TAG = "MainActivity";

	/**
	 * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	private FavoritesFragment favoritesFragment;

	private TrainFragment trainFragment;

	private BusFragment busFragment;

	/**
	 * Used to store the last screen title. For use in {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

//	private TrainData data;

	private Menu menu;

	private int currentPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		this.data = new TrainData();
//		this.data.read();

//		DataHolder dataHolder = DataHolder.getInstance();
//		Log.i(TAG, " " + String.valueOf(dataHolder.getBusData() != null));
//		Log.i(TAG, " " + String.valueOf(dataHolder.getTrainData() != null));
//		dataHolder.setTrainData(this.data);

		// BusData busData = BusData.getInstance();
		// busData.read();
		// dataHolder.setBusData(busData);

//		new LoadBusRoutes().execute();

		setContentView(R.layout.activity_main);

		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();
		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		currentPosition = position;
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getFragmentManager();
		switch (position) {
		case 0:
			if (favoritesFragment == null) {
				favoritesFragment = FavoritesFragment.newInstance(position + 1);
			}
			fragmentManager.beginTransaction().replace(R.id.container, favoritesFragment).commit();
			break;
		case 1:
			if (trainFragment == null) {
				trainFragment = TrainFragment.newInstance(position + 1);
			}
			fragmentManager.beginTransaction().replace(R.id.container, trainFragment).commit();
			break;
		case 2:
			if (busFragment == null) {
				busFragment = BusFragment.newInstance(position + 1);
			}
			fragmentManager.beginTransaction().replace(R.id.container, busFragment).commit();
			break;
		case 3:
			Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
			break;
		case 4:
			Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
			break;
		}
	}

	public void onSectionAttached(int number) {
		switch (number) {
		case 1:
			mTitle = getString(R.string.favorites);
			break;
		case 2:
			mTitle = getString(R.string.train);
			break;
		case 3:
			mTitle = getString(R.string.bus);
			break;
		case 4:
			mTitle = getString(R.string.nearby);
			break;
		case 5:
			mTitle = getString(R.string.alerts);
			break;
		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			if (currentPosition == 1) {
				getMenuInflater().inflate(R.menu.global, menu);
			} else {
				getMenuInflater().inflate(R.menu.main, menu);
			}
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.action_settings:
			return true;
		case R.id.action_refresh:
			MenuItem menuItem = item;
			menuItem.setActionView(R.layout.progressbar);
			menuItem.expandActionView();

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
			CtaConnectTask2 task;
			try {
				task = new CtaConnectTask2(FavoritesFragment.class, CtaRequestType.TRAIN_ARRIVALS, params, CtaRequestType.BUS_ARRIVALS, params2);
				task.execute((Void) null);
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			}
			Toast.makeText(this, "Refresh...!", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.action_search:
			Toast.makeText(this, "Search... !", Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if (currentPosition != 0) {
			onNavigationDrawerItemSelected(0);
		} else {
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Exit application")
					.setMessage("Are you sure you want to exit?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}

					}).setNegativeButton("No", null).show();
		}
	}

	public void stopRefreshAnimation() {
		if (menu != null) {
			MenuItem refreshMenuItem = menu.findItem(R.id.action_refresh);
			refreshMenuItem.collapseActionView();
			refreshMenuItem.setActionView(null);
		}
	}
}
