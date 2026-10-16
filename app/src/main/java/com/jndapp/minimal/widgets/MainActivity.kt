package com.jndapp.minimal.widgets

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.jndapp.minimal.widgets.services.WidgetKeepAliveService
import com.jndapp.minimal.widgets.ui.screens.OnboardingScreen
import com.jndapp.minimal.widgets.ui.screens.SettingsScreen
import com.jndapp.minimal.widgets.ui.screens.StudioScreen
import com.jndapp.minimal.widgets.ui.screens.WallpapersScreen
import com.jndapp.minimal.widgets.ui.screens.WidgetsScreen
import com.jndapp.minimal.widgets.ui.theme.MyUITheme

sealed class Screen(val title: String, val icon: ImageVector) {
    object Widgets : Screen("Widgets", Icons.Rounded.Widgets)
    object Wallpapers : Screen("Wallpapers", Icons.Rounded.Wallpaper)
    object Studio : Screen("Studio", Icons.Rounded.Palette)
    object Settings : Screen("Settings", Icons.Rounded.Settings)
}

class MainActivity : ComponentActivity() {

    private var appUpdateManager: AppUpdateManager? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize update manager
        try {
            appUpdateManager = AppUpdateManagerFactory.create(this)
            checkForAppUpdates(silent = true)
        } catch (_: Exception) {}

        // Keep widget services alive
        try {
            val keepAliveIntent = Intent(this, WidgetKeepAliveService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(keepAliveIntent)
            } else {
                startService(keepAliveIntent)
            }
        } catch (_: Exception) {}

        // Check activity recognition permission for health widgets
        checkStepSensorPermission()

        setContent {
            MyUITheme(darkTheme = true) {
                val prefs = remember { getSharedPreferences("myui_settings", Context.MODE_PRIVATE) }
                var hasCompletedOnboarding by remember {
                    mutableStateOf(prefs.getBoolean("has_completed_onboarding", false))
                }

                if (!hasCompletedOnboarding) {
                    OnboardingScreen(
                        onFinish = {
                            prefs.edit().putBoolean("has_completed_onboarding", true).apply()
                            hasCompletedOnboarding = true
                        }
                    )
                } else {
                    var selectedTabIndex by remember { mutableIntStateOf(0) }
                    val screens = listOf(Screen.Widgets, Screen.Wallpapers, Screen.Studio, Screen.Settings)

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            NavigationBar(
                                modifier = Modifier.navigationBarsPadding(),
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary
                            ) {
                                screens.forEachIndexed { index, screen ->
                                    val selected = selectedTabIndex == index
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { selectedTabIndex = index },
                                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                                        label = { Text(screen.title) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        AnimatedContent(
                            targetState = selectedTabIndex,
                            label = "screen_transition",
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            }
                        ) { targetIndex ->
                            when (targetIndex) {
                                0 -> WidgetsScreen(paddingValues = innerPadding)
                                1 -> WallpapersScreen(paddingValues = innerPadding)
                                2 -> StudioScreen(paddingValues = innerPadding)
                                3 -> SettingsScreen(
                                    paddingValues = innerPadding,
                                    onCheckUpdate = { checkForAppUpdates(silent = false) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkStepSensorPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
    }

    private fun checkForAppUpdates(silent: Boolean) {
        appUpdateManager?.appUpdateInfo?.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                appUpdateManager?.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    this,
                    100
                )
            } else if (!silent) {
                Toast.makeText(this, "App is up to date!", Toast.LENGTH_SHORT).show()
            }
        }?.addOnFailureListener {
            if (!silent) {
                Toast.makeText(this, "Unable to check for updates", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
