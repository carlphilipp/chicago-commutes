package fr.cph.chicago.service;

import android.util.SparseArray;

import java.util.List;

import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.TrainArrival;

public interface DataService {

    SparseArray<TrainArrival> loadFavoritesTrain();

    List<BusArrival> loadFavoritesBuses();

    List<BikeStation> loadAllBikes();

    BusData loadLocalBusData();

    TrainData loadLocalTrainData();
}
