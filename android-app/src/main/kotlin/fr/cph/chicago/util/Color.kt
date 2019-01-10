package fr.cph.chicago.util

import android.support.v4.content.ContextCompat
import fr.cph.chicago.R
import fr.cph.chicago.core.App

object Color {

    val grey: Int by lazy {
        ContextCompat.getColor(App.instance, R.color.grey)
    }

    val grey5: Int by lazy {
        ContextCompat.getColor(App.instance, R.color.grey_5)
    }

    val red: Int by lazy {
        ContextCompat.getColor(App.instance, R.color.red)
    }

    val green: Int by lazy {
        ContextCompat.getColor(App.instance, R.color.green)
    }

    val orange: Int by lazy {
        ContextCompat.getColor(App.instance, R.color.orange)
    }

    val white: Int by lazy {
        ContextCompat.getColor(App.instance, R.color.white)
    }

    val yellowLine: Int by lazy {
        ContextCompat.getColor(App.instance, R.color.yellowLine)
    }

    val yellowLineDark: Int by lazy {
        ContextCompat.getColor(App.instance, R.color.yellowLineDark)
    }
}
