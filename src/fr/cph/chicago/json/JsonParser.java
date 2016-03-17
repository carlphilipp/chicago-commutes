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

import android.support.annotation.NonNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;

/**
 * Json
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class JsonParser {

    private static ObjectMapper MAPPER;
    private static JsonParser INSTANCE;

    @NonNull
    public static JsonParser getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JsonParser();
            MAPPER = new ObjectMapper();
        }
        return INSTANCE;
    }

    @NonNull
    public List<BikeStation> parseStations(@NonNull final InputStream stream) throws ParserException {
        try {
            final DivvyJson divvyJson = MAPPER.readValue(stream, new TypeReference<DivvyJson>() {
            });
            return divvyJson.getStations();
        } catch (final IOException e) {
            throw new ParserException(TrackerException.ERROR, e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    private JsonParser() {
    }
}
