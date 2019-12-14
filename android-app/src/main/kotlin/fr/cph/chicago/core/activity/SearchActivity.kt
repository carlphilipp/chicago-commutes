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

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import fr.cph.chicago.R
import fr.cph.chicago.core.adapter.SearchAdapter
import fr.cph.chicago.exception.BaseException
import fr.cph.chicago.service.BikeService
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.TrainService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Singles
import kotlinx.android.synthetic.main.activity_search.searchListView
import kotlinx.android.synthetic.main.toolbar.toolbar
import org.apache.commons.lang3.StringUtils
import timber.log.Timber

class SearchActivity : AppCompatActivity() {

    companion object {
        private val trainService = TrainService
        private val busService = BusService
        private val bikeService = BikeService
    }

    private lateinit var searchView: SearchView
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var searchItem: MenuItem
    private var query: String = StringUtils.EMPTY
    private var clearFocus: Boolean = false

    private val supportActionBarNotNull: ActionBar
        get() = supportActionBar ?: throw BaseException()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this.isFinishing) {
            setContentView(R.layout.activity_search)
            setupToolbar()

            searchAdapter = SearchAdapter(this.baseContext)
            handleIntent(intent)

            searchListView.adapter = searchAdapter

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
            searchView.requestFocus()
            searchView.requestFocusFromTouch()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        searchItem = menu.add(android.R.string.search_go)
        searchItem.setIcon(R.drawable.ic_search_white_24dp)
        searchItem.actionView = searchView
        searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS or MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
        searchItem.expandActionView()

        // Trigger search
        searchView.setQuery(query, true)
        if (clearFocus) {
            searchView.clearFocus()
        }
        return super.onCreateOptionsMenu(menu)
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
        clearFocus = true
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBarNotNull
        actionBar.setDisplayShowHomeEnabled(true)
        actionBar.setDisplayHomeAsUpEnabled(true)
    }

    @SuppressLint("CheckResult")
    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            query = ((intent.getStringExtra(SearchManager.QUERY))
                ?: StringUtils.EMPTY).trim { it <= ' ' }
            val foundStations = trainService.searchStations(query)
            val foundBusRoutes = busService.searchBusRoutes(query)
            val foundBikeStations = bikeService.searchBikeStations(query)
            Singles.zip(foundStations,
                foundBusRoutes,
                foundBikeStations,
                zipper = { trains, buses, bikes -> Triple(trains, buses, bikes) })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result ->
                        searchAdapter.updateData(result.first, result.second, result.third)
                        searchAdapter.notifyDataSetChanged()
                    },
                    { error -> Timber.e(error) }
                )
        }
    }
}
