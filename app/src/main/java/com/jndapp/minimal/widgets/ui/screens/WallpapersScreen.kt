package com.jndapp.minimal.widgets.ui.screens

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jndapp.minimal.widgets.model.Wallpaper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpapersScreen(
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    var selectedCollection by remember { mutableStateOf("All") }
    var selectedWallpaper by remember { mutableStateOf<Wallpaper?>(null) }

    val collections = listOf("All", "Amoled", "Minimal", "Abstract", "Geometry", "Dark")

    val wallpapers = remember {
        listOf(
            Wallpaper(
                name = "Obsidian Waves",
                author = "JustNewDesigns",
                url = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe",
                thumbnail = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400",
                collections = "Amoled, Minimal",
                dimensions = "2160x3840"
            ),
            Wallpaper(
                name = "Amber Horizon",
                author = "JustNewDesigns",
                url = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e",
                thumbnail = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=400",
                collections = "Minimal, Dark",
                dimensions = "2160x3840"
            ),
            Wallpaper(
                name = "Neon Grid",
                author = "JustNewDesigns",
                url = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853",
                thumbnail = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=400",
                collections = "Abstract, Geometry",
                dimensions = "2160x3840"
            ),
            Wallpaper(
                name = "Monochrome Sand",
                author = "JustNewDesigns",
                url = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05",
                thumbnail = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=400",
                collections = "Amoled, Minimal",
                dimensions = "2160x3840"
            ),
            Wallpaper(
                name = "Pure Dark Minimal",
                author = "JustNewDesigns",
                url = "https://images.unsplash.com/photo-1579546929518-9e396f3cc809",
                thumbnail = "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=400",
                collections = "Dark, Amoled",
                dimensions = "2160x3840"
            ),
            Wallpaper(
                name = "Cyber Bloom",
                author = "JustNewDesigns",
                url = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5",
                thumbnail = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=400",
                collections = "Abstract, Dark",
                dimensions = "2160x3840"
            )
        )
    }

    val filteredWallpapers = remember(selectedCollection) {
        if (selectedCollection == "All") wallpapers
        else wallpapers.filter { it.collections.contains(selectedCollection, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)) {
            Text(
                text = "Wallpapers",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Curated backdrops calibrated for minimal widgets",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(collections) { collection ->
                val isSelected = selectedCollection == collection
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCollection = collection },
                    label = { Text(collection) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredWallpapers, key = { it.name }) { wallpaper ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clickable { selectedWallpaper = wallpaper },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFF202028),
                                            Color(0xFF101014),
                                            Color(0xFF08080C)
                                        )
                                    )
                                )
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                    )
                                )
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = wallpaper.name,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = wallpaper.author,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selectedWallpaper?.let { wp ->
        AlertDialog(
            onDismissRequest = { selectedWallpaper = null },
            title = { Text(wp.name, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Author: ${wp.author}")
                    Text("Dimensions: ${wp.dimensions}")
                    Text("Collections: ${wp.collections}")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "High resolution wallpaper ready for your homescreen.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(wp.url))
                        context.startActivity(browserIntent)
                        selectedWallpaper = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open / Download", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedWallpaper = null }) {
                    Text("Close")
                }
            }
        )
    }
}
