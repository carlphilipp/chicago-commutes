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

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import com.github.chrisbanes.photoview.PhotoView
import fr.cph.chicago.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CtaMapFragment : AbstractFragment() {

    @BindView(R.id.cta_map)
    lateinit var ctaMap: PhotoView
    private var bitmapCache: Bitmap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_cta_map, container, false)
        if (!mainActivity.isFinishing) {
            setBinder(rootView)
            loadBitmap()
        }
        return rootView
    }

    private fun loadBitmap() {
        Observable.fromCallable {
            if (bitmapCache != null) bitmapCache
            else BitmapFactory.decodeResource(resources, R.drawable.ctamap)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { bitmap ->
                this@CtaMapFragment.bitmapCache = bitmap
                ctaMap.setImageBitmap(bitmap)
            }
    }

    companion object {
        fun newInstance(sectionNumber: Int): CtaMapFragment {
            return AbstractFragment.Companion.fragmentWithBundle(CtaMapFragment(), sectionNumber) as CtaMapFragment
        }
    }
}
