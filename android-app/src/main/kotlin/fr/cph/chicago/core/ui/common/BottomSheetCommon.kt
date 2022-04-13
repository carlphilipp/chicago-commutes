package fr.cph.chicago.core.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import fr.cph.chicago.R
import fr.cph.chicago.core.model.BusDirections
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.dto.BusDetailsDTO
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.core.navigation.LocalNavController
import fr.cph.chicago.core.theme.FontSize
import fr.cph.chicago.core.theme.availableFonts
import fr.cph.chicago.core.ui.screen.BottomSheetContentType
import fr.cph.chicago.core.ui.screen.MapTrainViewModel
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
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable () -> Unit,
    onBackClick: () -> Unit,
) {
    Column(modifier = modifier.padding(horizontal = 20.dp)) {
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
        if (title != null) {
            TitleBottomSheet(
                modifier = Modifier.padding(bottom = 15.dp),
                title = title
            )
        }
        content()
        NavigationBarsSpacer()
    }
    androidx.activity.compose.BackHandler {
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
                        fontSize = (15 + it.offset).sp,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainMapBottomSheet(
    modifier: Modifier = Modifier,
    viewModel: MapTrainViewModel,
    onBackClick: () -> Unit,
) {
    BottomSheet(
        content = {
            Column(modifier = modifier.fillMaxWidth()) {
                when (viewModel.uiState.bottomSheetContentType) {
                    BottomSheetContentType.CHANGE_LINE -> ChangeLineTrainMapBottomSheet(viewModel = viewModel)
                    else -> ShowTrainDetailsTrainMapBottomSheet(viewModel = viewModel)
                }
            }
        },
        onBackClick = onBackClick,
    )
}

@Composable
private fun ChangeLineTrainMapBottomSheet(
    modifier: Modifier = Modifier,
    viewModel: MapTrainViewModel,
) {
    val scope = rememberCoroutineScope()
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TrainLineButton(
            trainLine = viewModel.uiState.line,
            showLine = true,
        )
        FilledTonalButton(
            onClick = {
                scope.launch {
                    viewModel.reloadData()
                }
            }
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Back",
            )
        }
    }

    Divider(
        modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Change Line",
            style = MaterialTheme.typography.titleMedium,
        )
    }
    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        columns = GridCells.Adaptive(minSize = 90.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(TrainLine.values()
            .filter { it != TrainLine.NA && it != viewModel.uiState.line }
            .toList()
        ) { trainLine ->
            TrainLineButton(
                trainLine = trainLine,
                onClick = {
                    viewModel.switchTrainLine(scope, trainLine)
                }
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun ShowTrainDetailsTrainMapBottomSheet(
    modifier: Modifier = Modifier,
    viewModel: MapTrainViewModel,
) {
    val scope = rememberCoroutineScope()
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TrainLineStyleText(
            text = "${viewModel.uiState.line.toStringWithLine()} - To: ${viewModel.uiState.train.destName}",
            color = viewModel.uiState.line.color,
            textColor = viewModel.uiState.line.textColor,
        )
        FilledTonalButton(
            onClick = {
                scope.launch {
                    viewModel.resetDetails()
                }
            }
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = null,
            )
        }
    }

    Divider(
        modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
    )

    // This needs to be done because it looks like HorizontalPager does not refresh data properly
    // and it get confused when the state is accessed directly (and updated later)
    val arrivals = viewModel.uiState.trainEtas.ifEmpty {
        listOf(Pair(first = "No result", second = "##"))
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    ) {
        HorizontalPager(
            modifier = Modifier,
            count = arrivals.size,
            itemSpacing = 10.dp,
            contentPadding = PaddingValues(start = 0.dp, end = 250.dp),
        ) { page ->
            TrainStopArrivalTimeView(
                title = arrivals[page].first,
                minutes = arrivals[page].second,
            )
        }
        Box(
            modifier = Modifier
                .height(87.dp)
                .width(14.dp)
                .align(Alignment.CenterEnd)
                .clip(RoundedCornerShape(topStart = 5.dp, bottomStart = 5.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f))
        ) {
            Icon(
                modifier = Modifier.align(Alignment.Center),
                imageVector = Icons.Filled.ArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun TrainStopArrivalTimeView(
    modifier: Modifier = Modifier,
    title: String,
    minutes: String,
) {
    Column(
        modifier = modifier
            .clip(
                RoundedCornerShape(
                    topStartPercent = 20,
                    topEndPercent = 20,
                    bottomEndPercent = 20,
                    bottomStartPercent = 20
                )
            )
            .background(MaterialTheme.colorScheme.surfaceVariant),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp, bottom = 4.dp)
                    .clip(
                        RoundedCornerShape(
                            topStartPercent = 20,
                            topEndPercent = 20,
                            bottomEndPercent = 20,
                            bottomStartPercent = 20
                        )
                    )
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = minutes,
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "min",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
