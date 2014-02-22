package fr.cph.chicago.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
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
	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.error);

		View loadLayout = findViewById(R.id.loading_layout);
		loadLayout.setVisibility(View.GONE);

		Intent intent = getIntent();
		String error = intent.getExtras().getString("error");
		TextView errorText = (TextView) findViewById(R.id.error_message);
		errorText.setText(error);
		Button button = (Button) findViewById(R.id.retry_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ErrorActivity.this, BaseActivity.class);
				intent.putExtra("error", true);
				finish();
				startActivity(intent);
			}
		});
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.empty, menu);
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle("Error");
		return true;
	}
}
