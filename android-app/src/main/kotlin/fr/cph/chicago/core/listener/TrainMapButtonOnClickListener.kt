package fr.cph.chicago.core.listener

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.map.TrainMapActivity
import fr.cph.chicago.core.adapter.PopupFavoritesTrainAdapter
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.util.Color

class TrainMapButtonOnClickListener(private val context: Context, private val trainLines: Set<TrainLine>) : View.OnClickListener {

    override fun onClick(v: View?) {
        if (trainLines.size == 1) {
            startTrainMapActivity(trainLines.iterator().next())
        } else {
            val colors = mutableListOf<Int>()
            val values = trainLines
                .flatMap { line ->
                    val color = if (line !== TrainLine.YELLOW) line.color else Color.yellowLine
                    colors.add(color)
                    listOf(line.toStringWithLine())
                }

            val ada = PopupFavoritesTrainAdapter(context, values, colors)

            val lines = trainLines.toList()

            val builder = AlertDialog.Builder(context)
            builder.setAdapter(ada) { _, position -> startTrainMapActivity(lines[position]) }

            val dialog = builder.create()
            dialog.show()
            dialog.window?.setLayout((App.instance.screenWidth * 0.7).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)

        }
    }

    private fun startTrainMapActivity(trainLine: TrainLine) {
        val extras = Bundle()
        val intent = Intent(context.applicationContext, TrainMapActivity::class.java)
        extras.putString(App.instance.getString(R.string.bundle_train_line), trainLine.toTextString())
        intent.putExtras(extras)
        context.startActivity(intent)
    }
}
