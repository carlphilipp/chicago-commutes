package fr.cph.chicago.service

import fr.cph.chicago.entity.BikeStation

interface BikeService {

    fun loadAllBikes(): List<BikeStation>

    fun loadBikes(id: Int): BikeStation
}
