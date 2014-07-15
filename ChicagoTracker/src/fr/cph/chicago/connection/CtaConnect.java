/**
 * Copyright 2014 Carl-Philipp Harmant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.connection;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.collections4.MultiMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import android.util.Log;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.util.Util;

/**
 * Class that build url and connect to CTA API
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class CtaConnect {

	/** Tag **/
	private static final String TAG = "CtaConnect";
	/** Singleton **/
	private static CtaConnect instance = null;
	/** The train arrival address **/
	private static final String BASE_URL_TRAIN_ARRIVALS = "http://lapi.transitchicago.com/api/1.0/ttarrivals.aspx";
	/** The train follow address **/
	private static final String BASE_URL_TRAIN_FOLLOW = "http://lapi.transitchicago.com/api/1.0/ttfollow.aspx";
	/** The train location address **/
	private static final String BASE_URL_TRAIN_LOCATION = "http://lapi.transitchicago.com/api/1.0/ttpositions.aspx";
	/** The buses routes address **/
	private static final String BASE_URL_BUS_ROUTES = "http://www.ctabustracker.com/bustime/api/v1/getroutes";
	/** The buses direction address **/
	private static final String BASE_URL_BUS_DIRECTION = "http://www.ctabustracker.com/bustime/api/v1/getdirections";
	/** The buses stops address **/
	private static final String BASE_URL_BUS_STOPS = "http://www.ctabustracker.com/bustime/api/v1/getstops";
	/** The buses vehicles address **/
	private static final String BASE_URL_BUS_VEHICLES = "http://www.ctabustracker.com/bustime/api/v1/getvehicles";
	/** The buses arrival address **/
	private static final String BASE_URL_BUS_ARRIVAL = "http://www.ctabustracker.com/bustime/api/v1/getpredictions";
	/** The buses pattern address **/
	private static final String BASE_URL_BUS_PATTERN = "http://www.ctabustracker.com/bustime/api/v1/getpatterns";
	/** The alert general address **/
	private static final String BASE_URL_ALERT_GENERAL = "http://www.transitchicago.com/api/1.0/alerts.aspx";
	/** The alert routes address **/
	private static final String BASE_URL_ALERT_ROUTES = "http://www.transitchicago.com/api/1.0/routes.aspx";
	/** The cta bus API key **/
	private String CTA_BUS_KEY;
	/** The cta train API key **/
	private String CTA_TRAIN_KEY;
	/** The http client **/
	private DefaultHttpClient client;

	/**
	 * Private constructor
	 */
	private CtaConnect() {
		this.client = new DefaultHttpClient();
		CTA_TRAIN_KEY = Util.getProperty("cta.train.key");
		CTA_BUS_KEY = Util.getProperty("cta.bus.key");
	}

	/**
	 * Get a singleton access to this class
	 * 
	 * @return a CtaConnect instance
	 */
	public final static CtaConnect getInstance() {
		if (instance == null) {
			instance = new CtaConnect();
		}
		return instance;
	}

	/**
	 * Connect
	 * 
	 * @param requestType
	 *            the type of request
	 * @param params
	 *            the params
	 * @return a string
	 * @throws ConnectException
	 */
	public final String connect(final CtaRequestType requestType, final MultiMap<String, String> params) throws ConnectException {
		StringBuilder adress = null;
		switch (requestType) {
		case TRAIN_ARRIVALS:
			adress = new StringBuilder(BASE_URL_TRAIN_ARRIVALS + "?key=" + CTA_TRAIN_KEY);
			break;
		case TRAIN_FOLLOW:
			adress = new StringBuilder(BASE_URL_TRAIN_FOLLOW + "?key=" + CTA_TRAIN_KEY);
			break;
		case TRAIN_LOCATION:
			adress = new StringBuilder(BASE_URL_TRAIN_LOCATION + "?key=" + CTA_TRAIN_KEY);
			break;
		case BUS_ROUTES:
			adress = new StringBuilder(BASE_URL_BUS_ROUTES + "?key=" + CTA_BUS_KEY);
			break;
		case BUS_DIRECTION:
			adress = new StringBuilder(BASE_URL_BUS_DIRECTION + "?key=" + CTA_BUS_KEY);
			break;
		case BUS_STOP_LIST:
			adress = new StringBuilder(BASE_URL_BUS_STOPS + "?key=" + CTA_BUS_KEY);
			break;
		case BUS_VEHICLES:
			adress = new StringBuilder(BASE_URL_BUS_VEHICLES + "?key=" + CTA_BUS_KEY);
			break;
		case BUS_ARRIVALS:
			adress = new StringBuilder(BASE_URL_BUS_ARRIVAL + "?key=" + CTA_BUS_KEY);
			break;
		case BUS_PATTERN:
			adress = new StringBuilder(BASE_URL_BUS_PATTERN + "?key=" + CTA_BUS_KEY);
			break;
		case ALERTS_GENERAL:
			adress = new StringBuilder(BASE_URL_ALERT_GENERAL + "?activeonly=false");
			break;
		case ALERTS_ROUTES:
			adress = new StringBuilder(BASE_URL_ALERT_ROUTES + "?");
			break;
		default:
			break;
		}
		for (Entry<String, Object> entry : params.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof String) {
				adress.append("&" + key + "=" + value);
			} else if (value instanceof List) {
				@SuppressWarnings("unchecked")
				List<String> list = (List<String>) value;
				for (String l : list) {
					adress.append("&" + key + "=" + l);
				}
			}
		}
		String xml = connectUrl(adress.toString());
		Log.v(TAG, "Result: " + xml);
		return xml;
	}

	/**
	 * Connect url
	 * 
	 * @param address
	 *            the address
	 * @return the answer
	 * @throws ConnectException
	 */
	private String connectUrl(final String address) throws ConnectException {
		String toreturn = null;
		try {
			client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
			client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
			Log.v(TAG, "adress: " + address);
			HttpGet get = new HttpGet(address);
			HttpResponse getResponse = client.execute(get);
			HttpEntity responseEntity = getResponse.getEntity();

			Charset charset = Charset.forName("UTF8");
			InputStreamReader in = new InputStreamReader(responseEntity.getContent(), charset);
			int c = in.read();
			StringBuilder build = new StringBuilder();
			while (c != -1) {
				build.append((char) c);
				c = in.read();
			}
			toreturn = build.toString();
		} catch (ConnectTimeoutException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new ConnectException(ConnectException.ERROR, e);
		} catch (UnknownHostException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new ConnectException(ConnectException.ERROR, e);
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new ConnectException(ConnectException.ERROR, e);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new ConnectException(ConnectException.ERROR, e);
		}
		return toreturn;
	}
}
