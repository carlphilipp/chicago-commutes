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

package fr.cph.chicago.core.listener

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.map.BusMapActivity
import fr.cph.chicago.core.model.BusRoute

class BusMapButtonOnClickListener(
    private val context: Context,
    private val busRoute: BusRoute,
    private val bounds: Set<String>) : View.OnClickListener {

    override fun onClick(v: View?) {
        val intent = Intent(context, BusMapActivity::class.java)
        val extras = Bundle()
        extras.putString(context.getString(R.string.bundle_bus_route_id), busRoute.id)
        extras.putStringArray(context.getString(R.string.bundle_bus_bounds), bounds.toTypedArray())
        intent.putExtras(extras)
        context.startActivity(intent)
    }
}
