package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.BoostState
import com.example.viewmodel.GameBoosterViewModel

@Composable
fun DashboardScreen(
    viewModel: GameBoosterViewModel,
    onNavigateToLauncher: () -> Unit,
    modifier: Modifier = Modifier
) {
    val liveFps by viewModel.liveFps.collectAsState()
    val ping by viewModel.ping.collectAsState()
    val ramUsage by viewModel.ramUsage.collectAsState()
    val cpuUsage by viewModel.cpuUsage.collectAsState()
    val temperature by viewModel.temperature.collectAsState()
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val batteryCharging by viewModel.batteryCharging.collectAsState()
    val boostState by viewModel.boostState.collectAsState()

    var showResultsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(boostState) {
        if (boostState is BoostState.Completed) {
            showResultsDialog = true
        }
    }

    // Results Alert Dialog
    if (showResultsDialog && boostState is BoostState.Completed) {
        val completed = boostState as BoostState.Completed
        AlertDialog(
            onDismissRequest = { 
                showResultsDialog = false
                viewModel.resetBoostState()
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showResultsDialog = false
                        viewModel.resetBoostState()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("OPTIMIZE COMPLETE", fontWeight = FontWeight.Bold)
                }
            },
            icon = {
                Icon(
                    Icons.Default.OfflineBolt, 
                    contentDescription = "Boost Success",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "SYSTEM DEEP-CLEAN COMPLETE", 
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Optimizations injected successfully. Idle background triggers terminated.",
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ResultStatItem(
                            label = "RAM Freed",
                            value = String.format("%.2f GB", completed.ramFreedGb),
                            icon = Icons.Default.Memory,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        ResultStatItem(
                            label = "Temp Dropped",
                            value = String.format("%.1f°C", completed.tempDropCelsius),
                            icon = Icons.Default.Thermostat,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        ResultStatItem(
                            label = "Ping Saved",
                            value = "-${completed.pingReductionMs}ms",
                            icon = Icons.Default.NetworkCheck,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // App Hero Header Banner
        item {
            StaggeredSpringEntrance(index = 0) {
                HeaderSection()
            }
        }

        // Bento Style Boost Dial Box (Major Core Card)
        item {
            StaggeredSpringEntrance(index = 1) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "ACTIVE BOOSTER CORE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "TAP TO RUN MEMORY & CPU OPTIMIZATION",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Icon(
                                Icons.Default.OfflineBolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        BoostDial(
                            boostState = boostState,
                            liveFps = liveFps,
                            onBoostClick = { viewModel.performHeavyBoost() }
                        )
                    }
                }
            }
        }

        // Real-Time Hardware Performance Telemetry Cards Grid Label
        item {
            StaggeredSpringEntrance(index = 2) {
                Text(
                    text = "REAL-TIME ENGINE SYSTEM STATUS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }
        }

        item {
            StaggeredSpringEntrance(index = 3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TelemetryCard(
                        title = "FPS LOCK",
                        value = "$liveFps",
                        subValue = "FRAME INTERVALS",
                        icon = Icons.Default.Speed,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        progress = liveFps / 120f,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryCard(
                        title = "LATENCY (PING)",
                        value = "${ping}ms",
                        subValue = if (ping < 20) "EXCELLENT" else if (ping < 35) "GOOD" else "HIGH LAGGING",
                        icon = Icons.Default.Wifi,
                        accentColor = if (ping < 25) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        progress = (100 - ping).coerceIn(1, 100) / 100f,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            StaggeredSpringEntrance(index = 4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TelemetryCard(
                        title = "RAM USAGE",
                        value = String.format("%.1f%%", ramUsage),
                        subValue = "ACTIVE CACHE",
                        icon = Icons.Default.Memory,
                        accentColor = MaterialTheme.colorScheme.primary,
                        progress = ramUsage / 100f,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryCard(
                        title = "CPU Governor",
                        value = String.format("%.1f%%", cpuUsage),
                        subValue = "8 CORE FREQ",
                        icon = Icons.Default.DeveloperMode,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        progress = cpuUsage / 100f,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            StaggeredSpringEntrance(index = 5) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TelemetryCard(
                        title = "CORE THERMAL",
                        value = String.format("%.1f °C", temperature),
                        subValue = if (temperature < 38f) "STABLE COOL" else "THERMAL LOCKING",
                        icon = Icons.Default.Thermostat,
                        accentColor = if (temperature < 38f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        progress = (temperature - 20) / 30f,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryCard(
                        title = "POWER STATUS",
                        value = "$batteryLevel%",
                        subValue = if (batteryCharging) "CHARGING (BYPASS)" else "BATTERY SUPPLY",
                        icon = if (batteryCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        progress = batteryLevel / 100f,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Launch Shortcut (Wide Bento Tile)
        item {
            StaggeredSpringEntrance(index = 6) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clickable { onNavigateToLauncher() }
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "GAME LAUNCHER ENGINE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Launch optimized games directly",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "Launcher shortcut",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "NothinG",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "CYBERNETIC PERFORMANCE FOR GAMING",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.OfflineBolt,
                contentDescription = "Core Active",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun BoostDial(
    boostState: BoostState,
    liveFps: Int,
    onBoostClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val glowAnim by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val scaleBoost = if (boostState is BoostState.Boosting) 0.95f else 1.0f

    Card(
        modifier = Modifier
            .size(240.dp)
            .shadow(
                elevation = 24.dp * glowAnim,
                shape = CircleShape,
                ambientColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), CircleShape),
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { if (boostState is BoostState.Idle) onBoostClick() }
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Circle Progress Ring Animation
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary

            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background track
                drawCircle(
                    color = primaryColor.copy(alpha = 0.05f),
                    style = Stroke(width = 8.dp.toPx())
                )

                val sweepAngle = when (boostState) {
                    is BoostState.Boosting -> boostState.progress * 360f
                    is BoostState.Completed -> 360f
                    else -> 0f
                }

                if (sweepAngle > 0f) {
                    drawArc(
                        brush = Brush.sweepGradient(listOf(primaryColor, secondaryColor)),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (boostState) {
                    is BoostState.Idle -> {
                        Icon(
                            Icons.Default.FlashOn,
                            contentDescription = "Boost now symbol",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "BOOST NOW",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "TAP TO RUN CLEANUP",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    }
                    is BoostState.Boosting -> {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = String.format("%.0f%%", boostState.progress * 100f),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = boostState.statusMessage.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    is BoostState.Completed -> {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Boost done icon",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ENGINE ACTIVE",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.tertiary,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "TAP AGAIN TO RETRY",
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetryCard(
    title: String,
    value: String,
    subValue: String,
    icon: ImageVector,
    accentColor: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(115.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accentColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subValue.uppercase(),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    letterSpacing = 0.5.sp
                )
            }

            // Small horizontal level bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }
        }
    }
}

@Composable
fun ResultStatItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.width(76.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            text = label.uppercase(),
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun StaggeredSpringEntrance(
    index: Int,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 70L)
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "entrance_alpha"
    )

    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 40.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "entrance_offset_y"
    )

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.92f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "entrance_scale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
                this.translationY = offsetY.toPx()
            }
    ) {
        content()
    }
}
