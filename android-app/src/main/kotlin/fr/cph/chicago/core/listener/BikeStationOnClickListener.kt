/**
 * Copyright 2018 Carl-Philipp Harmant
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

package fr.cph.chicago.core.listener

import android.content.Intent
import android.os.Bundle
import android.view.View
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.BikeStationActivity
import fr.cph.chicago.core.model.BikeStation

class BikeStationOnClickListener(private val station: BikeStation) : View.OnClickListener {

    override fun onClick(view: View) {
        val intent = Intent(view.context, BikeStationActivity::class.java)
        val extras = Bundle()
        extras.putParcelable(view.context.getString(R.string.bundle_bike_station), station)
        intent.putExtras(extras)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        view.context.startActivity(intent)
    }
}
