package fr.cph.chicago.core.composable.map

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.R
import fr.cph.chicago.core.composable.TopBar
import fr.cph.chicago.core.composable.settingsViewModel
import fr.cph.chicago.core.composable.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.toComposeColor
import fr.cph.chicago.util.MapUtil.chicagoPosition
import fr.cph.chicago.util.toLatLng
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import org.apache.commons.lang3.StringUtils
import timber.log.Timber
import javax.inject.Inject

class TrainMapComposable : ComponentActivity() {

    private val snackbarHostState = mutableStateOf(SnackbarHostState())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val line = if (savedInstanceState != null)
            savedInstanceState.getString(getString(R.string.bundle_train_line)) ?: StringUtils.EMPTY
        else
            intent.getStringExtra(getString(R.string.bundle_train_line)) ?: StringUtils.EMPTY
        val trainLine = TrainLine.fromXmlString(line)
        val viewModel = GoogleMapTrainViewModel().initModel(line = trainLine)

        setContent {
            ChicagoCommutesTheme(settingsViewModel = settingsViewModel) {
                TrainMapView(
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState.value,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainMapView(
    modifier: Modifier = Modifier,
    viewModel: GoogleMapTrainViewModel,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopBar(viewModel.uiState.line.toStringWithLine()) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) { data -> Snackbar(snackbarData = data) } },
        content = {
            var isMapLoaded by remember { mutableStateOf(false) }
            GoogleMapTrainMapView(
                viewModel = viewModel,
                onMapLoaded = { isMapLoaded = true },
            )
            // FIXME: refactor that into a component (see Nearby.kt)
            if (!isMapLoaded) {
                AnimatedVisibility(
                    modifier = Modifier.fillMaxSize(),
                    visible = !isMapLoaded,
                    enter = EnterTransition.None,
                    exit = fadeOut()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .wrapContentSize()
                    )
                }
            }
        }
    )
}

@Composable
fun GoogleMapTrainMapView(
    modifier: Modifier = Modifier,
    viewModel: GoogleMapTrainViewModel,
    onMapLoaded: () -> Unit,
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        // position = CameraPosition.fromLatLngZoom(uiState.nearbyMapCenterLocation.toLatLng(), uiState.nearbyZoomIn)
    }
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapType = MapType.NORMAL, isMyLocationEnabled = false),
        uiSettings = MapUiSettings(compassEnabled = false, myLocationButtonEnabled = false),
        onMapLoaded = {
            onMapLoaded()
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(chicagoPosition.toLatLng(), 11f)
            cameraPositionState.move(cameraUpdate)
        },
        onMapClick = {
            //mainViewModel.setShowNearbyDetails(false)
        }
    ) {
        Polyline(
            points = uiState.points,
            color = uiState.line.toComposeColor(),
            width = 10f,
        )
    }
}

data class GoogleMapTrainUiState(
    val line: TrainLine = TrainLine.NA,
    val points: List<LatLng> = listOf()
)

@HiltViewModel
class GoogleMapTrainViewModel @Inject constructor(
    private val trainService: TrainService = TrainService,
) : ViewModel() {
    var uiState by mutableStateOf(GoogleMapTrainUiState())
        private set

    fun initModel(line: TrainLine): GoogleMapTrainViewModel {
        uiState = uiState.copy(line = line)
        loadPositions(line)
        return this
    }

    fun loadPositions(line: TrainLine) {
        val positionsSingle = trainService.readPatterns(line)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { trainStationPattern ->
                    Timber.i("Found ${trainStationPattern.size} patterns")
                    uiState = uiState.copy(
                        points = trainStationPattern.map { it.position.toLatLng() }
                    )

                },
                {
                    Timber.e(it, "Could not load train patterns")
                    // TODO handle exception
                }
            )
    }
}
