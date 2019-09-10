/**
 * Copyright 2019 Carl-Philipp Harmant
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.core.activity

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import butterknife.BindString
import butterknife.BindView
import com.google.android.material.navigation.NavigationView
import fr.cph.chicago.Constants.SELECTED_ID
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.butterknife.ButterKnifeActivity
import fr.cph.chicago.core.fragment.FavoritesFragment
import fr.cph.chicago.core.fragment.buildFragment
import fr.cph.chicago.util.RateUtil

class MainActivity : ButterKnifeActivity(R.layout.activity_main), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        private val rateUtil: RateUtil = RateUtil
    }

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.main_drawer)
    lateinit var drawer: NavigationView
    @BindView(R.id.drawer_layout)
    lateinit var drawerLayout: DrawerLayout
    @BindView(R.id.container)
    lateinit var container: FrameLayout

    @BindString(R.string.bundle_title)
    lateinit var bundleTitle: String
    @BindString(R.string.favorites)
    lateinit var favorites: String
    @BindString(R.string.train)
    lateinit var train: String
    @BindString(R.string.bus)
    lateinit var bus: String
    @BindString(R.string.divvy)
    lateinit var divvy: String
    @BindString(R.string.nearby)
    lateinit var nearby: String
    @BindString(R.string.cta_map)
    lateinit var ctaMap: String
    @BindString(R.string.cta_alert)
    lateinit var ctaAlert: String
    @BindString(R.string.settings)
    lateinit var settings: String

    private var currentPosition: Int = 0

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var menuItem: MenuItem
    private lateinit var inputMethodManager: InputMethodManager

    private var title: String? = null

    override fun create(savedInstanceState: Bundle?) {
        currentPosition = when {
            savedInstanceState != null -> savedInstanceState.getInt(SELECTED_ID, R.id.navigation_favorites)
            intent.extras != null -> intent.extras!!.getInt(SELECTED_ID, R.id.navigation_favorites)
            else -> R.id.navigation_favorites
        }
        initView()
        setToolbar()

        inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        drawerToggle = object : ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                updateFragment(currentPosition)
            }
        }

        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        firstLoad()
    }

    override fun onResume() {
        super.onResume()
        if (title != null)
            setBarTitle(title!!)
    }

    override fun onBackPressed() {
        if (currentPosition == R.id.navigation_favorites) {
            finish()
        } else {
            onNavigationItemSelected(menuItem)
            loadFragment(currentPosition)
        }
    }

    private fun initView() {
        drawer.setNavigationItemSelectedListener(this)
        menuItem = drawer.menu.getItem(0)
    }

    private fun setToolbar() {
        toolbar.inflateMenu(R.menu.main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.elevation = 4f
        }
    }

    private fun setBarTitle(title: String) {
        this.title = title
        toolbar.title = title
    }

    private fun firstLoad() {
        setBarTitle(favorites)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, FavoritesFragment.newInstance(R.id.navigation_favorites))
            .commit()
        container.animate().alpha(1.0f)
    }

    private fun loadFragment(navigationId: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        var fragment = supportFragmentManager.findFragmentByTag(navigationId.toString())
        if (fragment == null) {
            fragment = buildFragment(navigationId)
            transaction.add(fragment, navigationId.toString())
        }
        transaction.replace(R.id.container, fragment).commit()
        container.animate().alpha(1.0f)
    }

    private fun updateFragment(position: Int) {
        when (position) {
            R.id.navigation_favorites -> loadFragment(R.id.navigation_favorites)
            R.id.navigation_train -> loadFragment(R.id.navigation_train)
            R.id.navigation_bus -> loadFragment(R.id.navigation_bus)
            R.id.navigation_bike -> loadFragment(R.id.navigation_bike)
            R.id.navigation_nearby -> loadFragment(R.id.navigation_nearby)
            R.id.navigation_cta_map -> loadFragment(R.id.navigation_cta_map)
            R.id.navigation_alert_cta -> loadFragment(R.id.navigation_alert_cta)
            R.id.navigation_settings -> loadFragment(R.id.navigation_settings)
        }
    }

    private fun itemSelection(position: Int) {
        currentPosition = position
        when (position) {
            R.id.navigation_favorites -> itemSelected(favorites, true)
            R.id.navigation_train -> itemSelected(train, false)
            R.id.navigation_bus -> itemSelected(bus, true)
            R.id.navigation_bike -> itemSelected(divvy, true)
            R.id.navigation_nearby -> itemSelected(nearby, false)
            R.id.navigation_cta_map -> itemSelected(ctaMap, true)
            R.id.navigation_alert_cta -> itemSelected(ctaAlert, true)
            R.id.navigation_rate_this_app -> rateUtil.rateThisApp(this)
            R.id.navigation_settings -> itemSelected(settings, false)
        }
    }

    private fun itemSelected(title: String, showActionBarMenu: Boolean) {
        container.animate().alpha(0.0f)
        setBarTitle(title)
        closeDrawerAndUpdateActionBar(showActionBarMenu)
    }

    private fun closeDrawerAndUpdateActionBar(showActionBarMenu: Boolean) {
        drawerLayout.closeDrawer(GravityCompat.START)
        if (showActionBarMenu)
            showActionBarMenu()
        else
            hideActionBarMenu()
        // Force keyboard to hide if present
        inputMethodManager.hideSoftInputFromWindow(drawerLayout.windowToken, 0)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        menuItem.isChecked = true
        currentPosition = menuItem.itemId
        if (!isFinishing)
            itemSelection(currentPosition)
        return true
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt(SELECTED_ID, currentPosition)
        if (title != null) savedInstanceState.putString(bundleTitle, title)
        super.onSaveInstanceState(savedInstanceState)
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        title = savedInstanceState.getString(bundleTitle)
        currentPosition = savedInstanceState.getInt(SELECTED_ID)
    }

    private fun hideActionBarMenu() {
        if (toolbar.menu.getItem(0).isVisible) {
            showHideActionBarMenu(false)
        }
    }

    private fun showActionBarMenu() {
        if (!toolbar.menu.getItem(0).isVisible) {
            showHideActionBarMenu(true)
        }
    }

    private fun showHideActionBarMenu(bool: Boolean) {
        toolbar.menu.getItem(0).isVisible = bool
    }
}
