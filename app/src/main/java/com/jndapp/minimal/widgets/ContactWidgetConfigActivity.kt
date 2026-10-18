package com.jndapp.minimal.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jndapp.minimal.widgets.ui.theme.MinimalWidgetsTheme
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ContactWidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var maxSlots = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            appWidgetId = intent?.getIntExtra("appWidgetId", AppWidgetManager.INVALID_APPWIDGET_ID) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        }

        // Detect number of contact slots supported by this widget layout
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val providerInfo = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) appWidgetManager.getAppWidgetInfo(appWidgetId) else null
        val className = providerInfo?.provider?.className ?: ""

        maxSlots = when {
            className.contains("Contact4", ignoreCase = true) -> 8
            className.contains("Contact3", ignoreCase = true) -> 4
            else -> 1
        }

        setContent {
            MinimalWidgetsTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                ) {
                    ContactConfigScreen(
                        appWidgetId = appWidgetId,
                        maxSlots = maxSlots,
                        onDismiss = { finish() },
                        onApply = {
                            val resultValue = Intent().apply {
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            }
                            setResult(Activity.RESULT_OK, resultValue)

                            // Trigger widget update broadcast
                            if (providerInfo != null) {
                                val updateIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                                    component = providerInfo.provider
                                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                                }
                                sendBroadcast(updateIntent)
                            }
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactConfigScreen(
    appWidgetId: Int,
    maxSlots: Int,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("contact_prefs", Context.MODE_PRIVATE) }
    val widgetPrefs = remember { context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE) }

    var selectedSlot by remember { mutableIntStateOf(0) }

    // Multi-slot data holders
    val names = remember {
        mutableStateListOf<String>().apply {
            for (i in 0 until maxSlots) {
                val key = if (i == 0) "name_$appWidgetId" else "name_${appWidgetId}_$i"
                add(prefs.getString(key, "") ?: "")
            }
        }
    }

    val numbers = remember {
        mutableStateListOf<String>().apply {
            for (i in 0 until maxSlots) {
                val key = if (i == 0) "number_$appWidgetId" else "number_${appWidgetId}_$i"
                add(prefs.getString(key, "") ?: "")
            }
        }
    }

    val photoVersions = remember { mutableStateListOf<Long>().apply { repeat(maxSlots) { add(System.currentTimeMillis()) } } }
    var selectedOpacity by remember {
        mutableFloatStateOf(widgetPrefs.getFloat("widget_opacity_$appWidgetId", 1.0f).coerceIn(0.2f, 1.0f))
    }

    val currentName = if (selectedSlot < names.size) names[selectedSlot] else ""
    val currentNumber = if (selectedSlot < numbers.size) numbers[selectedSlot] else ""

    // Contact Picker launcher
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                var displayName = ""
                var phoneNumber = ""
                var photoStream: InputStream? = null

                // Query Display Name
                val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
                var contactId = ""
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
                        if (nameIndex != -1) displayName = it.getString(nameIndex) ?: ""
                        if (idIndex != -1) contactId = it.getString(idIndex) ?: ""
                    }
                }

                // Query Phone Number
                if (contactId.isNotBlank()) {
                    val phoneCursor = context.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(contactId),
                        null
                    )
                    phoneCursor?.use {
                        if (it.moveToFirst()) {
                            phoneNumber = it.getString(0) ?: ""
                        }
                    }
                }

                // Query Photo Stream
                try {
                    photoStream = ContactsContract.Contacts.openContactPhotoInputStream(context.contentResolver, uri, true)
                } catch (_: Exception) {}

                // Save Photo locally if exists
                if (photoStream != null) {
                    val bitmap = BitmapFactory.decodeStream(photoStream)
                    if (bitmap != null) {
                        saveContactPhoto(context, appWidgetId, selectedSlot, bitmap)
                        photoVersions[selectedSlot] = System.currentTimeMillis()
                    }
                }

                if (displayName.isNotBlank()) names[selectedSlot] = displayName
                if (phoneNumber.isNotBlank()) numbers[selectedSlot] = phoneNumber

                Toast.makeText(context, "Imported: $displayName", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to import contact: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Gallery Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    saveContactPhoto(context, appWidgetId, selectedSlot, bitmap)
                    photoVersions[selectedSlot] = System.currentTimeMillis()
                    Toast.makeText(context, "Photo set successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error setting photo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Current Photo Bitmap for preview
    val currentPhotoBitmap = remember(selectedSlot, photoVersions[selectedSlot]) {
        loadContactPhotoBitmap(context, appWidgetId, selectedSlot)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Contact Widget Setup",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) "Widget #$appWidgetId" else "Configure Contact",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Multi-Slot Chips (if multi-contact widget)
                if (maxSlots > 1) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedSlot,
                        edgePadding = 0.dp,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    ) {
                        for (i in 0 until maxSlots) {
                            Tab(
                                selected = selectedSlot == i,
                                onClick = { selectedSlot = i },
                                text = {
                                    val slotName = names.getOrNull(i)?.takeIf { it.isNotBlank() } ?: "Contact ${i + 1}"
                                    Text(slotName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Scrollable Form Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Live Interactive Preview Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = selectedOpacity)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Circular Avatar Preview
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                if (currentPhotoBitmap != null) {
                                    Image(
                                        bitmap = currentPhotoBitmap.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    val initial = currentName.trim().firstOrNull()?.uppercase() ?: "C"
                                    Text(
                                        text = initial,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            // Details & Actions
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (currentName.isNotBlank()) currentName else "Favorite Contact",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (currentNumber.isNotBlank()) currentNumber else "Tap to set number",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Primary Action: Import from Contacts
                    FilledTonalButton(
                        onClick = { contactPickerLauncher.launch(null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.ContactPhone, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pick from Contacts (Auto-fill)", fontWeight = FontWeight.SemiBold)
                    }

                    // 3. Edit Name Field
                    OutlinedTextField(
                        value = currentName,
                        onValueChange = { names[selectedSlot] = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Contact Name") },
                        placeholder = { Text("e.g. Alex Smith") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        trailingIcon = {
                            if (currentName.isNotEmpty()) {
                                IconButton(onClick = { names[selectedSlot] = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    // 4. Setup Number Field
                    OutlinedTextField(
                        value = currentNumber,
                        onValueChange = { numbers[selectedSlot] = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Phone Number") },
                        placeholder = { Text("+1 234 567 8900") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        trailingIcon = {
                            if (currentNumber.isNotEmpty()) {
                                IconButton(onClick = { numbers[selectedSlot] = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Quick Test Buttons (Call & SMS)
                    if (currentNumber.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$currentNumber")).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(dialIntent)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test Call", fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$currentNumber")).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(smsIntent)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test SMS", fontSize = 13.sp)
                            }
                        }
                    }

                    // 5. Setup Photo / Avatar Controls
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Contact Photo / Avatar", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Choose Photo", fontSize = 13.sp)
                            }

                            if (currentPhotoBitmap != null) {
                                OutlinedButton(
                                    onClick = {
                                        removeContactPhoto(context, appWidgetId, selectedSlot)
                                        photoVersions[selectedSlot] = System.currentTimeMillis()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Remove", fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // 6. Opacity Slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Widget Background Transparency: ${(selectedOpacity * 100).toInt()}%",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = selectedOpacity,
                            onValueChange = { selectedOpacity = it },
                            valueRange = 0.2f..1.0f
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Apply & Save Button
                Button(
                    onClick = {
                        val editor = prefs.edit()
                        for (i in 0 until maxSlots) {
                            val nameKey = if (i == 0) "name_$appWidgetId" else "name_${appWidgetId}_$i"
                            val numKey = if (i == 0) "number_$appWidgetId" else "number_${appWidgetId}_$i"
                            editor.putString(nameKey, names.getOrNull(i) ?: "")
                            editor.putString(numKey, numbers.getOrNull(i) ?: "")
                        }
                        editor.apply()

                        widgetPrefs.edit().putFloat("widget_opacity_$appWidgetId", selectedOpacity).apply()
                        Toast.makeText(context, "Contact Widget saved!", Toast.LENGTH_SHORT).show()
                        onApply()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = androidx.compose.ui.graphics.Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Apply & Save Widget",
                        color = androidx.compose.ui.graphics.Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun saveContactPhoto(context: Context, appWidgetId: Int, slot: Int, sourceBitmap: Bitmap) {
    try {
        val size = Math.min(sourceBitmap.width, sourceBitmap.height).coerceAtMost(512)
        val cropped = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(cropped)
        val srcLeft = (sourceBitmap.width - size) / 2
        val srcTop = (sourceBitmap.height - size) / 2
        val srcRect = Rect(srcLeft, srcTop, srcLeft + size, srcTop + size)
        val dstRect = Rect(0, 0, size, size)
        canvas.drawBitmap(sourceBitmap, srcRect, dstRect, Paint(Paint.ANTI_ALIAS_FLAG))

        val file = File(context.filesDir, "contact_photo_${appWidgetId}_$slot.png")
        FileOutputStream(file).use { out ->
            cropped.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    } catch (_: Exception) {}
}

private fun removeContactPhoto(context: Context, appWidgetId: Int, slot: Int) {
    try {
        val file = File(context.filesDir, "contact_photo_${appWidgetId}_$slot.png")
        if (file.exists()) file.delete()
    } catch (_: Exception) {}
}

fun loadContactPhotoBitmap(context: Context, appWidgetId: Int, slot: Int): Bitmap? {
    val file = File(context.filesDir, "contact_photo_${appWidgetId}_$slot.png")
    if (file.exists()) {
        try {
            return BitmapFactory.decodeFile(file.absolutePath)
        } catch (_: Exception) {}
    }
    return null
}
