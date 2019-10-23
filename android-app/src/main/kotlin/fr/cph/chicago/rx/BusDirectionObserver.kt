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

package fr.cph.chicago.rx

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.BusBoundActivity
import fr.cph.chicago.core.activity.map.BusMapActivity
import fr.cph.chicago.core.adapter.PopupBusAdapter
import fr.cph.chicago.core.model.BusDirections
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.util.Util
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import timber.log.Timber

class BusDirectionObserver(private val viewClickable: View,
                           private val parent: ViewGroup,
                           private val convertView: View,
                           private val busRoute: BusRoute) : SingleObserver<BusDirections> {

    companion object {
        private val util = Util
    }

    override fun onSubscribe(d: Disposable) {}

    override fun onSuccess(busDirections: BusDirections) {
        val lBusDirections = busDirections.busDirections
        val data = lBusDirections
            .map { busDir -> busDir.text }
            .toMutableList()
        data.add(parent.context.getString(R.string.message_see_all_buses_on_line) + busDirections.id)

        val vi = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = vi.inflate(R.layout.popup_bus, parent, false)
        val listView = popupView.findViewById<ListView>(R.id.details)
        val ada = PopupBusAdapter(parent.context.applicationContext, data)
        listView.adapter = ada

        val alertDialog = AlertDialog.Builder(parent.context)
        alertDialog.setAdapter(ada) { _, pos ->
            val extras = Bundle()
            if (pos != data.size - 1) {
                val intent = Intent(parent.context, BusBoundActivity::class.java)
                extras.putString(parent.context.getString(R.string.bundle_bus_route_id), busRoute.id)
                extras.putString(parent.context.getString(R.string.bundle_bus_route_name), busRoute.name)
                extras.putString(parent.context.getString(R.string.bundle_bus_bound), lBusDirections[pos].text)
                extras.putString(parent.context.getString(R.string.bundle_bus_bound_title), lBusDirections[pos].text)
                intent.putExtras(extras)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                parent.context.applicationContext.startActivity(intent)
            } else {
                val busDirectionArray = arrayOfNulls<String>(lBusDirections.size)
                var i = 0
                for (busDir in lBusDirections) {
                    busDirectionArray[i++] = busDir.text
                }
                val intent = Intent(parent.context, BusMapActivity::class.java)
                extras.putString(parent.context.getString(R.string.bundle_bus_route_id), busDirections.id)
                extras.putStringArray(parent.context.getString(R.string.bundle_bus_bounds), busDirectionArray)
                intent.putExtras(extras)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                parent.context.applicationContext.startActivity(intent)
            }
        }
        alertDialog.setOnCancelListener { convertView.visibility = LinearLayout.GONE }
        val dialog = alertDialog.create()
        dialog.show()
        dialog.window?.setLayout((App.instance.screenWidth * 0.7).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        convertView.visibility = LinearLayout.GONE
        viewClickable.isClickable = true
    }

    override fun onError(throwable: Throwable) {
        viewClickable.isClickable = true
        util.handleConnectOrParserException(throwable, convertView)
        convertView.visibility = LinearLayout.GONE
        Timber.e(throwable, "Error while loading bus directions")
    }
}
