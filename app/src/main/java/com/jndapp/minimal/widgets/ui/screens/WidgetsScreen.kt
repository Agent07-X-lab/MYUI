package com.jndapp.minimal.widgets.ui.screens

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.jndapp.minimal.widgets.R
import com.jndapp.minimal.widgets.widgets.WidgetPinnedReceiver

data class FullWidgetItem(
    val id: String,
    val title: String,
    val category: String,
    val previewResId: Int,
    val size: String,
    val className: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetsScreen(
    paddingValues: PaddingValues,
    onNavigateToStudio: () -> Unit = {}
) {
    val context = LocalContext.current
    val favPrefs = remember { context.getSharedPreferences("myui_widget_favs", Context.MODE_PRIVATE) }
    var favoriteIds by remember {
        mutableStateOf(favPrefs.getStringSet("fav_ids", emptySet()) ?: emptySet())
    }

    // Load full 422 widgets from resources
    val allWidgets = remember {
        val ids = context.resources.getStringArray(R.array.widget_ids)
        val categories = context.resources.getStringArray(R.array.widget_category_names)
        val titles = context.resources.getStringArray(R.array.widget_names)
        val sizes = context.resources.getStringArray(R.array.widget_sizes)
        val imagesTypedArray = context.resources.obtainTypedArray(R.array.widget_images)

        val list = ArrayList<FullWidgetItem>(ids.size)
        for (i in ids.indices) {
            val id = ids[i]
            val title = if (i < titles.size) titles[i] else id
            val cat = if (i < categories.size) categories[i] else "General"
            val size = if (i < sizes.size) sizes[i] else "4x2"
            val imgResId = imagesTypedArray.getResourceId(i, 0)
            list.add(FullWidgetItem(id, title, cat, imgResId, size, "com.jndapp.minimal.widgets.widgets.$id"))
        }
        imagesTypedArray.recycle()
        list
    }

    // Category list derived from items
    val categories = remember(allWidgets) {
        listOf("All", "Favorites") + allWidgets.map { it.category }.distinct().sorted()
    }

    val sizes = listOf("All Sizes", "4x2", "4x1", "2x2", "1x1", "2x1")

    var selectedCategory by remember { mutableStateOf("All") }
    var selectedSize by remember { mutableStateOf("All Sizes") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedWidgetForDetail by remember { mutableStateOf<FullWidgetItem?>(null) }
    var showManualPlacementDialog by remember { mutableStateOf<FullWidgetItem?>(null) }

    fun toggleFavorite(widgetId: String) {
        val updated = favoriteIds.toMutableSet()
        if (updated.contains(widgetId)) {
            updated.remove(widgetId)
        } else {
            updated.add(widgetId)
        }
        favPrefs.edit().putStringSet("fav_ids", updated).apply()
        favoriteIds = updated
    }

    fun pinWidgetToHomeScreen(item: FullWidgetItem) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                try {
                    val componentName = ComponentName(context, item.className)
                    val callbackIntent = Intent(context, WidgetPinnedReceiver::class.java).apply {
                        putExtra("pinned_widget_title", item.title)
                    }
                    val callbackPendingIntent = PendingIntent.getBroadcast(
                        context,
                        item.id.hashCode(),
                        callbackIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                    val success = appWidgetManager.requestPinAppWidget(componentName, null, callbackPendingIntent)
                    if (success) {
                        Toast.makeText(context, "Adding '${item.title}' to home screen...", Toast.LENGTH_SHORT).show()
                    } else {
                        showManualPlacementDialog = item
                    }
                } catch (e: Exception) {
                    showManualPlacementDialog = item
                }
            } else {
                showManualPlacementDialog = item
            }
        } else {
            showManualPlacementDialog = item
        }
    }

    val filteredWidgets = remember(selectedCategory, selectedSize, searchQuery, favoriteIds, allWidgets) {
        allWidgets.filter { widget ->
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Favorites" -> favoriteIds.contains(widget.id)
                else -> widget.category.equals(selectedCategory, ignoreCase = true)
            }
            val matchesSize = when (selectedSize) {
                "All Sizes" -> true
                else -> widget.size.equals(selectedSize, ignoreCase = true)
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                widget.title.contains(searchQuery, ignoreCase = true) ||
                        widget.category.contains(searchQuery, ignoreCase = true) ||
                        widget.id.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSize && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
    ) {
        // App Bar & Title
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MyUI Widgets",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${filteredWidgets.size} of ${allWidgets.size} widgets available",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalIconButton(
                    onClick = {
                        selectedCategory = if (selectedCategory == "Favorites") "All" else "Favorites"
                    },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (selectedCategory == "Favorites") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = if (selectedCategory == "Favorites") Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorites",
                        tint = if (selectedCategory == "Favorites") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search 400+ widgets...", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )
        }

        // Category Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = { Text(category, fontSize = 13.sp) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Size Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sizes) { sizeTag ->
                val isSelected = selectedSize == sizeTag
                SuggestionChip(
                    onClick = { selectedSize = sizeTag },
                    label = { Text(sizeTag, fontSize = 12.sp) },
                    shape = RoundedCornerShape(10.dp),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Widget Grid (Adaptive for tablets & phones)
        if (filteredWidgets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Widgets,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No widgets found",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Try adjusting your search or category filters",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 165.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredWidgets, key = { it.id }) { widget ->
                    val isFav = favoriteIds.contains(widget.id)
                    WidgetGridCard(
                        widget = widget,
                        isFavorite = isFav,
                        onToggleFavorite = { toggleFavorite(widget.id) },
                        onPin = { pinWidgetToHomeScreen(widget) },
                        onClick = { selectedWidgetForDetail = widget }
                    )
                }
            }
        }
    }

    // Detail & Pin Dialog
    selectedWidgetForDetail?.let { widget ->
        val isFav = favoriteIds.contains(widget.id)
        Dialog(onDismissRequest = { selectedWidgetForDetail = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Preview Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF0F0F12))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (widget.previewResId != 0) {
                            Image(
                                painter = painterResource(id = widget.previewResId),
                                contentDescription = widget.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Icon(
                                Icons.Rounded.Widgets,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = widget.title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${widget.category} • Size ${widget.size}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { toggleFavorite(widget.id) }) {
                            Icon(
                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedWidgetForDetail = null
                                onNavigateToStudio()
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Studio", fontSize = 14.sp)
                        }

                        Button(
                            onClick = {
                                pinWidgetToHomeScreen(widget)
                                selectedWidgetForDetail = null
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add to Home", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    if (showManualPlacementDialog != null) {
        val widgetToPlace = showManualPlacementDialog!!
        AlertDialog(
            onDismissRequest = { showManualPlacementDialog = null },
            icon = {
                Icon(
                    Icons.Rounded.Widgets,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Add to Home Screen",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Your device launcher requires adding '${widgetToPlace.title}' from your home screen:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("1. Long-press an empty space on your Home Screen", fontSize = 13.sp)
                            Text("2. Tap 'Widgets'", fontSize = 13.sp)
                            Text("3. Scroll to 'MyUI' and pick '${widgetToPlace.title}'", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("4. Drag and drop onto your screen!", fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showManualPlacementDialog = null
                        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(homeIntent)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Go to Home Screen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualPlacementDialog = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun WidgetGridCard(
    widget: FullWidgetItem,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onPin: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Preview Image Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0D0D10))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (widget.previewResId != 0) {
                    Image(
                        painter = painterResource(id = widget.previewResId),
                        contentDescription = widget.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        Icons.Rounded.Widgets,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Size Badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.65f)
                ) {
                    Text(
                        text = widget.size,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Favorite Icon
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Widget Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = widget.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = widget.category,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                FilledIconButton(
                    onClick = onPin,
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = "Add Widget",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
