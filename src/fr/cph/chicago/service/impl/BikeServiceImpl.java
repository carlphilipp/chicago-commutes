package fr.cph.chicago.service.impl;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import fr.cph.chicago.connection.DivvyConnect;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.json.JsonParser;
import fr.cph.chicago.service.BikeService;
import fr.cph.chicago.util.Util;
import rx.exceptions.Exceptions;

public class BikeServiceImpl implements BikeService {

    private final JsonParser jsonParser;

    public BikeServiceImpl() {
        this.jsonParser = JsonParser.getInstance();
    }

    @Override
    public List<BikeStation> loadAllBikes() {
        try {
            final DivvyConnect divvyConnect = DivvyConnect.getInstance();
            final InputStream bikeContent = divvyConnect.connect();
            final List<BikeStation> bikeStations = jsonParser.parseStations(bikeContent);
            Collections.sort(bikeStations, Util.BIKE_COMPARATOR_NAME);
            return bikeStations;
        } catch (final Throwable throwable) {
            throw Exceptions.propagate(throwable);
        }
    }
}
