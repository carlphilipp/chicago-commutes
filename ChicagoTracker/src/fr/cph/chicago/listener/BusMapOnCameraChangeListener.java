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

public class BusMapOnCameraChangeListener implements OnCameraChangeListener {

	private float currentZoom = -1;
	private float oldZoom = -1;
	private Bitmap bhalfsize11;
	private Bitmap bhalfsize14;
	private List<Marker> mBusMarkers;
	private List<Marker> mBusStationMarkers;

	public BusMapOnCameraChangeListener() {
		Bitmap icon = BitmapFactory.decodeResource(ChicagoTracker.getAppContext().getResources(), R.drawable.bus_gta);
		this.bhalfsize11 = Bitmap.createScaledBitmap(icon, icon.getWidth() / 11, icon.getHeight() / 11, false);
		this.bhalfsize14 = Bitmap.createScaledBitmap(icon, icon.getWidth() / 6, icon.getHeight() / 6, false);
	}
	
	public void setBusMarkers(List<Marker> busMarkers){
		this.mBusMarkers = busMarkers;
	}
	
	public void setBusStationMarkers(List<Marker> busStationMarkers){
		this.mBusStationMarkers = busStationMarkers;
	}

	@Override
	public void onCameraChange(CameraPosition position) {
		if (position.zoom != currentZoom) {
			oldZoom = currentZoom;
			currentZoom = position.zoom;
			if (oldZoom != -1 && currentZoom == 11) {
				for (Marker marker : mBusMarkers) {
					marker.setIcon(BitmapDescriptorFactory.fromBitmap(bhalfsize11));
				}
			} else if (currentZoom == 13) {
				for (Marker marker : mBusMarkers) {
					marker.setIcon(BitmapDescriptorFactory.fromBitmap(bhalfsize14));
				}
			}
			if (currentZoom == 16) {
				for (Marker marker : mBusStationMarkers) {
					marker.setVisible(true);
				}
			} else if (currentZoom < 16 && oldZoom == 16) {
				for (Marker marker : mBusStationMarkers) {
					marker.setVisible(false);
				}
			}
		}
	}

}
