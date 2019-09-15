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

package fr.cph.chicago.core.fragment

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import butterknife.BindString
import butterknife.BindView
import fr.cph.chicago.Constants.SELECTED_ID
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.BaseActivity
import fr.cph.chicago.core.activity.DeveloperOptionsActivity
import fr.cph.chicago.core.model.Theme
import fr.cph.chicago.redux.ResetStateAction
import fr.cph.chicago.redux.store
import fr.cph.chicago.repository.RealmConfig
import fr.cph.chicago.service.PreferenceService

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    companion object {
        private val preferenceService = PreferenceService
        private val realmConfig = RealmConfig

        fun newInstance(sectionNumber: Int): SettingsFragment {
            return fragmentWithBundle(SettingsFragment(), sectionNumber) as SettingsFragment
        }
    }

    @BindString(R.string.settings)
    lateinit var settings: String
    @BindString(R.string.bundle_title)
    lateinit var bundleTitle: String

    @BindView(R.id.clear_cache)
    lateinit var clearCache: LinearLayout
    @BindView(R.id.version_number)
    lateinit var versionNumber: TextView
    @BindView(R.id.theme)
    lateinit var theme: LinearLayout
    @BindView(R.id.theme_name)
    lateinit var themeName: TextView
    @BindView(R.id.developer)
    lateinit var developerLayout: LinearLayout

    override fun onCreateView(savedInstanceState: Bundle?) {
        val version = "Version ${util.getCurrentVersion()}"
        versionNumber.text = version
        themeName.text = preferenceService.getTheme().description

        theme.setOnClickListener {
            val builder = AlertDialog.Builder(context!!, R.style.AlertDialog)
            val choices = Theme.values().map { it.description }.toTypedArray()
            val selected = choices.indexOf(preferenceService.getTheme().description)
            builder.setTitle("Theme change")
            builder.setSingleChoiceItems(choices, selected, null)
            builder.setPositiveButton("Save & Reload") { dialog: DialogInterface, _ ->
                val list = (dialog as AlertDialog).listView
                for (i in 0 until list.count) {
                    val checked = list.isItemChecked(i)
                    if (checked) {
                        preferenceService.saveTheme(Theme.values()[i])
                        reloadActivity()
                    }
                }
            }
            builder.setNegativeButton("Cancel", null)
            builder.show()
        }

        clearCache.setOnClickListener {
            val dialogClickListener = { _: Any, which: Any ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        cleanLocalData()
                        restartApp()
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                    }
                }
            }

            AlertDialog.Builder(context!!, R.style.AlertDialog)
                .setMessage("This is going to:\n\n- Delete all your favorites\n- Clear application cache\n- Reload the application")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show()
        }

        developerLayout.setOnClickListener {
            // start new developer activity
            val intent = Intent(context, DeveloperOptionsActivity::class.java)
            context?.startActivity(intent)
        }
    }

    private fun reloadActivity() {
        App.instance.themeSetup()
        val intent = activity?.intent
        intent?.putExtra(SELECTED_ID, R.id.navigation_settings)
        intent?.putExtra(bundleTitle, settings)
        activity?.finish()
        startActivity(activity?.intent)
    }

    private fun restartApp() {
        store.dispatch(ResetStateAction())
        val intent = Intent(context, BaseActivity::class.java)
        activity?.finish()
        startActivity(intent)
    }

    private fun cleanLocalData() {
        deleteCache(context)
        preferenceService.clearPreferences()
        realmConfig.cleanRealm()
    }

    private fun deleteCache(context: Context?) {
        try {
            context?.cacheDir?.deleteRecursively()
        } catch (ignored: Exception) {
        }
    }
}
