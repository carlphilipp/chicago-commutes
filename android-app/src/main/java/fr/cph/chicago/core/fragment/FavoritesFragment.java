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
import fr.cph.chicago.data.FavoritesData;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.dto.FavoritesDTO;
import fr.cph.chicago.rx.ObservableUtil;
import fr.cph.chicago.service.BusService;
import fr.cph.chicago.service.PreferenceService;
import fr.cph.chicago.task.RefreshTimingTask;
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

    private final Util util;
    private final ObservableUtil observableUtil;
    private final FavoritesData favoritesData;
    private final BusService busService;
    private final PreferenceService preferenceService;

    private FavoritesAdapter favoritesAdapter;
    private List<BusArrival> busArrivals;
    private SparseArray<TrainArrival> trainArrivals;
    private List<BikeStation> bikeStations;
    private RefreshTimingTask refreshTimingTask;

    private View rootView;

    public FavoritesFragment() {
        observableUtil = ObservableUtil.INSTANCE;
        util = Util.INSTANCE;
        favoritesData = FavoritesData.INSTANCE;
        busService = BusService.INSTANCE;
        preferenceService = PreferenceService.INSTANCE;
    }

    /**
     * Returns a new trainService of this fragment for the given section number.
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
        final Bundle bundle = activity.getIntent().getExtras();
        busArrivals = bundle == null
            ? savedInstanceState.getParcelableArrayList(getString(R.string.bundle_bus_arrivals))
            :  bundle.getParcelableArrayList(getString(R.string.bundle_bus_arrivals));
        trainArrivals = bundle == null
            ? savedInstanceState.getSparseParcelableArray(getString(R.string.bundle_train_arrivals))
            : bundle.getSparseParcelableArray(getString(R.string.bundle_train_arrivals));
        bikeStations = bundle == null
            ? savedInstanceState.getParcelableArrayList(getString(R.string.bundle_bike_stations))
            : bundle.getParcelableArrayList(getString(R.string.bundle_bike_stations));

        if (savedInstanceState != null) {
            if (App.Companion.checkTrainData(activity)) {
                App.Companion.checkBusData(activity);
            }
        }
        if (bikeStations == null) {
            bikeStations = new ArrayList<>();
        }
        util.trackScreen(getString(R.string.analytics_favorites_fragment));
    }

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        if (!activity.isFinishing()) {
            rootView = inflater.inflate(R.layout.fragment_main, container, false);
            setBinder(rootView);
            if (favoritesAdapter == null) {
                favoritesAdapter = new FavoritesAdapter(activity);
                favoritesData.setTrainArrivals(trainArrivals);
                favoritesData.setBusArrivals(busArrivals);
                favoritesData.setBikeStations(bikeStations);
                favoritesAdapter.refreshFavorites();
            }
            final RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(activity);
            listView.setAdapter(favoritesAdapter);
            listView.setLayoutManager(linearLayoutManager);
            floatingButton.setOnClickListener(v -> {
                if (bikeStations.isEmpty()) {
                    util.showMessage(activity, R.string.message_too_fast);
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
                swipeRefreshLayout.setColorSchemeColors(util.getRandomColor());
                util.trackAction(R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_ARRIVAL_URL);
                util.trackAction(R.string.analytics_category_req, R.string.analytics_action_get_train, TRAINS_ARRIVALS_URL);
                util.trackAction(R.string.analytics_category_req, R.string.analytics_action_get_divvy, getContext().getString(R.string.analytics_action_get_divvy_all));
                util.trackAction(R.string.analytics_category_ui, R.string.analytics_action_press, getContext().getString(R.string.analytics_action_refresh_fav));

                if (busService.getBusRoutes().isEmpty()
                    || activity.getIntent().getParcelableArrayListExtra(bundleBikeStation) == null
                    || activity.getIntent().getParcelableArrayListExtra(bundleBikeStation).size() == 0) {
                    activity.loadFirstData();
                }

                if (util.isNetworkAvailable()) {
                    final Observable<FavoritesDTO> zipped = observableUtil.createAllDataObservable(getActivity().getApplication());
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
            util.displayRateSnackBarIfNeeded(swipeRefreshLayout, activity);
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
        favoritesAdapter.refreshFavorites();
        favoritesAdapter.notifyDataSetChanged();
        if (refreshTimingTask.getStatus() == Status.FINISHED) {
            startRefreshTask();
        }
        boolean hasFav = preferenceService.hasFavorites();
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
        favoritesData.setBusArrivals(favoritesDTO.getBusArrivalDTO().getBusArrivals());
        favoritesData.setTrainArrivals(favoritesDTO.getTrainArrivalDTO().getTrainArrivalSparseArray());

        favoritesAdapter.refreshFavorites();
        favoritesAdapter.resetLastUpdate();
        favoritesAdapter.updateModel();
        favoritesAdapter.notifyDataSetChanged();

        rootView.setBackgroundResource(R.drawable.highlight_selector);
        rootView.postDelayed(() -> rootView.setBackgroundResource(R.drawable.bg_selector), 100);
        stopRefreshing();
        if (util.isAtLeastTwoErrors(favoritesDTO.getTrainArrivalDTO().getError(), favoritesDTO.getBusArrivalDTO().getError(), favoritesDTO.getBikeError())) {
            util.showMessage(activity, R.string.message_something_went_wrong);
        } else if (favoritesDTO.getTrainArrivalDTO().getError()) {
            util.showMessage(activity, R.string.message_error_train_favorites);
        } else if (favoritesDTO.getBusArrivalDTO().getError()) {
            util.showMessage(activity, R.string.message_error_bus_favorites);
        } else if (favoritesDTO.getBikeError()) {
            util.showMessage(activity, R.string.message_error_bike_favorites);
        }
    }

    /**
     * Display error
     *
     * @param message the message
     */
    public final void displayError(final int message) {
        util.showMessage(activity, message);
        stopRefreshing();
    }

    public final void setBikeStations(final List<BikeStation> bikeStations) {
        this.bikeStations = bikeStations;
        favoritesData.setBikeStations(bikeStations);
        favoritesAdapter.notifyDataSetChanged();
    }

    /**
     * Start refreshBusAndStation task
     */
    private void startRefreshTask() {
        refreshTimingTask = (RefreshTimingTask) new RefreshTimingTask(favoritesAdapter).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        favoritesAdapter.updateModel();
    }

    public void startRefreshing() {
        swipeRefreshLayout.setColorSchemeColors(util.getRandomColor());
        swipeRefreshLayout.setRefreshing(true);
    }

    private void stopRefreshing() {
        swipeRefreshLayout.setRefreshing(false);
    }
}
