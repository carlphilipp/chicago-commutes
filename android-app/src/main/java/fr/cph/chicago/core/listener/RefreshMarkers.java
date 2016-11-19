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

package fr.cph.chicago.core.listener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import com.annimon.stream.Stream;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

import fr.cph.chicago.R;

/**
 * Refresh buses and stop markers
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class RefreshMarkers {

    private final BitmapDescriptor bitmapDescrSmall;
    private final BitmapDescriptor bitmapDescrMedium;
    private final BitmapDescriptor bitmapDescrLarge;
    private BitmapDescriptor currentDescriptor;

    public RefreshMarkers(@NonNull final Context context) {
        final Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.bus);
        bitmapDescrSmall = createBitMapDescriptor(icon, 9);
        bitmapDescrMedium = createBitMapDescriptor(icon, 5);
        bitmapDescrLarge = createBitMapDescriptor(icon, 3);
        currentDescriptor = bitmapDescrSmall;
    }

    private BitmapDescriptor createBitMapDescriptor(final Bitmap icon, final int size) {
        final Bitmap bitmap = Bitmap.createScaledBitmap(icon, icon.getWidth() / size, icon.getHeight() / size, true);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void refresh(@NonNull final CameraPosition position,
                        @NonNull final List<Marker> busMarkers,
                        @NonNull final List<Marker> busStationMarkers) {
        float currentZoom = -1;
        if (position.zoom != currentZoom) {
            float oldZoom = currentZoom;
            currentZoom = position.zoom;

            // Handle bus bitmap
            if (isIn(currentZoom, 12.9f, 11f) && !isIn(oldZoom, 12.9f, 11f)) {
                Stream.of(busMarkers).forEach(marker -> marker.setIcon(bitmapDescrSmall));
                currentDescriptor = bitmapDescrSmall;
            } else if (isIn(currentZoom, 14.9f, 13f) && !isIn(oldZoom, 14.9f, 13f)) {
                Stream.of(busMarkers).forEach(marker -> marker.setIcon(bitmapDescrMedium));
                currentDescriptor = bitmapDescrMedium;
            } else if (isIn(currentZoom, 21f, 15f) && !isIn(oldZoom, 21f, 15f)) {
                Stream.of(busMarkers).forEach(marker -> marker.setIcon(bitmapDescrLarge));
                currentDescriptor = bitmapDescrLarge;
            }

            // Handle stops markers
            if (isIn(currentZoom, 21f, 16f) && !isIn(oldZoom, 21f, 16f)) {
                Stream.of(busStationMarkers).forEach(marker -> marker.setVisible(true));
            } else {
                Stream.of(busStationMarkers).forEach(marker -> marker.setVisible(false));
            }
        }
    }

    private boolean isIn(final float num, final float sup, final float inf) {
        return num >= inf && num <= sup;
    }

    public BitmapDescriptor getCurrentBitmapDescriptor() {
        return currentDescriptor;
    }
}
