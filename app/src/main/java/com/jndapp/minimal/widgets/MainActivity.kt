package com.jndapp.minimal.widgets

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import com.jndapp.minimal.widgets.services.WidgetKeepAliveService
import com.jndapp.minimal.widgets.ui.screens.SettingsScreen
import com.jndapp.minimal.widgets.ui.screens.StudioScreen
import com.jndapp.minimal.widgets.ui.screens.WallpapersScreen
import com.jndapp.minimal.widgets.ui.screens.WidgetsScreen
import com.jndapp.minimal.widgets.ui.theme.MyUITheme

sealed class Screen(val title: String, val icon: ImageVector) {
    object Widgets : Screen("Widgets", Icons.Rounded.Widgets)
    object Studio : Screen("Studio", Icons.Rounded.Palette)
    object Wallpapers : Screen("Wallpapers", Icons.Rounded.Wallpaper)
    object Settings : Screen("Settings", Icons.Rounded.Settings)
}

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyUITheme(darkTheme = true) {
                var selectedTabIndex by remember { mutableIntStateOf(0) }
                val screens = listOf(Screen.Widgets, Screen.Studio, Screen.Wallpapers, Screen.Settings)

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
                            0 -> WidgetsScreen(
                                paddingValues = innerPadding,
                                onNavigateToStudio = { selectedTabIndex = 1 }
                            )
                            1 -> StudioScreen(paddingValues = innerPadding)
                            2 -> WallpapersScreen(paddingValues = innerPadding)
                            3 -> SettingsScreen(
                                paddingValues = innerPadding,
                                onCheckUpdate = {
                                    try {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://github.com/Agent07-X-lab/MYUI/releases")
                                        )
                                        this@MainActivity.startActivity(intent)
                                    } catch (_: Exception) {
                                        Toast.makeText(this@MainActivity, "MyUI v2.2.06-foss (Latest FOSS Release)", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
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
}
