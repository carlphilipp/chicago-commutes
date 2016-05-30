package fr.cph.chicago.service.impl;

import android.content.Context;
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
            return Stream.of(bikeStations).sorted(Util.BIKE_COMPARATOR_NAME).collect(Collectors.toList());
        } catch (final Throwable throwable) {
            throw Exceptions.propagate(throwable);
        }
    }
}
