/**
 * Copyright 2016 Carl-Philipp Harmant
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.connection;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.annimon.stream.Stream;

import org.apache.commons.collections4.MultiValuedMap;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import fr.cph.chicago.R;
import fr.cph.chicago.exception.ConnectException;

/**
 * Class that build url and connect to CTA API
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class CtaConnect {

    /**
     * Tag
     **/
    private static final String TAG = CtaConnect.class.getSimpleName();
    /**
     * Singleton
     **/
    private static CtaConnect instance = null;
    /**
     * The cta bus API key
     **/
    private String ctaBusKey;
    /**
     * The cta train API key
     **/
    private String ctaTrainKey;

    private CtaConnect(){
    }

    private CtaConnect(@NonNull Context context) {
        ctaTrainKey = context.getString(R.string.cta_train_key);
        ctaBusKey = context.getString(R.string.cta_bus_key);
    }

    /**
     * Get a singleton access to this class
     *
     * @return a CtaConnect instance
     */
    @NonNull
    public static CtaConnect getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new CtaConnect(context);
        }
        return instance;
    }

    /**
     * Connect
     *
     * @param requestType the type of request
     * @param params      the params
     * @return a string
     * @throws ConnectException
     */
    @NonNull
    public final InputStream connect(@NonNull Context context, @NonNull final CtaRequestType requestType, @NonNull final MultiValuedMap<String, String> params) throws ConnectException {
        final StringBuilder address;
        switch (requestType) {
        case TRAIN_ARRIVALS:
            address = new StringBuilder(context.getString(R.string.url_train_arrivals) + "?key=" + ctaTrainKey);
            break;
        case TRAIN_FOLLOW:
            address = new StringBuilder(context.getString(R.string.url_train_follow) + "?key=" + ctaTrainKey);
            break;
        case TRAIN_LOCATION:
            address = new StringBuilder(context.getString(R.string.url_train_location) + "?key=" + ctaTrainKey);
            break;
        case BUS_ROUTES:
            address = new StringBuilder(context.getString(R.string.url_bus_routes) + "?key=" + ctaBusKey);
            break;
        case BUS_DIRECTION:
            address = new StringBuilder(context.getString(R.string.url_bus_direction) + "?key=" + ctaBusKey);
            break;
        case BUS_STOP_LIST:
            address = new StringBuilder(context.getString(R.string.url_bus_stop) + "?key=" + ctaBusKey);
            break;
        case BUS_VEHICLES:
            address = new StringBuilder(context.getString(R.string.url_bus_vehicles) + "?key=" + ctaBusKey);
            break;
        case BUS_ARRIVALS:
            address = new StringBuilder(context.getString(R.string.url_bus_arrival) + "?key=" + ctaBusKey);
            break;
        case BUS_PATTERN:
            address = new StringBuilder(context.getString(R.string.url_bus_pattern) + "?key=" + ctaBusKey);
            break;
        default:
            address = new StringBuilder();
        }
        Stream.of(params.asMap().entrySet())
            .flatMap(entry -> {
                final String key = entry.getKey();
                return Stream.of(entry.getValue()).map(value -> new StringBuilder().append("&").append(key).append("=").append(value));
            })
            .forEach(address::append);

        return connectUrl(address.toString());
    }

    /**
     * Connect url
     *
     * @param address the address
     * @return the answer
     * @throws ConnectException
     */
    @NonNull
    private InputStream connectUrl(@NonNull final String address) throws ConnectException {
        final HttpURLConnection urlConnection;
        final InputStream inputStream;
        try {
            Log.v(TAG, "Address: " + address);
            final URL url = new URL(address);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            inputStream = new BufferedInputStream(urlConnection.getInputStream());
        } catch (final IOException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ConnectException(ConnectException.ERROR, e);
        }
        return inputStream;
    }
}
