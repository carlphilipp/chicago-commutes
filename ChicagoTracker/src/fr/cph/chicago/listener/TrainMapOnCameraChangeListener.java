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

import java.util.List;

/**
 * Train map on camera change listener
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class TrainMapOnCameraChangeListener implements OnCameraChangeListener {

	private Bitmap bitmap1;
	private Bitmap bitmap2;
	private Bitmap bitmap3;
	private Bitmap currentBitmap;
	private List<Marker> trainMarkers;

	/**
	 *
	 */
	public TrainMapOnCameraChangeListener() {
		final Bitmap icon = BitmapFactory.decodeResource(ChicagoTracker.getContext().getResources(), R.drawable.train);
		this.bitmap1 = Bitmap.createScaledBitmap(icon, icon.getWidth() / 9, icon.getHeight() / 9, false);
		this.bitmap2 = Bitmap.createScaledBitmap(icon, icon.getWidth() / 5, icon.getHeight() / 5, false);
		this.bitmap3 = Bitmap.createScaledBitmap(icon, icon.getWidth() / 3, icon.getHeight() / 3, false);
		this.setCurrentBitmap(bitmap1);
	}

	/**
	 * @param trainMarkers
	 */
	public void setTrainMarkers(List<Marker> trainMarkers) {
		this.trainMarkers = trainMarkers;
	}

	@Override
	public void onCameraChange(CameraPosition position) {
		float currentZoom = -1;
		if (position.zoom != currentZoom) {
			float oldZoom = currentZoom;
			currentZoom = position.zoom;
			if (isIn(currentZoom, 12.9f, 11f) && !isIn(oldZoom, 12.9f, 11f)) {
				for (final Marker marker : trainMarkers) {
					setCurrentBitmap(bitmap1);
					marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap1));
				}
			} else if (isIn(currentZoom, 14.9f, 13f) && !isIn(oldZoom, 14.9f, 13f)) {
				for (final Marker marker : trainMarkers) {
					setCurrentBitmap(bitmap2);
					marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap2));
				}
			} else if (isIn(currentZoom, 21f, 15f) && !isIn(oldZoom, 21f, 15f)) {
				for (final Marker marker : trainMarkers) {
					setCurrentBitmap(bitmap3);
					marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap3));
				}
			}
		}
	}

	private boolean isIn(final float num, final float sup, final float inf) {
		return num >= inf && num <= sup;
	}

	public final Bitmap getCurrentBitmap() {
		return currentBitmap;
	}

	private void setCurrentBitmap(final Bitmap currentBitmap) {
		this.currentBitmap = currentBitmap;
	}

}
