package fr.cph.chicago.entity.dto;

import java.util.List;

import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.TrainArrival;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NearbyDTO {
    private TrainArrival trainArrivals;
    private List<BusArrival> busArrivals;
    private BikeStation bikeStations;
}
