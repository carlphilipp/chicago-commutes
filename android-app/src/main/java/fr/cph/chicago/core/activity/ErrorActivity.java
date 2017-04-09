/**
 * Copyright 2017 Carl-Philipp Harmant
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.core.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import fr.cph.chicago.R;

/**
 * Error activity that can be thrown from anywhere in the app
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
@SuppressWarnings("WeakerAccess")
public class ErrorActivity extends Activity {

    @BindView(R.id.loading_layout)
    View loadLayout;
    @BindView(R.id.error_message)
    TextView errorText;
    @BindView(R.id.retry_button)
    Button button;

    @BindString(R.string.bundle_error)
    String bundleError;

    private String error;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.error);
        ButterKnife.bind(this);

        loadLayout.setVisibility(View.GONE);
        if (error == null) {
            error = getIntent().getExtras().getString(bundleError);
        }

        errorText.setText(error);

        button.setOnClickListener(v -> {
            final Intent intent = new Intent(ErrorActivity.this, BaseActivity.class);
            intent.putExtra(bundleError, true);
            finish();
            startActivity(intent);
        });
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        error = savedInstanceState.getString(bundleError);
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putString(bundleError, error);
        super.onSaveInstanceState(savedInstanceState);
    }
}
