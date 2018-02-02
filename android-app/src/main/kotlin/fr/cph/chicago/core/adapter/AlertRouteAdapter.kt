package fr.cph.chicago.core.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import fr.cph.chicago.R
import fr.cph.chicago.entity.dto.RouteAlertsDTO

/**
 * Adapter that handle alert lists
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class AlertRouteAdapter(private val routeAlertsDTOS: List<RouteAlertsDTO>) : BaseAdapter() {

    override fun getCount(): Int {
        return routeAlertsDTOS.size
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
        val view = vi.inflate(R.layout.list_alert_route, parent, false)
        val item = getItem(position)

        val headline: TextView = view.findViewById(R.id.headline)
        headline.text = item.headLine

        val description: TextView = view.findViewById(R.id.description)
        description.text = item.description

        val impact: TextView = view.findViewById(R.id.impact)
        impact.text = item.impact

        val start: TextView = view.findViewById(R.id.start)
        start.text = item.start.toString()

        return view
    }
}
