package fr.cph.chicago.core.activity.butterknife

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import butterknife.BindView
import butterknife.ButterKnife
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import fr.cph.chicago.R

abstract class ButterKnifeFragmentMapActivity : FragmentActivity() {

    @BindView(R.id.mapView)
    @JvmField
    protected var mapView: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this.isFinishing) {
            Mapbox.getInstance(this, getString(R.string.mapbox_token))
            setContentView(R.layout.activity_map_mapbox)
            ButterKnife.bind(this)
            mapView!!.onCreate(savedInstanceState)
            create(savedInstanceState)
        }
    }

    abstract fun create(savedInstanceState: Bundle?)
}
