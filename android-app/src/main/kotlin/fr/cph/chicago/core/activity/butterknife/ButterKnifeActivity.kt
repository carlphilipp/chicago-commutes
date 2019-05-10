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

package fr.cph.chicago.core.activity.butterknife

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import fr.cph.chicago.service.PreferenceService

abstract class ButterKnifeActivity(private val contentView: Int) : AppCompatActivity() {

    companion object {
        private val preferenceService = PreferenceService
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(preferenceService.getCurrentTheme())
        super.onCreate(savedInstanceState)
        if (!this.isFinishing) {
            setContentView(contentView)
            ButterKnife.bind(this)
            create(savedInstanceState)
        }
    }

    abstract fun create(savedInstanceState: Bundle?)
}
