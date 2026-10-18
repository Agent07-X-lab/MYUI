package com.jndapp.minimal.widgets.ui.screens

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jndapp.minimal.widgets.model.StudioTheme
import com.jndapp.minimal.widgets.model.StudioThemes

private const val TAG = "StudioScreen"

val QUICK_PALETTE = listOf(
    Color(0xFFFFBF00) to "Amber",
    Color(0xFFFF3B30) to "Ruby Red",
    Color(0xFFFF6F61) to "Coral",
    Color(0xFFFF9500) to "Orange",
    Color(0xFFFFD700) to "Gold",
    Color(0xFF76FF03) to "Lime",
    Color(0xFF00E676) to "Emerald",
    Color(0xFF00BFA5) to "Teal",
    Color(0xFF00E5FF) to "Cyan",
    Color(0xFF00B0FF) to "Sky Blue",
    Color(0xFF2979FF) to "Electric Blue",
    Color(0xFF3D5AFE) to "Indigo",
    Color(0xFF651FFF) to "Purple",
    Color(0xFF7C4DFF) to "Violet",
    Color(0xFFD500F9) to "Magenta",
    Color(0xFFFF4081) to "Hot Pink",
    Color(0xFFFFFFFF) to "Pure White",
    Color(0xFF333333) to "Charcoal"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    val studioPrefs = remember { context.getSharedPreferences("studio_prefs", Context.MODE_PRIVATE) }
    val widgetPrefs = remember { context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE) }

    val savedThemeName = studioPrefs.getString("selected_theme_name", null)
    val initialTheme = remember {
        val allThemes = StudioThemes.darkThemes + StudioThemes.lightThemes +
                StudioThemes.pastelThemes + StudioThemes.mutedThemes
        allThemes.find { it.name == savedThemeName } ?: run {
            val savedAccent = studioPrefs.getInt("theme_accent_color", 0)
            val savedBg = studioPrefs.getInt("theme_background_color", 0)
            if (savedAccent != 0 && savedBg != 0) {
                StudioTheme(
                    name = savedThemeName ?: "Custom",
                    description = "Custom customized colors",
                    accentColor = Color(savedAccent),
                    backgroundColor = Color(savedBg),
                    surfaceColor = Color(studioPrefs.getInt("theme_surface_color", savedBg))
                )
            } else {
                StudioThemes.darkThemes.first()
            }
        }
    }

    var selectedTab by remember { mutableStateOf("Dark") }
    var selectedTheme by remember { mutableStateOf(initialTheme) }
    var widgetOpacity by remember { mutableFloatStateOf(widgetPrefs.getFloat("widget_opacity", 1.0f)) }
    var showColorDialog by remember { mutableStateOf(false) }

    val tabs = listOf("Dark", "Light", "Pastel", "Muted", "Custom")

    val currentThemeList = when (selectedTab) {
        "Dark" -> StudioThemes.darkThemes
        "Light" -> StudioThemes.lightThemes
        "Pastel" -> StudioThemes.pastelThemes
        "Muted" -> StudioThemes.mutedThemes
        else -> emptyList()
    }

    fun applyTheme(theme: StudioTheme) {
        selectedTheme = theme
        try {
            studioPrefs.edit()
                .putString("selected_theme_name", theme.name)
                .putInt("theme_accent_color", theme.accentColor.toArgb())
                .putInt("theme_background_color", theme.backgroundColor.toArgb())
                .putInt("theme_surface_color", theme.surfaceColor.toArgb())
                .apply()

            widgetPrefs.edit()
                .putFloat("widget_opacity", widgetOpacity)
                .putInt("widget_accent_color", theme.accentColor.toArgb())
                .putInt("widget_background_color", theme.backgroundColor.toArgb())
                .putInt("widget_color", theme.accentColor.toArgb())
                .putInt("widget_background_tint_color", theme.backgroundColor.toArgb())
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save studio preferences", e)
        }

        // Reliably notify all active widgets with explicit component intents
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val installed = appWidgetManager.installedProviders
            for (p in installed) {
                if (p.provider.packageName == context.packageName) {
                    val ids = appWidgetManager.getAppWidgetIds(p.provider)
                    if (ids != null && ids.isNotEmpty()) {
                        val updateIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                            component = p.provider
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                        }
                        context.sendBroadcast(updateIntent)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error notifying active widgets", e)
        }

        try {
            Toast.makeText(context, "Theme '${theme.name}' applied!", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {}
    }

    fun setQuickAccent(accent: Color, name: String) {
        val updatedTheme = selectedTheme.copy(
            name = name,
            description = "Custom accent on ${if (selectedTheme.backgroundColor == Color.White) "Light" else "Dark"}",
            accentColor = accent
        )
        applyTheme(updatedTheme)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Widget Studio",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Choose colors, presets, transparency, and background for your widgets",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Live Preview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE PREVIEW",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = selectedTheme.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                selectedTheme.backgroundColor.copy(alpha = widgetOpacity)
                            )
                            .border(
                                width = 1.dp,
                                color = selectedTheme.accentColor.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mini Clock Preview
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(selectedTheme.surfaceColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(30.dp)
                                        .background(selectedTheme.accentColor)
                                        .align(Alignment.TopCenter)
                                        .offset(y = 15.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(20.dp)
                                        .background(if (selectedTheme.backgroundColor == Color.White) Color.DarkGray else Color.White)
                                        .align(Alignment.CenterEnd)
                                        .offset(x = (-25).dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(selectedTheme.accentColor)
                                )
                            }

                            // Mini Battery Preview
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    progress = { 0.88f },
                                    modifier = Modifier.size(72.dp),
                                    color = selectedTheme.accentColor,
                                    strokeWidth = 6.dp,
                                    trackColor = selectedTheme.surfaceColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "88%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTheme.backgroundColor == Color.White) Color.Black else Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Opacity Slider
                    Text(
                        text = "Widget Background Opacity: ${(widgetOpacity * 100).toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = widgetOpacity,
                        onValueChange = {
                            widgetOpacity = it
                            widgetPrefs.edit().putFloat("widget_opacity", it).apply()
                        },
                        onValueChangeFinished = {
                            applyTheme(selectedTheme)
                        },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = selectedTheme.accentColor,
                            activeTrackColor = selectedTheme.accentColor
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { applyTheme(selectedTheme) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = selectedTheme.accentColor)
                    ) {
                        Icon(
                            Icons.Rounded.Palette,
                            contentDescription = null,
                            tint = if (selectedTheme.accentColor == Color.White) Color.Black else Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Apply to All Widgets",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Quick Accent Color Palette Row
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QUICK ACCENT COLORS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        TextButton(
                            onClick = { showColorDialog = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Custom Hex/RGB", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QUICK_PALETTE.forEach { (color, colorName) ->
                            val isChosen = selectedTheme.accentColor == color
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isChosen) 3.dp else 1.dp,
                                        color = if (isChosen) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        setQuickAccent(color, colorName)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isChosen) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = colorName,
                                        tint = if (color == Color.White || color == Color(0xFFFFD700) || color == Color(0xFFFFBF00)) Color.Black else Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Palette Category Tabs
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tabs) { tab ->
                    val isSelected = selectedTab == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        label = { Text(tab) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }
        }

        // Custom Mode Section
        if (selectedTab == "Custom") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Background Style",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val bgOptions = listOf(
                                Triple("OLED Black", Color(0xFF000000), Color(0xFF141416)),
                                Triple("Dark Slate", Color(0xFF1E1E1E), Color(0xFF2C2C2E)),
                                Triple("Pure White", Color(0xFFFFFFFF), Color(0xFFE5E5EA)),
                                Triple("Soft Grey", Color(0xFFF5F5F7), Color(0xFFE0E0E0))
                            )
                            bgOptions.forEach { (label, bg, surf) ->
                                val isSelected = selectedTheme.backgroundColor == bg
                                OutlinedButton(
                                    onClick = {
                                        val updated = selectedTheme.copy(
                                            backgroundColor = bg,
                                            surfaceColor = surf
                                        )
                                        applyTheme(updated)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent
                                    ),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(
                                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f)
                                        )
                                    ),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { showColorDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Rounded.ColorLens, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Custom Color Mixer", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Preset Themes
        items(currentThemeList) { theme ->
            val isSelected = selectedTheme.name == theme.name
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { applyTheme(theme) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(theme.backgroundColor)
                            .border(2.dp, theme.accentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(theme.accentColor)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = theme.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = theme.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    if (showColorDialog) {
        CustomColorPickerDialog(
            initialColor = selectedTheme.accentColor,
            onDismiss = { showColorDialog = false },
            onColorSelected = { chosenColor ->
                showColorDialog = false
                val hexStr = String.format("#%06X", (0xFFFFFF and chosenColor.toArgb()))
                setQuickAccent(chosenColor, "Custom $hexStr")
            }
        )
    }
}

@Composable
fun CustomColorPickerDialog(
    initialColor: Color,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    var red by remember { mutableFloatStateOf(initialColor.red) }
    var green by remember { mutableFloatStateOf(initialColor.green) }
    var blue by remember { mutableFloatStateOf(initialColor.blue) }
    var hexInput by remember {
        mutableStateOf(String.format("#%06X", (0xFFFFFF and initialColor.toArgb())))
    }

    val currentColor = Color(red, green, blue, 1f)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Color Mixer", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Color Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(currentColor)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("#%06X", (0xFFFFFF and currentColor.toArgb())),
                        fontWeight = FontWeight.Bold,
                        color = if ((red * 0.299 + green * 0.587 + blue * 0.114) > 0.5) Color.Black else Color.White
                    )
                }

                // Red Slider
                Column {
                    Text("Red: ${(red * 255).toInt()}", fontSize = 12.sp, color = Color(0xFFFF5252))
                    Slider(
                        value = red,
                        onValueChange = {
                            red = it
                            hexInput = String.format("#%06X", (0xFFFFFF and Color(red, green, blue).toArgb()))
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFF5252),
                            activeTrackColor = Color(0xFFFF5252)
                        )
                    )
                }

                // Green Slider
                Column {
                    Text("Green: ${(green * 255).toInt()}", fontSize = 12.sp, color = Color(0xFF69F0AE))
                    Slider(
                        value = green,
                        onValueChange = {
                            green = it
                            hexInput = String.format("#%06X", (0xFFFFFF and Color(red, green, blue).toArgb()))
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF69F0AE),
                            activeTrackColor = Color(0xFF69F0AE)
                        )
                    )
                }

                // Blue Slider
                Column {
                    Text("Blue: ${(blue * 255).toInt()}", fontSize = 12.sp, color = Color(0xFF448AFF))
                    Slider(
                        value = blue,
                        onValueChange = {
                            blue = it
                            hexInput = String.format("#%06X", (0xFFFFFF and Color(red, green, blue).toArgb()))
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF448AFF),
                            activeTrackColor = Color(0xFF448AFF)
                        )
                    )
                }

                // Hex input
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { newHex ->
                        hexInput = newHex
                        try {
                            val cleanHex = if (newHex.startsWith("#")) newHex else "#$newHex"
                            if (cleanHex.length == 7) {
                                val parsed = android.graphics.Color.parseColor(cleanHex)
                                red = android.graphics.Color.red(parsed) / 255f
                                green = android.graphics.Color.green(parsed) / 255f
                                blue = android.graphics.Color.blue(parsed) / 255f
                            }
                        } catch (_: Exception) {}
                    },
                    label = { Text("Hex Code (#RRGGBB)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onColorSelected(currentColor) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Select Color", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
