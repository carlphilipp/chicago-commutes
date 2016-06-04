package fr.cph.chicago.service;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.TrainArrival;
import id.ridsatrio.optio.Optional;

public interface TrainService {

    SparseArray<TrainArrival> loadFavoritesTrain(@NonNull final Context context);

    TrainData loadLocalTrainData(@NonNull final Context context);

    Optional<TrainArrival> loadStationTrainArrival(@NonNull final Context context, int stationId);
}
