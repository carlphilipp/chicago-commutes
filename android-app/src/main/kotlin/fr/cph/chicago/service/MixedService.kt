package fr.cph.chicago.service

import android.util.SparseArray
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.dto.BusArrivalDTO
import fr.cph.chicago.core.model.dto.FavoritesDTO
import fr.cph.chicago.core.model.dto.FirstLoadDTO
import fr.cph.chicago.core.model.dto.LocalDTO
import fr.cph.chicago.core.model.dto.TrainArrivalDTO
import fr.cph.chicago.rx.RxUtil.handleError
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
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

        return Single.zip(
            trainLocalData,
            busLocalData,
            BiFunction { trainLocalError: Boolean, busLocalError: Boolean ->
                LocalDTO(trainLocalError, busLocalError)
            }
        )
    }

    fun baseArrivals(): Single<FavoritesDTO> {
        // Train online favorites
        val favoritesTrainArrivals = favoritesTrainArrivalDTO().observeOn(Schedulers.computation())

        // Bus online favorites
        val favoritesBusArrivals = favoritesBusArrivalDTO().observeOn(Schedulers.computation())

        return Single.zip(
            favoritesTrainArrivals,
            favoritesBusArrivals,
            BiFunction { trainArrivalsDTO: TrainArrivalDTO, busArrivalsDTO: BusArrivalDTO ->
                FavoritesDTO(trainArrivalsDTO, busArrivalsDTO, false, listOf())
            })
    }

    fun favorites(): Single<FavoritesDTO> {
        // Train online favorites
        val trainArrivals = favoritesTrainArrivalDTO().observeOn(Schedulers.computation())
        // Bus online favorites
        val busArrivals = favoritesBusArrivalDTO().observeOn(Schedulers.computation())
        // Bikes online all stations
        val bikeStationsObservable = bikeService.allBikeStations().observeOn(Schedulers.computation()).onErrorReturn(handleError())
        return Single.zip(busArrivals, trainArrivals, bikeStationsObservable,
            Function3 { busArrivalDTO: BusArrivalDTO, trainArrivalsDTO: TrainArrivalDTO, bikeStations: List<BikeStation>
                ->
                FavoritesDTO(trainArrivalsDTO, busArrivalDTO, bikeStations.isEmpty(), bikeStations)
            })
    }

    fun busRoutesAndBikeStation(): Single<FirstLoadDTO> {
        val busRoutesSingle = busService.busRoutes().onErrorReturn(handleError()).observeOn(Schedulers.computation())
        val bikeStationsSingle = bikeService.allBikeStations().onErrorReturn(handleError()).observeOn(Schedulers.computation())

        return Single.zip(
            busRoutesSingle,
            bikeStationsSingle,
            BiFunction { busRoutes, bikeStations ->
                FirstLoadDTO(busRoutes.isEmpty(), bikeStations.isEmpty(), busRoutes, bikeStations)
            }
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
