package fr.cph.chicago.core.composable.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import fr.cph.chicago.core.composable.MainViewModel
import fr.cph.chicago.util.GoogleMapUtil

@Composable
fun Nearby(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel) {

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(GoogleMapUtil.chicago, 14f)
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
    ) {
        Marker(
            position = GoogleMapUtil.chicago,
            title = "Chicago",
            snippet = "Marker in Chicago"
        )
    }
}
