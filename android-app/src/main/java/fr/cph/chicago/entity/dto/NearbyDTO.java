package fr.cph.chicago.entity.dto;

import android.util.SparseArray;

import java.util.List;
import java.util.Map;

import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NearbyDTO {
    private TrainArrivalDTO trainArrivalDTO;
    private SparseArray<Map<String, List<BusArrival>>> busArrivalDTO;

    private boolean bikeError;
    private List<BikeStation> bikeStations;
}
