package fr.cph.chicago.service

import android.util.SparseArray
import fr.cph.chicago.core.model.dto.BusArrivalDTO
import fr.cph.chicago.core.model.dto.FavoritesDTO
import fr.cph.chicago.core.model.dto.FirstLoadDTO
import fr.cph.chicago.core.model.dto.LocalDTO
import fr.cph.chicago.core.model.dto.TrainArrivalDTO
import fr.cph.chicago.rx.RxUtil.handleListError
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

object MixedService {

    private val trainService = TrainService
    private val busService = BusService
    private val bikeService = BikeService

    fun local(): Single<LocalDTO> {
        // Train local data
        val trainLocalData: Single<Boolean> = trainService.loadLocalTrainData()

        // Bus local data
        val busLocalData: Single<Boolean> = busService.loadLocalBusData()

        return Singles.zip(
            trainLocalData,
            busLocalData,
            zipper = { trainError, busError -> LocalDTO(trainError, busError) }
        )
    }

    fun baseArrivals(): Single<FavoritesDTO> {
        // Train online favorites
        val favoritesTrainArrivals = favoritesTrainArrivalDTO().observeOn(Schedulers.computation())

        // Bus online favorites
        val favoritesBusArrivals = favoritesBusArrivalDTO().observeOn(Schedulers.computation())

        return Singles.zip(
            favoritesTrainArrivals,
            favoritesBusArrivals,
            zipper = { trainArrivalsDTO, busArrivalsDTO -> FavoritesDTO(trainArrivalsDTO, busArrivalsDTO, false, listOf()) })
    }

    fun favorites(): Single<FavoritesDTO> {
        // Train online favorites
        val trainArrivals = favoritesTrainArrivalDTO().observeOn(Schedulers.computation())
        // Bus online favorites
        val busArrivals = favoritesBusArrivalDTO().observeOn(Schedulers.computation())
        // Bikes online all stations
        val bikeStationsObservable = bikeService.allBikeStations().observeOn(Schedulers.computation()).onErrorReturn(handleListError())
        return Singles.zip(busArrivals, trainArrivals, bikeStationsObservable,
            zipper = { busArrivalDTO, trainArrivalsDTO, bikeStations ->
                FavoritesDTO(trainArrivalsDTO, busArrivalDTO, bikeStations.isEmpty(), bikeStations)
            })
    }

    fun busRoutesAndBikeStation(): Single<FirstLoadDTO> {
        val busRoutesSingle = busService.busRoutes().onErrorReturn(handleListError()).observeOn(Schedulers.computation())
        val bikeStationsSingle = bikeService.allBikeStations().onErrorReturn(handleListError()).observeOn(Schedulers.computation())
        return Singles.zip(
            busRoutesSingle,
            bikeStationsSingle,
            zipper = { busRoutes, bikeStations -> FirstLoadDTO(busRoutes.isEmpty(), bikeStations.isEmpty(), busRoutes, bikeStations) }
        )
    }

    private fun favoritesTrainArrivalDTO(): Single<TrainArrivalDTO> {
        return trainService.loadFavoritesTrain()
            .onErrorReturn { throwable ->
                Timber.e(throwable, "Could not load favorites trains")
                TrainArrivalDTO(SparseArray(), true)
            }
    }

    private fun favoritesBusArrivalDTO(): Single<BusArrivalDTO> {
        return busService.loadFavoritesBuses()
            .onErrorReturn { throwable ->
                Timber.e(throwable, "Could not load bus arrivals")
                BusArrivalDTO(listOf(), true)
            }
    }
}
