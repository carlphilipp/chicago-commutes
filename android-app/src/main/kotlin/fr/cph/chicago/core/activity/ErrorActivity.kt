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
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import butterknife.BindView
import butterknife.ButterKnife
import fr.cph.chicago.R
import fr.cph.chicago.service.PreferenceService

/**
 * BusArrivalError activity that can be thrown from anywhere in the app
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class ErrorActivity : Activity() {

    @BindView(R.id.retry_button)
    lateinit var button: Button

    private val preferenceService: PreferenceService = PreferenceService

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(preferenceService.getCurrentTheme())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.error)
        ButterKnife.bind(this)

        button.setOnClickListener {
            val intent = Intent(this@ErrorActivity, BaseActivity::class.java)
            finish()
            startActivity(intent)
        }
    }
}
