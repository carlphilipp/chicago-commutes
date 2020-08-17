/**
 * Copyright 2020 Carl-Philipp Harmant
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

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import fr.cph.chicago.R
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_cta_map.ctaMap
import timber.log.Timber

class CtaMapFragment : Fragment(R.layout.fragment_cta_map) {

    companion object {
        fun newInstance(sectionNumber: Int): CtaMapFragment {
            return fragmentWithBundle(CtaMapFragment(), sectionNumber) as CtaMapFragment
        }
    }

    private var bitmapCache: Bitmap? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loadBitmap()
    }

    @SuppressLint("CheckResult")
    private fun loadBitmap() {
        Observable.fromCallable {
            if (bitmapCache != null) bitmapCache
            else BitmapFactory.decodeResource(resources, R.drawable.ctamap)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { bitmap ->
                    this@CtaMapFragment.bitmapCache = bitmap
                    ctaMap.setImageBitmap(bitmap)
                },
                { error -> Timber.e(error) })
    }
}
