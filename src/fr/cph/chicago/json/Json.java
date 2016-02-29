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

package fr.cph.chicago.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * Json
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class Json {
	public List<BikeStation> parseStations(final String jsonString) throws ParserException {
		try {
			final JSONObject json = new JSONObject(jsonString);
			final String stationList = json.getString("stationBeanList");
			final ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(stationList, new TypeReference<List<BikeStation>>() {
			});
		} catch (final JSONException | IOException e) {
			throw new ParserException(TrackerException.ERROR, e);
		}
	}
}
