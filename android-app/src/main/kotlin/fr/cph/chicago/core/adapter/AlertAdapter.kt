package fr.cph.chicago.core.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import fr.cph.chicago.R
import fr.cph.chicago.entity.dto.AlertType
import fr.cph.chicago.entity.dto.RouteAlertsDTO

/**
 * Adapter that will handle Train station list
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class AlertAdapter(private val routeAlertsDTOS: List<RouteAlertsDTO>) : BaseAdapter() {

    override fun getCount(): Int {
        return routeAlertsDTOS.size - 1
    }

    override fun getItem(position: Int): RouteAlertsDTO {
        return routeAlertsDTOS[position]
    }

    override fun getItemId(position: Int): Long {
        return 0L
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val vi = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = vi.inflate(R.layout.list_train_line, parent, false)

        val color: LinearLayout = view.findViewById(R.id.station_color_value)
        color.setBackgroundColor(Color.parseColor(getItem(position).routeBackgroundColor))

        val stationName: TextView = view.findViewById(R.id.station_name_value)
        val item = getItem(position)
        if (item.alertType === AlertType.TRAIN) {
            stationName.text = item.routeName + " - " + item.routeStatus
        } else {
            stationName.text = item.id + " - " + item.routeStatus
        }

        return view
    }
}
