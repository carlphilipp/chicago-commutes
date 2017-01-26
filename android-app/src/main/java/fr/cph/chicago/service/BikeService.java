package fr.cph.chicago.service;

import com.annimon.stream.Optional;

import java.util.List;

import fr.cph.chicago.entity.BikeStation;

public interface BikeService {

    List<BikeStation> loadAllBikes();

    Optional<BikeStation> loadBikes(int id);
}
