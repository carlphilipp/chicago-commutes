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
import android.support.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

import fr.cph.chicago.App;
import fr.cph.chicago.R;

/**
 * Train map on camera change listener
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class TrainMapOnCameraChangeListener implements OnCameraChangeListener {

    private final BitmapDescriptor bitmapDescriptor1;
    private final BitmapDescriptor bitmapDescriptor2;
    private final BitmapDescriptor bitmapDescriptor3;
    private BitmapDescriptor currentBitmapDescriptor;
    private List<Marker> trainMarkers;

    public TrainMapOnCameraChangeListener() {
        final Bitmap icon = BitmapFactory.decodeResource(App.getContext().getResources(), R.drawable.train);
        final Bitmap bitmap1 = Bitmap.createScaledBitmap(icon, icon.getWidth() / 9, icon.getHeight() / 9, true);
        final Bitmap bitmap2 = Bitmap.createScaledBitmap(icon, icon.getWidth() / 5, icon.getHeight() / 5, true);
        final Bitmap bitmap3 = Bitmap.createScaledBitmap(icon, icon.getWidth() / 3, icon.getHeight() / 3, true);
        this.bitmapDescriptor1 = BitmapDescriptorFactory.fromBitmap(bitmap1);
        this.bitmapDescriptor2 = BitmapDescriptorFactory.fromBitmap(bitmap2);
        this.bitmapDescriptor3 = BitmapDescriptorFactory.fromBitmap(bitmap3);
        this.setCurrentBitmapDescriptor(bitmapDescriptor2);
    }

    public void setTrainMarkers(@NonNull final List<Marker> trainMarkers) {
        this.trainMarkers = trainMarkers;
    }

    @Override
    public void onCameraChange(final CameraPosition position) {
        float currentZoom = -1;
        if (position.zoom != currentZoom) {
            float oldZoom = currentZoom;
            currentZoom = position.zoom;

            if (isIn(currentZoom, 12.9f, 11f) && !isIn(oldZoom, 12.9f, 11f)) {
                for (final Marker marker : trainMarkers) {
                    marker.setIcon(bitmapDescriptor1);
                }
                setCurrentBitmapDescriptor(bitmapDescriptor1);
            } else if (isIn(currentZoom, 14.9f, 13f) && !isIn(oldZoom, 14.9f, 13f)) {
                for (final Marker marker : trainMarkers) {
                    marker.setIcon(bitmapDescriptor2);
                }
                setCurrentBitmapDescriptor(bitmapDescriptor2);
            } else if (isIn(currentZoom, 21f, 15f) && !isIn(oldZoom, 21f, 15f)) {
                for (final Marker marker : trainMarkers) {
                    marker.setIcon(bitmapDescriptor3);
                }
                setCurrentBitmapDescriptor(bitmapDescriptor3);
            }
        }
    }

    private boolean isIn(final float num, final float sup, final float inf) {
        return num >= inf && num <= sup;
    }

    @NonNull
    public final BitmapDescriptor getCurrentBitmapDescriptor() {
        return currentBitmapDescriptor;
    }

    private void setCurrentBitmapDescriptor(@NonNull final BitmapDescriptor currentBitmapDescriptor) {
        this.currentBitmapDescriptor = currentBitmapDescriptor;
    }
}
