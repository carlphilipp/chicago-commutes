package fr.cph.chicago.core.ui.screen.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.cph.chicago.R
import fr.cph.chicago.core.navigation.DisplayTopBar
import fr.cph.chicago.core.navigation.NavigationViewModel
import fr.cph.chicago.core.ui.screen.Screen
import fr.cph.chicago.launchWithDelay
import fr.cph.chicago.util.Util

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    viewModel: SettingsViewModel,
    navigationViewModel: NavigationViewModel,
    topBarTitle: String,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollBehavior by remember { mutableStateOf(navigationViewModel.uiState.settingsAboutScrollBehavior) }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        content = {
            Column {
                DisplayTopBar(
                    screen = Screen.SettingsAbout,
                    title = topBarTitle,
                    viewModel = navigationViewModel,
                    scrollBehavior = scrollBehavior,
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                        ) {
                            Image(
                                modifier = Modifier
                                    .height(100.dp)
                                    .align(Alignment.CenterHorizontally),
                                painter = painterResource(R.drawable.ic_launcher_web),
                                contentDescription = null,
                                contentScale = ContentScale.FillHeight,
                            )
                            Text(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                text = stringResource(R.string.current_app_name),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                text = Util.getCurrentVersion(),
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }
                    item {
                        SettingsElementView(
                            imageVector = Icons.Outlined.RateReview,
                            title = "Rate app!",
                            description = "Rate our app on the market",
                            onClick = {
                                scope.launchWithDelay(viewModel.uiState.animationSpeed.clickDelay) {

                                }
                            }
                        )

                        SettingsElementView(
                            imageVector = Icons.Outlined.Code,
                            title = "GitHub",
                            description = "Open source project",
                            onClick = {
                                scope.launchWithDelay(viewModel.uiState.animationSpeed.clickDelay) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/carlphilipp/chicago-commutes"))
                                    context.startActivity(intent)
                                }
                            }
                        )
                        SettingsElementView(
                            imageVector = Icons.Outlined.Book,
                            title = "License",
                            description = "Apache License 2.0",
                            onClick = {
                                scope.launchWithDelay(viewModel.uiState.animationSpeed.clickDelay) {

                                }
                            }
                        )
                    }
                }
            }
        })
}
