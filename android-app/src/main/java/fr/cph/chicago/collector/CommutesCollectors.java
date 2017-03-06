package fr.cph.chicago.collector;


import com.annimon.stream.Collector;
import com.annimon.stream.function.BiConsumer;
import com.annimon.stream.function.Function;
import com.annimon.stream.function.Supplier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.enumeration.TrainLine;

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

    public static Collector<List<TrainLine>, Set<TrainLine>, Set<TrainLine>> toTrainLineCollector() {
        return new Collector<List<TrainLine>, Set<TrainLine>, Set<TrainLine>>() {
            @Override
            public Supplier<Set<TrainLine>> supplier() {
                return TreeSet::new;
            }

            @Override
            public BiConsumer<Set<TrainLine>, List<TrainLine>> accumulator() {
                return Set::addAll;
            }

            @Override
            public Function<Set<TrainLine>, Set<TrainLine>> finisher() {
                return null;
            }
        };
    }
}
