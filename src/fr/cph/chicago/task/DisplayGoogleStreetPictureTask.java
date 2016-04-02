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

package fr.cph.chicago.task;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;

import fr.cph.chicago.App;
import fr.cph.chicago.R;
import fr.cph.chicago.connection.GStreetViewConnect;
import fr.cph.chicago.util.Util;

/**
 * Display google street view image
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
// TODO do the update of image and text in the activity. This class should only do the call
public final class DisplayGoogleStreetPictureTask extends AsyncTask<Double, Void, Drawable> {

    private static final String TAG = DisplayGoogleStreetPictureTask.class.getSimpleName();

    private Activity activity;
    private ImageView streetViewImage;
    private TextView streetViewText;

    public DisplayGoogleStreetPictureTask(@NonNull final Activity activity, @NonNull final ImageView streetViewImage, @NonNull final TextView streetViewText) {
        this.activity = activity;
        this.streetViewImage = streetViewImage;
        this.streetViewText = streetViewText;
    }

    @Override
    protected final Drawable doInBackground(final Double... params) {
        try {
            final GStreetViewConnect connect = GStreetViewConnect.getInstance();
            double latitude = params[0];
            double longitude = params[1];
            Util.trackAction(activity, R.string.analytics_category_req, R.string.analytics_action_get_google, R.string.analytics_action_get_google_map_street_view, 0);
            return connect.connect(latitude, longitude);
        } catch (IOException e) {
            Log.e(TAG, "Error while connecting to google street view API", e);
            return null;
        }
    }

    @Override
    protected final void onPostExecute(final Drawable result) {
        final int height = (int) activity.getResources().getDimension(R.dimen.activity_station_street_map_height);
        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) streetViewImage.getLayoutParams();
        final ViewGroup.LayoutParams params = streetViewImage.getLayoutParams();
        params.height = height;
        params.width = layoutParams.width;

        streetViewImage.setLayoutParams(params);
        streetViewImage.setImageDrawable(result);
        streetViewText.setText(App.getContext().getString(R.string.station_activity_street_view));
    }
}
