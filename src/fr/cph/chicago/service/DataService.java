package fr.cph.chicago.service;

import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.util.List;

import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.TrainArrival;

public interface DataService {

    SparseArray<TrainArrival> loadFavoritesTrain();

    List<BusArrival> loadFavoritesBuses();

    List<BikeStation> loadAllBikes();

    List<BusStop> loadOneBusStop(@NonNull final String stopId, @NonNull final String bound);

    BusData loadLocalBusData();

    TrainData loadLocalTrainData();

    BusDirections loadBusDirections(@NonNull final String busRouteId);
}
