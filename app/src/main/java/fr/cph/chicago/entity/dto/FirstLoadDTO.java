package fr.cph.chicago.entity.dto;

import java.util.List;

import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusRoute;
import lombok.Data;

@Data
public class FirstLoadDTO {
    private boolean busRoutesError;
    private boolean bikeStationsError;
    private List<BusRoute> busRoutes;
    private List<BikeStation> bikeStations;
}
