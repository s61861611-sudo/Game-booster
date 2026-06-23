package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GameBoosterViewModel

@Composable
fun SettingsScreen(
    viewModel: GameBoosterViewModel,
    modifier: Modifier = Modifier
) {
    val darkMode by viewModel.isDarkMode.collectAsState()
    val autoBoost by viewModel.autoBoost.collectAsState()
    val overlayControls by viewModel.overlayControls.collectAsState()
    val notificationControls by viewModel.notificationControls.collectAsState()
    val selectedDns by viewModel.selectedDnsProfile.collectAsState()
    val renderProfile by viewModel.renderProfile.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title
        item {
            Column {
                Text(
                    text = "SETTINGS ENGINE",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Fine-tune performance triggers and application look-and-feel",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Section: Appearance Settings
        item {
            SettingsCategoryHeader(title = "Appearance & Interface")
        }

        item {
            SettingsSwitchCard(
                title = "Force Dark Mode",
                description = "Locks application graphics into dynamic cyber dark theme palettes.",
                icon = Icons.Default.Brightness4,
                checked = darkMode,
                onCheckedChange = { viewModel.toggleDarkMode(it) }
            )
        }

        item {
            SettingsSwitchCard(
                title = "Floating FPS Overlay Widget",
                description = "Shows real-time hardware frame rates overlay while gaming. Requires overlay drawing authorization.",
                icon = Icons.Default.PictureInPicture,
                checked = overlayControls,
                onCheckedChange = { viewModel.toggleOverlayControls(it) }
            )
        }

        // Section: Booster Profiles
        item {
            SettingsCategoryHeader(title = "Booster & Automation Rules")
        }

        item {
            SettingsSwitchCard(
                title = "Intelligent Auto-Boost",
                description = "Automatically cleans cache blocks and terminates dead background threads in 30-minute intervals.",
                icon = Icons.Default.Loop,
                checked = autoBoost,
                onCheckedChange = { viewModel.toggleAutoBoost(it) }
            )
        }

        item {
            SettingsSwitchCard(
                title = "Foreground Engine Notifications",
                description = "Spawns a persistent low-latency system channel task to keep FPS monitor service alive in intense gaming sessions.",
                icon = Icons.Default.NotificationsActive,
                checked = notificationControls,
                onCheckedChange = { viewModel.toggleNotificationControls(it) }
            )
        }

        // Selected summaries
        item {
            SettingsValueCard(
                title = "Dynamic Gateway DNS Profile",
                subtitle = "Active Profile",
                value = if (selectedDns == "Auto") "Auto Low Ping Selection" else "$selectedDns Mode",
                icon = Icons.Default.Dns
            )
        }

        item {
            SettingsValueCard(
                title = "Active Vulkan Rendering Pipeline",
                subtitle = "Governor profile",
                value = renderProfile,
                icon = Icons.Default.Memory
            )
        }

        // Section: App Metadata Info
        item {
            SettingsCategoryHeader(title = "Booster Specifications")
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    BuildInfoRow(label = "Application Version", value = "NothinG Booster v1.4.2")
                    BuildInfoRow(label = "System API Target Level", value = "Android 15+ (API 36)")
                    BuildInfoRow(label = "Secure Binder Provider", value = "Shizuku Multi-Channel binder v1.0")
                }
            }
        }
    }
}

@Composable
fun SettingsCategoryHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsSwitchCard(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.secondary,
                    checkedTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                )
            )
        }
    }
}

@Composable
fun SettingsValueCard(
    title: String,
    subtitle: String,
    value: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = value.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun BuildInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
