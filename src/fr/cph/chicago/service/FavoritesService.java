package fr.cph.chicago.service;

import android.util.SparseArray;

import java.util.List;

import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.TrainArrival;

public interface FavoritesService {
    SparseArray<TrainArrival> loadFavoritesTrain();

    List<BusArrival> loadFavoritesBuses();
}
