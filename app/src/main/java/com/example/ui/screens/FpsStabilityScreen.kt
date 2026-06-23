package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GameBoosterViewModel

@Composable
fun FpsStabilityScreen(
    viewModel: GameBoosterViewModel,
    modifier: Modifier = Modifier
) {
    val liveFps by viewModel.liveFps.collectAsState()
    val stabilityIndex by viewModel.liveStabilityIndex.collectAsState()
    val frameTime by viewModel.liveFrameTime.collectAsState()

    val targetFps by viewModel.fpsTargetProfile.collectAsState()
    val renderProfile by viewModel.renderProfile.collectAsState()
    val ultraLowGraphics by viewModel.ultraLowGraphics.collectAsState()

    // Calculate simulated stable parameters based on selected target profile
    val simulatedAvg = remember(liveFps, targetFps) {
        if (liveFps > targetFps) targetFps.toFloat() else liveFps.toFloat()
    }
    val simulatedMin = remember(targetFps) { (targetFps * 0.88f).toInt() }
    val simulatedMax = remember(targetFps) { targetFps }
    val simulatedDrops = remember(targetFps) { (15 - (targetFps / 10)).coerceAtLeast(3) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header
        item {
            Column {
                Text(
                    text = "FRAME ENGINE & GRAPHICS",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Configure graphic limits, synchronization locks and frame stability",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // FPS Target Profile Picker
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
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "FRAME LIMITS GATES (TARGET FPS)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val fpsOptions = listOf(30, 60, 90, 120)
                        fpsOptions.forEach { fps ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (targetFps == fps) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        2.dp,
                                        if (targetFps == fps) MaterialTheme.colorScheme.secondary
                                        else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.updateFpsTarget(fps) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$fps",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (targetFps == fps) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "FPS",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Frame Stability Analysis Dashboard
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
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "FRAME CONSISTENCY DIAGNOSTICS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "STABILITY INDEX",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format("%.1f%%", stabilityIndex),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "FRAME DRAW TIME",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format("%.2f ms", frameTime),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // 4 Quadrants of Frame analysis
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("AVERAGE FPS", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format("%.1f", simulatedAvg), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("LOWEST FPS", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$simulatedMin", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("HIGHEST FPS", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$simulatedMax", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("FRAME DROPS", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$simulatedDrops", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }

        // Renderer Pipeline Optimizations
        item {
            Text(
                text = "SELECTABLE RENDERING PROFILES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Renderer cards
        data class RenderProfileInfo(val name: String, val desc: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

        val profiles = listOf(
            RenderProfileInfo("Performance Mode", "Focuses all GPU and CPU clock cycles on achieving maximum FPS frame generation, lowering display quality slightly if thermal limits are triggered.", Icons.Default.Bolt),
            RenderProfileInfo("Balanced Mode", "Maintains an optimal ratio of graphic details, physical vertex computations, and battery cooling.", Icons.Default.Equalizer),
            RenderProfileInfo("Ultra Smooth Mode", "Utilizes custom V-Sync pipeline triggers and memory alignments to minimize frame times and input delays.", Icons.Default.AutoAwesome)
        )

        profiles.forEach { profile ->
            val name = profile.name
            val desc = profile.desc
            val icon = profile.icon
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .border(
                            width = 1.dp,
                            color = if (renderProfile == name) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clickable { viewModel.updateRenderProfile(name) },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (renderProfile == name) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
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
                                contentDescription = name,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = desc,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        RadioButton(
                            selected = renderProfile == name,
                            onClick = { viewModel.updateRenderProfile(name) },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        // Ultra Low Graphics Mode
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ultra Low Graphics Mode",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Disables heavy dynamic lighting shadows, reduces standard animation speeds, and reduces drawing scales to maximize FPS in heavy skirmishes.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Switch(
                        checked = ultraLowGraphics,
                        onCheckedChange = { viewModel.toggleUltraLowGraphics(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.secondary,
                            checkedTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                        )
                    )
                }
            }
        }
    }
}
