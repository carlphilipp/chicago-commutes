package fr.cph.chicago.service.impl;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import fr.cph.chicago.connection.DivvyConnect;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.parser.JsonParser;
import fr.cph.chicago.service.BikeService;
import fr.cph.chicago.util.Util;
import rx.exceptions.Exceptions;

public class BikeServiceImpl implements BikeService {

    private final JsonParser jsonParser;

    public BikeServiceImpl() {
        this.jsonParser = JsonParser.getInstance();
    }

    @Override
    public List<BikeStation> loadAllBikes(@NonNull final Context context) {
        try {
            final DivvyConnect divvyConnect = DivvyConnect.getInstance();
            final InputStream bikeContent = divvyConnect.connect(context);
            final List<BikeStation> bikeStations = jsonParser.parseStations(bikeContent);
            Collections.sort(bikeStations, Util.BIKE_COMPARATOR_NAME);
            return bikeStations;
        } catch (final Throwable throwable) {
            throw Exceptions.propagate(throwable);
        }
    }
}
