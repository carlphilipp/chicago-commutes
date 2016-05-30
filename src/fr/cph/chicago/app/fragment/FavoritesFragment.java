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

package fr.cph.chicago.app.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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

import fr.cph.chicago.app.App;
import fr.cph.chicago.R;
import fr.cph.chicago.app.activity.MainActivity;
import fr.cph.chicago.app.activity.SearchActivity;
import fr.cph.chicago.app.adapter.FavoritesAdapter;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.rx.observable.ObservableUtil;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.entity.dto.FavoritesDTO;
import rx.Observable;

/**
 * FavoritesData Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class FavoritesFragment extends Fragment {

    private static final String TAG = FavoritesFragment.class.getSimpleName();
    private static final String ARG_SECTION_NUMBER = "section_number";

    private FavoritesAdapter favoritesAdapter;
    private List<BusArrival> busArrivals;
    private SparseArray<TrainArrival> trainArrivals;
    private List<BikeStation> bikeStations;
    private RefreshTimingTask refreshTimingTask;

    private MainActivity activity;
    private RelativeLayout welcomeLayout;
    private View rootView;
    private SwipeRefreshLayout swipeRefreshLayout;

    /**
     * Returns a new instance of this fragment for the given section number.
     *
     * @param sectionNumber the section number
     * @return a favorite fragment
     */
    @NonNull
    public static FavoritesFragment newInstance(final int sectionNumber) {
        final FavoritesFragment fragment = new FavoritesFragment();
        final Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
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
            boolean boolTrain = App.checkTrainData(activity);
            if (boolTrain) {
                App.checkBusData(activity);
            }
        }
        if (bikeStations == null) {
            bikeStations = new ArrayList<>();
        }
        Util.trackScreen(getContext(), getString(R.string.analytics_favorites_fragment));
    }

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        if (!activity.isFinishing()) {
            rootView = inflater.inflate(R.layout.fragment_main, container, false);
            welcomeLayout = (RelativeLayout) rootView.findViewById(R.id.welcome);
            swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.activity_main_swipe_refresh_layout);
            if (favoritesAdapter == null) {
                favoritesAdapter = new FavoritesAdapter(activity);
                favoritesAdapter.setTrainArrivals(trainArrivals);
                favoritesAdapter.setBusArrivals(busArrivals);
                favoritesAdapter.setBikeStations(bikeStations);
                favoritesAdapter.setFavorites();
            }
            final RecyclerView listView = (RecyclerView) rootView.findViewById(R.id.favorites_list);
            final RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(activity);
            final FloatingActionButton floatingButton = (FloatingActionButton) rootView.findViewById(R.id.floating_button);

            listView.setAdapter(favoritesAdapter);
            listView.setLayoutManager(mLayoutManager);
            floatingButton.setOnClickListener(v -> {
                if (bikeStations.isEmpty()) {
                    Util.showMessage(activity, R.string.message_too_fast);
                } else {
                    final Intent intent = new Intent(activity, SearchActivity.class);
                    intent.putParcelableArrayListExtra(getString(R.string.bundle_bike_stations), (ArrayList<BikeStation>) bikeStations);
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
                swipeRefreshLayout.setColorSchemeColors(Util.getRandomColor());
                Util.trackAction(activity, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.url_bus_arrival, 0);
                Util.trackAction(activity, R.string.analytics_category_req, R.string.analytics_action_get_train, R.string.url_train_arrivals, 0);
                Util.trackAction(activity, R.string.analytics_category_req, R.string.analytics_action_get_divvy, R.string.analytics_action_get_divvy_all, 0);
                Util.trackAction(activity, R.string.analytics_category_ui, R.string.analytics_action_press, R.string.analytics_action_refresh_fav, 0);

                final DataHolder dataHolder = DataHolder.getInstance();
                if (dataHolder.getBusData() == null
                    || dataHolder.getBusData().getBusRoutes() == null
                    || dataHolder.getBusData().getBusRoutes().size() == 0
                    || activity.getIntent().getParcelableArrayListExtra(getString(R.string.bundle_bike_stations)) == null
                    || activity.getIntent().getParcelableArrayListExtra(getString(R.string.bundle_bike_stations)).size() == 0) {
                    activity.loadFirstData();
                }

                if (Util.isNetworkAvailable(getContext())) {
                    final Observable<FavoritesDTO> zipped = ObservableUtil.createAllDataObservable(getContext());
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
        if (welcomeLayout != null) {
            boolean hasFav = Preferences.hasFavorites(getContext(), App.PREFERENCE_FAVORITES_TRAIN, App.PREFERENCE_FAVORITES_BUS, App.PREFERENCE_FAVORITES_BIKE);
            if (!hasFav) {
                welcomeLayout.setVisibility(View.VISIBLE);
            } else {
                welcomeLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public final void onAttach(final Context context) {
        super.onAttach(context);
        activity = context instanceof Activity ? (MainActivity) context : null;
    }

    @Override
    public final void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(getString(R.string.bundle_bus_arrivals), (ArrayList<BusArrival>) busArrivals);
        outState.putSparseParcelableArray(getString(R.string.bundle_train_arrivals), trainArrivals);
        outState.putParcelableArrayList(getString(R.string.bundle_bike_stations), (ArrayList<BikeStation>) bikeStations);
    }

    public final void reloadData(final FavoritesDTO favoritesDTO) {
        boolean error = false;
        if (!favoritesDTO.isBikeError()) {
            // Put into intent new bike stations data
            activity.getIntent().putParcelableArrayListExtra(getString(R.string.bundle_bike_stations), (ArrayList<BikeStation>) favoritesDTO.getBikeStations());
            bikeStations = favoritesDTO.getBikeStations();
        } else {
            error = true;
        }

        if (!favoritesDTO.isBusError()) {
            favoritesAdapter.setBusArrivals(favoritesDTO.getBusArrivals());
        } else {
            error = true;
        }

        if (!favoritesDTO.isTrainError()) {
            favoritesAdapter.setTrainArrivals(favoritesDTO.getTrainArrivals());
        } else {
            error = true;
        }
        favoritesAdapter.setFavorites();
        favoritesAdapter.refreshUpdated();
        favoritesAdapter.refreshUpdatedView();
        favoritesAdapter.notifyDataSetChanged();

        // Highlight background
        rootView.setBackgroundResource(R.drawable.highlight_selector);
        rootView.postDelayed(() -> rootView.setBackgroundResource(R.drawable.bg_selector), 100);
        stopRefreshing();
        if (error) {
            Util.showMessage(activity, R.string.message_something_went_wrong);
        }
    }

    /**
     * Display error
     *
     * @param message the message
     */
    public final void displayError(@NonNull final Integer message) {
        Util.showMessage(activity, message);
        stopRefreshing();
    }

    public final void setBikeStations(final List<BikeStation> bikeStations) {
        this.bikeStations = bikeStations;
        favoritesAdapter.setBikeStations(bikeStations);
        favoritesAdapter.notifyDataSetChanged();
    }

    /**
     * Start refresh task
     */
    private void startRefreshTask() {
        refreshTimingTask = (RefreshTimingTask) new RefreshTimingTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        favoritesAdapter.refreshUpdatedView();
    }

    public void startRefreshing() {
        swipeRefreshLayout.setColorSchemeColors(Util.getRandomColor());
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
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Log.v(TAG, "Stopping thread. Normal Behavior");
                }
            }
            return null;
        }
    }
}
