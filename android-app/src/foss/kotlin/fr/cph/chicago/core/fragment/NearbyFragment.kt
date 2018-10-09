/**
 * Copyright 2018 Carl-Philipp Harmant
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

package fr.cph.chicago.core.fragment

import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import butterknife.BindView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import fr.cph.chicago.R
import fr.cph.chicago.core.adapter.SlidingUpAdapter
import pub.devrel.easypermissions.EasyPermissions

class NearbyFragment : Fragment(R.layout.fragment_nearby), EasyPermissions.PermissionCallbacks {

    @BindView(R.id.activity_bar)
    lateinit var progressBar: ProgressBar
    @BindView(R.id.sliding_layout)
    lateinit var slidingUpPanelLayout: SlidingUpPanelLayout
    @BindView(R.id.loading_layout_container)
    lateinit var layoutContainer: LinearLayout

    lateinit var slidingUpAdapter: SlidingUpAdapter

    override fun onCreateView() {
        slidingUpAdapter = SlidingUpAdapter(this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    fun showProgress(show: Boolean) {
        if (isAdded) {
            if (show) {
                progressBar.visibility = View.VISIBLE
                progressBar.progress = 50
            } else {
                progressBar.visibility = View.GONE
            }
        }
    }

    companion object {

        private val TAG = NearbyFragment::class.java.simpleName

        fun newInstance(sectionNumber: Int): NearbyFragment {
            return Fragment.fragmentWithBundle(NearbyFragment(), sectionNumber) as NearbyFragment
        }
    }
}
