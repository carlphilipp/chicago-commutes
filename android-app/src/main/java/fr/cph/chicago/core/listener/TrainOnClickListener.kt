/**
 * Copyright 2017 Carl-Philipp Harmant
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

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup

import java.util.ArrayList

import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.StationActivity
import fr.cph.chicago.core.activity.TrainMapActivity
import fr.cph.chicago.core.adapter.PopupTrainAdapter
import fr.cph.chicago.entity.enumeration.TrainLine
import fr.cph.chicago.util.Util

/**
 * FavoritesData train on click listener
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class TrainOnClickListener(private val context: Context,
                           private val stationId: Int,
                           private val trainLines: Set<TrainLine>) : OnClickListener {

    override fun onClick(view: View) {
        if (!Util.isNetworkAvailable(view.context)) {
            Util.showNetworkErrorMessage(view)
        } else {
            val values = ArrayList<String>()
            val colors = ArrayList<Int>()
            values.add(view.context.getString(R.string.message_open_details))
            for (line in trainLines) {
                values.add(line.toString() + " line - See trains")
                if (line !== TrainLine.YELLOW) {
                    colors.add(line.color)
                } else {
                    colors.add(ContextCompat.getColor(view.context, R.color.yellowLine))
                }
            }
            val ada = PopupTrainAdapter(view.context, values, colors)

            val lines = ArrayList<TrainLine>()
            lines.addAll(trainLines)

            val builder = AlertDialog.Builder(context)
            builder.setAdapter(ada) { _, position ->
                val extras = Bundle()
                if (position == 0) {
                    // Start station activity
                    val intent = Intent(view.context, StationActivity::class.java)
                    extras.putInt(view.context.getString(R.string.bundle_train_stationId), stationId)
                    intent.putExtras(extras)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    view.context.startActivity(intent)
                } else {
                    // Follow all trains from given line on google map view
                    val intent = Intent(view.context, TrainMapActivity::class.java)
                    extras.putString(view.context.getString(R.string.bundle_train_line), lines[position - 1].toTextString())
                    intent.putExtras(extras)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    view.context.startActivity(intent)
                }
            }

            val dialog = builder.create()
            dialog.show()
            if (dialog.window != null) {
                dialog.window!!.setLayout((App.screenWidth * 0.7).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        }
    }
}