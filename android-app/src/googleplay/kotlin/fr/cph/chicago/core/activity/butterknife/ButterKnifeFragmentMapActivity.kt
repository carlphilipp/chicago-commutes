package fr.cph.chicago.core.activity.butterknife

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import butterknife.ButterKnife
import com.google.android.gms.maps.MapsInitializer
import fr.cph.chicago.R

abstract class ButterKnifeFragmentMapActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this.isFinishing) {
            MapsInitializer.initialize(applicationContext)
            setContentView(R.layout.activity_map)
            ButterKnife.bind(this)
            create(savedInstanceState)
        }
    }

    abstract fun create(savedInstanceState: Bundle?)
}
