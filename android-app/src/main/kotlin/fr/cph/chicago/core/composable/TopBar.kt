package fr.cph.chicago.core.composable

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.cph.chicago.core.activity.SearchViewModel
import fr.cph.chicago.core.composable.common.TextFieldMaterial3

@Composable
fun TopBar(title: String, openDrawer: () -> Unit, onSearch: () -> Unit, showSearch: Boolean = false) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        navigationIcon = {
            IconButton(onClick = { openDrawer() }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu"
                )
            }
        },
        actions = {
            if (showSearch) {
                IconButton(onClick = onSearch) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search"
                    )
                }
            }
        },
    )
}

// TODO refactor
@Composable
fun RefreshTopBar(title: String, showRefresh: Boolean = false, onRefresh: () -> Unit = {}) {
    val activity = LocalLifecycleOwner.current as ComponentActivity
    CenterAlignedTopAppBar(
        title = {
            Text(
                modifier = Modifier.padding(start = 15.dp),
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            IconButton(onClick = { activity.finish() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            if (showRefresh) {
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }
        }
    )
}

@Composable
fun SearchTopBar(viewModel: SearchViewModel) {
    val activity = LocalLifecycleOwner.current as ComponentActivity
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    CenterAlignedTopAppBar(
        title = {
            TextFieldMaterial3(
                text = searchText,
                onValueChange = { textFieldValue ->
                    searchText = textFieldValue
                    viewModel.search(searchText.text)
                }
            )
        },
        navigationIcon = {
            IconButton(onClick = { activity.finish() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }

        }
    )
}
