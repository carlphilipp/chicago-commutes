/**
 * Copyright 2020 Carl-Philipp Harmant
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
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.TrainListStationActivity
import fr.cph.chicago.core.adapter.TrainStationAdapter
import fr.cph.chicago.core.model.enumeration.TrainLine
import kotlinx.android.synthetic.main.fragment_train.trainListView

/**
 * Train Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class TrainFragment : Fragment(R.layout.fragment_train) {

    companion object {
        fun newInstance(sectionNumber: Int): TrainFragment {
            return fragmentWithBundle(TrainFragment(), sectionNumber) as TrainFragment
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val ada = TrainStationAdapter()
        trainListView.adapter = ada
        trainListView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(context, TrainListStationActivity::class.java)
            val extras = Bundle()
            val line = TrainLine.values()[position].toString()
            extras.putString(getString(R.string.bundle_train_line), line)
            intent.putExtras(extras)
            startActivity(intent)
        }
    }
}
