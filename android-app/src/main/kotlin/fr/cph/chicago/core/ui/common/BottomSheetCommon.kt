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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.cph.chicago.R
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.navigation.LocalNavController
import fr.cph.chicago.core.ui.screen.Screen
import fr.cph.chicago.core.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun BottomSheet(
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
                                mainViewModel.uiState.modalBottomSheetState.hide()
                                while (mainViewModel.uiState.modalBottomSheetState.isAnimationRunning) {
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
