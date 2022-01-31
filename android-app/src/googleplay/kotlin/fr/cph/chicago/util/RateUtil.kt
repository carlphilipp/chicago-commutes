package fr.cph.chicago.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.service.PreferenceService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.util.Date
import java.util.concurrent.TimeUnit

object RateUtil {

    private val util = Util
    private val preferenceService = PreferenceService

    fun rateThisApp(activity: Activity) {
        val intent = Intent(Intent.ACTION_VIEW)
        try {
            intent.data = Uri.parse("market://details?id=fr.cph.chicago")
            activity.startActivity(intent)
        } catch (ex: Exception) {
            MaterialAlertDialogBuilder(activity)
                .setMessage("Play Store not found on your device")
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .show()
        }
    }

    @SuppressLint("CheckResult")
    fun displayRateSnackBarIfNeeded(view: View, activity: Activity) {
        Observable.fromCallable { preferenceService.getRateLastSeen() }
            .delay(2L, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { lastSeen ->
                    val now = Date()
                    // if it has been more than 30 days or if it's the first time
                    if (now.time - lastSeen.time > 2592000000L) {
                        showRateSnackBar(view, activity)
                        preferenceService.setRateLastSeen()
                    }
                },
                { error -> Timber.e(error) })
    }

    private fun showRateSnackBar(view: View, activity: Activity) {
        val background = util.getAttribute(view.context, R.attr.colorAccent)
        val textColor = ContextCompat.getColor(App.instance, R.color.greenLineDark)
        val snackBar1 = Snackbar.make(view, "Do you like this app?", Snackbar.LENGTH_LONG)
            .setAction("YES") { view1 ->
                val snackBar2 = Snackbar.make(view1, "Rate this app on the market", Snackbar.LENGTH_LONG)
                    .setAction("OK") { rateThisApp(activity) }
                    .setActionTextColor(textColor)
                    .setDuration(10000)
                snackBar2.view.setBackgroundColor(background)
                snackBar2.show()
            }
            .setActionTextColor(textColor)
            .setDuration(10000)
        snackBar1.view.setBackgroundColor(background)
        snackBar1.show()
    }
}
