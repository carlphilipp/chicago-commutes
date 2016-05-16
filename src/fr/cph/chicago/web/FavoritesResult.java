package fr.cph.chicago.web;

import android.util.SparseArray;

import java.util.List;

import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.TrainArrival;
import lombok.Data;

@Data
public class FavoritesResult {
    private boolean trainError;
    private boolean busError;
    private boolean bikeError;
    private SparseArray<TrainArrival> trainArrivals;
    private List<BusArrival> busArrivals;
    private List<BikeStation> bikeStations;
}
