package fr.cph.chicago.service.impl;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.io.InputStream;
import java.util.List;

import fr.cph.chicago.connection.DivvyConnect;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.parser.JsonParser;
import fr.cph.chicago.service.BikeService;
import fr.cph.chicago.util.Util;
import rx.exceptions.Exceptions;

public class BikeServiceImpl implements BikeService {

    @Override
    public List<BikeStation> loadAllBikes() {
        try {
            final InputStream bikeContent = DivvyConnect.INSTANCE.connect();
            final List<BikeStation> bikeStations = JsonParser.INSTANCE.parseStations(bikeContent);
            return Stream.of(bikeStations).sorted(Util.BIKE_COMPARATOR_NAME).collect(Collectors.toList());
        } catch (final Throwable throwable) {
            throw Exceptions.propagate(throwable);
        }
    }
}
