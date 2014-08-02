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

/**
 * Bus map camera on change listener. Handle the update of markers on the map
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BusMapOnCameraChangeListener implements OnCameraChangeListener {
	/** **/
	private float mCurrentZoom = -1;
	/** **/
	private float mOldZoom = -1;
	/** **/
	private Bitmap mBitmap1;
	/** **/
	private Bitmap mBitmap2;
	/** **/
	private Bitmap mBitmap3;
	/** **/
	private Bitmap mCurrentBitmap;
	/** **/
	private List<Marker> mBusMarkers;
	/** **/
	private List<Marker> mBusStationMarkers;

	public BusMapOnCameraChangeListener() {
		Bitmap icon = BitmapFactory.decodeResource(ChicagoTracker.getAppContext().getResources(), R.drawable.bus);
		this.mBitmap1 = Bitmap.createScaledBitmap(icon, icon.getWidth() / 9, icon.getHeight() / 9, false);
		this.mBitmap2 = Bitmap.createScaledBitmap(icon, icon.getWidth() / 5, icon.getHeight() / 5, false);
		this.mBitmap3 = Bitmap.createScaledBitmap(icon, icon.getWidth() / 3, icon.getHeight() / 3, false);
		this.setCurrentBitmap(mBitmap1);
	}

	public void setBusMarkers(List<Marker> busMarkers) {
		this.mBusMarkers = busMarkers;
	}

	public void setBusStationMarkers(List<Marker> busStationMarkers) {
		this.mBusStationMarkers = busStationMarkers;
	}

	@Override
	public void onCameraChange(CameraPosition position) {
		if (position.zoom != mCurrentZoom) {
			mOldZoom = mCurrentZoom;
			mCurrentZoom = position.zoom;
			if (isIn(mCurrentZoom, 12.9f, 11f) && !isIn(mOldZoom, 12.9f, 11f)) {
				for (Marker marker : mBusMarkers) {
					this.setCurrentBitmap(mBitmap1);
					marker.setIcon(BitmapDescriptorFactory.fromBitmap(mBitmap1));
				}
			} else if (isIn(mCurrentZoom, 14.9f, 13f) && !isIn(mOldZoom, 14.9f, 13f)) {
				for (Marker marker : mBusMarkers) {
					this.setCurrentBitmap(mBitmap2);
					marker.setIcon(BitmapDescriptorFactory.fromBitmap(mBitmap2));
				}
			} else if (isIn(mCurrentZoom, 21f, 15f) && !isIn(mOldZoom, 21f, 15f)) {
				for (Marker marker : mBusMarkers) {
					this.setCurrentBitmap(mBitmap3);
					marker.setIcon(BitmapDescriptorFactory.fromBitmap(mBitmap3));
				}
			}
			if (isIn(mCurrentZoom, 21f, 16f) && !isIn(mOldZoom, 21f, 16f)) {
				for (Marker marker : mBusStationMarkers) {
					marker.setVisible(true);
				}
			} else if (!isIn(mCurrentZoom, 21f, 16f) && isIn(mOldZoom, 21f, 16f)) {
				for (Marker marker : mBusStationMarkers) {
					marker.setVisible(false);
				}
			}
		}
	}

	public boolean isIn(float num, float sup, float inf) {
		return num >= inf && num <= sup;
	}

	public final Bitmap getCurrentBitmap() {
		return mCurrentBitmap;
	}

	private void setCurrentBitmap(final Bitmap currentBitmap) {
		this.mCurrentBitmap = currentBitmap;
	}

}
