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

package fr.cph.chicago.entity.factory;

import android.support.annotation.NonNull;

import java.util.List;

import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.Stop;

/**
 * Factory that build a station
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class StationFactory {

    /**
     *
     */
    private StationFactory() {
    }

    /**
     * Build a station
     *
     * @param id    the id
     * @param name  the name
     * @param stops the stops
     * @return a station
     */
    @NonNull
    public static Station buildStation(@NonNull final Integer id, @NonNull final String name, @NonNull final List<Stop> stops) {
        final Station station = new Station();
        station.setId(id);
        station.setName(name);
        station.setStops(stops);
        return station;
    }
}
