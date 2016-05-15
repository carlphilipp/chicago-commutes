/**
 * Copyright 2016 Carl-Philipp Harmant
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

package fr.cph.chicago.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import fr.cph.chicago.App;
import fr.cph.chicago.R;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.service.FavoritesService;
import fr.cph.chicago.service.FavoritesServiceImpl;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.web.FavoritesResult;
import fr.cph.chicago.xml.XmlParser;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

import static fr.cph.chicago.connection.CtaRequestType.BUS_ARRIVALS;
import static fr.cph.chicago.connection.CtaRequestType.TRAIN_ARRIVALS;

/**
 * This class represents the base activity of the application It will load the loading screen and/or the main
 * activity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BaseActivity extends Activity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    private final FavoritesService service;

    public BaseActivity(){
        service = new FavoritesServiceImpl();
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        loadLocalData();
        loadFavorites();
    }

    private void loadLocalData() {
        final TrainData trainData = TrainData.getInstance();
        trainData.read();
        final BusData busData = BusData.getInstance();
        busData.readBusStops();
        final DataHolder dataHolder = DataHolder.getInstance();
        dataHolder.setBusData(busData);
        dataHolder.setTrainData(trainData);
    }

    private void loadFavorites() {
        final Observable<SparseArray<TrainArrival>> trainArrivalsObservable = Observable.create(
            (Subscriber<? super SparseArray<TrainArrival>> subscriber) -> subscriber.onNext(service.loadFavoritesTrain()))
            .onErrorReturn(throwable -> {
                Log.e(TAG, throwable.getMessage(), throwable);
                return new SparseArray<>();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        final Observable<List<BusArrival>> busArrivalsObservable = Observable.create(
            (Subscriber<? super List<BusArrival>> subscriber) ->  subscriber.onNext(service.loadFavoritesBuses()))
            .onErrorReturn(throwable -> {
                Log.e(TAG, throwable.getMessage(), throwable);
                return new ArrayList<>();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        Observable.zip(trainArrivalsObservable, busArrivalsObservable, new Func2<SparseArray<TrainArrival>, List<BusArrival>, FavoritesResult>() {
                @Override
                public FavoritesResult call(final SparseArray<TrainArrival> trainArrivals, final List<BusArrival> busArrivals) {
                    App.modifyLastUpdate(Calendar.getInstance().getTime());
                    trackWithGoogleAnalytics();
                    final FavoritesResult favoritesResult = new FavoritesResult();
                    favoritesResult.setTrainArrivals(trainArrivals);
                    favoritesResult.setBusArrivals(busArrivals);
                    return favoritesResult;
                }
            }
        ).subscribe(
            this::startMainActivity,
            onError -> {
                Log.e(TAG, onError.getMessage(), onError);
                displayError("Oops, something went wrong!");
            }
        );
    }


    private void trackWithGoogleAnalytics() {
        Util.trackAction(this, R.string.analytics_category_req, R.string.analytics_action_get_train, R.string.url_train_arrivals, 0);
        Util.trackAction(this, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.url_bus_arrival, 0);
    }

    /**
     * Finish current activity and start main activity with custom transition
     *
     * @param trainArrivals the train arrivals
     * @param busArrivals   the bus arrivals
     */
    private void startMainActivity(@NonNull final FavoritesResult result) {
        if (!isFinishing()) {
            final Intent intent = new Intent(this, MainActivity.class);
            final Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(getString(R.string.bundle_bus_arrivals), (ArrayList<BusArrival>) result.getBusArrivals());
            bundle.putSparseParcelableArray(getString(R.string.bundle_train_arrivals), result.getTrainArrivals());
            intent.putExtras(bundle);

            finish();
            startActivity(intent);
        }
    }

    public void displayError(@NonNull final String message) {
        DataHolder.getInstance().setTrainData(null);
        DataHolder.getInstance().setBusData(null);
        App.startErrorActivity(this, message);
    }
}
