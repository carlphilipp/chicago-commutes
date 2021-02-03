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

package fr.cph.chicago.util

import android.view.View
import com.google.android.material.snackbar.Snackbar
import java.util.Date

/**
 * Object that save in memory all the snackbar that need to be shown. If there is too many at the same time, we just show one.
 */
object SnackBarUtil {

    private val timeUtil = TimeUtil
    private val snackbars = mutableListOf<Pair<Date, Snackbar>>()

    private val callback: Snackbar.Callback = object : Snackbar.Callback() {
        override fun onDismissed(snackbar: Snackbar?, event: Int) {
            snackbars
                .find { pair -> pair.second == snackbar }
                ?.let { pair ->
                    snackbars.remove(pair)
                    snackbars.toList()
                        .filter { p -> timeUtil.isLessThan2SecondDifference(pair.first, p.first) }
                        .let { result -> snackbars.removeAll(result) }
                    if (snackbars.size > 0) {
                        displaySnackBar(snackbars[0].second)
                    }
                }
        }
    }

    private fun displaySnackBar(snackbar: Snackbar) {
        snackbar.show()
    }

    fun smartShow(view: View, text: CharSequence) {
        val snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG)
        snackbar.addCallback(callback)
        val first = snackbars.size == 0
        snackbars.add(Pair(Date(), snackbar))
        if (first) {
            displaySnackBar(snackbar)
        }
    }
}
