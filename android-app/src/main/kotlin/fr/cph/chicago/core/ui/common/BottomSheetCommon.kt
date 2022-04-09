package fr.cph.chicago.core.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.cph.chicago.R
import fr.cph.chicago.core.model.BusDirections
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.dto.BusDetailsDTO
import fr.cph.chicago.core.navigation.LocalNavController
import fr.cph.chicago.core.theme.FontSize
import fr.cph.chicago.core.theme.availableFonts
import fr.cph.chicago.core.ui.screen.Screen
import fr.cph.chicago.core.ui.screen.settings.SettingsViewModel
import fr.cph.chicago.core.viewmodel.MainViewModel
import fr.cph.chicago.service.BusService
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
private fun LoadingBottomSheet(
    isError: Boolean,
    isLoading: Boolean,
    title: String,
    onBackClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val sheetContent: @Composable () -> Unit = when {
        isError -> {
            { Text(modifier = Modifier.padding(bottom = 15.dp), text = "Could not load directions!") }
        }
        isLoading -> {
            {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 15.dp),
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        else -> content
    }
    BottomSheet(title = title, content = sheetContent, onBackClick = onBackClick)
}

@Composable
fun BottomSheet(
    title: String,
    content: @Composable () -> Unit,
    onBackClick: () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
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
    androidx.activity.compose.BackHandler {
        Timber.e("*********************** BACK")
        onBackClick()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontTypefaceBottomView(
    title: String,
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
) {
    BottomSheet(
        title = title,
        content = {
            val fontSelected = remember { mutableStateOf(viewModel.uiState.fontTypeFace) }
            availableFonts.forEach {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            fontSelected.value = it.key
                            viewModel.setFontTypeFace(fontSelected.value)
                        },
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = it.key == fontSelected.value,
                        onClick = {
                            fontSelected.value = it.key
                            viewModel.setFontTypeFace(fontSelected.value)
                        })
                    Text(
                        text = it.key,
                        fontFamily = it.value.toFontFamily(),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontSizeBottomView(
    title: String,
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
) {
    val fontSelected = remember { mutableStateOf(viewModel.uiState.fontSize) }
    BottomSheet(
        title = title,
        content = {
            FontSize.values().forEach {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            fontSelected.value = it
                            viewModel.setFontSize(fontSelected.value)
                        },
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = it == fontSelected.value,
                        onClick = {
                            fontSelected.value = it
                            viewModel.setFontSize(fontSelected.value)
                        })
                    Text(
                        text = it.description,
                        fontSize = (20 + it.offset).sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimationSpeedBottomView(
    title: String,
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
) {
    val speedSelected = remember { mutableStateOf(viewModel.uiState.animationSpeed) }
    BottomSheet(
        title = title,
        content = {
            AnimationSpeed.allAnimationsSpeed().forEach {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            speedSelected.value = it
                            viewModel.setAnimationSpeed(speedSelected.value)
                        },
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = it == speedSelected.value,
                        onClick = {
                            speedSelected.value = it
                            viewModel.setAnimationSpeed(speedSelected.value)
                        })
                    Text(
                        text = it.name,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Composable
fun ShowBusDetailsBottomView(
    mainViewModel: MainViewModel,
    busDetailsDTOs: List<BusDetailsDTO>,
    onBackClick: () -> Unit,
) {
    Timber.d("Compose ShowBusDetailsBottomView")
    val scope = rememberCoroutineScope()
    val navController = LocalNavController.current
    val keyboardController = LocalSoftwareKeyboardController.current

    BottomSheet(
        title = stringResource(id = R.string.bus_choose_stop),
        content = {
            busDetailsDTOs.forEachIndexed { index, busDetailsDTO ->
                val modifier = if (index == busDetailsDTOs.size - 1) {
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
                            navController.navigate(
                                screen = Screen.BusDetails,
                                arguments = mapOf(
                                    "busStopId" to busDetailsDTO.stopId.toString(),
                                    "busStopName" to busDetailsDTO.stopName,
                                    "busRouteId" to busDetailsDTO.busRouteId,
                                    "busRouteName" to busDetailsDTO.routeName,
                                    "bound" to busDetailsDTO.bound,
                                    "boundTitle" to busDetailsDTO.boundTitle,
                                ),
                                closeKeyboard = {
                                    keyboardController?.hide()
                                }
                            )
                        }
                    },
                ) {
                    Text(
                        text = "${busDetailsDTO.stopName} (${busDetailsDTO.bound})",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ShowMapMultipleTrainLinesBottomView(
    trainStation: TrainStation,
    mainViewModel: MainViewModel,
    onBackClick: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val navController = LocalNavController.current

    BottomSheet(
        title = stringResource(id = R.string.train_choose_line),
        content = {
            val lines = trainStation.lines
            val modifier = Modifier
                .padding(bottom = 20.dp)
                .fillMaxWidth()
            if (lines.size <= 3) {
                Row(
                    modifier = modifier,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    trainStation.lines.forEach { trainLine ->
                        TrainLineButton(
                            trainLine = trainLine,
                            onClick = {
                                scope.launch {
                                    mainViewModel.uiState.favModalBottomSheetState.hide()
                                    while (mainViewModel.uiState.favModalBottomSheetState.isAnimationRunning) {
                                        // wait. Is that actually ok to do that?
                                    }
                                    navController.navigate(Screen.TrainMap, mapOf("line" to trainLine.toTextString()))
                                }
                            }
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    modifier = modifier,
                    columns = GridCells.Adaptive(minSize = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(trainStation.lines.toList()) { trainLine ->
                        TrainLineButton(
                            trainLine = trainLine,
                            onClick = {
                                scope.launch {
                                    mainViewModel.uiState.favModalBottomSheetState.hide()
                                    while (mainViewModel.uiState.favModalBottomSheetState.isAnimationRunning) {
                                        // wait. Is that actually ok to do that?
                                    }
                                    navController.navigate(Screen.TrainMap, mapOf("line" to trainLine.toTextString()))
                                }
                            }
                        )
                    }
                }
            }
        },
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ShowBusBoundBottomView(
    busRoute: BusRoute,
    mainViewModel: MainViewModel,
    busService: BusService = BusService,
    onBackClick: () -> Unit,
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
        },
        onBackClick = onBackClick
    )
}
