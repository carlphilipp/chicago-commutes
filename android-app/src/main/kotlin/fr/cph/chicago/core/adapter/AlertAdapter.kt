package fr.cph.chicago.core.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import fr.cph.chicago.R
import fr.cph.chicago.entity.dto.AlertType
import fr.cph.chicago.entity.dto.RoutesAlertsDTO

/**
 * Adapter that handle alert lists
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class AlertAdapter(private val routesAlertsDTOS: List<RoutesAlertsDTO>) : BaseAdapter() {

    override fun getCount(): Int {
        return routesAlertsDTOS.size
    }

    override fun getItem(position: Int): RoutesAlertsDTO {
        return routesAlertsDTOS[position]
    }

    override fun getItemId(position: Int): Long {
        return 0L
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val vi = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = vi.inflate(R.layout.list_alerts, parent, false)
        val item = getItem(position)

        Log.i("DERP", item.routeName + " " + item.routeBackgroundColor)

        val color: LinearLayout = view.findViewById(R.id.station_color_value)
        color.setBackgroundColor(
            if (item.alertType == AlertType.TRAIN)
                Color.parseColor(item.routeBackgroundColor)
            else
                Color.GRAY
        )

        val stationName: TextView = view.findViewById(R.id.station_name_value)
        stationName.text =
            if (item.alertType === AlertType.TRAIN)
                item.routeName
            else
                item.id + " - " + item.routeName

        val status: TextView = view.findViewById(R.id.status)
        status.text = item.routeStatus
        return view
    }
}
