package fr.cph.chicago.service

import com.annimon.stream.Optional

import fr.cph.chicago.entity.BikeStation

interface BikeService {

    fun loadAllBikes(): List<BikeStation>

    fun loadBikes(id: Int): Optional<BikeStation>
}
