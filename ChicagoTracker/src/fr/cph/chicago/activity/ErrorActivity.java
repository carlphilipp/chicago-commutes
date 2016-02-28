/**
 * Copyright 2016 Carl-Philipp Harmant
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import fr.cph.chicago.R;

/**
 * Error activity that can be thrown from anywhere in the app
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class ErrorActivity extends Activity {
	/**
	 * Error
	 **/
	private String error;

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.error);

		final View loadLayout = findViewById(R.id.loading_layout);
		loadLayout.setVisibility(View.GONE);
		if (error == null) {
			error = getIntent().getExtras().getString(getString(R.string.bundle_error));
		}
		final TextView errorText = (TextView) findViewById(R.id.error_message);
		errorText.setText(error);
		final Button button = (Button) findViewById(R.id.retry_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent intent = new Intent(ErrorActivity.this, BaseActivity.class);
				intent.putExtra("error", true);
				finish();
				startActivity(intent);
			}
		});
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		error = savedInstanceState.getString("error");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("error", error);
		super.onSaveInstanceState(savedInstanceState);
	}
}
