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

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.map.TrainMapActivity
import fr.cph.chicago.core.activity.station.TrainStationActivity
import fr.cph.chicago.core.adapter.PopupTrainAdapter
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.util.Color
import java.math.BigInteger

/**
 * Favorites train on click listener
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class TrainOnClickListener(private val parent: ViewGroup, private val stationId: BigInteger, private val trainLines: Set<TrainLine>) : OnClickListener {

    override fun onClick(view: View) {
        val values = mutableListOf(view.context.getString(R.string.message_open_details))
        val colors = mutableListOf<Int>()
        for (line in trainLines) {
            values.add("$line line - See trains")
            colors.add(if (line !== TrainLine.YELLOW) line.color else Color.yellowLine)
        }
        val ada = PopupTrainAdapter(parent.context.applicationContext, values, colors)

        val lines = mutableListOf<TrainLine>()
        lines.addAll(trainLines)

        val alertDialog = AlertDialog.Builder(parent.context)
        alertDialog.setAdapter(ada) { _, position ->
            val extras = Bundle()
            val intent: Intent
            if (position == 0) {
                // Start train station activity
                intent = Intent(view.context, TrainStationActivity::class.java)
                extras.putString(view.context.getString(R.string.bundle_train_stationId), stationId.toString())
            } else {
                // Follow all trains from given line on google map view
                intent = Intent(view.context, TrainMapActivity::class.java)
                extras.putString(view.context.getString(R.string.bundle_train_line), lines[position - 1].toTextString())
            }
            intent.putExtras(extras)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            view.context.startActivity(intent)
        }

        val dialog = alertDialog.create()
        dialog.show()
        dialog.window?.setLayout((App.instance.screenWidth * 0.7).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
