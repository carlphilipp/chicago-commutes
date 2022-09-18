/**
 * Copyright 2021 Carl-Philipp Harmant
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.service

import fr.cph.chicago.core.model.dto.BaseDTO
import fr.cph.chicago.core.model.dto.BusArrivalDTO
import fr.cph.chicago.core.model.dto.FavoritesDTO
import fr.cph.chicago.core.model.dto.FirstLoadDTO
import fr.cph.chicago.core.model.dto.TrainArrivalDTO
import fr.cph.chicago.exception.BaseException
import fr.cph.chicago.redux.store
import fr.cph.chicago.rx.RxUtil.handleListError
import fr.cph.chicago.rx.RxUtil.handleMapError
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

object MixedService {

    private val trainService = TrainService
    private val busService = BusService
    private val bikeService = BikeService
    private val preferenceService = PreferenceService

    fun baseData(): Single<BaseDTO> {
        return Single.zip(
            trainService.loadLocalTrainData(),
            busService.loadLocalBusData()
        ) { trainError, busError ->
            if (trainError || busError) throw BaseException()
            true
        }
            .flatMap {
                Single.zip(
                    baseArrivals().observeOn(Schedulers.computation()),
                    preferenceService.getTrainFavorites().observeOn(Schedulers.computation()),
                    preferenceService.getBusFavorites().observeOn(Schedulers.computation()),
                    preferenceService.getBikeFavorites().observeOn(Schedulers.computation())
                ) { favoritesDTO, favoritesTrains, favoritesBuses, favoritesBikes ->
                    val trainArrivals = if (favoritesDTO.trainArrivalDTO.error)
                        TrainArrivalDTO(store.state.trainArrivalsDTO.trainsArrivals, true)
                    else
                        favoritesDTO.trainArrivalDTO
                    val busArrivals = if (favoritesDTO.busArrivalDTO.error)
                        BusArrivalDTO(store.state.busArrivalsDTO.busArrivals, true)
                    else
                        favoritesDTO.busArrivalDTO
                    val favoritesBusRoute = busService.extractBusRouteFavorites(favoritesBuses)
                    BaseDTO(
                        trainArrivalsDTO = trainArrivals,
                        busArrivalsDTO = busArrivals,
                        trainFavorites = favoritesTrains,
                        busFavorites = favoritesBuses,
                        busRouteFavorites = favoritesBusRoute,
                        bikeFavorites = favoritesBikes
                    )
                }
            }
    }

    private fun baseArrivals(): Single<FavoritesDTO> {
        // Train online favorites
        val favoritesTrainArrivals = favoritesTrainArrivalDTO().observeOn(Schedulers.computation())

        // Bus online favorites
        val favoritesBusArrivals = favoritesBusArrivalDTO().observeOn(Schedulers.computation())

        return Single.zip(
            favoritesTrainArrivals,
            favoritesBusArrivals
        ) { trainArrivalsDTO, busArrivalsDTO -> FavoritesDTO(trainArrivalsDTO, busArrivalsDTO, false, mapOf()) }
    }

    fun favorites(): Single<FavoritesDTO> {
        // Train online favorites
        val trainArrivals = favoritesTrainArrivalDTO().observeOn(Schedulers.computation())
        // Bus online favorites
        val busArrivals = favoritesBusArrivalDTO().observeOn(Schedulers.computation())
        // Bikes online all stations
        val bikeStationsObs = bikeService.allBikeStations().observeOn(Schedulers.computation()).onErrorReturn(handleMapError())
        return Single.zip(busArrivals, trainArrivals, bikeStationsObs) { busArrivalDTO, trainArrivalsDTO, bikeStations ->
            FavoritesDTO(trainArrivalsDTO, busArrivalDTO, bikeStations.isEmpty(), bikeStations)
        }
    }

    fun busRoutesAndBikeStation(): Single<FirstLoadDTO> {
        val busRoutesSingle = busService.busRoutes().onErrorReturn(handleListError()).observeOn(Schedulers.computation())
        val bikeStationsSingle = bikeService.allBikeStations().onErrorReturn(handleMapError()).observeOn(Schedulers.computation())
        return Single.zip(
            busRoutesSingle,
            bikeStationsSingle
        ) { busRoutes, bikeStations -> FirstLoadDTO(busRoutes.isEmpty(), bikeStations.isEmpty(), busRoutes, bikeStations) }
    }

    private fun favoritesTrainArrivalDTO(): Single<TrainArrivalDTO> {
        return trainService.loadFavoritesTrain()
            .onErrorReturn { throwable ->
                Timber.e(throwable, "Could not load favorites trains")
                TrainArrivalDTO(mutableMapOf(), true)
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
