package fr.cph.chicago.rx

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.mapbox.geojson.Feature
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.map.PROPERTY_DESTINATION
import fr.cph.chicago.util.Util
import java.lang.ref.WeakReference

abstract class AConsumer {

    protected fun addParams(container: RelativeLayout, size: Int) {
        val params = container.layoutParams
        params.width = Util.convertDpToPixel(200)
        params.height = (Util.convertDpToPixel(21) * size) + Util.convertDpToPixel(30)
        container.layoutParams = params
    }

    // FIXME: duplicated code
    protected fun createView(feature: Feature, activity: WeakReference<out Activity>): View {
        val inflater = LayoutInflater.from(activity.get())
        val view = inflater.inflate(R.layout.marker_mapbox, null)
        val destination = feature.getStringProperty(PROPERTY_DESTINATION)
        val title = view.findViewById(R.id.title) as TextView
        title.text = destination
        return view
    }

}
