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

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ListView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import butterknife.BindView
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.butterknife.ButterKnifeActivity
import fr.cph.chicago.core.adapter.SearchAdapter
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.service.BikeService
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.TrainService
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function3
import org.apache.commons.lang3.StringUtils

class SearchActivity : ButterKnifeActivity(R.layout.activity_search) {

    companion object {
        private val trainService = TrainService
        private val busService = BusService
        private val bikeService = BikeService
    }

    @BindView(R.id.container)
    lateinit var container: FrameLayout
    @BindView(R.id.search_list)
    lateinit var listView: ListView
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    private lateinit var searchView: SearchView
    private lateinit var searchAdapter: SearchAdapter
    private var query: String = StringUtils.EMPTY

    private val supportActionBarNotNull: ActionBar
        get() = supportActionBar ?: throw RuntimeException()

    override fun create(savedInstanceState: Bundle?) {
        setupToolbar()

        searchAdapter = SearchAdapter(this.baseContext)
        handleIntent(intent)

        listView.adapter = searchAdapter

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = SearchView(supportActionBarNotNull.themedContext)
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.isSubmitButtonEnabled = true
        searchView.setIconifiedByDefault(false)
        searchView.isIconified = false
        searchView.maxWidth = 1000
        searchView.isFocusable = true
        searchView.isFocusableInTouchMode = true
        // FIXME remove clearfocus if possible
        searchView.clearFocus()
        searchView.requestFocus()
        searchView.requestFocusFromTouch()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val searchItem = menu.add(android.R.string.search_go)
        searchItem.setIcon(R.drawable.ic_search_white_24dp)
        searchItem.actionView = searchView
        searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS or MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
        searchItem.expandActionView()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onResume() {
        // Hide keyboard
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        super.onResume()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(SearchManager.QUERY, query)
        super.onSaveInstanceState(outState)
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        query = savedInstanceState.getString(SearchManager.QUERY, StringUtils.EMPTY)
        searchView.setQuery(query, true)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBarNotNull
        actionBar.setDisplayShowHomeEnabled(true)
        actionBar.setDisplayHomeAsUpEnabled(true)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            query = intent.getStringExtra(SearchManager.QUERY).trim { it <= ' ' }
            val foundStations = trainService.searchStations(query)
            val foundBusRoutes = busService.searchBusRoutes(query)
            val foundBikeStations = bikeService.searchBikeStations(query)
            Single.zip(foundStations,
                foundBusRoutes,
                foundBikeStations,
                Function3 { trains: List<TrainStation>, buses: List<BusRoute>, bikes: List<BikeStation> ->
                    Triple(trains, buses, bikes)
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result ->
                    searchAdapter.updateData(result.first, result.second, result.third)
                    searchAdapter.notifyDataSetChanged()
                }
        }
    }

}
