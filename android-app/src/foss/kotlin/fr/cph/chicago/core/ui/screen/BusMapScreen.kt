package fr.cph.chicago.core.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.service.BusService
import javax.inject.Inject

@Composable
fun BusMapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapBusViewModel,
    navigationViewModel: NavigationViewModel,
    title: String,
) {
}


@HiltViewModel
class MapBusViewModel @Inject constructor(
    busRouteId: String,
    private val busService: BusService = BusService,
) : ViewModel() {

}
