package fr.cph.chicago.listener;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.Toast;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.activity.StationActivity;
import fr.cph.chicago.util.Util;

public class FavoritesTrainOnClickListener implements OnClickListener {
	/** The main activity **/
	private MainActivity mActivity;
	/** The layout that is used to display a fade black background **/
	private FrameLayout firstLayout;
	/** The station id **/
	private int stationId;

	public FavoritesTrainOnClickListener(final MainActivity activity, final FrameLayout firstLayout, final int stationId) {
		this.mActivity = activity;
		this.firstLayout = firstLayout;
		this.stationId = stationId;
	}

	@Override
	public void onClick(final View view) {
		if (!Util.isNetworkAvailable()) {
			Toast.makeText(mActivity, "No network connection detected!", Toast.LENGTH_LONG).show();
		} else {
			LayoutInflater layoutInflater = (LayoutInflater) mActivity.getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View popupView = layoutInflater.inflate(R.layout.popup_train, null);
			
			int[] screenSize = Util.getScreenSize();

			final PopupWindow popup = new PopupWindow(popupView, (int) (screenSize[0] * 0.7), LayoutParams.WRAP_CONTENT);
			popup.setFocusable(true);
			popup.setBackgroundDrawable(ChicagoTracker.getAppContext().getResources().getDrawable(R.drawable.any_selector));
			firstLayout.getForeground().setAlpha(210);

			Button btnDetails = (Button) popupView.findViewById(R.id.details);
			btnDetails.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					popup.dismiss();
					Intent intent = new Intent(ChicagoTracker.getAppContext(), StationActivity.class);
					Bundle extras = new Bundle();
					extras.putInt("stationId", stationId);
					intent.putExtras(extras);
					mActivity.startActivity(intent);
					mActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
				}
			});
			popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
				@Override
				public void onDismiss() {
					firstLayout.getForeground().setAlpha(0);
				}
			});

			popup.showAtLocation(firstLayout, Gravity.CENTER, 0, 0);
		}
	}
}
