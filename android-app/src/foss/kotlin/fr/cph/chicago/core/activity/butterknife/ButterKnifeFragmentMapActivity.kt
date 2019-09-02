/**
 * Copyright 2019 Carl-Philipp Harmant
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

package fr.cph.chicago.core.activity.butterknife

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
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
            mapView?.onCreate(savedInstanceState)
            create(savedInstanceState)
        }
    }

    abstract fun create(savedInstanceState: Bundle?)
}
