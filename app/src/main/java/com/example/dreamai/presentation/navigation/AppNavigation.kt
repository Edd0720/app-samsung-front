package com.example.dreamai.presentation.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.material3.SnackbarHostState
import com.example.dreamai.HealthConnectManager
import com.example.healthconnect.codelab.presentation.screen.sleep.SleepSessionScreen
import com.example.healthconnect.codelab.presentation.screen.sleep.SleepSessionViewModel
import com.example.healthconnect.codelab.presentation.screen.sleep.SleepSessionViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
  navController: NavHostController,
  healthConnectManager: HealthConnectManager,
  snackbarHostState: SnackbarHostState,
  scope: CoroutineScope,
) {
  NavHost(navController = navController, startDestination = Screen.SleepSessions.route) {
    composable(Screen.SleepSessions.route) {
      val viewModel: SleepSessionViewModel = viewModel(
        factory = SleepSessionViewModelFactory(healthConnectManager = healthConnectManager)
      )

      val permissionsGranted by viewModel.permissionsGranted
      val sleepSessions by viewModel.sleepSessions
      val permissions = viewModel.permissions
      val backgroundReadPermissions = viewModel.backgroundReadPermissions
      val backgroundReadAvailable by viewModel.backgroundReadAvailable
      val backgroundReadGranted by viewModel.backgroundReadGranted
      val historyReadPermissions = viewModel.historyReadPermissions
      val historyReadAvailable by viewModel.historyReadAvailable
      val historyReadGranted by viewModel.historyReadGranted
      val onPermissionsResult = { viewModel.initialLoad() }

      val permissionsLauncher =
        rememberLauncherForActivityResult(viewModel.permissionsLauncher) {
          onPermissionsResult()
        }

      SleepSessionScreen(
        permissions = permissions,
        permissionsGranted = permissionsGranted,
        backgroundReadPermissions = backgroundReadPermissions,
        backgroundReadAvailable = backgroundReadAvailable,
        backgroundReadGranted = backgroundReadGranted,
        historyReadPermissions = historyReadPermissions,
        historyReadAvailable = historyReadAvailable,
        historyReadGranted = historyReadGranted,
        sessionsList = sleepSessions,
        uiState = viewModel.uiState,
        onInsertClick = {
          viewModel.initialLoad()
        },
        onError = { exception ->
          scope.launch {
            snackbarHostState.showSnackbar(
              message = exception?.localizedMessage ?: "Error desconocido"
            )
          }
        },
        onPermissionsResult = {
          viewModel.initialLoad()
        },
        onPermissionsLaunch = { values ->
          permissionsLauncher.launch(values)
        }
      )
    }
  }
}
