package fr.cph.chicago.core.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.service.TrainService
import javax.inject.Inject

@Composable
fun TrainMapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapTrainViewModel,
    navigationViewModel: NavigationViewModel,
    title: String,
) {

}

@HiltViewModel
class MapTrainViewModel @Inject constructor(
    line: TrainLine,
    private val trainService: TrainService = TrainService,
) : ViewModel() {

}
