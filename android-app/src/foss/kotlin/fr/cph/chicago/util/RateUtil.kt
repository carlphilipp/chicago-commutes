package fr.cph.chicago.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object RateUtil {

    fun rateThisApp(activity: Activity) {
        val intent = Intent(Intent.ACTION_VIEW)
        try {
            intent.data = Uri.parse("market://details?id=fr.cph.chicago.foss")
            activity.startActivity(intent)
        } catch (ex: Exception) {
            MaterialAlertDialogBuilder(activity)
                .setMessage("F-Droid Store not found on your device")
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .show()
        }
    }

    fun displayRateSnackBarIfNeeded(view: View, activity: Activity) {
        // no-op
    }
}
