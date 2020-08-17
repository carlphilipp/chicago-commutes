/**
 * Copyright 2020 Carl-Philipp Harmant
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
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import fr.cph.chicago.R
import fr.cph.chicago.service.PreferenceService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_developer_options.cacheDetailsLayout
import kotlinx.android.synthetic.main.activity_developer_options.showCacheLayout
import kotlinx.android.synthetic.main.toolbar.toolbar
import timber.log.Timber

class DeveloperOptionsActivity : AppCompatActivity() {

    companion object {
        private val preferenceService = PreferenceService
    }

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this.isFinishing) {
            setContentView(R.layout.activity_developer_options)
            preferenceService.getAllFavorites()
                .observeOn(Schedulers.computation())
                .map { favorites -> favorites.preferences }
                .flatMapObservable { preferences -> Observable.fromIterable(preferences) }
                .filter { preference -> preference.favorites.isNotEmpty() }
                .sorted { preference1, preference2 -> preference1.name.value.compareTo(preference2.name.value) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { preference ->
                        addCacheData(preference.name.value, R.layout.cache_developer_options_title)
                        preference.favorites.forEach { entry -> addCacheData(entry, R.layout.cache_developer_options_value) }
                    },
                    { throwable ->
                        Timber.e(throwable)
                    })

            showCacheLayout.setOnClickListener {
                cacheDetailsLayout.visibility = if (cacheDetailsLayout.visibility == View.GONE) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
            setToolBar()
        }
    }

    private fun addCacheData(text: String, @LayoutRes layout: Int) {
        val row = View.inflate(this, layout, null)
        val titleLinearLayout = row.findViewById<LinearLayout>(R.id.row)
        val dataView = row.findViewById<TextView>(R.id.data)
        dataView.text = text
        cacheDetailsLayout.addView(titleLinearLayout)
    }

    private fun setToolBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.elevation = 4f
        }
        toolbar.title = title
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setOnClickListener { finish() }
    }
}
