package fr.cph.chicago.entity.dto;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import fr.cph.chicago.entity.BusArrival;

public class BusArrivalDTO extends TreeMap<String, Map<String, List<BusArrival>>> {
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
