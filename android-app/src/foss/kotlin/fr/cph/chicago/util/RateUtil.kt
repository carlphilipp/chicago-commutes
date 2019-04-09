package fr.cph.chicago.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.service.PreferenceService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.Date
import java.util.concurrent.TimeUnit

object RateUtil {

    private val TAG = RateUtil::class.java.simpleName

    fun rateThisApp(activity: Activity) {
        val intent = Intent(Intent.ACTION_VIEW)
        try {
            intent.data = Uri.parse("market://details?id=fr.cph.chicago.foss")
            activity.startActivity(intent)
        } catch (ex: Exception) {
            AlertDialog.Builder(activity)
                .setMessage("F-Droid Store not found on your device")
                .setPositiveButton(android.R.string.yes) { _, _ -> }
                .show()
        }
    }

    fun displayRateSnackBarIfNeeded(view: View, activity: Activity) {
        // no-op
    }
}
