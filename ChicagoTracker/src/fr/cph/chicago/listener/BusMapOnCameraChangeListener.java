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

package fr.cph.chicago.listener;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Bus map camera on change listener. Handle the update of markers on the map
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BusMapOnCameraChangeListener implements OnCameraChangeListener {
	/** **/
	private float currentZoom = -1;
	/** **/
	private float oldZoom = -1;
	/** **/
	private Bitmap bitmap1;
	/** **/
	private Bitmap bitmap2;
	/** **/
	private Bitmap bitmap3;
	/** **/
	private Bitmap currentBitmap;
	/** **/
	private List<Marker> busMarkers;
	/** **/
	private List<Marker> busStationMarkers;

	public BusMapOnCameraChangeListener() {
		Bitmap icon = BitmapFactory.decodeResource(ChicagoTracker.getAppContext().getResources(), R.drawable.bus);
		this.bitmap1 = Bitmap.createScaledBitmap(icon, icon.getWidth() / 9, icon.getHeight() / 9, false);
		this.bitmap2 = Bitmap.createScaledBitmap(icon, icon.getWidth() / 5, icon.getHeight() / 5, false);
		this.bitmap3 = Bitmap.createScaledBitmap(icon, icon.getWidth() / 3, icon.getHeight() / 3, false);
		this.setCurrentBitmap(bitmap1);
		this.busMarkers = new ArrayList<>();
		this.busStationMarkers = new ArrayList<>();
	}

	public void setBusMarkers(List<Marker> busMarkers) {
		this.busMarkers = busMarkers;
	}

	public void setBusStationMarkers(List<Marker> busStationMarkers) {
		this.busStationMarkers = busStationMarkers;
	}

	@Override
	public void onCameraChange(CameraPosition position) {
		if (position.zoom != currentZoom) {
			oldZoom = currentZoom;
			currentZoom = position.zoom;
			if (isIn(currentZoom, 12.9f, 11f) && !isIn(oldZoom, 12.9f, 11f)) {
				for (Marker marker : busMarkers) {
					this.setCurrentBitmap(bitmap1);
					marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap1));
				}
			} else if (isIn(currentZoom, 14.9f, 13f) && !isIn(oldZoom, 14.9f, 13f)) {
				for (Marker marker : busMarkers) {
					this.setCurrentBitmap(bitmap2);
					marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap2));
				}
			} else if (isIn(currentZoom, 21f, 15f) && !isIn(oldZoom, 21f, 15f)) {
				for (Marker marker : busMarkers) {
					this.setCurrentBitmap(bitmap3);
					marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap3));
				}
			}
			if (isIn(currentZoom, 21f, 16f) && !isIn(oldZoom, 21f, 16f)) {
				for (Marker marker : busStationMarkers) {
					marker.setVisible(true);
				}
			} else if (!isIn(currentZoom, 21f, 16f) && isIn(oldZoom, 21f, 16f)) {
				for (Marker marker : busStationMarkers) {
					marker.setVisible(false);
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
