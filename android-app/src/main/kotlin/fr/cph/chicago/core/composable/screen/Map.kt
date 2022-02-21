package fr.cph.chicago.core.composable.screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidViewBinding
import fr.cph.chicago.R
import fr.cph.chicago.databinding.FragmentCtaMapBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

@Composable
fun Map() {
    private var bitmapCache: Bitmap? = null

    /*Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ctamap),
            contentDescription = "CTA map"
        )
    }*/
    AndroidViewBinding(FragmentCtaMapBinding::inflate) {
        //exampleView.setBackgroundColor(Color.GRAY)
        Observable.fromCallable {
            if (bitmapCache != null) bitmapCache!!
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
