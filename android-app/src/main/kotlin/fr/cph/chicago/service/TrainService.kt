package fr.cph.chicago.service

import android.content.Context
import android.util.SparseArray
import fr.cph.chicago.repository.TrainRepository
import fr.cph.chicago.entity.TrainArrival

interface TrainService {

    fun loadFavoritesTrain(context: Context): SparseArray<TrainArrival>

    fun loadLocalTrainData(context: Context): Any

    fun loadLocalTrainDataIdNeeded(context: Context)

    fun loadStationTrainArrival(context: Context, stationId: Int): TrainArrival
}
