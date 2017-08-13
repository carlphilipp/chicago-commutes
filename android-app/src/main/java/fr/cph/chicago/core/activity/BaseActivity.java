/**
 * Copyright 2017 Carl-Philipp Harmant
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.core.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import butterknife.BindString;
import butterknife.ButterKnife;
import fr.cph.chicago.R;
import fr.cph.chicago.core.App;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.dto.BusArrivalDTO;
import fr.cph.chicago.entity.dto.FavoritesDTO;
import fr.cph.chicago.entity.dto.TrainArrivalDTO;
import fr.cph.chicago.rx.observable.ObservableUtil;
import fr.cph.chicago.service.BusService;
import fr.cph.chicago.service.TrainService;
import fr.cph.chicago.service.impl.BusServiceImpl;
import fr.cph.chicago.service.impl.TrainServiceImpl;
import fr.cph.chicago.util.RealmUtil;
import fr.cph.chicago.util.Util;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static fr.cph.chicago.Constants.BUSES_ARRIVAL_URL;
import static fr.cph.chicago.Constants.TRAINS_ARRIVALS_URL;
import static fr.cph.chicago.core.App.setupContextData;

/**
 * This class represents the base activity of the application It will load the loading screen and/or the main
 * activity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
@SuppressWarnings("WeakerAccess")
public class BaseActivity extends Activity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    @BindString(R.string.bundle_error)
    String bundleError;
    @BindString(R.string.message_something_went_wrong)
    String somethingWentWrong;

    private final TrainService trainService;
    private final BusService busService;

    public BaseActivity() {
        trainService = TrainServiceImpl.INSTANCE;
        busService = BusServiceImpl.INSTANCE;
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        ButterKnife.bind(this);

        setupContextData(getApplicationContext());
        setUpRealm();
        loadLocalAndFavoritesData();
        trackWithGoogleAnalytics();
    }

    private void setUpRealm() {
        RealmUtil.setUpRealm(getApplicationContext());
    }

    private void loadLocalAndFavoritesData() {

        // Train local data
        final Observable<TrainData> trainLocalData = Observable.create(
            (ObservableEmitter<TrainData> observableOnSubscribe) -> {
                if (!observableOnSubscribe.isDisposed()) {
                    observableOnSubscribe.onNext(trainService.loadLocalTrainData(getApplicationContext()));
                    observableOnSubscribe.onComplete();
                }
            })
            .doOnNext(DataHolder.INSTANCE::setTrainData)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        // Bus local data
        final Observable<BusData> busLocalData = Observable.create(
            (ObservableEmitter<BusData> observableOnSubscribe) -> {
                if (!observableOnSubscribe.isDisposed()) {
                    observableOnSubscribe.onNext(busService.loadLocalBusData(getApplicationContext()));
                    observableOnSubscribe.onComplete();
                }
            })
            .doOnNext(DataHolder.INSTANCE::setBusData)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        // Train online favorites
        final Observable<TrainArrivalDTO> trainOnlineFavorites = ObservableUtil.createFavoritesTrainArrivalsObservable(getApplicationContext());

        // Bus online favorites
        final Observable<BusArrivalDTO> busOnlineFavorites = ObservableUtil.createFavoritesBusArrivalsObservable(getApplicationContext());

        // Run local first and then online: Ensure that local data is loaded first
        Observable.zip(trainLocalData, busLocalData, (trainData, busData) -> true)
            .doOnComplete(() ->
                Observable.zip(trainOnlineFavorites, busOnlineFavorites, (trainArrivalsDTO, busArrivalsDTO) -> {
                        App.setLastUpdate(Calendar.getInstance().getTime());
                        return new FavoritesDTO(trainArrivalsDTO, busArrivalsDTO, false, Collections.emptyList());
                    }
                ).subscribe(this::startMainActivity, onError -> {
                        Log.e(TAG, onError.getMessage(), onError);
                        startErrorActivity();
                    }
                )
            ).subscribe();
    }

    private void trackWithGoogleAnalytics() {
        Util.trackAction(this, R.string.analytics_category_req, R.string.analytics_action_get_train, TRAINS_ARRIVALS_URL);
        Util.trackAction(this, R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_ARRIVAL_URL);
    }

    /**
     * Finish current activity and start main activity with custom transition
     *
     * @param result the trains and buses arrivals
     */
    private void startMainActivity(@NonNull final FavoritesDTO result) {
        final Intent intent = new Intent(this, MainActivity.class);
        final Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(getString(R.string.bundle_bus_arrivals), (ArrayList<BusArrival>) result.getBusArrivalDTO().getBusArrivals());
        bundle.putSparseParcelableArray(getString(R.string.bundle_train_arrivals), result.getTrainArrivalDTO().getTrainArrivalSparseArray());
        bundle.putBoolean(getString(R.string.bundle_train_error), result.getTrainArrivalDTO().getError());
        bundle.putBoolean(getString(R.string.bundle_bus_error), result.getBusArrivalDTO().getError());
        intent.putExtras(bundle);

        finish();
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void startErrorActivity() {
        // Reset data
        DataHolder.INSTANCE.setTrainData(null);
        DataHolder.INSTANCE.setBusData(null);

        // Start error activity
        final Intent intent = new Intent(this, ErrorActivity.class);
        final Bundle extras = new Bundle();
        extras.putString(bundleError, somethingWentWrong);
        intent.putExtras(extras);
        finish();
        startActivity(intent);
    }
}
