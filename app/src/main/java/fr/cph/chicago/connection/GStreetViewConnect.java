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

package fr.cph.chicago.connection;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.annimon.stream.Optional;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.URL;

import fr.cph.chicago.R;

import static fr.cph.chicago.Constants.GOOGLE_STREET_VIEW_URL;

/**
 * Class that access google street api. Singleton
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class GStreetViewConnect {

    private static final String TAG = GStreetViewConnect.class.getSimpleName();
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 300;

    private String googleKey;

    private static GStreetViewConnect INSTANCE = null;

    private GStreetViewConnect() {
    }

    /**
     * Private constructor, that get the API key from property file
     */
    private GStreetViewConnect(@NonNull final Context context) {
        googleKey = context.getString(R.string.google_maps_api_key);
    }

    /**
     * Get INSTANCE of this class
     */
    @NonNull
    public static GStreetViewConnect getInstance(@NonNull final Context context) {
        if (INSTANCE == null) {
            INSTANCE = new GStreetViewConnect(context);
        }
        return INSTANCE;
    }

    @NonNull
    public final Optional<Drawable> connect(final double latitude, final double longitude) {
        final StringBuilder address = new StringBuilder(GOOGLE_STREET_VIEW_URL);
        address.append("?key=");
        address.append(googleKey);
        address.append("&sensor=false");
        address.append("&size=").append(WIDTH).append("x").append(HEIGHT);
        address.append("&fov=120");
        address.append("&location=");
        address.append(latitude);
        address.append(",").append(longitude);
        return connectUrl(address.toString());
    }

    /**
     * Connect to the API and get the MAP
     *
     * @param address the address to connect to
     * @return a drawable map
     */
    @NonNull
    private Optional<Drawable> connectUrl(@NonNull final String address) {
        Log.v(TAG, "Address: " + address);
        InputStream is = null;
        try {
            is = (InputStream) new URL(address).getContent();
            return Optional.of(Drawable.createFromStream(is, "src name"));
        } catch (final Exception e) {
            Log.e(TAG, e.getMessage(), e);
            // TODO add a temporary image here
            return Optional.empty();
        } finally {
            IOUtils.closeQuietly(is);
        }
    }
}
