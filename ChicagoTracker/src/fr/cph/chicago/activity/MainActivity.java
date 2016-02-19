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

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.data.AlertData;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.fragment.AlertFragment;
import fr.cph.chicago.fragment.BikeFragment;
import fr.cph.chicago.fragment.BusFragment;
import fr.cph.chicago.fragment.FavoritesFragment;
import fr.cph.chicago.fragment.MapFragment;
import fr.cph.chicago.fragment.NearbyFragment;
import fr.cph.chicago.fragment.SettingsFragment;
import fr.cph.chicago.fragment.TrainFragment;
import fr.cph.chicago.fragment.drawer.NavigationDrawerFragment;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

	/**
	 * Favorites fragment
	 **/
	private FavoritesFragment mFavoritesFragment;

	private TrainFragment mTrainFragment;

	private Toolbar toolbar;
	private NavigationView mDrawer;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle drawerToggle;
	private int mSelectedId;
	private CharSequence title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		ChicagoTracker.container = (FrameLayout) findViewById(R.id.container);
		ChicagoTracker.container.getForeground().setAlpha(0);

		setToolbar();
		initView();

		drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		mDrawerLayout.setDrawerListener(drawerToggle);
		drawerToggle.syncState();
		//default it set first item as selected
		mSelectedId = savedInstanceState == null ? R.id.navigation_item_1 : savedInstanceState.getInt("SELECTED_ID");
		itemSelection(mSelectedId);

		title = getTitle();

		FragmentManager fragmentManager = getSupportFragmentManager();
		mFavoritesFragment = FavoritesFragment.newInstance(mSelectedId + 1);
		fragmentManager.beginTransaction().replace(R.id.container, mFavoritesFragment).commit();
	}

	private void setToolbar() {
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
		}
	}

	private void initView() {
		mDrawer = (NavigationView) findViewById(R.id.main_drawer);
		mDrawer.setNavigationItemSelectedListener(this);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
	}

	private void itemSelection(int mSelectedId) {
		FragmentManager fragmentManager = getSupportFragmentManager();

		switch (mSelectedId) {

		case R.id.navigation_item_1:
			title = getString(R.string.favorites);
			if (mFavoritesFragment == null) {
				mFavoritesFragment = FavoritesFragment.newInstance(mSelectedId + 1);
			}
			if (!this.isFinishing()) {
				fragmentManager.beginTransaction().replace(R.id.container, mFavoritesFragment).commit();
			}
			mDrawerLayout.closeDrawer(GravityCompat.START);
			break;

		case R.id.navigation_item_2:
			title = getString(R.string.train);
			if (mTrainFragment == null) {
				mTrainFragment = TrainFragment.newInstance(mSelectedId + 1);
			}
			if (!this.isFinishing()) {
				fragmentManager.beginTransaction().replace(R.id.container, mTrainFragment).commit();
			}
			mDrawerLayout.closeDrawer(GravityCompat.START);
			break;

		case R.id.navigation_item_3:
			title = getString(R.string.bus);
			if (busFragment == null) {
				busFragment = BusFragment.newInstance(mSelectedId + 1);
			}
			if (!this.isFinishing()) {
				fragmentManager.beginTransaction().replace(R.id.container, busFragment).commit();
			}
			mDrawerLayout.closeDrawer(GravityCompat.START);
			break;

		case R.id.navigation_item_4:
			title = getString(R.string.divvy);
			if (bikeFragment == null) {
				bikeFragment = BikeFragment.newInstance(mSelectedId + 1);
			}
			if (!this.isFinishing()) {
				fragmentManager.beginTransaction().replace(R.id.container, bikeFragment).commit();
			}
			mDrawerLayout.closeDrawer(GravityCompat.START);
			break;

		case R.id.navigation_item_5:
			title = getString(R.string.nearby);
			if (nearbyFragment == null) {
				nearbyFragment = NearbyFragment.newInstance(mSelectedId + 1);
			}
			if (!this.isFinishing()) {
				fragmentManager.beginTransaction().replace(R.id.container, nearbyFragment).commit();
			}
			mDrawerLayout.closeDrawer(GravityCompat.START);
			break;

		case R.id.navigation_item_6:
			title = getString(R.string.alerts);
			if (mapFragment == null) {
				mapFragment = MapFragment.newInstance(mSelectedId + 1);
			}
			if (!this.isFinishing()) {
				fragmentManager.beginTransaction().replace(R.id.container, mapFragment).commit();
			}
			mDrawerLayout.closeDrawer(GravityCompat.START);
			break;

		case R.id.navigation_item_7:
			title = getString(R.string.map);
			if (nearbyFragment == null) {
				nearbyFragment = NearbyFragment.newInstance(mSelectedId + 1);
			}
			if (!this.isFinishing()) {
				fragmentManager.beginTransaction().replace(R.id.container, nearbyFragment).commit();
			}
			mDrawerLayout.closeDrawer(GravityCompat.START);
			break;

		case R.id.navigation_item_8:
			title = getString(R.string.settings);
			if (settingsFragment == null) {
				//settingsFragment = SettingsFragment.newInstance(mSelectedId + 1);
			}
			if (!this.isFinishing()) {
				//fragmentManager.beginTransaction().replace(R.id.container, settingsFragment).commit();
			}
			mDrawerLayout.closeDrawer(GravityCompat.START);
			break;
		}
		toolbar.setTitle(title);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem menuItem) {
		menuItem.setChecked(true);
		mSelectedId = menuItem.getItemId();
		itemSelection(mSelectedId);
		return true;
	}

	@Override
	public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
		super.onSaveInstanceState(outState, outPersistentState);
		//save selected item so it will remains same even after orientation change
		outState.putInt("SELECTED_ID", mSelectedId);
	}

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
		//restoreActionBar();
	}

	public final void startRefreshAnimation() {
		//		if (menu != null) {
		//			MenuItem refreshMenuItem = menu.findItem(R.id.action_refresh);
		//			refreshMenuItem.setActionView(R.layout.progressbar);
		//			refreshMenuItem.expandActionView();
		//		}
	}

	public final void stopRefreshAnimation() {
		if (menu != null) {
			MenuItem refreshMenuItem = menu.findItem(R.id.action_refresh);
			refreshMenuItem.collapseActionView();
			refreshMenuItem.setActionView(null);
		}
	}

	//@Override
	public void onNewIntent(Intent intent) {
		//super.onNewIntent(intent);
		//this.setIntent(intent);
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
			return null;
		}

		@Override
		protected final void onProgressUpdate(Void... progress) {
			startRefreshAnimation();
		}

		@Override
		protected final void onPostExecute(final Void result) {
		}
	}
}
