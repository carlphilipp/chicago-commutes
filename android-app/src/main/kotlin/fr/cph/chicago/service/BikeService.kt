package fr.cph.chicago.service

import fr.cph.chicago.client.DivvyClient
import fr.cph.chicago.entity.BikeStation
import fr.cph.chicago.parser.JsonParser
import fr.cph.chicago.service.BikeService
import io.reactivex.exceptions.Exceptions

object BikeService : BikeService {

    override fun loadAllBikes(): List<BikeStation> {
        try {
            val bikeContent = DivvyClient.connect()
            val bikeStations = JsonParser.parseStations(bikeContent)
            return bikeStations.sortedWith(compareBy(BikeStation::name)).toList()
        } catch (throwable: Throwable) {
            throw Exceptions.propagate(throwable)
        }
    }

    override fun loadBikes(id: Int): BikeStation {
        try {
            return loadAllBikes().first { (bikeId) -> bikeId == id }
        } catch (throwable: Throwable) {
            throw Exceptions.propagate(throwable)
        }
    }
}
