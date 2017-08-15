package fr.cph.chicago.service.impl;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import java.io.InputStream;
import java.util.List;

import fr.cph.chicago.client.DivvyClient;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.parser.JsonParser;
import fr.cph.chicago.service.BikeService;
import fr.cph.chicago.util.Util;
import io.reactivex.exceptions.Exceptions;

public enum BikeServiceImpl implements BikeService {
    INSTANCE;

    @Override
    public List<BikeStation> loadAllBikes() {
        try {
            final InputStream bikeContent = DivvyClient.Companion.getINSTANCE().connect();
            final List<BikeStation> bikeStations = JsonParser.Companion.getINSTANCE().parseStations(bikeContent);
            return Stream.of(bikeStations).sorted(Util.BIKE_COMPARATOR_NAME).collect(Collectors.toList());
        } catch (final Throwable throwable) {
            throw Exceptions.propagate(throwable);
        }
    }

    @Override
    public Optional<BikeStation> loadBikes(final int id) {
        return Stream.of(loadAllBikes()).filter(value -> value.getId() == id).findFirst();
    }
}
