package com.nxo.debloat

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import rikka.shizuku.Shizuku
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private val executor = Executors.newSingleThreadExecutor()

    private val shizukuPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            refreshState()
        }

    private var shizukuReady by mutableStateOf(false)
    private var logText by mutableStateOf("Ready.")
    private var monitorText by mutableStateOf("Device monitor: waiting")
    private var packageText by mutableStateOf("Checking Free Fire packages…")
    private var touchText by mutableStateOf("Not checked")
    private var surfaceText by mutableStateOf("Not checked")
    private var selectedGame by mutableStateOf(GameTools.FF)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GameNotificationListener.listener = {
            runOnUiThread {
                monitorText = "Notification: $it"
                logText = "Monitoring event received."
            }
        }

        setContent {
            NXOTheme {
                NXOApp()
            }
        }

        refreshState()
        refreshPackages()
    }

    override fun onResume() {
        super.onResume()
        refreshState()
    }

    private fun refreshState() {
        shizukuReady = Shell.available()
        monitorText = if (notificationAccess()) {
            "Device monitor: notification access active"
        } else {
            "Device monitor: notification access not granted"
        }
    }

    private fun notificationAccess(): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver, "enabled_notification_listeners"
        ) ?: return false
        val me = ComponentName(this, GameNotificationListener::class.java).flattenToString()
        return enabled.split(":").any { it.equals(me, true) }
    }

    private fun refreshPackages() {
        executor.execute {
            val ff = GameTools.installed(GameTools.FF)
            val max = GameTools.installed(GameTools.MAX)
            runOnUiThread {
                packageText = "FF: ${if (ff) "installed" else "not found"} • MAX: ${if (max) "installed" else "not found"}"
            }
        }
    }

    private fun requireShizuku(action: () -> Unit) {
        if (!Shell.available()) {
            logText = "Start Shizuku and grant NXO DEBLOAT permission first."
            return
        }
        executor.execute(action)
    }

    private fun compile(pkg: String, mode: String) {
        requireShizuku {
            val result = if (mode == "AOT") {
                GameTools.compileSpeed(pkg)
            } else {
                GameTools.compileSpeedProfile(pkg)
            }
            runOnUiThread {
                logText = "$mode compile ${if (result.ok) "completed" else "failed"}:\n${result.text}"
            }
        }
    }

    private fun cleanCache(pkg: String) {
        requireShizuku {
            val result = GameTools.clearAppCache(pkg)
            runOnUiThread {
                logText = "Cache clean ${if (result.ok) "completed" else "failed"}:\n${result.text}"
            }
        }
    }

    private fun tuneRefresh() {
        val info = DeviceInfo.displayInfo(this)
        requireShizuku {
            val result = GameTools.requestMaxRefreshRate(info.maxHz)
            runOnUiThread {
                logText = "Requested max ${info.maxHz} Hz. ${result.text}"
            }
        }
    }

    private fun diagnostics() {
        requireShizuku {
            val touch = GameTools.inputDiagnostics()
            val sf = GameTools.gpuSurfaceDiagnostics()
            runOnUiThread {
                touchText = touch.text.ifBlank { "No touch diagnostics returned." }
                surfaceText = sf.text.ifBlank { "No SurfaceFlinger diagnostics returned." }
            }
        }
    }

    @Composable
    private fun NXOApp() {
        val display = DeviceInfo.displayInfo(this)

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("NXO DEBLOAT", fontWeight = FontWeight.Bold)
                            Text("Shizuku • non-root", fontSize = 12.sp)
                        }
                    }
                )
            }
        ) { pad ->
            Column(
                Modifier
                    .padding(pad)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StatusCard(
                    title = "Daemon & Device Monitor",
                    status = if (shizukuReady) "SHIZUKU READY" else "SHIZUKU OFF",
                    body = "${DeviceInfo.summary(this@MainActivity)}\n$monitorText",
                    button = "Open Shizuku"
                ) {
                    try {
                        startActivity(Intent("moe.shizuku.privileged.api.MAIN_SETTINGS"))
                    } catch (_: Exception) {
                        logText = "Open Shizuku manually, then grant permission."
                    }
                }

                Card {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Free Fire Optimizer", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(packageText, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selectedGame == GameTools.FF, { selectedGame = GameTools.FF }, { Text("FF") })
                            FilterChip(selectedGame == GameTools.MAX, { selectedGame = GameTools.MAX }, { Text("FF MAX") })
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { compile(selectedGame, "AOT") }, modifier = Modifier.weight(1f)) {
                                Text("AOT Compile")
                            }
                            OutlinedButton(onClick = { compile(selectedGame, "JIT/Profile") }, modifier = Modifier.weight(1f)) {
                                Text("Profile Compile")
                            }
                        }

                        OutlinedButton(
                            onClick = { cleanCache(selectedGame) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Auto Clean Cache") }

                        Text(
                            "JIT is managed by Android ART at runtime. The buttons expose real ART compilation modes; they do not fake a JIT switch.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Card {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Refresh / FPS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "Supported: ${display.supportedHz.joinToString(", ") { "%.0f".format(it) }} Hz\nMaximum detected: ${"%.2f".format(display.maxHz)} Hz",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { tuneRefresh() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Auto Max Refresh Rate") }
                        OutlinedButton(
                            onClick = { diagnostics() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Detect FPS / SurfaceFlinger") }
                        Text(
                            "The app requests the highest refresh mode reported by Android. The system/OEM scheduler can still reject or change it.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Card {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Touch / SurfaceFlinger", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Touch diagnostics", fontWeight = FontWeight.SemiBold)
                        Text(touchText, style = MaterialTheme.typography.bodySmall)
                        HorizontalDivider()
                        Text("SurfaceFlinger diagnostics", fontWeight = FontWeight.SemiBold)
                        Text(surfaceText, style = MaterialTheme.typography.bodySmall)
                        Text(
                            "No fake touch-speed number is shown. Physical polling/touch sampling is hardware and OEM dependent.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Card {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Crosshair", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Crosshair menu is kept inside the normal Activity; no floating overlay is created.")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(onClick = { logText = "Crosshair preset: Dot" }, label = { Text("Dot") })
                            AssistChip(onClick = { logText = "Crosshair preset: Plus" }, label = { Text("Plus") })
                            AssistChip(onClick = { logText = "Crosshair preset: Circle" }, label = { Text("Circle") })
                        }
                    }
                }

                Card {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Daemon / Notification Monitor", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(monitorText)
                        Button(
                            onClick = {
                                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Open Notification Access") }
                    }
                }

                Card {
                    Column(Modifier.padding(18.dp)) {
                        Text("Live log", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(logText, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    @Composable
    private fun StatusCard(
        title: String,
        status: String,
        body: String,
        button: String,
        onClick: () -> Unit
    ) {
        Card {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    AssistChip(onClick = {}, label = { Text(status) })
                }
                Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                    Text(button)
                }
            }
        }
    }

    @Composable
    private fun NXOTheme(content: @Composable () -> Unit) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = Color(0xFF9CCBFF),
                secondary = Color(0xFFB8C8DA),
                background = Color(0xFF0B0F14),
                surface = Color(0xFF11161C)
            ),
            content = content
        )
    }

    override fun onDestroy() {
        GameNotificationListener.listener = null
        executor.shutdownNow()
        super.onDestroy()
    }
}
