/**
 * Copyright 2016 Carl-Philipp Harmant
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.adapter.BikeAdapter;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.util.Util;

/**
 * Bike Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BikeFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private View rootView;
    private RelativeLayout loadingLayout;
    private TextView filterView;
    private ListView bikeListView;

    private MainActivity mainActivity;
    private BikeAdapter bikeAdapter;
    private List<BikeStation> bikeStations;

    /**
     * Returns a new instance of this fragment for the given section number.
     *
     * @param sectionNumber the section number
     * @return the fragment
     */
    @NonNull
    public static BikeFragment newInstance(final int sectionNumber) {
        final BikeFragment fragment = new BikeFragment();
        final Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public final void onAttach(final Context context) {
        super.onAttach(context);
        mainActivity = context instanceof Activity ? (MainActivity) context : null;
    }

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bikeStations = getBikeStations(savedInstanceState);

        setHasOptionsMenu(true);

        Util.trackScreen(getString(R.string.analytics_bike_fragment));
    }

    @NonNull
    private List<BikeStation> getBikeStations(@Nullable final Bundle savedInstanceState) {
        List<BikeStation> bikeStations;
        if (savedInstanceState != null) {
            bikeStations = savedInstanceState.getParcelableArrayList(getString(R.string.bundle_bike_stations));
        } else {
            final Bundle bundle = mainActivity.getIntent().getExtras();
            bikeStations = bundle.getParcelableArrayList(getString(R.string.bundle_bike_stations));
        }
        if (bikeStations == null) {
            bikeStations = new ArrayList<>();
        }
        return bikeStations;
    }

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_bike, container, false);
        if (!mainActivity.isFinishing()) {
            loadingLayout = (RelativeLayout) rootView.findViewById(R.id.loading_relativeLayout);
            bikeListView = (ListView) rootView.findViewById(R.id.bike_list);
            filterView = (TextView) rootView.findViewById(R.id.bike_filter);
            if (!bikeStations.isEmpty()) {
                loadList();
            } else {
                loadError();
            }
        }
        return rootView;
    }

    private void loadList() {
        final EditText filter = (EditText) rootView.findViewById(R.id.bike_filter);
        if (bikeAdapter == null) {
            bikeAdapter = new BikeAdapter(mainActivity);
        }
        bikeListView.setAdapter(bikeAdapter);
        filter.addTextChangedListener(new TextWatcher() {

            private List<BikeStation> bikeStations;

            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
                bikeStations = new ArrayList<>();
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                bikeStations.addAll(
                    Stream.of(BikeFragment.this.bikeStations)
                        .filter(bikeStation -> StringUtils.containsIgnoreCase(bikeStation.getName(), s.toString().trim()))
                        .collect(Collectors.toList())
                );
            }

            @Override
            public void afterTextChanged(final Editable s) {
                bikeAdapter.setBikeStations(this.bikeStations);
                bikeAdapter.notifyDataSetChanged();
            }
        });
        bikeListView.setVisibility(ListView.VISIBLE);
        filterView.setVisibility(ListView.VISIBLE);
        loadingLayout.setVisibility(RelativeLayout.INVISIBLE);
        final RelativeLayout errorLayout = (RelativeLayout) rootView.findViewById(R.id.error_layout);
        errorLayout.setVisibility(RelativeLayout.INVISIBLE);
    }

    private void loadError() {
        loadingLayout.setVisibility(RelativeLayout.INVISIBLE);
        final RelativeLayout errorLayout = (RelativeLayout) rootView.findViewById(R.id.error_layout);
        errorLayout.setVisibility(RelativeLayout.VISIBLE);
    }

    public final void setBikeStations(@NonNull final List<BikeStation> bikeStations) {
        this.bikeStations = bikeStations;
        if (bikeAdapter == null) {
            loadList();
        } else {
            bikeAdapter.setBikeStations(bikeStations);
            bikeAdapter.notifyDataSetChanged();
        }
    }
}
