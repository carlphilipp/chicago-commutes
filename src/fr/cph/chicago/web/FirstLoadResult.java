package fr.cph.chicago.web;

import java.util.List;

import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusRoute;
import lombok.Data;

@Data
public class FirstLoadResult {
    private boolean busRoutesError;
    private boolean bikeStationsError;
    private List<BusRoute> busRoutes;
    private List<BikeStation> bikeStations;
}
