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

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;

public abstract class GoogleMapListener implements View.OnClickListener {

	protected Activity activity;
	protected double latitude;
	protected double longitude;

	public GoogleMapListener(@NonNull final Activity activity, final double latitude, final double longitude) {
		this.activity = activity;
		this.latitude = latitude;
		this.longitude = longitude;
	}
}
