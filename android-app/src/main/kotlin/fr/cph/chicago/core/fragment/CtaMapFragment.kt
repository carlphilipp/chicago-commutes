/**
 * Copyright 2021 Carl-Philipp Harmant
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fr.cph.chicago.R
import fr.cph.chicago.databinding.FragmentCtaMapBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class CtaMapFragment : Fragment() {

    companion object {
        fun newInstance(sectionNumber: Int): CtaMapFragment {
            return fragmentWithBundle(CtaMapFragment(), sectionNumber) as CtaMapFragment
        }
    }

    private var bitmapCache: Bitmap? = null

    private var _binding: FragmentCtaMapBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentCtaMapBinding.inflate(inflater, container, false)
        return binding.root
    }

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
                    binding.ctaMap.setImageBitmap(bitmap)
                },
                { error -> Timber.e(error) })
    }
}
