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

package fr.cph.chicago.core.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import fr.cph.chicago.R;
import fr.cph.chicago.core.App;
import fr.cph.chicago.core.activity.SearchActivity;
import fr.cph.chicago.core.adapter.FavoritesAdapter;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.FavoritesData;
import fr.cph.chicago.data.PreferencesImpl;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.dto.FavoritesDTO;
import fr.cph.chicago.rx.ObservableUtil;
import fr.cph.chicago.util.Util;
import io.reactivex.Observable;

import static fr.cph.chicago.Constants.BUSES_ARRIVAL_URL;
import static fr.cph.chicago.Constants.TRAINS_ARRIVALS_URL;

/**
 * FavoritesData Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
@SuppressWarnings("WeakerAccess")
public class FavoritesFragment extends AbstractFragment {

    private static final String TAG = FavoritesFragment.class.getSimpleName();

    @BindView(R.id.welcome)
    RelativeLayout welcomeLayout;
    @BindView(R.id.activity_main_swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.favorites_list)
    RecyclerView listView;
    @BindView(R.id.floating_button)
    FloatingActionButton floatingButton;

    @BindString(R.string.bundle_bike_stations)
    String bundleBikeStation;
    @BindString(R.string.bundle_bus_arrivals)
    String bundleBusArrivals;
    @BindString(R.string.bundle_train_arrivals)
    String bundleTrainArrivals;

    private FavoritesAdapter favoritesAdapter;
    private List<BusArrival> busArrivals;
    private SparseArray<TrainArrival> trainArrivals;
    private List<BikeStation> bikeStations;
    private RefreshTimingTask refreshTimingTask;

    private View rootView;

    /**
     * Returns a new INSTANCE of this fragment for the given section number.
     *
     * @param sectionNumber the section number
     * @return a favorite fragment
     */
    @NonNull
    public static FavoritesFragment newInstance(final int sectionNumber) {
        return (FavoritesFragment) fragmentWithBundle(new FavoritesFragment(), sectionNumber);
    }

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            final Bundle bundle = activity.getIntent().getExtras();
            busArrivals = bundle.getParcelableArrayList(getString(R.string.bundle_bus_arrivals));
            trainArrivals = bundle.getSparseParcelableArray(getString(R.string.bundle_train_arrivals));
            bikeStations = bundle.getParcelableArrayList(getString(R.string.bundle_bike_stations));
        } else {
            busArrivals = savedInstanceState.getParcelableArrayList(getString(R.string.bundle_bus_arrivals));
            trainArrivals = savedInstanceState.getSparseParcelableArray(getString(R.string.bundle_train_arrivals));
            bikeStations = savedInstanceState.getParcelableArrayList(getString(R.string.bundle_bike_stations));
            boolean boolTrain = App.Companion.checkTrainData(activity);
            if (boolTrain) {
                App.Companion.checkBusData(activity);
            }
        }
        if (bikeStations == null) {
            bikeStations = new ArrayList<>();
        }
        Util.INSTANCE.trackScreen((App) getActivity().getApplication(), getString(R.string.analytics_favorites_fragment));
    }

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        if (!activity.isFinishing()) {
            rootView = inflater.inflate(R.layout.fragment_main, container, false);
            setBinder(rootView);
            if (favoritesAdapter == null) {
                favoritesAdapter = new FavoritesAdapter(activity);
                FavoritesData.INSTANCE.setTrainArrivals(trainArrivals);
                FavoritesData.INSTANCE.setBusArrivals(busArrivals);
                FavoritesData.INSTANCE.setBikeStations(bikeStations);
                favoritesAdapter.setFavorites();
            }
            final RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(activity);
            listView.setAdapter(favoritesAdapter);
            listView.setLayoutManager(mLayoutManager);
            floatingButton.setOnClickListener(v -> {
                if (bikeStations.isEmpty()) {
                    Util.INSTANCE.showMessage(activity, R.string.message_too_fast);
                } else {
                    final Intent intent = new Intent(activity, SearchActivity.class);
                    intent.putParcelableArrayListExtra(bundleBikeStation, (ArrayList<BikeStation>) bikeStations);
                    activity.startActivity(intent);
                }
            });
            listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
                    if (dy > 0 && floatingButton.isShown()) {
                        floatingButton.hide();
                    } else if (dy < 0 && !floatingButton.isShown()) {
                        floatingButton.show();
                    }
                }
            });

            swipeRefreshLayout.setOnRefreshListener(() -> {
                swipeRefreshLayout.setColorSchemeColors(Util.INSTANCE.getRandomColor());
                Util.INSTANCE.trackAction((App) getActivity().getApplication(), R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_ARRIVAL_URL);
                Util.INSTANCE.trackAction((App) getActivity().getApplication(), R.string.analytics_category_req, R.string.analytics_action_get_train, TRAINS_ARRIVALS_URL);
                Util.INSTANCE.trackAction((App) getActivity().getApplication(), R.string.analytics_category_req, R.string.analytics_action_get_divvy, getContext().getString(R.string.analytics_action_get_divvy_all));
                Util.INSTANCE.trackAction((App) getActivity().getApplication(), R.string.analytics_category_ui, R.string.analytics_action_press, getContext().getString(R.string.analytics_action_refresh_fav));

                final DataHolder dataHolder = DataHolder.INSTANCE;
                if (dataHolder.getBusData().getBusRoutes().size() == 0
                    || activity.getIntent().getParcelableArrayListExtra(bundleBikeStation) == null
                    || activity.getIntent().getParcelableArrayListExtra(bundleBikeStation).size() == 0) {
                    activity.loadFirstData();
                }

                if (Util.INSTANCE.isNetworkAvailable(getContext())) {
                    final Observable<FavoritesDTO> zipped = ObservableUtil.INSTANCE.createAllDataObservable(getActivity().getApplication());
                    zipped.subscribe(
                        this::reloadData,
                        onError -> {
                            Log.e(TAG, onError.getMessage(), onError);
                            this.displayError(R.string.message_something_went_wrong);
                        }
                    );
                } else {
                    this.displayError(R.string.message_network_error);
                }
            });

            startRefreshTask();
            Util.INSTANCE.displayRateSnackBarIfNeeded(swipeRefreshLayout, activity);
        }
        return rootView;
    }

    @Override
    public final void onPause() {
        super.onPause();
        if (refreshTimingTask != null) {
            refreshTimingTask.cancel(true);
        }
    }

    @Override
    public final void onStop() {
        super.onStop();
        if (refreshTimingTask != null) {
            refreshTimingTask.cancel(true);
        }
    }

    @Override
    public final void onDestroy() {
        super.onDestroy();
        if (refreshTimingTask != null) {
            refreshTimingTask.cancel(true);
        }
    }

    @Override
    public final void onResume() {
        super.onResume();
        favoritesAdapter.setFavorites();
        favoritesAdapter.notifyDataSetChanged();
        if (refreshTimingTask.getStatus() == Status.FINISHED) {
            startRefreshTask();
        }
        boolean hasFav = PreferencesImpl.INSTANCE.hasFavorites(getContext());
        welcomeLayout.setVisibility(hasFav ? View.GONE : View.VISIBLE);
    }

    @Override
    public final void onSaveInstanceState(final Bundle outState) {
        outState.putParcelableArrayList(bundleBusArrivals, (ArrayList<BusArrival>) busArrivals);
        outState.putSparseParcelableArray(bundleTrainArrivals, trainArrivals);
        outState.putParcelableArrayList(bundleBikeStation, (ArrayList<BikeStation>) bikeStations);
        super.onSaveInstanceState(outState);
    }

    public final void reloadData(final FavoritesDTO favoritesDTO) {
        activity.getIntent().putParcelableArrayListExtra(bundleBikeStation, (ArrayList<BikeStation>) favoritesDTO.getBikeStations());
        bikeStations = favoritesDTO.getBikeStations();
        FavoritesData.INSTANCE.setBusArrivals(favoritesDTO.getBusArrivalDTO().getBusArrivals());
        FavoritesData.INSTANCE.setTrainArrivals(favoritesDTO.getTrainArrivalDTO().getTrainArrivalSparseArray());

        favoritesAdapter.setFavorites();
        favoritesAdapter.refreshUpdated();
        favoritesAdapter.refreshUpdatedView();
        favoritesAdapter.notifyDataSetChanged();

        rootView.setBackgroundResource(R.drawable.highlight_selector);
        rootView.postDelayed(() -> rootView.setBackgroundResource(R.drawable.bg_selector), 100);
        stopRefreshing();
        if (Util.INSTANCE.isAtLeastTwoErrors(favoritesDTO.getTrainArrivalDTO().getError(), favoritesDTO.getBusArrivalDTO().getError(), favoritesDTO.getBikeError())) {
            Util.INSTANCE.showMessage(activity, R.string.message_something_went_wrong);
        } else if (favoritesDTO.getTrainArrivalDTO().getError()) {
            Util.INSTANCE.showMessage(activity, R.string.message_error_train_favorites);
        } else if (favoritesDTO.getBusArrivalDTO().getError()) {
            Util.INSTANCE.showMessage(activity, R.string.message_error_bus_favorites);
        } else if (favoritesDTO.getBikeError()) {
            Util.INSTANCE.showMessage(activity, R.string.message_error_bike_favorites);
        }
    }

    /**
     * Display error
     *
     * @param message the message
     */
    public final void displayError(final int message) {
        Util.INSTANCE.showMessage(activity, message);
        stopRefreshing();
    }

    public final void setBikeStations(final List<BikeStation> bikeStations) {
        this.bikeStations = bikeStations;
        FavoritesData.INSTANCE.setBikeStations(bikeStations);
        favoritesAdapter.notifyDataSetChanged();
    }

    /**
     * Start refreshBusAndStation task
     */
    private void startRefreshTask() {
        refreshTimingTask = (RefreshTimingTask) new RefreshTimingTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        favoritesAdapter.refreshUpdatedView();
    }

    public void startRefreshing() {
        swipeRefreshLayout.setColorSchemeColors(Util.INSTANCE.getRandomColor());
        swipeRefreshLayout.setRefreshing(true);
    }

    private void stopRefreshing() {
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * RefreshTask
     *
     * @author Carl-Philipp Harmant
     * @version 1
     */
    private class RefreshTimingTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected final void onProgressUpdate(final Void... values) {
            super.onProgressUpdate();
            favoritesAdapter.refreshUpdatedView();
        }

        @Override
        protected final Void doInBackground(final Void... params) {
            while (!this.isCancelled()) {
                Log.v(TAG, "Updated of time " + Thread.currentThread().getId());
                try {
                    publishProgress();
                    Thread.sleep(10000);
                } catch (final InterruptedException e) {
                    Log.v(TAG, "Stopping thread. Normal Behavior");
                }
            }
            return null;
        }
    }
}
