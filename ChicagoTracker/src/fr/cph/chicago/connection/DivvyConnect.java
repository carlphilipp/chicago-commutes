/**
 * Copyright 2016 Carl-Philipp Harmant
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
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import android.util.Log;
import fr.cph.chicago.exception.ConnectException;

public class DivvyConnect {

	/** Tag **/
	private static final String TAG = "DivvyConnect";
	/** The http client **/
	private DefaultHttpClient client;
	/** Singleton **/
	private static DivvyConnect instance = null;

	private static final String URL = "http://www.divvybikes.com/stations/json";

	private DivvyConnect() {
		this.client = new DefaultHttpClient();
	}

	public final static DivvyConnect getInstance() {
		if (instance == null) {
			instance = new DivvyConnect();
		}
		return instance;
	}

	public final String connect() throws ConnectException {
		String toreturn = null;
		try {
			client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
			client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
			Log.v(TAG, "adress: " + URL);
			HttpGet get = new HttpGet(URL);
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
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new ConnectException(ConnectException.ERROR, e);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new ConnectException(ConnectException.ERROR, e);
		}
		Log.v(TAG, "Divvy: " + toreturn);
		return toreturn;
	}

}
