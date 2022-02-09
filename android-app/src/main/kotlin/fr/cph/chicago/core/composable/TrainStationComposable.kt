package fr.cph.chicago.core.composable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.cph.chicago.R
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.redux.State
import java.math.BigInteger
import org.rekotlin.StoreSubscriber
import timber.log.Timber


class TrainStationComposable : ComponentActivity(), StoreSubscriber<State> {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val stationId = BigInteger(intent.extras?.getString(getString(R.string.bundle_train_stationId), "0")!!)
        setContent {
            ChicagoCommutesTheme {
                TrainStationView(stationId)
            }
        }
    }
    override fun newState(state: State) {
        Timber.i("new state")
    }
}

@Composable
fun TrainStationView(stationId: BigInteger) {
    Column {
        TopBar("$stationId")
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Train station $stationId")
        }
    }
}
