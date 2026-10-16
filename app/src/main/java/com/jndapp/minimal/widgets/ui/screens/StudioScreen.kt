package com.jndapp.minimal.widgets.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Palette
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    val studioPrefs = remember { context.getSharedPreferences("studio_prefs", Context.MODE_PRIVATE) }
    val widgetPrefs = remember { context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE) }

    var selectedTab by remember { mutableStateOf("Dark") }
    var selectedTheme by remember { mutableStateOf(StudioThemes.darkThemes.first()) }
    var widgetOpacity by remember { mutableFloatStateOf(widgetPrefs.getFloat("widget_opacity", 1.0f)) }

    val tabs = listOf("Dark", "Light", "Pastel", "Muted")

    val currentThemeList = when (selectedTab) {
        "Dark" -> StudioThemes.darkThemes
        "Light" -> StudioThemes.lightThemes
        "Pastel" -> StudioThemes.pastelThemes
        else -> StudioThemes.mutedThemes
    }

    fun applyTheme(theme: StudioTheme) {
        selectedTheme = theme
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
            .apply()

        Toast.makeText(context, "Theme '${theme.name}' applied to widgets!", Toast.LENGTH_SHORT).show()
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
                    text = "Customize colors, backgrounds, and transparency for your widgets",
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
                                color = selectedTheme.accentColor.copy(alpha = 0.3f),
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
                                        .background(Color.White)
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
                        Icon(Icons.Rounded.Palette, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Apply to All Widgets", color = Color.Black, fontWeight = FontWeight.Bold)
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

        // Theme Items
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = theme.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            if (theme.isPro) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "PRO",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
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
}
