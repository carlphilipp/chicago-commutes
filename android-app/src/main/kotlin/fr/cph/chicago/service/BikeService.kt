package fr.cph.chicago.service

import fr.cph.chicago.client.DivvyClient
import fr.cph.chicago.entity.BikeStation
import fr.cph.chicago.parser.JsonParser
import io.reactivex.exceptions.Exceptions

object BikeService {

    private val client = DivvyClient
    private val jsonParser = JsonParser

    fun loadAllBikeStations(): List<BikeStation> {
        try {
            val bikeStationsInputStream = client.getBikeStations()
            return jsonParser.parseStations(bikeStationsInputStream).sortedWith(compareBy(BikeStation::name)).toList()
        } catch (throwable: Throwable) {
            throw Exceptions.propagate(throwable)
        }
    }

    fun findBikeStation(id: Int): BikeStation {
        try {
            return loadAllBikeStations().first { (bikeId) -> bikeId == id }
        } catch (throwable: Throwable) {
            throw Exceptions.propagate(throwable)
        }
    }
}
