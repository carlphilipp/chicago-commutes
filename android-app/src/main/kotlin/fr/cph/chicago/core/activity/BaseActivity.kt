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

import android.content.Intent
import android.os.Bundle
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.butterknife.ButterKnifeActivity
import fr.cph.chicago.redux.AppState
import fr.cph.chicago.redux.BaseAction
import fr.cph.chicago.redux.mainStore
import fr.cph.chicago.repository.RealmConfig
import org.rekotlin.StoreSubscriber

/**
 * This class represents the base activity of the application It will load the loading screen and/or the main
 * activity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BaseActivity : ButterKnifeActivity(R.layout.loading), StoreSubscriber<AppState> {

    private val realmConfig: RealmConfig = RealmConfig

    override fun create(savedInstanceState: Bundle?) {
        mainStore.subscribe(this)
        setUpRealm()
        mainStore.dispatch(BaseAction())
    }

    private fun setUpRealm() {
        realmConfig.setUpRealm()
    }

    override fun newState(state: AppState) {
        if (state.lastAction is BaseAction) {
            mainStore.unsubscribe(this)
            startMainActivity()
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        finish()
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}
