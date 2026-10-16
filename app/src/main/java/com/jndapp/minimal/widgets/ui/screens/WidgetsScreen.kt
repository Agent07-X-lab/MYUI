package com.jndapp.minimal.widgets.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jndapp.minimal.widgets.models.MoreApp
import com.jndapp.minimal.widgets.widgets.analog.analog1
import com.jndapp.minimal.widgets.widgets.battery.Battery1
import java.io.InputStreamReader

data class WidgetItem(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val providerClass: Class<*>? = null,
    val previewType: String = "analog"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetsScreen(
    paddingValues: PaddingValues,
    onWidgetClick: (WidgetItem) -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("widget_favorites_prefs", Context.MODE_PRIVATE) }
    var favoriteIds by remember {
        mutableStateOf(prefs.getStringSet("favorite_widget_ids", emptySet()) ?: emptySet())
    }

    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val categories = listOf("All", "Favorites", "Clock", "Battery", "Weather", "Calendar", "Notes", "Health")

    val widgetList = remember {
        listOf(
            WidgetItem("analog1", "Minimal Mono 01", "Clock", "Classic analog clock with dynamic second hand", analog1::class.java, "analog"),
            WidgetItem("analog2", "Minimal Mono 02", "Clock", "Subtle index hour ticks with warm amber accents", analog1::class.java, "analog"),
            WidgetItem("analog3", "Nothing Dial", "Clock", "Dot-matrix inspired analog face with bold hands", analog1::class.java, "analog"),
            WidgetItem("battery1", "Power Ring 01", "Battery", "Circular gradient battery progress ring with status text", Battery1::class.java, "battery"),
            WidgetItem("battery2", "Power Ring 02", "Battery", "Minimal numeric level with charging indicator", Battery1::class.java, "battery"),
            WidgetItem("weather1", "Atmosphere Mini", "Weather", "Current temperature, high/low, and conditions icon", null, "weather"),
            WidgetItem("weather2", "Weather Forecast", "Weather", "5-day minimal vertical weather outlook", null, "weather"),
            WidgetItem("calendar1", "Month Grid", "Calendar", "Clean modern month matrix with highlighted today", null, "calendar"),
            WidgetItem("calendar2", "Agenda Bar", "Calendar", "Minimal day schedule and upcoming events list", null, "calendar"),
            WidgetItem("notes1", "Quick Scratchpad", "Notes", "Tap-to-edit homescreen minimal post-it note", null, "notes"),
            WidgetItem("pedometer1", "Daily Step Goal", "Health", "Step counter with radial goal completion arc", null, "health")
        )
    }

    val moreApps = remember {
        try {
            val stream = context.assets.open("more_apps.json")
            val type = object : TypeToken<List<MoreApp>>() {}.type
            Gson().fromJson<List<MoreApp>>(InputStreamReader(stream), type) ?: emptyList()
        } catch (e: Exception) {
            emptyList<MoreApp>()
        }
    }

    val filteredWidgets = remember(selectedCategory, searchQuery, favoriteIds) {
        widgetList.filter { widget ->
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Favorites" -> favoriteIds.contains(widget.id)
                else -> widget.category.equals(selectedCategory, ignoreCase = true)
            }
            val matchesSearch = searchQuery.isEmpty() ||
                    widget.title.contains(searchQuery, ignoreCase = true) ||
                    widget.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    fun toggleFavorite(id: String) {
        val updated = favoriteIds.toMutableSet()
        if (updated.contains(id)) updated.remove(id) else updated.add(id)
        favoriteIds = updated
        prefs.edit().putStringSet("favorite_widget_ids", updated).apply()
    }

    fun pinWidget(widget: WidgetItem) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && widget.providerClass != null) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val myProvider = ComponentName(context, widget.providerClass)
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                appWidgetManager.requestPinAppWidget(myProvider, null, null)
            }
        }
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
                    text = "Minimal Widgets",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Refined widgets tailored for your home screen",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                placeholder = { Text("Search widgets by name or style…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }
        }

        items(filteredWidgets, key = { it.id }) { widget ->
            val isFav = favoriteIds.contains(widget.id)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onWidgetClick(widget) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when (widget.previewType) {
                            "analog" -> {
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(30.dp)
                                            .background(MaterialTheme.colorScheme.primary)
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
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                            "battery" -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        progress = { 0.84f },
                                        modifier = Modifier.size(70.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 6.dp,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("84%", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            "weather" -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("24°", fontSize = 36.sp, fontWeight = FontWeight.Light, color = Color.White)
                                    Text("Sunny • H:26° L:18°", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            else -> {
                                Text(
                                    text = widget.category.uppercase(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = widget.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = widget.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { toggleFavorite(widget.id) }) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFav) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            FilledTonalIconButton(
                                onClick = { pinWidget(widget) },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Add Widget")
                            }
                        }
                    }
                }
            }
        }

        if (moreApps.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "MORE BY JUSTNEWDESIGNS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(moreApps) { app ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(app.play_store_url))
                            context.startActivity(intent)
                        },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Apps,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = app.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = app.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
