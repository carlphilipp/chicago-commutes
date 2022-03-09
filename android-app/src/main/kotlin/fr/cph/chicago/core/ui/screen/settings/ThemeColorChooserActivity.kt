package fr.cph.chicago.core.ui.screen.settings

import android.os.Build
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.core.theme.blue_line
import fr.cph.chicago.core.theme.defaultButtonShape
import fr.cph.chicago.core.theme.green_line
import fr.cph.chicago.core.theme.red_line
import fr.cph.chicago.core.theme.yellow_line
import fr.cph.chicago.core.ui.common.ChipMaterial3
import fr.cph.chicago.core.ui.common.ColoredBox
import fr.cph.chicago.core.ui.common.NavigationBarsSpacer
import fr.cph.chicago.core.ui.common.SwitchMaterial3
import fr.cph.chicago.core.ui.common.TextFieldMaterial3
import fr.cph.chicago.core.ui.common.ThemeColorButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeChooserSettingsScreen(viewModel: SettingsViewModel) {
    val context = LocalContext.current as ComponentActivity
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        item {
            ElevatedCard(modifier = Modifier.padding(start = 15.dp, end = 15.dp, top = 15.dp)) {
                Column(modifier = Modifier.padding(15.dp)) {
                    val title = if (viewModel.uiState.themeColorAutomatic) "Using system theme" else "Using manual theme"

                    Text(
                        modifier = Modifier,
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val subtitle = when {
                            viewModel.uiState.dynamicColorEnabled && viewModel.uiState.themeColorAutomatic -> {
                                "Dynamic colors enabled"
                            }
                            !viewModel.uiState.themeColorAutomatic -> {
                                "Dynamic colors can only be used in automatic"
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // FIXME: Not sure if there is a better way?
                val onClick = { viewModel.setThemeColorAutomatic(!viewModel.uiState.themeColorAutomatic) }
                if (viewModel.uiState.themeColorAutomatic) {
                    Button(
                        modifier = Modifier.weight(0.5f),
                        onClick = { },
                        shape = defaultButtonShape,
                    ) {
                        Text(text = "Automatic")
                    }
                    TextButton(
                        modifier = Modifier.weight(0.5f),
                        onClick = onClick,
                        shape = defaultButtonShape,
                    ) {
                        Text(text = "Manual")
                    }
                } else {
                    TextButton(
                        modifier = Modifier.weight(0.4f),
                        onClick = onClick,
                        shape = defaultButtonShape,
                    ) {
                        Text(text = "Automatic")
                    }
                    Button(
                        modifier = Modifier.weight(0.4f),
                        onClick = {},
                        shape = defaultButtonShape,
                    ) {
                        Text(text = "Manual")
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                DisplayElementSwitchView(
                    imageVector = Icons.Outlined.Palette,
                    title = "Dynamic color",
                    description = "Use your wallpaper color",
                    isChecked = viewModel.uiState.dynamicColorEnabled,
                    enabled = viewModel.uiState.themeColorAutomatic,
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
                val enable = !viewModel.uiState.themeColorAutomatic
                val alpha = if (viewModel.uiState.themeColorAutomatic) 0.2f else 1.0f
                ThemeColorButton(
                    color = blue_line,
                    enabled = enable,
                    alpha = alpha,
                    onClick = {
                        Toast.makeText(context, "TODO. Not sure how to", Toast.LENGTH_SHORT).show()
                    }
                )
                ThemeColorButton(
                    color = red_line,
                    enabled = enable,
                    alpha = alpha,
                    onClick = {
                        Toast.makeText(context, "TODO. Not sure how to", Toast.LENGTH_SHORT).show()
                    }
                )
                ThemeColorButton(
                    color = yellow_line,
                    enabled = enable,
                    alpha = alpha,
                    onClick = {
                        Toast.makeText(context, "TODO. Not sure how to", Toast.LENGTH_SHORT).show()
                    }
                )
                ThemeColorButton(
                    color = green_line,
                    enabled =
                    enable,
                    alpha = alpha,
                    onClick = {
                        Toast.makeText(context, "TODO. Not sure how to", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
        item { NavigationBarsSpacer() }
    }
}
