package fr.cph.chicago.service;

import android.util.SparseArray;

import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.TrainArrival;

public interface TrainService {

    SparseArray<TrainArrival> loadFavoritesTrain();

    TrainData loadLocalTrainData();
}
