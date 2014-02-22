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

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.Toast;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.fragment.AlertFragment;
import fr.cph.chicago.fragment.BusFragment;
import fr.cph.chicago.fragment.FavoritesFragment;
import fr.cph.chicago.fragment.MapFragment;
import fr.cph.chicago.fragment.NavigationDrawerFragment;
import fr.cph.chicago.fragment.NearbyFragment;
import fr.cph.chicago.fragment.TrainFragment;
import fr.cph.chicago.task.CtaConnectTask;
import fr.cph.chicago.util.Util;

/**
 * Activity that is the using loading fragments as needed.
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

	/** Fragment managing the behaviors, interactions and presentation of the navigation drawer. **/
	private NavigationDrawerFragment mNavigationDrawerFragment;
	/** Favorites fragment **/
	private FavoritesFragment favoritesFragment;
	/** Train fragment **/
	private TrainFragment trainFragment;
	/** Bus Fragment **/
	private BusFragment busFragment;
	/** Nearby fragment **/
	private NearbyFragment nearbyFragment;
	/** Alert Fragment **/
	private AlertFragment alertFragment;
	/** Map fragment **/
	private MapFragment mapFragment;
	/** Title **/
	private CharSequence mTitle;
	/** Menu **/
	private Menu menu;
	/** Current position **/
	private int currentPosition;

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		ChicagoTracker.container = (FrameLayout) findViewById(R.id.container);
		ChicagoTracker.container.getForeground().setAlpha(0);

		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();
		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

		// Preventing keyboard from moving background when showing up
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
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
		case 0:
			if (favoritesFragment == null) {
				favoritesFragment = FavoritesFragment.newInstance(position + 1);
			}
			ft.replace(R.id.container, favoritesFragment).commit();
			break;
		case 1:
			if (trainFragment == null) {
				trainFragment = TrainFragment.newInstance(position + 1);
			}
			ft.replace(R.id.container, trainFragment).commit();
			break;
		case 2:
			if (busFragment == null) {
				busFragment = BusFragment.newInstance(position + 1);
			}
			ft.replace(R.id.container, busFragment).commit();
			break;
		case 3:
			if (nearbyFragment == null) {
				nearbyFragment = NearbyFragment.newInstance(position + 1);
				ft.replace(R.id.container, nearbyFragment).commit();
			} else {
				if (oldPosition == 3) {
					fragmentManager.beginTransaction().commit();
				} else {
					ft.replace(R.id.container, nearbyFragment).commit();
				}
			}
			break;
		case 4:
			if (alertFragment == null) {
				alertFragment = AlertFragment.newInstance(position + 1);
			}
			ft.replace(R.id.container, alertFragment).commit();
			break;
		case 5:
			if (mapFragment == null) {
				mapFragment = MapFragment.newInstance(position + 1);
			}
			ft.replace(R.id.container, mapFragment).commit();
			break;
		}
	}

	/**
	 * Change title
	 * 
	 * @param number
	 *            the tab number
	 */
	public final void onSectionAttached(final int number) {
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
		case 6:
			mTitle = getString(R.string.map);
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
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		this.menu = menu;
		// if (!mNavigationDrawerFragment.isDrawerOpen()) {
		// Only show items in the action bar relevant to this screen
		// if the drawer is not showing. Otherwise, let the drawer
		// decide what to show in the action bar.
		// if (currentPosition == 1 || currentPosition == 5) {
		// getMenuInflater().inflate(R.menu.global, menu);
		// } else {
		getMenuInflater().inflate(R.menu.main, menu);
		// }
		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

		// restoreActionBar();
		return super.onCreateOptionsMenu(menu);
		// }
		// return super.onCreateOptionsMenu(menu);
	}

	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			if (currentPosition != 3) {
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
				CtaConnectTask task;
				try {
					task = new CtaConnectTask(favoritesFragment, FavoritesFragment.class, CtaRequestType.TRAIN_ARRIVALS, params,
							CtaRequestType.BUS_ARRIVALS, params2);
					task.execute((Void) null);
				} catch (ParserException e) {
					ChicagoTracker.displayError(this, e);
					return true;
				}
				Toast.makeText(this, "Refresh...!", Toast.LENGTH_SHORT).show();
			} else {
				nearbyFragment.reloadData();
			}
			return true;
		case R.id.action_search:
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public final void onBackPressed() {
		if (currentPosition != 0 && currentPosition != 5) {
			mNavigationDrawerFragment.selectItem(0, true);
		} else if (currentPosition == 5) {
			if (mapFragment.isCenteredAlready()) {
				mNavigationDrawerFragment.selectItem(0, true);
			} else {
				mapFragment.resetImage();
			}
		} else {
			exitAlertDialog();
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
	 * Stop animation in men
	 */
	public final void stopRefreshAnimation() {
		if (menu != null) {
			MenuItem refreshMenuItem = menu.findItem(R.id.action_refresh);
			refreshMenuItem.collapseActionView();
			refreshMenuItem.setActionView(null);
		}
	}

	/**
	 * Show a dialog
	 */
	private final void exitAlertDialog() {
		new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Exit application")
				.setMessage("Are you sure you want to exit?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						DataHolder.getInstance().setBusData(null);
						DataHolder.getInstance().setTrainData(null);
						finish();
					}
				}).setNegativeButton("No", null).show();
	}
}
