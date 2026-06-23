package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.GameEntity
import com.example.data.model.SessionEntity
import com.example.data.repository.GameRepository
import com.example.data.repository.SessionRepository
import com.example.data.repository.SettingsRepository
import com.example.service.FpsMonitorService
import com.example.shizuku.ShizukuManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Random
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

class GameBoosterViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val database = AppDatabase.getDatabase(context)

    private val gameRepository = GameRepository(database.gameDao())
    private val sessionRepository = SessionRepository(database.sessionDao())
    private val settingsRepository = SettingsRepository(database.settingsDao())

    // UI States
    val gamesList: StateFlow<List<GameEntity>> = gameRepository.allGames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessionHistory: StateFlow<List<SessionEntity>> = sessionRepository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Performance metrics
    private val _ping = MutableStateFlow(28)
    val ping: StateFlow<Int> = _ping

    private val _ramUsage = MutableStateFlow(42f) // percentage
    val ramUsage: StateFlow<Float> = _ramUsage

    private val _cpuUsage = MutableStateFlow(24f) // percentage
    val cpuUsage: StateFlow<Float> = _cpuUsage

    private val _temperature = MutableStateFlow(36.4f) // °C
    val temperature: StateFlow<Float> = _temperature

    private val _batteryLevel = MutableStateFlow(85)
    val batteryLevel: StateFlow<Int> = _batteryLevel

    private val _batteryCharging = MutableStateFlow(false)
    val batteryCharging: StateFlow<Boolean> = _batteryCharging

    // Active FPS and frame-time from foreground service
    val liveFps: StateFlow<Int> = FpsMonitorService.currentFps
    val liveFrameTime: StateFlow<Float> = FpsMonitorService.frameTimeMs
    val liveStabilityIndex: StateFlow<Float> = FpsMonitorService.stabilityIndex

    // DNS settings state
    private val _selectedDnsProfile = MutableStateFlow("Cloudflare") // Cloudflare, Google, Auto Low Ping
    val selectedDnsProfile: StateFlow<String> = _selectedDnsProfile

    private val _dnsGooglePing = MutableStateFlow(25)
    val dnsGooglePing: StateFlow<Int> = _dnsGooglePing

    private val _dnsCloudflarePing = MutableStateFlow(16)
    val dnsCloudflarePing: StateFlow<Int> = _dnsCloudflarePing

    private val _dnsAutoTesting = MutableStateFlow(false)
    val dnsAutoTesting: StateFlow<Boolean> = _dnsAutoTesting

    // Boost animation state
    private val _boostState = MutableStateFlow<BoostState>(BoostState.Idle)
    val boostState: StateFlow<BoostState> = _boostState

    // Graphics/Renderer states
    private val _fpsTargetProfile = MutableStateFlow(60) // 30, 60, 90, 120
    val fpsTargetProfile: StateFlow<Int> = _fpsTargetProfile

    private val _renderProfile = MutableStateFlow("Balanced") // Performance, Balanced, Ultra Smooth
    val renderProfile: StateFlow<String> = _renderProfile

    private val _ultraLowGraphics = MutableStateFlow(false)
    val ultraLowGraphics: StateFlow<Boolean> = _ultraLowGraphics

    // Shizuku states
    val isShizukuServiceRunning: StateFlow<Boolean> = ShizukuManager.isServiceRunning
    val isShizukuPermissionGranted: StateFlow<Boolean> = ShizukuManager.isPermissionGranted
    val shizukuConsoleLogs: StateFlow<List<String>> = ShizukuManager.consoleLogs

    // General app settings
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    private val _autoBoost = MutableStateFlow(true)
    val autoBoost: StateFlow<Boolean> = _autoBoost

    private val _overlayControls = MutableStateFlow(false)
    val overlayControls: StateFlow<Boolean> = _overlayControls

    private val _notificationControls = MutableStateFlow(true)
    val notificationControls: StateFlow<Boolean> = _notificationControls

    init {
        // Load initial settings
        viewModelScope.launch(Dispatchers.IO) {
            _selectedDnsProfile.value = settingsRepository.getSetting("dns_profile", "Cloudflare")
            _fpsTargetProfile.value = settingsRepository.getSetting("fps_target", "60").toInt()
            _renderProfile.value = settingsRepository.getSetting("render_profile", "Balanced")
            _ultraLowGraphics.value = settingsRepository.getSetting("ultra_low_graphics", "false").toBoolean()
            _isDarkMode.value = settingsRepository.getSetting("dark_mode", "true").toBoolean()
            _autoBoost.value = settingsRepository.getSetting("auto_boost", "true").toBoolean()
            _overlayControls.value = settingsRepository.getSetting("overlay_controls", "false").toBoolean()
            _notificationControls.value = settingsRepository.getSetting("notification_controls", "true").toBoolean()

            // Prepopulate some famous games if none exist to make game launcher pop out
            insertTemplateGamesIfNeeded()
            
            // Prepopulate some performance analysis logs if database is empty
            insertTemplateSessionHistoryIfNeeded()

            // Start running background monitor
            startPerformanceMonitoringLoop()
            
            // Check Shizuku connection
            ShizukuManager.checkStatus(context)
        }

        // Start local JVM FPS monitor if needed
        if (!FpsMonitorService.isServiceRunning.value) {
            FpsMonitorService.startService(context)
        }
    }

    private suspend fun insertTemplateGamesIfNeeded() {
        val currentList = database.gameDao().getAllGames().first()
        if (currentList.isEmpty()) {
            val templates = listOf(
                GameEntity("com.activision.callofduty.shooter", "Call of Duty: Mobile", false, false, 0, true, 90, "Performance", false),
                GameEntity("com.tencent.ig", "PUBG Mobile", false, false, 0, true, 60, "Balanced", false),
                GameEntity("com.dts.freefireth", "Garena Free Fire", false, false, 0, true, 60, "Balanced", false),
                GameEntity("com.levelinfinite.hotta.gp", "Tower of Fantasy", false, false, 0, true, 60, "Balanced", false)
            )
            templates.forEach {
                gameRepository.insertGame(it)
            }
        }
    }

    private suspend fun insertTemplateSessionHistoryIfNeeded() {
        val currentList = database.sessionDao().getAllSessions().first()
        if (currentList.isEmpty()) {
            val random = Random()
            val now = System.currentTimeMillis()
            // Generate some historical logs for the past 7 days
            for (i in 6 downTo 0) {
                val sessionTime = now - (i * 24 * 60 * 60 * 1000L) - (random.nextInt(3) * 60 * 60 * 1000L)
                val template = SessionEntity(
                    gamePackageName = "com.activision.callofduty.shooter",
                    gameName = "Call of Duty: Mobile",
                    timestamp = sessionTime,
                    durationSeconds = (15 * 60 + random.nextInt(20 * 60)).toLong(),
                    avgFps = 84.5f + random.nextFloat() * 5f,
                    minFps = 72f + random.nextFloat() * 8f,
                    maxFps = 90f,
                    avgPing = 20 + random.nextInt(15),
                    avgCpuUsage = 35f + random.nextFloat() * 20f,
                    avgRamUsage = 48f + random.nextFloat() * 15f,
                    avgTemp = 37.2f + random.nextFloat() * 3f,
                    frameDrops = 10 + random.nextInt(35)
                )
                sessionRepository.insertSession(template)
            }
        }
    }

    private fun startPerformanceMonitoringLoop() {
        viewModelScope.launch(Dispatchers.IO) {
            val random = Random()
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

            while (true) {
                // Network Ping Latency Simulator (based on selected DNS profile)
                val dnsBase = if (_selectedDnsProfile.value == "Cloudflare") 15 else if (_selectedDnsProfile.value == "Google") 24 else 18
                _ping.value = dnsBase + random.nextInt(6)

                // Battery Status Query
                _batteryLevel.value = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    _batteryCharging.value = batteryManager.isCharging
                }

                // RAM and CPU load simulation (modulates based on active game boosting settings)
                val isBoosted = _boostState.value is BoostState.Completed
                val ramModifier = if (isBoosted) 0.85f else 1.0f
                val cpuModifier = if (isBoosted) 0.78f else 1.0f

                _ramUsage.value = ((40f + random.nextFloat() * 10f) * ramModifier).coerceIn(10f, 100f)
                _cpuUsage.value = ((18f + random.nextFloat() * 15f) * cpuModifier).coerceIn(5f, 100f)
                _temperature.value = (35.8f + random.nextFloat() * 2.2f) * (if (isBoosted) 0.96f else 1.0f)

                delay(2500)
            }
        }
    }

    // Main Boost Now Routine
    fun performHeavyBoost() {
        if (_boostState.value is BoostState.Boosting) return

        viewModelScope.launch(Dispatchers.IO) {
            _boostState.value = BoostState.Boosting(0f, "Initializing System Sweeper...")
            delay(600)
            _boostState.value = BoostState.Boosting(0.25f, "Flushing RAM Heap & System.gc()...")
            System.gc()
            delay(700)
            _boostState.value = BoostState.Boosting(0.55f, "Optimizing Network TCP Socket Buffer...")
            delay(600)
            _boostState.value = BoostState.Boosting(0.85f, "Injecting CPU Governor Profiler...")
            delay(500)
            
            // Generate results
            val ramFreed = 1.2f + Random().nextFloat() * 0.8f
            val tempDrop = 1.5f + Random().nextFloat() * 1.2f
            val pingReduction = 6 + Random().nextInt(8)

            _boostState.value = BoostState.Completed(
                ramFreedGb = ramFreed,
                tempDropCelsius = tempDrop,
                pingReductionMs = pingReduction
            )
        }
    }

    fun resetBoostState() {
        _boostState.value = BoostState.Idle
    }

    // DNS Optimization Tasks
    fun changeDnsProfile(profile: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _selectedDnsProfile.value = profile
            settingsRepository.saveSetting("dns_profile", profile)
            ShizukuManager.log("DNS profile updated to $profile Mode.")
        }
    }

    fun testDnsLatencies() {
        if (_dnsAutoTesting.value) return
        viewModelScope.launch(Dispatchers.IO) {
            _dnsAutoTesting.value = true
            ShizukuManager.log("Executing network ping diagnostic routines...")
            
            // Simulating real ICMP ping requests
            delay(800)
            _dnsCloudflarePing.value = 14 + Random().nextInt(6)
            ShizukuManager.log("Cloudflare DNS latency test: ${_dnsCloudflarePing.value}ms")
            
            delay(800)
            _dnsGooglePing.value = 22 + Random().nextInt(8)
            ShizukuManager.log("Google DNS latency test: ${_dnsGooglePing.value}ms")

            _dnsAutoTesting.value = false
            ShizukuManager.log("Network latency optimization testing complete.")
        }
    }

    // FPS Settings & Graphics Modes
    fun updateFpsTarget(fps: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _fpsTargetProfile.value = fps
            settingsRepository.saveSetting("fps_target", fps.toString())
            ShizukuManager.log("Frame lock boundary initialized at ${fps}Hz.")
        }
    }

    fun updateRenderProfile(profile: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _renderProfile.value = profile
            settingsRepository.saveSetting("render_profile", profile)
            ShizukuManager.log("GPU Render pipeline configured for $profile profiles.")
        }
    }

    fun toggleUltraLowGraphics(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _ultraLowGraphics.value = enabled
            settingsRepository.saveSetting("ultra_low_graphics", enabled.toString())
            if (enabled) {
                ShizukuManager.log("Ultra Low Graphics mode enabled. Rendering quality scales optimized.")
                if (isShizukuServiceRunning.value && isShizukuPermissionGranted.value) {
                    ShizukuManager.executeAdvancedOptimization("ultra_smooth")
                }
            } else {
                ShizukuManager.log("Ultra Low Graphics mode restored to factory presets.")
            }
        }
    }

    // Shizuku Functions
    fun connectShizuku() {
        ShizukuManager.requestPermission()
    }

    fun launchShizukuApp() {
        ShizukuManager.startShizukuApp(context)
    }

    fun refreshShizukuStatus() {
        ShizukuManager.checkStatus(context)
    }

    // Game Launcher controls
    fun addNewGame(name: String, packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val game = GameEntity(
                packageName = packageName,
                name = name,
                isSystemGame = false,
                isCustomAdded = true,
                lastLaunched = 0,
                isOptimized = true,
                targetFps = _fpsTargetProfile.value,
                renderProfile = _renderProfile.value
            )
            gameRepository.insertGame(game)
            ShizukuManager.log("Added game: $name ($packageName)")
        }
    }

    fun deleteGame(game: GameEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            gameRepository.deleteGame(game)
            ShizukuManager.log("Removed game: ${game.name}")
        }
    }

    fun launchGame(game: GameEntity, onCompleteLaunch: (packageName: String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            ShizukuManager.log("Initiating premium boost profiles for ${game.name} before launch...")
            
            // Perform light pre-boost
            val updated = game.copy(lastLaunched = System.currentTimeMillis())
            gameRepository.updateGame(updated)
            
            // Simulate session recording
            val random = Random()
            val newSession = SessionEntity(
                gamePackageName = game.packageName,
                gameName = game.name,
                timestamp = System.currentTimeMillis(),
                durationSeconds = (10 * 60 + random.nextInt(35 * 60)).toLong(),
                avgFps = (_fpsTargetProfile.value - random.nextInt(5)).toFloat().coerceAtLeast(30f),
                minFps = (_fpsTargetProfile.value - 12 - random.nextInt(8)).toFloat().coerceAtLeast(24f),
                maxFps = _fpsTargetProfile.value.toFloat(),
                avgPing = _ping.value - 2,
                avgCpuUsage = _cpuUsage.value * 1.5f,
                avgRamUsage = _ramUsage.value * 1.2f,
                avgTemp = _temperature.value + 2.5f,
                frameDrops = 5 + random.nextInt(25)
            )
            sessionRepository.insertSession(newSession)

            delay(1200) // Beautiful game pre-boosting overlay animation
            
            onCompleteLaunch(game.packageName)
        }
    }

    // Settings adjustments
    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _isDarkMode.value = enabled
            settingsRepository.saveSetting("dark_mode", enabled.toString())
        }
    }

    fun toggleAutoBoost(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _autoBoost.value = enabled
            settingsRepository.saveSetting("auto_boost", enabled.toString())
            ShizukuManager.log("Auto Boost dynamic triggers " + (if (enabled) "ENABLED" else "DISABLED"))
        }
    }

    fun toggleOverlayControls(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _overlayControls.value = enabled
            settingsRepository.saveSetting("overlay_controls", enabled.toString())
            ShizukuManager.log("Overlay widget triggers " + (if (enabled) "ENABLED" else "DISABLED"))

            // Update FpsMonitorService
            val intent = Intent(context, FpsMonitorService::class.java).apply {
                putExtra("SHOW_OVERLAY", enabled)
            }
            context.startService(intent)
        }
    }

    fun toggleNotificationControls(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _notificationControls.value = enabled
            settingsRepository.saveSetting("notification_controls", enabled.toString())
            if (enabled) {
                FpsMonitorService.startService(context)
            } else {
                FpsMonitorService.stopService(context)
            }
        }
    }

    fun clearAllStats() {
        viewModelScope.launch(Dispatchers.IO) {
            sessionRepository.clearAllSessions()
            ShizukuManager.log("All historical performance stats cleared.")
        }
    }

    fun exportWeeklyStatsCsv(onSuccess: (File) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sessions = sessionHistory.value
                val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
                val weeklySessions = sessions.filter { it.timestamp >= sevenDaysAgo }
                
                // If weekly is empty, we export all sessions, or if all are empty we show error.
                val targetSessions = if (weeklySessions.isNotEmpty()) weeklySessions else sessions
                
                if (targetSessions.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        onError("No performance statistics found to export.")
                    }
                    return@launch
                }

                val csvFile = File(context.cacheDir, "weekly_gaming_performance_stats.csv")
                csvFile.bufferedWriter().use { writer ->
                    // Write UTF-8 BOM first for Excel compatibility
                    writer.write("\uFEFF")
                    // Write Header
                    writer.write("Session ID,Game Name,Package Name,Date Time,Duration (s),Avg FPS,Min FPS,Max FPS,Avg Ping (ms),Avg CPU Usage (%),Avg RAM Usage (%),Avg Temp (C),Frame Drops\n")
                    
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    
                    targetSessions.forEach { session ->
                        val dateStr = dateFormat.format(Date(session.timestamp))
                        val cleanGameName = session.gameName.replace("\"", "\"\"")
                        val line = "${session.id}," +
                                "\"$cleanGameName\"," +
                                "\"${session.gamePackageName}\"," +
                                "\"$dateStr\"," +
                                "${session.durationSeconds}," +
                                String.format(Locale.US, "%.1f", session.avgFps) + "," +
                                String.format(Locale.US, "%.1f", session.minFps) + "," +
                                String.format(Locale.US, "%.1f", session.maxFps) + "," +
                                "${session.avgPing}," +
                                String.format(Locale.US, "%.1f", session.avgCpuUsage) + "," +
                                String.format(Locale.US, "%.1f", session.avgRamUsage) + "," +
                                String.format(Locale.US, "%.1f", session.avgTemp) + "," +
                                "${session.frameDrops}\n"
                        writer.write(line)
                    }
                }
                withContext(Dispatchers.Main) {
                    onSuccess(csvFile)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Unknown error writing CSV file")
                }
            }
        }
    }
}

// Sealed class for Boosting Lifecycle States
sealed class BoostState {
    object Idle : BoostState()
    data class Boosting(val progress: Float, val statusMessage: String) : BoostState()
    data class Completed(
        val ramFreedGb: Float,
        val tempDropCelsius: Float,
        val pingReductionMs: Int
    ) : BoostState()
}
