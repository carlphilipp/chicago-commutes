package fr.cph.chicago.entity.dto;

import android.util.SparseArray;

import java.util.List;

import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.TrainArrival;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FavoritesDTO {
    private boolean trainError;
    private boolean busError;
    private boolean bikeError;
    private SparseArray<TrainArrival> trainArrivals;
    private List<BusArrival> busArrivals;
    private List<BikeStation> bikeStations;
}
