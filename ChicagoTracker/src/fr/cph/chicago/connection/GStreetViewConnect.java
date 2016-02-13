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
import java.io.InputStream;
import java.net.URL;

import android.graphics.drawable.Drawable;
import android.util.Log;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.util.Util;

/**
 * Class that access google street api. Singleton
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class GStreetViewConnect {

	/** The tag **/
	private static final String TAG = "GStreetViewConnect";
	/** The base url of API **/
	private static final String BASE_URL = "http://maps.googleapis.com/maps/api/streetview";
	/** The google key **/
	private String GOOGLE_KEY;
	/** Width of the picture **/
	private static final int WIDTH = 1000;
	/** Height of the picture **/
	private static final int HEIGTH = 300;
	/** This class is a singleton **/
	private static GStreetViewConnect instance = null;

	/**
	 * Private constructor, that get the API key from property file
	 */
	private GStreetViewConnect() {
		GOOGLE_KEY = Util.getProperty("google.streetmap.key");
	}

	/**
	 * Get instance of this class
	 * 
	 * @return
	 */
	public static GStreetViewConnect getInstance() {
		if (instance == null) {
			instance = new GStreetViewConnect();
		}
		return instance;
	}

	/**
	 * Build Url
	 * 
	 * @param position
	 *            position that we want to access
	 * @return a drawable
	 * @throws IOException
	 *             an exception
	 */
	public final Drawable connect(final Position position) throws IOException {
		StringBuilder adress = new StringBuilder(BASE_URL);
		adress.append("?key=" + GOOGLE_KEY);
		adress.append("&sensor=false");
		adress.append("&size=" + WIDTH + "x" + HEIGTH);
		adress.append("&fov=120");
		adress.append("&location=" + position.getLatitude() + "," + position.getLongitude());
		return connectUrl(adress.toString());
	}

	/**
	 * Connect to the API and get the MAP
	 * 
	 * @param address
	 *            the address to connect to
	 * @return a drawable map
	 * @throws IOException
	 */
	private Drawable connectUrl(final String address) throws IOException {
		Log.v(TAG, "adress: " + address);
		try {
			InputStream is = (InputStream) new URL(address).getContent();
			Drawable d = Drawable.createFromStream(is, "src name");
			return d;
		} catch (Exception e) {
			return null;
		}
	}
}
