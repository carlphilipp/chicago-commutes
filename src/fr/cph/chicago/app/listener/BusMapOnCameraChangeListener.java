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

package fr.cph.chicago.app.listener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

import fr.cph.chicago.R;

/**
 * Bus map camera on change listener. Handle the update of markers on the map
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BusMapOnCameraChangeListener implements OnCameraChangeListener {

    private final BitmapDescriptor bitmapDescriptor1;
    private final BitmapDescriptor bitmapDescriptor2;
    private final BitmapDescriptor bitmapDescriptor3;
    private BitmapDescriptor currentBitmapDescriptor;
    private List<Marker> busMarkers;
    private List<Marker> busStationMarkers;

    public BusMapOnCameraChangeListener(@NonNull final Context context) {
        final Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.bus);
        final Bitmap bitmap1 = Bitmap.createScaledBitmap(icon, icon.getWidth() / 9, icon.getHeight() / 9, true);
        final Bitmap bitmap2 = Bitmap.createScaledBitmap(icon, icon.getWidth() / 5, icon.getHeight() / 5, true);
        final Bitmap bitmap3 = Bitmap.createScaledBitmap(icon, icon.getWidth() / 3, icon.getHeight() / 3, true);
        this.bitmapDescriptor1 = BitmapDescriptorFactory.fromBitmap(bitmap1);
        this.bitmapDescriptor2 = BitmapDescriptorFactory.fromBitmap(bitmap2);
        this.bitmapDescriptor3 = BitmapDescriptorFactory.fromBitmap(bitmap3);
        this.setCurrentBitmapDescriptor(bitmapDescriptor2);
        this.busMarkers = new ArrayList<>();
        this.busStationMarkers = new ArrayList<>();
    }

    public void setBusMarkers(@NonNull final List<Marker> busMarkers) {
        this.busMarkers = busMarkers;
    }

    public void setBusStationMarkers(@NonNull final List<Marker> busStationMarkers) {
        this.busStationMarkers = busStationMarkers;
    }

    @Override
    public void onCameraChange(final CameraPosition position) {
        float currentZoom = -1;
        if (position.zoom != currentZoom) {
            float oldZoom = currentZoom;
            currentZoom = position.zoom;

            // Handle bus bitmap
            if (isIn(currentZoom, 12.9f, 11f) && !isIn(oldZoom, 12.9f, 11f)) {
                for (final Marker marker : busMarkers) {
                    marker.setIcon(bitmapDescriptor1);
                }
                setCurrentBitmapDescriptor(bitmapDescriptor1);
            } else if (isIn(currentZoom, 14.9f, 13f) && !isIn(oldZoom, 14.9f, 13f)) {
                for (final Marker marker : busMarkers) {
                    marker.setIcon(bitmapDescriptor2);
                }
                setCurrentBitmapDescriptor(bitmapDescriptor2);
            } else if (isIn(currentZoom, 21f, 15f) && !isIn(oldZoom, 21f, 15f)) {
                for (final Marker marker : busMarkers) {
                    marker.setIcon(bitmapDescriptor3);
                }
                setCurrentBitmapDescriptor(bitmapDescriptor3);
            }

            // Handle stops markers
            if (isIn(currentZoom, 21f, 16f) && !isIn(oldZoom, 21f, 16f)) {
                for (final Marker marker : busStationMarkers) {
                    marker.setVisible(true);
                }
            } else {
                for (final Marker marker : busStationMarkers) {
                    marker.setVisible(false);
                }
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
