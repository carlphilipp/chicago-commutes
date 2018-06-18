/**
 * Copyright 2018 Carl-Philipp Harmant
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

package fr.cph.chicago.core.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import butterknife.BindString
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.butterknife.ButterKnifeActivity
import fr.cph.chicago.core.model.dto.BusArrivalDTO
import fr.cph.chicago.core.model.dto.FavoritesDTO
import fr.cph.chicago.core.model.dto.TrainArrivalDTO
import fr.cph.chicago.repository.RealmConfig
import fr.cph.chicago.rx.ObservableUtil
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.Util
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.util.Calendar

/**
 * This class represents the base activity of the application It will load the loading screen and/or the main
 * activity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BaseActivity : ButterKnifeActivity(R.layout.loading) {

    @BindString(R.string.bundle_error)
    lateinit var bundleError: String
    @BindString(R.string.message_something_went_wrong)
    lateinit var somethingWentWrong: String

    private val trainService: TrainService = TrainService
    private val busService: BusService = BusService
    private val realmConfig: RealmConfig = RealmConfig
    private val observableUtil: ObservableUtil = ObservableUtil

    override fun create(savedInstanceState: Bundle?) {
        setUpRealm()
        loadLocalAndFavoritesData()
    }

    private fun setUpRealm() {
        realmConfig.setUpRealm()
    }

    private fun loadLocalAndFavoritesData() {

        // Train local data
        val trainLocalData = Observable.create { observableOnSubscribe: ObservableEmitter<Any> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(trainService.loadLocalTrainData())
                observableOnSubscribe.onComplete()
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

        // Bus local data
        val busLocalData = Observable.create { observableOnSubscribe: ObservableEmitter<Any> ->
            if (!observableOnSubscribe.isDisposed) {
                observableOnSubscribe.onNext(busService.loadLocalBusData())
                observableOnSubscribe.onComplete()
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

        // Train online favorites
        val trainOnlineFavorites = observableUtil.createFavoritesTrainArrivalsObservable()

        // Bus online favorites
        val busOnlineFavorites = observableUtil.createFavoritesBusArrivalsObservable()

        // Run local first and then online: Ensure that local data is loaded first
        Observable.zip(trainLocalData, busLocalData, BiFunction { _: Any, _: Any -> true })
            .doOnComplete {
                Observable.zip(trainOnlineFavorites, busOnlineFavorites, BiFunction { trainArrivalsDTO: TrainArrivalDTO, busArrivalsDTO: BusArrivalDTO ->
                    trainService.setStationError(false)
                    busService.setBusRouteError(false)
                    (application as App).lastUpdate = Calendar.getInstance().time
                    FavoritesDTO(trainArrivalsDTO, busArrivalsDTO, false, listOf())
                })
                    .subscribe({ this.startMainActivity(it) }, { onError ->
                        Log.e(TAG, onError.message, onError)
                        startErrorActivity()
                    })
            }.subscribe()
    }

    /**
     * Finish current activity and start main activity with custom transition
     *
     * @param result the trains and buses arrivals
     */
    private fun startMainActivity(result: FavoritesDTO) {
        val intent = Intent(this, MainActivity::class.java)
        val bundle = Bundle()
        bundle.putParcelableArrayList(getString(R.string.bundle_bus_arrivals), Util.asParcelableArrayList(result.busArrivalDTO.busArrivals))
        bundle.putSparseParcelableArray(getString(R.string.bundle_train_arrivals), result.trainArrivalDTO.trainArrivalSparseArray)
        bundle.putBoolean(getString(R.string.bundle_train_error), result.trainArrivalDTO.error)
        bundle.putBoolean(getString(R.string.bundle_bus_error), result.busArrivalDTO.error)
        intent.putExtras(bundle)

        finish()
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun startErrorActivity() {
        // Set BusArrivalError
        trainService.setStationError(true)
        busService.setBusRouteError(true)

        // Start error activity
        val intent = Intent(this, ErrorActivity::class.java)
        val extras = Bundle()
        extras.putString(bundleError, somethingWentWrong)
        intent.putExtras(extras)
        finish()
        startActivity(intent)
    }

    companion object {
        private val TAG = BaseActivity::class.java.simpleName
    }
}
