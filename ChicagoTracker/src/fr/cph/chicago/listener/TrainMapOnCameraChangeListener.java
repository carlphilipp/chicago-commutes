package fr.cph.chicago.listener;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;

import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;

public class TrainMapOnCameraChangeListener implements OnCameraChangeListener {

	private float currentZoom = -1;
	private float oldZoom = -1;
	private Bitmap bitmap1;
	private Bitmap bitmap2;
	private Bitmap bitmap3;
	private Bitmap currentBitmap;
	private List<Marker> mTrainMarkers;

	public TrainMapOnCameraChangeListener() {
		Bitmap icon = BitmapFactory.decodeResource(ChicagoTracker.getAppContext().getResources(), R.drawable.train_gta);
		this.bitmap1 = Bitmap.createScaledBitmap(icon, icon.getWidth() / 11, icon.getHeight() / 11, false);
		this.bitmap2 = Bitmap.createScaledBitmap(icon, icon.getWidth() / 6, icon.getHeight() / 6, false);
		this.bitmap3 = Bitmap.createScaledBitmap(icon, icon.getWidth() / 4, icon.getHeight() / 4, false);
		this.setCurrentBitmap(bitmap1);
	}

	public void setTrainMarkers(List<Marker> trainMarkers) {
		this.mTrainMarkers = trainMarkers;
	}

	@Override
	public void onCameraChange(CameraPosition position) {
		if (position.zoom != currentZoom) {
			oldZoom = currentZoom;
			currentZoom = position.zoom;
			if (isIn(currentZoom, 12.9f, 11f) && !isIn(oldZoom, 12.9f, 11f)) {
				for (Marker marker : mTrainMarkers) {
					this.setCurrentBitmap(bitmap1);
					marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap1));
				}
			} else if (isIn(currentZoom, 14.9f, 13f) && !isIn(oldZoom, 14.9f, 13f)) {
				for (Marker marker : mTrainMarkers) {
					this.setCurrentBitmap(bitmap2);
					marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap2));
				}
			} else if (isIn(currentZoom, 21f, 15f) && !isIn(oldZoom, 21f, 15f)) {
				for (Marker marker : mTrainMarkers) {
					this.setCurrentBitmap(bitmap3);
					marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap3));
				}
			}
		}
	}

	public boolean isIn(float num, float sup, float inf) {
		return num >= inf && num <= sup;
	}

	public final Bitmap getCurrentBitmap() {
		return currentBitmap;
	}

	private void setCurrentBitmap(final Bitmap currentBitmap) {
		this.currentBitmap = currentBitmap;
	}

}
