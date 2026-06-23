package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SessionEntity
import com.example.viewmodel.GameBoosterViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnalyticsScreen(
    viewModel: GameBoosterViewModel,
    modifier: Modifier = Modifier
) {
    val sessions by viewModel.sessionHistory.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf("Weekly") } // Daily, Weekly, Monthly

    // Filtered historical logs based on chosen interval window
    val filteredSessions = remember(sessions, selectedTab) {
        val count = if (selectedTab == "Daily") 3 else if (selectedTab == "Weekly") 7 else 15
        sessions.take(count).reversed()
    }

    val avgFps = remember(filteredSessions) {
        if (filteredSessions.isEmpty()) 60f
        else filteredSessions.map { it.avgFps }.average().toFloat()
    }

    val avgPing = remember(filteredSessions) {
        if (filteredSessions.isEmpty()) 22
        else filteredSessions.map { it.avgPing }.average().toInt()
    }

    val totalGamingTimeMin = remember(filteredSessions) {
        if (filteredSessions.isEmpty()) 0L
        else filteredSessions.map { it.durationSeconds }.sum() / 60
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Module Title
        item {
            Column {
                Text(
                    text = "PERFORMANCE ANALYTICS",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Historical telemetry trends of gaming performance metrics",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Time Window Filters Selector
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                val tabs = listOf("Daily", "Weekly", "Monthly")
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.surface
                                else Color.Transparent
                            )
                            .clickable { selectedTab = tab },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // Overview KPI metrics cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnalyticsKpiCard(
                    title = "Avg Stability FPS",
                    value = String.format("%.1f", avgFps),
                    icon = Icons.Default.TrendingUp,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                AnalyticsKpiCard(
                    title = "Avg Network Ping",
                    value = "${avgPing}ms",
                    icon = Icons.Default.NetworkCheck,
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                AnalyticsKpiCard(
                    title = "Total Active Min",
                    value = "${totalGamingTimeMin}m",
                    icon = Icons.Default.HourglassEmpty,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Graph 1: FPS History Canvas
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
                        text = "FPS HISTORICAL TELEMETRY TRENDS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (filteredSessions.isEmpty()) {
                        EmptyGraphPlaceholder()
                    } else {
                        // Drawing FPS custom line graph
                        val fpsValues = filteredSessions.map { it.avgFps }
                        val labels = filteredSessions.map { formatTimestamp(it.timestamp) }
                        TelemetryAreaChart(
                            values = fpsValues,
                            labels = labels,
                            lineColor = MaterialTheme.colorScheme.secondary,
                            maxConstraint = 120f
                        )
                    }
                }
            }
        }

        // Graph 2: Network Ping Latency History Canvas
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
                        text = "PING NETWORK STABILITY HISTORY (MS)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (filteredSessions.isEmpty()) {
                        EmptyGraphPlaceholder()
                    } else {
                        // Drawing Ping custom line graph (lower values are better)
                        val pingValues = filteredSessions.map { it.avgPing.toFloat() }
                        val labels = filteredSessions.map { formatTimestamp(it.timestamp) }
                        TelemetryAreaChart(
                            values = pingValues,
                            labels = labels,
                            lineColor = MaterialTheme.colorScheme.tertiary,
                            maxConstraint = 60f
                        )
                    }
                }
            }
        }

        // Data Management - Export CSV and Reset Cache
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "DATA MANAGEMENT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Export weekly gaming telemetry logs as a CSV spreadsheet or wipe the database.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.exportWeeklyStatsCsv(
                                    onSuccess = { file ->
                                        try {
                                            val authority = "${context.packageName}.fileprovider"
                                            val uri = FileProvider.getUriForFile(context, authority, file)
                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/csv"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(Intent.createChooser(intent, "Export Gaming Statistics"))
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error sharing CSV: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    onError = { error ->
                                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            modifier = Modifier.weight(1.4f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("EXPORT WEEKLY CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        
                        OutlinedButton(
                            onClick = { viewModel.clearAllStats() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("RESET", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsKpiCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(95.dp)
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
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 10.sp
                )
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun TelemetryAreaChart(
    values: List<Float>,
    labels: List<String>,
    lineColor: Color,
    maxConstraint: Float
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        val width = size.width
        val height = size.height

        val paddingLeft = 30f
        val paddingRight = 10f
        val paddingTop = 10f
        val paddingBottom = 20f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        if (values.size < 2) return@Canvas

        val maxVal = maxConstraint.coerceAtLeast(values.maxOrNull() ?: 1f)
        val minVal = 0f

        val stepX = chartWidth / (values.size - 1)
        val valueRange = maxVal - minVal

        // Draw horizontal help lines
        val helperLines = 3
        for (i in 0..helperLines) {
            val yFactor = i.toFloat() / helperLines
            val yCo = paddingTop + chartHeight * (1f - yFactor)
            drawLine(
                color = Color.White.copy(alpha = 0.05f),
                start = Offset(paddingLeft, yCo),
                end = Offset(width - paddingRight, yCo),
                strokeWidth = 2f
            )
        }

        // Construct linear path
        val path = Path()
        val filledPath = Path()

        values.forEachIndexed { index, valRaw ->
            val x = paddingLeft + (index * stepX)
            val valPercent = (valRaw - minVal) / valueRange
            val y = paddingTop + chartHeight * (1f - valPercent)

            if (index == 0) {
                path.moveTo(x, y)
                filledPath.moveTo(x, y)
            } else {
                path.lineTo(x, y)
                filledPath.lineTo(x, y)
            }

            // Close the path at the end for gradient fill
            if (index == values.size - 1) {
                filledPath.lineTo(x, paddingTop + chartHeight)
                filledPath.lineTo(paddingLeft, paddingTop + chartHeight)
                filledPath.close()
            }
        }

        // Draw filled gradient underneath the telemetry path
        drawPath(
            path = filledPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.25f),
                    Color.Transparent
                )
            )
        )

        // Draw active telemetry path line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 5f)
        )

        // Draw data points anchors
        values.forEachIndexed { index, valRaw ->
            val x = paddingLeft + (index * stepX)
            val valPercent = (valRaw - minVal) / valueRange
            val y = paddingTop + chartHeight * (1f - valPercent)

            drawCircle(
                color = Color.White,
                radius = 6f,
                center = Offset(x, y)
            )
            drawCircle(
                color = lineColor,
                radius = 3.5f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun EmptyGraphPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("No gaming logs registered yet for this time block.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
}

fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val sdf = SimpleDateFormat("MM/dd", Locale.getDefault())
    return sdf.format(date)
}
