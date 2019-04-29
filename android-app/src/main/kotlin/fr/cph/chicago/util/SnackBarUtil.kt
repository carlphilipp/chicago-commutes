package fr.cph.chicago.util

import android.view.View
import com.google.android.material.snackbar.Snackbar
import java.util.Date

/**
 * Object that save in memory all the snackbar that need to be shown. If there is too many at the same time, we just show one.
 */
object SnackBarUtil {

    private val snackbars = mutableListOf<Pair<Date, Snackbar>>()

    private val callback: Snackbar.Callback = object : Snackbar.Callback() {
        override fun onDismissed(snackbar: Snackbar?, event: Int) {
            snackbars
                .find { pair -> pair.second == snackbar }
                ?.let { pair ->
                    snackbars.remove(pair)
                    snackbars.toList()
                        .filter { p -> TimeUtil.isLessThan2SecondDifference(pair.first, p.first) }
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
