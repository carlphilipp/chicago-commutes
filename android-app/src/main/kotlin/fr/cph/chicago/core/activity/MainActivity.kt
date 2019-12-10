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
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import fr.cph.chicago.Constants.SELECTED_ID
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.fragment.Fragment
import fr.cph.chicago.core.fragment.buildFragment
import fr.cph.chicago.redux.store
import fr.cph.chicago.util.RateUtil
import kotlinx.android.synthetic.main.activity_main.drawer
import kotlinx.android.synthetic.main.activity_main.drawerLayout
import kotlinx.android.synthetic.main.activity_main.searchContainer
import kotlinx.android.synthetic.main.toolbar.toolbar
import org.apache.commons.lang3.StringUtils

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        private val rateUtil: RateUtil = RateUtil
    }

    private var currentPosition: Int = 0

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var favoriteMenuItem: MenuItem
    private lateinit var inputMethodManager: InputMethodManager
    lateinit var tb: Toolbar

    private var title: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this.isFinishing) {
            setContentView(R.layout.activity_main)
            this.tb = toolbar
            if (store.state.ctaTrainKey == StringUtils.EMPTY) {
                // Start error activity when state is empty (usually when android restart the activity on error)
                App.startErrorActivity()
                finish()
            }
            currentPosition = when {
                savedInstanceState != null -> savedInstanceState.getInt(SELECTED_ID, R.id.navigation_favorites)
                intent.extras != null -> intent.extras!!.getInt(SELECTED_ID, R.id.navigation_favorites)
                else -> R.id.navigation_favorites
            }
            title = when {
                savedInstanceState != null -> savedInstanceState.getString(getString(R.string.bundle_title), getString(R.string.favorites))
                intent.extras != null -> intent.extras!!.getString(getString(R.string.bundle_title), getString(R.string.favorites))
                else -> getString(R.string.favorites)
            }
            drawer.setNavigationItemSelectedListener(this)
            favoriteMenuItem = drawer.menu.getItem(0)
            if (currentPosition == R.id.navigation_favorites) {
                favoriteMenuItem.isChecked = true
            }
            toolbar.inflateMenu(R.menu.main)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                toolbar.elevation = 4f
            }

            inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            drawerToggle = object : ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
                override fun onDrawerClosed(drawerView: View) {
                    super.onDrawerClosed(drawerView)
                    updateFragment(currentPosition)
                }
            }

            drawerLayout.addDrawerListener(drawerToggle)
            drawerToggle.syncState()

            setBarTitle(title!!)
            updateFragment(currentPosition)
        }
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
            // Switch to favorites if in another fragment
            onNavigationItemSelected(favoriteMenuItem)
            loadFragment(currentPosition)
        }
    }

    private fun setBarTitle(title: String) {
        this.title = title
        toolbar.title = title
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

    private fun loadFragment(navigationId: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        var fragment = supportFragmentManager.findFragmentByTag(navigationId.toString())
        if (fragment == null) {
            fragment = buildFragment(navigationId)
            transaction.add(fragment, navigationId.toString())
        }
        transaction.replace(R.id.searchContainer, fragment).commit()
        showHideActionBarMenu((fragment as Fragment).hasActionBar())
        searchContainer.animate().alpha(1.0f)
    }

    private fun itemSelected(title: String) {
        searchContainer.animate().alpha(0.0f)
        setBarTitle(title)
        closeDrawerAndUpdateActionBar()
    }

    private fun closeDrawerAndUpdateActionBar() {
        drawerLayout.closeDrawer(GravityCompat.START)
        // Force keyboard to hide if present
        inputMethodManager.hideSoftInputFromWindow(drawerLayout.windowToken, 0)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        if (!isFinishing) {
            menuItem.isChecked = true
            if (currentPosition != menuItem.itemId) {
                currentPosition = menuItem.itemId
                when (menuItem.itemId) {
                    R.id.navigation_favorites -> itemSelected(getString(R.string.favorites))
                    R.id.navigation_train -> itemSelected(getString(R.string.train))
                    R.id.navigation_bus -> itemSelected(getString(R.string.bus))
                    R.id.navigation_bike -> itemSelected(getString(R.string.divvy))
                    R.id.navigation_nearby -> itemSelected(getString(R.string.nearby))
                    R.id.navigation_cta_map -> itemSelected(getString(R.string.cta_map))
                    R.id.navigation_alert_cta -> itemSelected(getString(R.string.cta_alert))
                    R.id.navigation_rate_this_app -> rateUtil.rateThisApp(this)
                    R.id.navigation_settings -> itemSelected(getString(R.string.settings))
                }
            } else {
                currentPosition = menuItem.itemId
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
        return true
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt(SELECTED_ID, currentPosition)
        if (title != null) savedInstanceState.putString(getString(R.string.bundle_title), title)
        super.onSaveInstanceState(savedInstanceState)
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        title = savedInstanceState.getString(getString(R.string.bundle_title))
        currentPosition = savedInstanceState.getInt(SELECTED_ID)
    }

    private fun showHideActionBarMenu(show: Boolean) {
        toolbar.menu.getItem(0).isVisible = show
    }
}
