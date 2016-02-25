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

package fr.cph.chicago.fragment.drawer;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import fr.cph.chicago.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer. See the <a
 * href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction"> design guidelines</a> for a complete explanation of the
 * behaviors implemented here.
 */

// TODO to delete
public class NavigationDrawerFragment extends Fragment {

	/** Remember the position of the selected item. **/
	private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
	/**
	 * Per the design guidelines, you should show the drawer on launch until the user manually expands it. This shared preference tracks this.
	 */
	private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
	/**
	 * A pointer to the current callback instance (the Activity).
	 */
	private NavigationDrawerCallbacks callbacks;
	/**
	 * Helper component that ties the action bar to the navigation drawer.
	 */
	private ActionBarDrawerToggle drawerToggle;
	/** The drawer layout **/
	private DrawerLayout drawerLayout;
	/** the drawer list view **/
	private ListView drawerListView;
	/** The fragment container **/
	private View fragmentContainerView;
	/** The current selected position **/
	private int currentSelectedPosition = 0;
	/** The saved instance **/
	private boolean fromSavedInstanceState;
	/** The user learned drawer **/
	private boolean userLearnedDrawer;
	/** The pending runnable thread **/
	private Runnable pendingRunnable;

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Read in the flag indicating whether or not the user has demonstrated awareness of the
		// drawer. See PREF_USER_LEARNED_DRAWER for details.
		final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		userLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

		if (savedInstanceState != null) {
			currentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
			fromSavedInstanceState = true;
		}

		// Select either the default item (0) or the last selected item.
		selectItem(currentSelectedPosition, false);

		// Indicate that this fragment would like to influence the set of actions in the action bar.
		setHasOptionsMenu(true);
	}

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		drawerListView = (ListView) inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
		drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				selectItem(position, false);
			}
		});
		final List<DrawerItem> drawerItems = new ArrayList<>();
		final DrawerItem item1 = new DrawerItem(getString(R.string.favorites), R.drawable.ic_favorite_white_24dp);
		final DrawerItem item2 = new DrawerItem(getString(R.string.train), R.drawable.ic_train_white_24dp);
		final DrawerItem item3 = new DrawerItem(getString(R.string.bus), R.drawable.ic_directions_bus_white_24dp);
		final DrawerItem item4 = new DrawerItem(getString(R.string.divvy), R.drawable.ic_directions_bike_white_24dp);
		final DrawerItem item5 = new DrawerItem(getString(R.string.nearby), R.drawable.ic_near_me_white_24dp);
		final DrawerItem item8 = new DrawerItem(getString(R.string.settings), R.drawable.ic_settings_white_24dp);
		drawerItems.add(item1);
		drawerItems.add(item2);
		drawerItems.add(item3);
		drawerItems.add(item4);
		drawerItems.add(item5);
		drawerItems.add(item8);
		final CustomDrawerAdapter ada = new CustomDrawerAdapter(getActivity(), R.layout.custom_drawer_item, drawerItems);
		drawerListView.setAdapter(ada);
		drawerListView.setItemChecked(currentSelectedPosition, true);
		drawerListView.setSelector(R.drawable.drawer_selector);
		return drawerListView;
	}

	public final boolean isDrawerOpen() {
		return drawerLayout != null && drawerLayout.isDrawerOpen(fragmentContainerView);
	}

	/**
	 * Users of this fragment must call this method to set up the navigation drawer interactions.
	 *
	 * @param fragmentId
	 *            The android:id of this fragment in its activity's layout.
	 * @param drawerLayout
	 *            The DrawerLayout containing this fragment's UI.
	 */
	public final void setUp(final int fragmentId, final DrawerLayout drawerLayout) {
		fragmentContainerView = getActivity().findViewById(fragmentId);
		this.drawerLayout = drawerLayout;

		// set a custom shadow that overlays the main content when the drawer opens
		this.drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		// set up the drawer's list view with items and click listener

		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the navigation drawer and the action bar app icon.
		drawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
				NavigationDrawerFragment.this.drawerLayout, /* DrawerLayout object */
				R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
				R.string.navigation_drawer_open, /* "open drawer" description for accessibility */
				R.string.navigation_drawer_close /* "close drawer" description for accessibility */
		) {
			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				if (!isAdded()) {
					return;
				}

				if (pendingRunnable != null) {
					final Handler mHandler = new Handler();
					mHandler.post(pendingRunnable);
					pendingRunnable = null;
				}

				getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				if (!isAdded()) {
					return;
				}

				if (!userLearnedDrawer) {
					// The user manually opened the drawer; store this flag to prevent auto-showing
					// the navigation drawer automatically in the future.
					userLearnedDrawer = true;
					final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
					sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
				}
				getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
			}
		};

		// If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
		// per the navigation drawer design guidelines.
		if (!userLearnedDrawer && !fromSavedInstanceState) {
			this.drawerLayout.openDrawer(fragmentContainerView);
		}

		// Defer code dependent on restoration of previous instance state.
		this.drawerLayout.post(new Runnable() {
			@Override
			public void run() {
				drawerToggle.syncState();
			}
		});

		this.drawerLayout.setDrawerListener(drawerToggle);
	}

	/**
	 * Select item
	 *
	 * @param position
	 *            the position
	 * @param backPressed
	 *            if back pressed
	 */
	public void selectItem(final int position, final boolean backPressed) {
		currentSelectedPosition = position;
		pendingRunnable = new Runnable() {
			@Override
			public void run() {
				if (callbacks != null) {
					callbacks.onNavigationDrawerItemSelected(position);
				}
			}
		};

		if (backPressed) {
			Handler mHandler = new Handler();
			mHandler.post(pendingRunnable);
			pendingRunnable = null;
		}

		if (drawerListView != null) {
			drawerListView.setItemChecked(position, true);
		}
		if (drawerLayout != null) {
			drawerLayout.closeDrawer(fragmentContainerView);
		} else {
			callbacks.onNavigationDrawerItemSelected(position);
		}
	}

	@Override
	public final void onAttach(final Activity activity) {
		super.onAttach(activity);
		try {
			callbacks = (NavigationDrawerCallbacks) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
		}
	}

	@Override
	public final void onDetach() {
		super.onDetach();
		callbacks = null;
	}

	@Override
	public final void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_SELECTED_POSITION, currentSelectedPosition);
	}

	@Override
	public final void onConfigurationChanged(final Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Forward the new configuration the drawer toggle component.
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public final void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		// If the drawer is open, show the global app actions in the action bar. See also
		// showGlobalContextActionBar, which controls the top-left area of the action bar.
		if (drawerLayout != null && isDrawerOpen()) {
			inflater.inflate(R.menu.empty, menu);
			showGlobalContextActionBar();
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Per the navigation drawer design guidelines, updates the action bar to show the global app 'context', rather than just what's in the current
	 * screen.
	 */
	private void showGlobalContextActionBar() {
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setTitle(R.string.app_name);
	}

	/**
	 * Get action bar
	 *
	 * @return the action bar
	 */
	private ActionBar getActionBar() {
		return getActivity().getActionBar();
	}

	/**
	 * Callbacks interface that all activities using this fragment must implement.
	 */
	public interface NavigationDrawerCallbacks {
		/**
		 * Called when an item in the navigation drawer is selected.
		 */
		void onNavigationDrawerItemSelected(int position);
	}
}
