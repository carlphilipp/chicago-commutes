/**
 * Copyright 2021 Carl-Philipp Harmant
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
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.station.TrainStationActivity
import java.math.BigInteger

class TrainDetailsButtonOnClickListener(private val stationId: BigInteger) : View.OnClickListener {

    override fun onClick(v: View?) {
        // Start train station activity
        val extras = Bundle()
        val intent = Intent(App.instance.applicationContext, TrainStationActivity::class.java)
        extras.putString(App.instance.getString(R.string.bundle_train_stationId), stationId.toString())
        intent.putExtras(extras)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        App.instance.startActivity(intent)
    }
}
