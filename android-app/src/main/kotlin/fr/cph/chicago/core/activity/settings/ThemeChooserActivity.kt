package fr.cph.chicago.core.activity.settings

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import fr.cph.chicago.core.activity.CustomComponentActivity
import fr.cph.chicago.core.model.Theme
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.core.theme.ChicagoCommutesTheme
import fr.cph.chicago.core.theme.blue_line
import fr.cph.chicago.core.theme.green_line
import fr.cph.chicago.core.theme.red_line
import fr.cph.chicago.core.theme.yellow_line
import fr.cph.chicago.core.ui.LargeTopBar
import fr.cph.chicago.core.ui.common.ChipMaterial3
import fr.cph.chicago.core.ui.common.ColoredBox
import fr.cph.chicago.core.ui.common.NavigationBarsSpacer
import fr.cph.chicago.core.ui.common.SwitchMaterial3
import fr.cph.chicago.core.ui.common.TextFieldMaterial3
import fr.cph.chicago.core.ui.common.ThemeColorButton
import fr.cph.chicago.core.ui.screen.SettingsViewModel
import fr.cph.chicago.core.viewmodel.settingsViewModel

class ThemeChooserActivity : CustomComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChicagoCommutesTheme(settingsViewModel = settingsViewModel) {
                ThemeChooserView(viewModel = settingsViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeChooserView(viewModel: SettingsViewModel) {

    val context = LocalContext.current as ComponentActivity
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = remember(decayAnimationSpec) {
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(decayAnimationSpec)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopBar(
                title = "Theme",
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = { context.finish() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            item {
                ElevatedCard(modifier = Modifier.padding(horizontal = 15.dp)) {
                    Column(modifier = Modifier.padding(15.dp)) {
                        Text(
                            modifier = Modifier,
                            text = "Using system theme",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.padding(10.dp))
                        Text(
                            modifier = Modifier,
                            text = "Lorem ipsum dolor sit amet, consectetur adipisci elit, sed eiusmod tempor incidunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliq",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(modifier = Modifier.padding(10.dp))
                        Text(
                            text = "Lorem ipsum dolor sit amet, consectetur",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Spacer(modifier = Modifier.padding(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TrainLine.values().forEach { trainLine ->
                                ColoredBox(color = trainLine.color)
                            }
                        }
                        Spacer(modifier = Modifier.padding(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Button(onClick = { Toast.makeText(context, "test", Toast.LENGTH_SHORT).show() }) {
                                Text(text = "Button")
                            }
                            ElevatedButton(onClick = { Toast.makeText(context, "test", Toast.LENGTH_SHORT).show() }) {
                                Text(text = "Elevated")
                            }
                            FilledTonalButton(onClick = { Toast.makeText(context, "test", Toast.LENGTH_SHORT).show() }) {
                                Text(text = "Filled")
                            }
                        }
                        Spacer(modifier = Modifier.padding(10.dp))
                        val textValue = remember { mutableStateOf(TextFieldValue()) }
                        TextFieldMaterial3(
                            text = textValue.value,
                            onValueChange = {
                                textValue.value = it
                            }
                        )
                        Spacer(modifier = Modifier.padding(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val checked = remember { mutableStateOf(true) }
                            SwitchMaterial3(
                                onCheckedChange = {
                                    checked.value = !checked.value
                                },
                                checked = checked.value
                            )
                            val isSelected = remember { mutableStateOf(true) }
                            ChipMaterial3(
                                text = "Chip",
                                isSelected = isSelected.value,
                                onClick = {
                                    isSelected.value = !isSelected.value
                                }
                            )
                            val boxSelected = remember { mutableStateOf(true) }
                            Checkbox(checked = boxSelected.value, onCheckedChange = {
                                boxSelected.value = !boxSelected.value
                            })
                            val radioSelected = remember { mutableStateOf(true) }
                            RadioButton(selected = radioSelected.value, onClick = {
                                radioSelected.value = !radioSelected.value
                            })
                        }
                    }
                }
            }
            item {
                DisplayElementSwitchView(
                    imageVector = Icons.Outlined.Palette,
                    title = "Theme selection",
                    description = "Automatic or manual",
                    isChecked = viewModel.uiState.theme == Theme.AUTO,
                    onClick = {

                    }
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ThemeColorButton(color = blue_line, enabled = false, onClick = {})
                    ThemeColorButton(color = red_line, enabled = false, onClick = {})
                    ThemeColorButton(color = yellow_line, enabled = false, onClick = {})
                    ThemeColorButton(color = green_line, enabled = false, onClick = {})
                }
            }
            item { NavigationBarsSpacer() }
        }
    }
}
