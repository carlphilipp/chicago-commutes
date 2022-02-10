package fr.cph.chicago.core.composable

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import fr.cph.chicago.R
import fr.cph.chicago.client.GoogleStreetClient
import fr.cph.chicago.core.App
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.redux.State
import fr.cph.chicago.service.TrainService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import org.rekotlin.StoreSubscriber
import timber.log.Timber
import java.math.BigInteger

private val googleStreetClient = GoogleStreetClient

class TrainStationComposable : ComponentActivity(), StoreSubscriber<State> {
    private var googleStreetMapImage = mutableStateOf<Drawable>(ShapeDrawable())
    private var showGoogleStreetImage = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        googleStreetMapImage.value = AppCompatResources.getDrawable(App.instance.applicationContext, R.drawable.placeholder_street_view)!!
        val stationId = BigInteger(intent.extras?.getString(getString(R.string.bundle_train_stationId), "0")!!)
        val trainStation = TrainService.getStation(stationId)
        val position = trainStation.stops[0].position
        loadGoogleStreetImage(position)
        setContent {
            ChicagoCommutesTheme {
                TrainStationView(
                    trainStation = trainStation,
                    googleStreetMapImage = googleStreetMapImage.value,
                    showGoogleStreetImage = showGoogleStreetImage.value)
            }
        }
    }

    override fun newState(state: State) {
        Timber.i("new state")
    }

    @SuppressLint("CheckResult")
    fun loadGoogleStreetImage(position: Position) {
        googleStreetClient.getImage(position.latitude, position.longitude, 1000, 400)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { drawable ->
                    googleStreetMapImage.value = drawable
                    showGoogleStreetImage.value = true
                },
                { error ->
                    Timber.e(error, "Error while loading street view image")
                }
            )
    }
}

@Composable
fun TrainStationView(modifier: Modifier = Modifier, trainStation: TrainStation, googleStreetMapImage: Drawable, showGoogleStreetImage: Boolean) {
    val activity = (LocalLifecycleOwner.current as ComponentActivity)
    Column {
        Surface {
            AnimatedVisibility(
                visible = showGoogleStreetImage,
                enter = fadeIn(animationSpec = tween(durationMillis = 1500)),
            ) {
                Image(
                    bitmap = googleStreetMapImage.toBitmap().asImageBitmap(),
                    contentDescription = "Google image street view",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            AnimatedVisibility(
                visible = !showGoogleStreetImage,
                exit = fadeOut(animationSpec = tween(durationMillis = 300)),
            ) {
                Image(
                    bitmap = AppCompatResources.getDrawable(App.instance.applicationContext, R.drawable.placeholder_street_view)!!.toBitmap().asImageBitmap(),
                    contentDescription = "Placeholder",
                    //contentScale = ContentScale.Crop,
                    //modifier = Modifier.fillMaxWidth().height(400.dp),
                )
            }
            FilledTonalButton(
                modifier = Modifier.padding(10.dp),
                onClick = { activity.finish() },
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
        }
        Text(text = "Train station ${trainStation.id}")
    }
}
