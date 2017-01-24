package fr.cph.chicago.service.impl;

import android.support.annotation.NonNull;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.io.InputStream;
import java.util.List;

import fr.cph.chicago.connection.DivvyConnect;
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
            final InputStream bikeContent = DivvyConnect.INSTANCE.connect();
            final List<BikeStation> bikeStations = JsonParser.INSTANCE.parseStations(bikeContent);
            return Stream.of(bikeStations).sorted(Util.BIKE_COMPARATOR_NAME).collect(Collectors.toList());
        } catch (final Throwable throwable) {
            throw Exceptions.propagate(throwable);
        }
    }

    @Override
    public List<BikeStation> loadBikes(@NonNull final List<Integer> ids) {
        return Stream.of(loadAllBikes()).filter(value -> ids.contains(value.getId())).collect(Collectors.toList());
    }
}
