package com.jndapp.minimal.widgets.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*

@Composable
fun SettingsScreen(
    paddingValues: PaddingValues,
    onCheckUpdate: () -> Unit = {}
) {
    val context = LocalContext.current
    var showWhatsNewDialog by remember { mutableStateOf(false) }

    val lottieComposition by rememberLottieComposition(LottieCompositionSpec.Asset("panda_animation.json"))
    val lottieProgress by animateLottieCompositionAsState(
        composition = lottieComposition,
        iterations = LottieConstants.IterateForever
    )

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
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
                    text = "Settings",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Minimal Widgets V2.2.06 (16)",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // App Maintenance Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    SettingsRow(
                        icon = Icons.Rounded.SystemUpdate,
                        title = "Tap To Check Update",
                        subtitle = "Check Google Play for new widgets and improvements",
                        onClick = onCheckUpdate
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    SettingsRow(
                        icon = Icons.Rounded.NewReleases,
                        title = "What's New",
                        subtitle = "Changelog and recent feature additions",
                        onClick = { showWhatsNewDialog = true }
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    SettingsRow(
                        icon = Icons.Rounded.Restore,
                        title = "Restore Purchases",
                        subtitle = "Restore Pro features and unlocked themes",
                        onClick = {
                            Toast.makeText(context, "Checking existing purchase entitlements…", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // Social Connect Section
        item {
            Text(
                text = "SOCIAL CONNECT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    SettingsRow(
                        icon = Icons.Rounded.Share,
                        title = "Twitter / X",
                        subtitle = "@justnewdesigns",
                        onClick = { openUrl("https://twitter.com/justnewdesigns") }
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    SettingsRow(
                        icon = Icons.Rounded.PhotoCamera,
                        title = "Instagram",
                        subtitle = "@justnewdesigns",
                        onClick = { openUrl("https://instagram.com/justnewdesigns") }
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    SettingsRow(
                        icon = Icons.Rounded.PlayArrow,
                        title = "YouTube",
                        subtitle = "JustNewDesigns previews & tutorials",
                        onClick = { openUrl("https://youtube.com/@justnewdesigns") }
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    SettingsRow(
                        icon = Icons.Rounded.Language,
                        title = "Website",
                        subtitle = "justnewdesigns.com",
                        onClick = { openUrl("https://justnewdesigns.com") }
                    )
                }
            }
        }

        // Lottie Mascot Animation & Footer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LottieAnimation(
                    composition = lottieComposition,
                    progress = { lottieProgress },
                    modifier = Modifier.size(160.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Designed by JustNewDesigns",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Crafted with precision for minimal aesthetics",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }

    if (showWhatsNewDialog) {
        AlertDialog(
            onDismissRequest = { showWhatsNewDialog = false },
            title = { Text("What's New in v2.2.06", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Added 15+ new minimal dark & pastel themes in Studio.")
                    Text("• Improved battery widget sync with battery saving mode.")
                    Text("• Optimized compass sensor listener for lower battery drain.")
                    Text("• Android 15 edge-to-edge support.")
                    Text("• Bug fixes and layout alignments for Nothing and Pixel phones.")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showWhatsNewDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Awesome", color = Color.Black)
                }
            }
        )
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}
