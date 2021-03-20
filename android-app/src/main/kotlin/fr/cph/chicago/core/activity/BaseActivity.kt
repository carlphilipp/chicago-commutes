/**
 * Copyright 2021 Carl-Philipp Harmant
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

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import fr.cph.chicago.R
import fr.cph.chicago.R.string
import fr.cph.chicago.databinding.LoadingBinding
import fr.cph.chicago.redux.BaseAction
import fr.cph.chicago.redux.DefaultSettingsAction
import fr.cph.chicago.redux.State
import fr.cph.chicago.redux.Status
import fr.cph.chicago.redux.store
import fr.cph.chicago.repository.RealmConfig
import org.rekotlin.StoreSubscriber
import timber.log.Timber

/**
 * This class represents the base activity of the application It will load the loading screen and then the main
 * activity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BaseActivity : AppCompatActivity(), StoreSubscriber<State> {

    private val realmConfig = RealmConfig
    private lateinit var binding: LoadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this.isFinishing) {
            binding = LoadingBinding.inflate(layoutInflater)
            setContentView(R.layout.loading)
            store.subscribe(this)
            setUpRealm()
            binding.included.retryButton.setOnClickListener {
                if (binding.included.failureLayout.visibility != View.GONE) binding.included.failureLayout.visibility = View.GONE
                if (binding.loadingLayout.visibility != View.VISIBLE) binding.loadingLayout.visibility = View.VISIBLE
                store.dispatch(BaseAction())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val defaultSettingsAction = DefaultSettingsAction(
            ctaTrainKey = applicationContext.getString(string.cta_train_key),
            ctaBusKey = applicationContext.getString(string.cta_bus_key),
            googleStreetKey = applicationContext.getString(string.google_maps_api_key)
        )
        store.dispatch(defaultSettingsAction)
        store.dispatch(BaseAction())
    }

    private fun setUpRealm() {
        realmConfig.setUpRealm()
    }

    override fun newState(state: State) {
        when (state.status) {
            Status.SUCCESS -> {
                store.unsubscribe(this)
                startMainActivity()
            }
            Status.FAILURE -> {
                store.unsubscribe(this)
                startMainActivity()
            }
            Status.FULL_FAILURE -> {
                if (binding.included.failureLayout.visibility != View.VISIBLE) binding.included.failureLayout.visibility = View.VISIBLE
                if (binding.loadingLayout.visibility != View.GONE) binding.loadingLayout.visibility = View.GONE
            }
            else -> Timber.d("Unknown status on new state")
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }
}
