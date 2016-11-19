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

package fr.cph.chicago.map;

import android.content.Context;
import android.support.annotation.NonNull;

import com.annimon.stream.Stream;
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
public class RefreshBusMarkers extends RefreshMarkers {

    public RefreshBusMarkers(@NonNull final Context context) {
        super(context, R.drawable.bus);
    }

    public void refreshBusAndStation(@NonNull final CameraPosition position,
                                     @NonNull final List<Marker> busMarkers,
                                     @NonNull final List<Marker> busStationMarkers) {
        refresh(position, busMarkers);
        float currentZoom = -1;
        if (position.zoom != currentZoom) {
            float oldZoom = currentZoom;
            currentZoom = position.zoom;

            // Handle stops markers
            if (isIn(currentZoom, 21f, 16f) && !isIn(oldZoom, 21f, 16f)) {
                Stream.of(busStationMarkers).forEach(marker -> marker.setVisible(true));
            } else {
                Stream.of(busStationMarkers).forEach(marker -> marker.setVisible(false));
            }
        }
    }
}
