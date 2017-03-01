package fr.cph.chicago.service;

import android.content.Context;
import android.util.SparseArray;

import com.annimon.stream.Optional;

import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.TrainArrival;

public interface TrainService {

    SparseArray<TrainArrival> loadFavoritesTrain(Context context);

    TrainData loadLocalTrainData(Context context);

    Optional<TrainArrival> loadStationTrainArrival(Context context, int stationId);
}
