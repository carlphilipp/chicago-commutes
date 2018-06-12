package fr.cph.chicago.core.listener

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.TrainMapActivity
import fr.cph.chicago.core.adapter.PopupFavoritesTrainAdapter
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.util.Util

class TrainMapButtonOnClickListener(private val activity: Activity, private val trainLines: Set<TrainLine>) : View.OnClickListener {

    private val util = Util

    override fun onClick(v: View) {
        if (!util.isNetworkAvailable()) {
            util.showNetworkErrorMessage(activity)
        } else {
            if (trainLines.size == 1) {
                startTrainMapActivity(trainLines.iterator().next())
            } else {
                val colors = mutableListOf<Int>()
                val values = trainLines
                    .flatMap { line ->
                        val color = if (line !== TrainLine.YELLOW) line.color else ContextCompat.getColor(activity.applicationContext, R.color.yellowLine)
                        colors.add(color)
                        listOf(line.toStringWithLine())
                    }

                val ada = PopupFavoritesTrainAdapter(activity, values, colors)

                val lines = trainLines.toList()

                val builder = AlertDialog.Builder(activity)
                builder.setAdapter(ada) { _, position -> startTrainMapActivity(lines[position]) }

                val dialog = builder.create()
                dialog.show()
                if (dialog.window != null) {
                    dialog.window.setLayout((App.instance.screenWidth * 0.7).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
                }
            }
        }
    }

    private fun startTrainMapActivity(trainLine: TrainLine) {
        val extras = Bundle()
        val intent = Intent(activity.applicationContext, TrainMapActivity::class.java)
        extras.putString(App.instance.getString(R.string.bundle_train_line), trainLine.toTextString())
        intent.putExtras(extras)
        activity.startActivity(intent)
    }
}
