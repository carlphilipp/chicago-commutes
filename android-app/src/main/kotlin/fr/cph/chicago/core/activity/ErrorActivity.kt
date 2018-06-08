/**
 * Copyright 2018 Carl-Philipp Harmant
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
import android.widget.Button
import android.widget.TextView
import butterknife.BindString
import butterknife.BindView
import butterknife.ButterKnife
import fr.cph.chicago.R

/**
 * Error activity that can be thrown from anywhere in the app
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class ErrorActivity : Activity() {

    @BindView(R.id.loading_layout)
    lateinit var loadLayout: View
    @BindView(R.id.error_message)
    lateinit var errorText: TextView
    @BindView(R.id.retry_button)
    lateinit var button: Button

    @BindString(R.string.bundle_error)
    lateinit var bundleError: String

    private lateinit var error: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.error)
        ButterKnife.bind(this)

        loadLayout.visibility = View.GONE
        error = intent.getStringExtra(bundleError)

        errorText.text = error

        button.setOnClickListener { _ ->
            val intent = Intent(this@ErrorActivity, BaseActivity::class.java)
            intent.putExtra(bundleError, true)
            finish()
            startActivity(intent)
        }
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        error = savedInstanceState.getString(bundleError)
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putString(bundleError, error)
        super.onSaveInstanceState(savedInstanceState)
    }
}
