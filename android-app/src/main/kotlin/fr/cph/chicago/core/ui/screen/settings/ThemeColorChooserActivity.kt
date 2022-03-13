package fr.cph.chicago.core.ui.screen.settings

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.android.material.color.DynamicColors
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.core.navigation.DisplayTopBar
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.theme.ThemeColor
import fr.cph.chicago.core.theme.defaultButtonShape
import fr.cph.chicago.core.ui.common.ChipMaterial3
import fr.cph.chicago.core.ui.common.ColoredBox
import fr.cph.chicago.core.ui.common.NavigationBarsSpacer
import fr.cph.chicago.core.ui.common.SwitchMaterial3
import fr.cph.chicago.core.ui.common.TextFieldMaterial3
import fr.cph.chicago.core.ui.common.ThemeColorButton
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeChooserSettingsScreen(
    viewModel: SettingsViewModel,
    navigationViewModel: NavigationViewModel,
    topBarTitle: String,
) {
    val context = LocalContext.current as ComponentActivity

    Scaffold(
        content = {
            Column {
                DisplayTopBar(
                    title = topBarTitle,
                    viewModel = navigationViewModel,
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                ) {
                    item {
                        ElevatedCard(modifier = Modifier.padding(start = 15.dp, end = 15.dp, top = 15.dp)) {
                            Column(modifier = Modifier.padding(15.dp)) {
                                val title = "Using color ${MaterialTheme.colorScheme.primary.toArgb()}"

                                Text(
                                    modifier = Modifier,
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                if (DynamicColors.isDynamicColorAvailable()) {
                                    val subtitle = when {
                                        viewModel.uiState.dynamicColorEnabled -> {
                                            "Dynamic colors enabled"
                                        }
                                        else -> {
                                            "Dynamic color disabled"
                                        }
                                    }
                                    Spacer(modifier = Modifier.padding(4.dp))
                                    Text(
                                        modifier = Modifier,
                                        text = subtitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
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
                                    Button(
                                        onClick = { Toast.makeText(context, "test", Toast.LENGTH_SHORT).show() },
                                        shape = defaultButtonShape,
                                    ) {
                                        Text(text = "Button")
                                    }
                                    ElevatedButton(
                                        onClick = {
                                            Toast.makeText(context, "test", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = defaultButtonShape,
                                    ) {
                                        Text(text = "Elevated")
                                    }
                                    FilledTonalButton(
                                        onClick = { Toast.makeText(context, "test", Toast.LENGTH_SHORT).show() },
                                        shape = defaultButtonShape,
                                    ) {
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
                        if (DynamicColors.isDynamicColorAvailable()) {
                            DisplayElementSwitchView(
                                imageVector = Icons.Outlined.Palette,
                                title = "Dynamic color",
                                description = "Use your wallpaper color",
                                isChecked = viewModel.uiState.dynamicColorEnabled,
                                enabled = true,
                                onClick = {
                                    viewModel.setDynamicColor(!viewModel.uiState.dynamicColorEnabled)
                                }
                            )
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 15.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val enabled = !(DynamicColors.isDynamicColorAvailable() && viewModel.uiState.dynamicColorEnabled)
                            val alpha = if (enabled) 1.0f else 0.2f
                            ThemeColorButton(
                                color = ThemeColor.Blue.lightTheme.primary,
                                enabled = enabled,
                                alpha = alpha,
                                onClick = { viewModel.setThemeColor(ThemeColor.Blue) }
                            )
                            ThemeColorButton(
                                color = ThemeColor.Green.lightTheme.primary,
                                enabled = enabled,
                                alpha = alpha,
                                onClick = { viewModel.setThemeColor(ThemeColor.Green) }
                            )
                            ThemeColorButton(
                                color = ThemeColor.Purple.lightTheme.primary,
                                enabled = enabled,
                                alpha = alpha,
                                onClick = { viewModel.setThemeColor(ThemeColor.Purple) }
                            )
                            ThemeColorButton(
                                color = ThemeColor.Orange.lightTheme.primary,
                                enabled = enabled,
                                alpha = alpha,
                                onClick = { viewModel.setThemeColor(ThemeColor.Orange) }
                            )
                        }
                    }
                    item { NavigationBarsSpacer() }
                }
            }
        })
}
