package fr.cph.chicago.core.composable.viewmodel

import fr.cph.chicago.core.composable.MainViewModel
import fr.cph.chicago.core.composable.common.LocationViewModel
import fr.cph.chicago.core.composable.screen.SettingsViewModel

val mainViewModel = MainViewModel()
val settingsViewModel = SettingsViewModel().initModel()
val locationViewModel = LocationViewModel()
