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

import android.util.Log;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.util.Util;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Map.Entry;

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
	private static final String TAG = "CtaConnect";
	/**
	 * Singleton
	 **/
	private static CtaConnect instance = null;
	/**
	 * The train arrival address
	 **/
	private static final String BASE_URL_TRAIN_ARRIVALS = "http://lapi.transitchicago.com/api/1.0/ttarrivals.aspx";
	/**
	 * The train follow address
	 **/
	private static final String BASE_URL_TRAIN_FOLLOW = "http://lapi.transitchicago.com/api/1.0/ttfollow.aspx";
	/**
	 * The train location address
	 **/
	private static final String BASE_URL_TRAIN_LOCATION = "http://lapi.transitchicago.com/api/1.0/ttpositions.aspx";
	/**
	 * The buses routes address
	 **/
	private static final String BASE_URL_BUS_ROUTES = "http://www.ctabustracker.com/bustime/api/v1/getroutes";
	/**
	 * The buses direction address
	 **/
	private static final String BASE_URL_BUS_DIRECTION = "http://www.ctabustracker.com/bustime/api/v1/getdirections";
	/**
	 * The buses stops address
	 **/
	private static final String BASE_URL_BUS_STOPS = "http://www.ctabustracker.com/bustime/api/v1/getstops";
	/**
	 * The buses vehicles address
	 **/
	private static final String BASE_URL_BUS_VEHICLES = "http://www.ctabustracker.com/bustime/api/v1/getvehicles";
	/**
	 * The buses arrival address
	 **/
	private static final String BASE_URL_BUS_ARRIVAL = "http://www.ctabustracker.com/bustime/api/v1/getpredictions";
	/**
	 * The buses pattern address
	 **/
	private static final String BASE_URL_BUS_PATTERN = "http://www.ctabustracker.com/bustime/api/v1/getpatterns";
	/**
	 * The cta bus API key
	 **/
	private static String CTA_BUS_KEY;
	/**
	 * The cta train API key
	 **/
	private static String CTA_TRAIN_KEY;

	/**
	 * Private constructor
	 */
	private CtaConnect() {
		CTA_TRAIN_KEY = Util.getProperty("cta.train.key");
		CTA_BUS_KEY = Util.getProperty("cta.bus.key");
	}

	/**
	 * Get a singleton access to this class
	 *
	 * @return a CtaConnect instance
	 */
	public static CtaConnect getInstance() {
		if (instance == null) {
			instance = new CtaConnect();
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
	public final String connect(final CtaRequestType requestType, final MultiValuedMap<String, String> params) throws ConnectException {
		StringBuilder address = null;
		switch (requestType) {
		case TRAIN_ARRIVALS:
			address = new StringBuilder(BASE_URL_TRAIN_ARRIVALS + "?key=" + CTA_TRAIN_KEY);
			break;
		case TRAIN_FOLLOW:
			address = new StringBuilder(BASE_URL_TRAIN_FOLLOW + "?key=" + CTA_TRAIN_KEY);
			break;
		case TRAIN_LOCATION:
			address = new StringBuilder(BASE_URL_TRAIN_LOCATION + "?key=" + CTA_TRAIN_KEY);
			break;
		case BUS_ROUTES:
			address = new StringBuilder(BASE_URL_BUS_ROUTES + "?key=" + CTA_BUS_KEY);
			break;
		case BUS_DIRECTION:
			address = new StringBuilder(BASE_URL_BUS_DIRECTION + "?key=" + CTA_BUS_KEY);
			break;
		case BUS_STOP_LIST:
			address = new StringBuilder(BASE_URL_BUS_STOPS + "?key=" + CTA_BUS_KEY);
			break;
		case BUS_VEHICLES:
			address = new StringBuilder(BASE_URL_BUS_VEHICLES + "?key=" + CTA_BUS_KEY);
			break;
		case BUS_ARRIVALS:
			address = new StringBuilder(BASE_URL_BUS_ARRIVAL + "?key=" + CTA_BUS_KEY);
			break;
		case BUS_PATTERN:
			address = new StringBuilder(BASE_URL_BUS_PATTERN + "?key=" + CTA_BUS_KEY);
			break;
		default:
			break;
		}
		for (final Entry<String, Collection<String>> entry : params.asMap().entrySet()) {
			final String key = entry.getKey();
			final Collection<String> values = entry.getValue();
			for (final String value : values) {
				address.append("&").append(key).append("=").append(value);
			}
		}
		final String xml = connectUrl(address.toString());
		Log.v(TAG, "Result: " + xml);
		return xml;
	}

	/**
	 * Connect url
	 *
	 * @param address the address
	 * @return the answer
	 * @throws ConnectException
	 */
	private String connectUrl(final String address) throws ConnectException {
		String toReturn = null;
		HttpURLConnection urlConnection = null;
		InputStream inputStream = null;
		try {
			Log.v(TAG, "Address: " + address);
			final URL url = new URL(address);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setConnectTimeout(10000);
			urlConnection.setReadTimeout(10000);
			inputStream = new BufferedInputStream(urlConnection.getInputStream());
			toReturn = IOUtils.toString(inputStream);
		} catch (final IOException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new ConnectException(ConnectException.ERROR, e);
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			IOUtils.closeQuietly(inputStream);
		}
		return toReturn;
	}
}
