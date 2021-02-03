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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.station.BusStopActivity
import fr.cph.chicago.core.adapter.PopupBusDetailsFavoritesAdapter
import fr.cph.chicago.core.model.dto.BusDetailsDTO

class BusStopOnClickListener(private val context: Context, private val parent: ViewGroup, private val busDetailsDTOs: List<BusDetailsDTO>) : View.OnClickListener {

    override fun onClick(view: View) {
        if (busDetailsDTOs.size == 1) {
            onClickOneBound()
        } else {
            onClickMultipleBound()
        }
    }

    private fun onClickOneBound() {
        val busDetails = busDetailsDTOs[0]
        startBusStopActivity(busDetails)
    }

    private fun onClickMultipleBound() {
        val ada = PopupBusDetailsFavoritesAdapter(context, busDetailsDTOs)
        val view = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = view.inflate(R.layout.popup_bus, parent, false)
        val listView = popupView.findViewById<ListView>(R.id.details)
        listView.adapter = ada
        val builder = AlertDialog.Builder(context)
        builder.setAdapter(ada) { _, position ->
            val busDetails = busDetailsDTOs[position]
            startBusStopActivity(busDetails)
        }
        val dialog = builder.create()
        dialog.show()
        dialog.window?.setLayout((App.instance.screenWidth * 0.7).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun startBusStopActivity(busDetails: BusDetailsDTO) {
        val intent = Intent(context, BusStopActivity::class.java)
        val extras = Bundle()
        extras.putString(context.getString(R.string.bundle_bus_stop_id), busDetails.stopId.toString())
        extras.putString(context.getString(R.string.bundle_bus_route_id), busDetails.busRouteId)
        extras.putString(context.getString(R.string.bundle_bus_route_name), busDetails.routeName)
        extras.putString(context.getString(R.string.bundle_bus_bound), busDetails.bound)
        extras.putString(context.getString(R.string.bundle_bus_bound_title), busDetails.boundTitle)

        intent.putExtras(extras)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
