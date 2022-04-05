package fr.cph.chicago.core.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import fr.cph.chicago.R
import fr.cph.chicago.core.model.BusDirections
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.navigation.LocalNavController
import fr.cph.chicago.core.ui.screen.Screen
import fr.cph.chicago.core.viewmodel.MainViewModel
import fr.cph.chicago.service.BusService
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
private fun LoadingBottomSheet(
    isError: Boolean,
    isLoading: Boolean,
    title: String,
    content: @Composable () -> Unit
) {
    val sheetContent: @Composable () -> Unit = when {
        isError -> {{ Text(modifier = Modifier.padding(bottom = 15.dp), text = "Could not load directions!") }}
        isLoading -> {{
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp),
            ) {
                CircularProgressIndicator()
            }
        }}
        else -> content
    }
    BottomSheet(title = title, content = sheetContent)
}

@Composable
private fun BottomSheet(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.4f))
            )
        }
        TitleBottomSheet(
            modifier = Modifier.padding(bottom = 15.dp),
            title = title
        )
        content()
        NavigationBarsSpacer()
    }
}

@Composable
private fun TitleBottomSheet(
    modifier: Modifier = Modifier,
    title: String
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ShowMapMultipleTrainLinesBottomView(
    trainStation: TrainStation,
    mainViewModel: MainViewModel,
) {
    val scope = rememberCoroutineScope()
    val navController = LocalNavController.current

    BottomSheet(
        title = stringResource(id = R.string.train_choose_line),
        content = {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                trainStation.lines.forEachIndexed() { index, trainLine ->
                    val modifier = if (index == trainStation.lines.size - 1) {
                        Modifier.fillMaxWidth()
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp)
                    }
                    OutlinedButton(
                        modifier = modifier,
                        onClick = {
                            scope.launch {
                                mainViewModel.uiState.favModalBottomSheetState.hide()
                                while (mainViewModel.uiState.favModalBottomSheetState.isAnimationRunning) {
                                    // wait. Is that actually ok to do that?
                                }
                                navController.navigate(Screen.TrainMap, mapOf("line" to trainLine.toTextString()))
                            }
                        },
                    ) {
                        Icon(
                            modifier = Modifier.padding(end = 10.dp),
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = trainLine.color,
                        )
                        Text(
                            text = trainLine.toStringWithLine(),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

            }
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ShowBusBoundBottomView(
    busRoute: BusRoute,
    mainViewModel: MainViewModel,
    busService: BusService = BusService,
) {
    Timber.d("Compose ShowBusBoundBottomView")
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var foundBusDirections by remember { mutableStateOf(BusDirections("")) }
    var isError by remember { mutableStateOf(false) }
    val navController = LocalNavController.current

    LaunchedEffect(mainViewModel.uiState.busModalBottomSheetState.currentValue) {
        if (mainViewModel.uiState.busModalBottomSheetState.currentValue == ModalBottomSheetValue.Hidden) {
            isLoading = true
            isError = false
            foundBusDirections = BusDirections("")
        }
    }

    LaunchedEffect(key1 = busRoute, block = {
        scope.launch {
            busService.loadBusDirectionsSingle(busRoute.id)
                .subscribe(
                    { busDirections ->
                        foundBusDirections = busDirections
                        isLoading = false
                        isError = false
                    },
                    { error ->
                        Timber.e(error, "Could not load bus directions")
                        isLoading = false
                        isError = true
                    }
                )
        }
    })

    LoadingBottomSheet(
        isError = isError,
        isLoading = isLoading,
        title = "${busRoute.id} - ${busRoute.name}",
        content = {
            foundBusDirections.busDirections.forEachIndexed { index, busDirection ->
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val lBusDirections = foundBusDirections.busDirections
                        scope.launch {
                            mainViewModel.uiState.busModalBottomSheetState.hide()
                            while (mainViewModel.uiState.busModalBottomSheetState.isAnimationRunning) {
                                // wait. Is that actually ok to do that?
                            }
                            navController.navigate(
                                screen = Screen.BusBound,
                                arguments = mapOf(
                                    "busRouteId" to busRoute.id,
                                    "busRouteName" to busRoute.name,
                                    "bound" to lBusDirections[index].text,
                                    "boundTitle" to lBusDirections[index].text
                                ),
                            )
                        }
                    },
                ) {
                    Text(
                        text = busDirection.text,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    )
}
