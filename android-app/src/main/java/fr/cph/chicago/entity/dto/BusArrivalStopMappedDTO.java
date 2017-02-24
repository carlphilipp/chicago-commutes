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

package fr.cph.chicago.entity.dto;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import fr.cph.chicago.entity.BusArrival;

public class BusArrivalStopMappedDTO extends TreeMap<String, Map<String, List<BusArrival>>> {
    // stop name => { bound => BusArrival }

    public void addBusArrival(@NonNull final BusArrival busArrival) {
        if (containsKey(busArrival.getStopName())) {
            final Map<String, List<BusArrival>> tempMap = get(busArrival.getStopName());
            if (tempMap.containsKey(busArrival.getRouteDirection())) {
                final List<BusArrival> arrivals = tempMap.get(busArrival.getRouteDirection());
                arrivals.add(busArrival);
            } else {
                final List<BusArrival> arrivals = new ArrayList<>();
                arrivals.add(busArrival);
                tempMap.put(busArrival.getRouteDirection(), arrivals);
            }
        } else {
            final Map<String, List<BusArrival>> tempMap = new TreeMap<>();
            final List<BusArrival> arrivals = new ArrayList<>();
            arrivals.add(busArrival);
            tempMap.put(busArrival.getRouteDirection(), arrivals);
            put(busArrival.getStopName(), tempMap);
        }
    }

    public boolean containsStopNameAndBound(@NonNull final String stopName, @NonNull final String bound) {
        return containsKey(stopName) && get(stopName).containsKey(bound);
    }
}
