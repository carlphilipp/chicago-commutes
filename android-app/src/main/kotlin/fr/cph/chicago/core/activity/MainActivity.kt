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
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import butterknife.BindString
import butterknife.BindView
import com.google.android.material.navigation.NavigationView
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.butterknife.ButterKnifeActivity
import fr.cph.chicago.core.fragment.AlertFragment
import fr.cph.chicago.core.fragment.BikeFragment
import fr.cph.chicago.core.fragment.BusFragment
import fr.cph.chicago.core.fragment.CtaMapFragment
import fr.cph.chicago.core.fragment.FavoritesFragment
import fr.cph.chicago.core.fragment.NearbyFragment
import fr.cph.chicago.core.fragment.SettingsFragment
import fr.cph.chicago.core.fragment.TrainFragment
import fr.cph.chicago.util.RateUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit


class MainActivity : ButterKnifeActivity(R.layout.activity_main), NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.main_drawer)
    lateinit var drawer: NavigationView
    @BindView(R.id.drawer_layout)
    lateinit var drawerLayout: DrawerLayout

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

    private val rateUtil: RateUtil = RateUtil

    private var currentPosition: Int = 0

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var menuItem: MenuItem
    private lateinit var inputMethodManager: InputMethodManager

    private var favoritesFragment: FavoritesFragment? = null
    private var trainFragment: TrainFragment? = null
    private var busFragment: BusFragment? = null
    private var bikeFragment: BikeFragment? = null
    private var nearbyFragment: NearbyFragment? = null
    private var ctaMapFragment: CtaMapFragment? = null
    private var alertFragment: AlertFragment? = null
    private var settingsFragment: SettingsFragment? = null

    private var title: String? = null

    override fun create(savedInstanceState: Bundle?) {
        initView()
        setToolbar()

        inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        currentPosition = savedInstanceState?.getInt(SELECTED_ID) ?: R.id.navigation_favorites
        itemSelection(currentPosition)
    }

    override fun onBackPressed() {
        if (currentPosition == R.id.navigation_favorites) {
            finish()
        } else {
            onNavigationItemSelected(menuItem)
        }
    }

    override fun onResume() {
        super.onResume()
        if (title != null)
            setBarTitle(title!!)
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

    private fun itemSelection(position: Int) {
        currentPosition = position
        when (position) {
            R.id.navigation_favorites -> {
                setBarTitle(favorites)
                favoritesFragment = favoritesFragment ?: FavoritesFragment.newInstance(position + 1)
                supportFragmentManager.beginTransaction().replace(R.id.container, favoritesFragment as androidx.fragment.app.Fragment).commit()
                closeDrawerAndUpdateActionBar(true)
            }
            R.id.navigation_train -> {
                setBarTitle(train)
                trainFragment = trainFragment ?: TrainFragment.newInstance(position + 1)
                supportFragmentManager.beginTransaction().replace(R.id.container, trainFragment as androidx.fragment.app.Fragment).commit()
                closeDrawerAndUpdateActionBar(false)
            }
            R.id.navigation_bus -> {
                setBarTitle(bus)
                busFragment = busFragment ?: BusFragment.newInstance(position + 1)
                supportFragmentManager.beginTransaction().replace(R.id.container, busFragment as androidx.fragment.app.Fragment).commit()
                closeDrawerAndUpdateActionBar(true)
            }
            R.id.navigation_bike -> {
                setBarTitle(divvy)
                bikeFragment = bikeFragment ?: BikeFragment.newInstance(position + 1)
                supportFragmentManager.beginTransaction().replace(R.id.container, bikeFragment as androidx.fragment.app.Fragment).commit()
                closeDrawerAndUpdateActionBar(true)
            }
            R.id.navigation_nearby -> {
                setBarTitle(nearby)
                nearbyFragment = nearbyFragment ?: NearbyFragment.newInstance(position + 1)
                Observable.fromCallable { drawerLayout.closeDrawer(GravityCompat.START) }
                    .delay(500, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError { throwable -> Timber.e(throwable) }
                    .subscribe { supportFragmentManager.beginTransaction().replace(R.id.container, nearbyFragment as androidx.fragment.app.Fragment).commitAllowingStateLoss() }
                drawerLayout.closeDrawer(GravityCompat.START)
                hideActionBarMenu()
            }
            R.id.navigation_cta_map -> {
                setBarTitle(ctaMap)
                ctaMapFragment = ctaMapFragment ?: CtaMapFragment.newInstance(position + 1)
                supportFragmentManager.beginTransaction().replace(R.id.container, ctaMapFragment as androidx.fragment.app.Fragment).commit()
                closeDrawerAndUpdateActionBar(false)
            }
            R.id.alert_cta -> {
                setBarTitle(ctaAlert)
                alertFragment = alertFragment ?: AlertFragment.newInstance(position + 1)
                supportFragmentManager.beginTransaction().replace(R.id.container, alertFragment as androidx.fragment.app.Fragment).commit()
                closeDrawerAndUpdateActionBar(true)
            }
            R.id.rate_this_app -> rateUtil.rateThisApp(this)
            R.id.settings -> {
                setBarTitle(settings)
                settingsFragment = settingsFragment ?: SettingsFragment.newInstance(position + 1)
                supportFragmentManager.beginTransaction().replace(R.id.container, settingsFragment as androidx.fragment.app.Fragment).commit()
                closeDrawerAndUpdateActionBar(false)
            }
        }
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

    companion object {
        private const val SELECTED_ID = "SELECTED_ID"
    }
}
