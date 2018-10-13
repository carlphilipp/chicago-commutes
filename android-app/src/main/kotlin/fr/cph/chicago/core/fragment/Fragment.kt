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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import butterknife.Unbinder
import fr.cph.chicago.core.activity.MainActivity

abstract class Fragment(private val layout: Int) : android.support.v4.app.Fragment() {

    protected lateinit var mainActivity: MainActivity
    protected lateinit var rootView: View
    private lateinit var unbinder: Unbinder

    private fun setBinder(rootView: View) {
        unbinder = ButterKnife.bind(this, rootView)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbinder.unbind()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (!mainActivity.isFinishing) {
            rootView = inflater.inflate(layout, container, false)
            setBinder(rootView)
            onCreateView(savedInstanceState)
        }
        return rootView
    }

    abstract fun onCreateView(savedInstanceState: Bundle?)

    companion object {

        private const val ARG_SECTION_NUMBER = "section_number"

        fun fragmentWithBundle(fragment: Fragment, sectionNumber: Int): Fragment {
            val args = Bundle()
            args.putInt(ARG_SECTION_NUMBER, sectionNumber)
            fragment.arguments = args
            return fragment
        }
    }
}
