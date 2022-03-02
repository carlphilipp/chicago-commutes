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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import fr.cph.chicago.databinding.ErrorBinding

/**
 * BusArrivalError activity that can be thrown from anywhere in the app
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
// FIXME: See how that can be redone with compose
class ErrorActivity : Activity() {

    private lateinit var binding: ErrorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ErrorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.failureLayout.visibility = View.VISIBLE
        binding.retryButton.setOnClickListener {
            val intent = Intent(this@ErrorActivity, BaseActivity::class.java)
            finish()
            startActivity(intent)
        }
    }
}
