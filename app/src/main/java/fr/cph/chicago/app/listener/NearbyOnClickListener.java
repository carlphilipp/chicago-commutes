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

import android.view.View;

import com.annimon.stream.Stream;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

public class NearbyOnClickListener implements View.OnClickListener {

    private final GoogleMap googleMap;
    private final List<Marker> markers;
    private final int id;
    private final double latitude;
    private final double longitude;

    public NearbyOnClickListener(final GoogleMap googleMap, final List<Marker> markers, final int id, final double latitude, final double longitude) {
        this.googleMap = googleMap;
        this.markers = markers;
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public void onClick(final View v) {
        final LatLng latLng = new LatLng(latitude, longitude);
        final CameraPosition current = new CameraPosition.Builder().target(latLng).zoom(15.5f).bearing(0).tilt(0).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(current), Math.max(1000, 1), null);
        Stream.of(markers)
            .filter(marker -> marker.getSnippet().equals(Integer.toString(id)))
            .findFirst()
            .ifPresent(Marker::showInfoWindow);
    }
}
