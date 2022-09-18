package fr.cph.chicago.stub

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection

@OptIn(ExperimentalMaterial3Api::class)
class DummyTopAppBarScrollBehavior: TopAppBarScrollBehavior {
    override val isPinned: Boolean
        get() = TODO("Not yet implemented")
    override val nestedScrollConnection: NestedScrollConnection
        get() = TODO("Not yet implemented")
    override val state: TopAppBarState
        get() = TODO("Not yet implemented")
}
