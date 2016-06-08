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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import java.util.Locale;

public class GoogleStreetOnClickListener extends GoogleMapListener {

	public GoogleStreetOnClickListener(final double latitude, final double longitude) {
		super(latitude, longitude);
	}

	@Override
	public void onClick(final View v) {
		String uri = String.format(Locale.ENGLISH, "google.streetview:cbll=%f,%f&cbp=1,180,,0,1&mz=1", latitude, longitude);
		final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
		intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			v.getContext().startActivity(intent);
		} catch (final ActivityNotFoundException ex) {
			uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?q=&layer=c&cbll=%f,%f&cbp=11,0,0,0,0", latitude, longitude);
			final Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            unrestrictedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            v.getContext().startActivity(unrestrictedIntent);
		}
	}
}
