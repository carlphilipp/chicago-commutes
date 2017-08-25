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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import butterknife.BindString;
import butterknife.BindView;
import fr.cph.chicago.R;
import fr.cph.chicago.core.App;
import fr.cph.chicago.core.activity.TrainListStationActivity;
import fr.cph.chicago.core.adapter.TrainStationAdapter;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.util.Util;

/**
 * Train Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
@SuppressWarnings("WeakerAccess")
public final class TrainFragment extends AbstractFragment {

    @BindView(R.id.train_list)
    ListView listView;
    @BindString(R.string.bundle_train_line)
    String bundleTrainLine;

    private final Util util;

    public TrainFragment() {
        util = Util.INSTANCE;
    }

    /**
     * Returns a new instance of this fragment for the given section number.
     *
     * @param sectionNumber the section number
     * @return a train fragment
     */
    public static TrainFragment newInstance(final int sectionNumber) {
        return (TrainFragment) fragmentWithBundle(new TrainFragment(), sectionNumber);
    }

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        util.trackScreen(getString(R.string.analytics_train_fragment));
    }

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_train, container, false);
        setBinder(rootView);
        final TrainStationAdapter ada = new TrainStationAdapter();
        listView.setAdapter(ada);
        listView.setOnItemClickListener((parentView, childView, position, id) -> {
            final Intent intent = new Intent(getContext(), TrainListStationActivity.class);
            final Bundle extras = new Bundle();
            final String line = TrainLine.values()[position].toString();
            extras.putString(bundleTrainLine, line);
            intent.putExtras(extras);
            startActivity(intent);
        });
        return rootView;
    }
}
