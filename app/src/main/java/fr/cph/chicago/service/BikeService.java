package fr.cph.chicago.service;

import java.util.List;

import fr.cph.chicago.entity.BikeStation;

public interface BikeService {

    List<BikeStation> loadAllBikes();
}
