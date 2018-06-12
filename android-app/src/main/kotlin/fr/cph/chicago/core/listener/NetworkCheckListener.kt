package fr.cph.chicago.core.listener

import android.app.Activity
import android.view.View
import fr.cph.chicago.util.Util

abstract class NetworkCheckListener(private val activity: Activity) : View.OnClickListener {

    protected val util = Util

    override fun onClick(v: View) {
        if (!util.isNetworkAvailable()) {
            util.showNetworkErrorMessage(activity)
        } else onClick()
    }

    abstract fun onClick()
}
