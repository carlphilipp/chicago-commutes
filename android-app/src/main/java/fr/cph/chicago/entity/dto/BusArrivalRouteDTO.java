package fr.cph.chicago.entity.dto;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import fr.cph.chicago.entity.BusArrival;

/**
 * @author cpharmant
 */
public class BusArrivalRouteDTO extends TreeMap<String, Map<String, List<BusArrival>>> {
    // route => { bound => BusArrival }

    public void addBusArrival(@NonNull final BusArrival busArrival) {
        if (containsKey(busArrival.getRouteId())) {
            final Map<String, List<BusArrival>> tempMap = get(busArrival.getRouteId());
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
            put(busArrival.getRouteId(), tempMap);
        }
    }
}
