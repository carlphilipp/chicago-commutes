package fr.cph.chicago.collector;


import com.annimon.stream.Collector;
import com.annimon.stream.function.BiConsumer;
import com.annimon.stream.function.Function;
import com.annimon.stream.function.Supplier;

import java.util.HashMap;
import java.util.Map;

import fr.cph.chicago.entity.Eta;

public class CommutesCollectors {

    public static Collector<Eta, Map<String, String>, Map<String, String>> toTrainArrivalByLine() {
        return new Collector<Eta, Map<String, String>, Map<String, String>>() {
            @Override
            public Supplier<Map<String, String>> supplier() {
                return HashMap::new;
            }

            @Override
            public BiConsumer<Map<String, String>, Eta> accumulator() {
                return (map, eta) -> {
                    final String stopNameData = eta.getDestName();
                    final String timingData = eta.getTimeLeftDueDelay();
                    final String value = map.containsKey(stopNameData)
                        ? map.get(stopNameData) + " " + timingData
                        : timingData;
                    map.put(stopNameData, value);
                };
            }

            @Override
            public Function<Map<String, String>, Map<String, String>> finisher() {
                return null;
            }
        };
    }
}
