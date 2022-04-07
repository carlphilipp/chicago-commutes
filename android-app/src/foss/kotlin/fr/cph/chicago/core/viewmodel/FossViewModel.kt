package fr.cph.chicago.core.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material.ExperimentalMaterialApi
import androidx.core.content.ContextCompat
import timber.log.Timber

val mainViewModel = FossMainViewModel()

class FossMainViewModel() : MainViewModel() {
    @OptIn(ExperimentalMaterialApi::class)
    override fun startMarket(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW)
        try {
            intent.data = Uri.parse("market://details?id=fr.cph.chicago.foss")
            ContextCompat.startActivity(context, intent, null)
        } catch (ex: Exception) {
            Timber.e(ex, "Could not start market")
            uiState = uiState.copy(
                startMarket = false,
                startMarketFailed = true
            )
        }
    }
}
