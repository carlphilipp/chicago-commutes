/**
 * Copyright 2019 Carl-Philipp Harmant
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

package fr.cph.chicago.core.fragment

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import butterknife.BindString
import butterknife.BindView
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.TrainListStationActivity
import fr.cph.chicago.core.adapter.TrainStationAdapter
import fr.cph.chicago.core.model.enumeration.TrainLine

/**
 * Train Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class TrainFragment : Fragment(R.layout.fragment_train) {

    companion object {
        /**
         * Returns a new instance of this fragment for the given section number.
         *
         * @param sectionNumber the section number
         * @return a train fragment
         */
        fun newInstance(sectionNumber: Int): TrainFragment {
            return fragmentWithBundle(TrainFragment(), sectionNumber) as TrainFragment
        }
    }

    @BindView(R.id.train_list)
    lateinit var listView: ListView
    @BindString(R.string.bundle_train_line)
    lateinit var bundleTrainLine: String

    override fun onCreateView(savedInstanceState: Bundle?) {
        val ada = TrainStationAdapter()
        listView.adapter = ada
        listView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(context, TrainListStationActivity::class.java)
            val extras = Bundle()
            val line = TrainLine.values()[position].toString()
            extras.putString(bundleTrainLine, line)
            intent.putExtras(extras)
            startActivity(intent)
        }
    }
}
