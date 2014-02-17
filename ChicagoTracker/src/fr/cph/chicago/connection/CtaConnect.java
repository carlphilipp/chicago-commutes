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

public class CtaConnect {

	private static final String TAG = "CtaConnect";

	private static CtaConnect instance = null;

	private static final String BASE_URL_TRAIN_ARRIVALS = "http://lapi.transitchicago.com/api/1.0/ttarrivals.aspx";
	private static final String BASE_URL_TRAIN_FOLLOW = "http://lapi.transitchicago.com/api/1.0/ttfollow.aspx";
	private static final String BASE_URL_TRAIN_LOCATION = "http://lapi.transitchicago.com/api/1.0/ttpositions.aspx";

	private static final String BASE_URL_BUS_ROUTES = "http://www.ctabustracker.com/bustime/api/v1/getroutes";
	private static final String BASE_URL_BUS_DIRECTION = "http://www.ctabustracker.com/bustime/api/v1/getdirections";
	private static final String BASE_URL_BUS_STOPS = "http://www.ctabustracker.com/bustime/api/v1/getstops";
	private static final String BASE_URL_BUS_ARRIVAL = "http://www.ctabustracker.com/bustime/api/v1/getpredictions";

	private String CTA_BUS_KEY;
	private String CTA_TRAIN_KEY;

	private DefaultHttpClient client;

	private CtaConnect() {
		this.client = new DefaultHttpClient();
		CTA_TRAIN_KEY = Util.getProperty("cta.train.key");
		CTA_BUS_KEY = Util.getProperty("cta.bus.key");
	}

	public final static CtaConnect getInstance() {
		if (instance == null) {
			instance = new CtaConnect();
		}
		return instance;
	}

	private String connectUrl(final String adress) throws ConnectException {
		String toreturn = null;
		try {
			client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
			client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
			Log.v(TAG, "adress: " + adress);
			HttpGet get = new HttpGet(adress);
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
			throw new ConnectException("Connect exception", e);
		} catch (UnknownHostException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new ConnectException("Connect exception", e);
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new ConnectException("Connect exception", e);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new ConnectException("Connect exception", e);
		}
		return toreturn;
	}

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
		case BUS_ARRIVALS:
			adress = new StringBuilder(BASE_URL_BUS_ARRIVAL + "?key=" + CTA_BUS_KEY);
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
}
