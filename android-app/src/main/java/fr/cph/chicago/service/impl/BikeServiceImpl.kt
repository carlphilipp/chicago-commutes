package fr.cph.chicago.service.impl

import fr.cph.chicago.client.DivvyClient
import fr.cph.chicago.entity.BikeStation
import fr.cph.chicago.parser.JsonParser
import fr.cph.chicago.service.BikeService
import io.reactivex.exceptions.Exceptions

class BikeServiceImpl : BikeService {

    private object Holder {
        val INSTANCE = BikeServiceImpl()
    }

    override fun loadAllBikes(): List<BikeStation> {
        try {
            val bikeContent = DivvyClient.INSTANCE.connect()
            val bikeStations = JsonParser.INSTANCE.parseStations(bikeContent)
            return bikeStations.sortedWith(compareBy(BikeStation::name)).toList()
        } catch (throwable: Throwable) {
            throw Exceptions.propagate(throwable)
        }

    }

    override fun loadBikes(id: Int): BikeStation {
        try {
            return loadAllBikes().filter { value -> value.id == id }.first()
        } catch (throwable: Throwable) {
            throw Exceptions.propagate(throwable)
        }
    }

    companion object {
        val INSTANCE: BikeServiceImpl by lazy { Holder.INSTANCE }
    }
}
