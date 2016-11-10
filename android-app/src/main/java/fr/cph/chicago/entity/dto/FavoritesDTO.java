package fr.cph.chicago.entity.dto;

import java.util.List;

import fr.cph.chicago.entity.BikeStation;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FavoritesDTO {
    private TrainArrivalDTO trainArrivalDTO;
    private BusArrivalDTO busArrivalDTO;

    private boolean bikeError;
    private List<BikeStation> bikeStations;
}
