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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import fr.cph.chicago.app.App;
import fr.cph.chicago.R;
import fr.cph.chicago.app.activity.MainActivity;
import fr.cph.chicago.app.adapter.BusAdapter;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.util.Util;

/**
 * Bus Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BusFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this fragment.
     **/
    private static final String ARG_SECTION_NUMBER = "section_number";

    private EditText textFilter;
    private ListView listView;

    private MainActivity activity;
    private BusAdapter busAdapter;

    /**
     * Returns a new instance of this fragment for the given section number.
     *
     * @param sectionNumber the section number
     * @return the fragment
     */
    @NonNull
    public static BusFragment newInstance(final int sectionNumber) {
        final BusFragment fragment = new BusFragment();
        final Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public final void onAttach(final Context context) {
        super.onAttach(context);
        activity = context instanceof Activity ? (MainActivity) context : null;
    }

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.checkBusData(activity);
        Util.trackScreen(getString(R.string.analytics_bus_fragment));
    }

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_bus, container, false);
        if (!activity.isFinishing()) {
            textFilter = (EditText) rootView.findViewById(R.id.bus_filter);
            listView = (ListView) rootView.findViewById(R.id.bus_list);
            addView();
        }
        return rootView;
    }

    public final void update() {
        addView();
    }

    private void addView() {
        busAdapter = new BusAdapter(activity);
        listView.setAdapter(busAdapter);
        textFilter.setVisibility(TextView.VISIBLE);
        textFilter.addTextChangedListener(new TextWatcher() {

            final BusData busData = DataHolder.getInstance().getBusData();
            List<BusRoute> busRoutes = null;

            @Override
            public void beforeTextChanged(final CharSequence c, final int start, final int count, final int after) {
                busRoutes = new ArrayList<>();
            }

            @Override
            public void onTextChanged(final CharSequence c, final int start, final int before, final int count) {
                final List<BusRoute> busRoutes = busData.getBusRoutes();
                final CharSequence trimmed = c.toString().trim();
                this.busRoutes.addAll(
                    Stream.of(busRoutes)
                        .filter(busRoute -> StringUtils.containsIgnoreCase(busRoute.getId(), trimmed) || StringUtils.containsIgnoreCase(busRoute.getName(), trimmed))
                        .collect(Collectors.toList())
                );
            }

            @Override
            public void afterTextChanged(final Editable s) {
                busAdapter.setRoutes(busRoutes);
                busAdapter.notifyDataSetChanged();
            }
        });
    }
}
