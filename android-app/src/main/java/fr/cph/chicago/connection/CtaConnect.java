/**
 * Copyright 2017 Carl-Philipp Harmant
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

import android.support.annotation.NonNull;

import com.annimon.stream.Stream;

import org.apache.commons.collections4.MultiValuedMap;

import java.io.InputStream;

import fr.cph.chicago.core.App;
import fr.cph.chicago.exception.ConnectException;

import static fr.cph.chicago.Constants.BUSES_ARRIVAL_URL;
import static fr.cph.chicago.Constants.BUSES_DIRECTION_URL;
import static fr.cph.chicago.Constants.BUSES_PATTERN_URL;
import static fr.cph.chicago.Constants.BUSES_ROUTES_URL;
import static fr.cph.chicago.Constants.BUSES_STOP_URL;
import static fr.cph.chicago.Constants.BUSES_VEHICLES_URL;
import static fr.cph.chicago.Constants.TRAINS_ARRIVALS_URL;
import static fr.cph.chicago.Constants.TRAINS_FOLLOW_URL;
import static fr.cph.chicago.Constants.TRAINS_LOCATION_URL;

/**
 * Class that build url and connect to CTA API
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public enum CtaConnect {
    INSTANCE;

    private static final String QUERY_PARAM_KEY = "?key=";

    /**
     * Connect
     *
     * @param requestType the type of request
     * @param params      the params
     * @return a string
     * @throws ConnectException the connection exception
     */
    @NonNull
    public final InputStream connect(@NonNull final CtaRequestType requestType, @NonNull final MultiValuedMap<String, String> params) throws ConnectException {
        final String ctaTrainKey = App.getCtaTrainKey();
        final String ctaBusKey = App.getCtaBusKey();
        final StringBuilder address;
        switch (requestType) {
            case TRAIN_ARRIVALS:
                address = new StringBuilder(TRAINS_ARRIVALS_URL + QUERY_PARAM_KEY + ctaTrainKey);
                break;
            case TRAIN_FOLLOW:
                address = new StringBuilder(TRAINS_FOLLOW_URL + QUERY_PARAM_KEY + ctaTrainKey);
                break;
            case TRAIN_LOCATION:
                address = new StringBuilder(TRAINS_LOCATION_URL + QUERY_PARAM_KEY + ctaTrainKey);
                break;
            case BUS_ROUTES:
                address = new StringBuilder(BUSES_ROUTES_URL + QUERY_PARAM_KEY + ctaBusKey);
                break;
            case BUS_DIRECTION:
                address = new StringBuilder(BUSES_DIRECTION_URL + QUERY_PARAM_KEY + ctaBusKey);
                break;
            case BUS_STOP_LIST:
                address = new StringBuilder(BUSES_STOP_URL + QUERY_PARAM_KEY + ctaBusKey);
                break;
            case BUS_VEHICLES:
                address = new StringBuilder(BUSES_VEHICLES_URL + QUERY_PARAM_KEY + ctaBusKey);
                break;
            case BUS_ARRIVALS:
                address = new StringBuilder(BUSES_ARRIVAL_URL + QUERY_PARAM_KEY + ctaBusKey);
                break;
            case BUS_PATTERN:
                address = new StringBuilder(BUSES_PATTERN_URL + QUERY_PARAM_KEY + ctaBusKey);
                break;
            default:
                address = new StringBuilder();
        }
        Stream.of(params.asMap().entrySet())
            .flatMap(entry -> Stream.of(entry.getValue()).map(value -> new StringBuilder().append("&").append(entry.getKey()).append("=").append(value)))
            .forEach(address::append);
        return Connect.INSTANCE.connect(address.toString());
    }
}
