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

import android.content.Intent;
import android.net.Uri;
import android.view.View;

public class GoogleMapOnClickListener extends GoogleMapListener {

	public GoogleMapOnClickListener(final double latitude, final double longitude) {
		super(latitude, longitude);
	}

	@Override
	public void onClick(final View v) {
		final String uri = "http://maps.google.com/maps?z=12&t=m&q=loc:" + latitude + "+" + longitude;
		final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
		i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		v.getContext().startActivity(i);
	}
}
