package com.nxo.debloat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val permissionRequest = 1101
    private val permissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, _ ->
        if (requestCode == permissionRequest) runOnUiThread { }
    }
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        Shizuku.addRequestPermissionResultListener(permissionListener)
        setContent { NxoApp() }
    }

    override fun onDestroy() {
        GameNotificationListener.listener = null
        Shizuku.removeRequestPermissionResultListener(permissionListener)
        super.onDestroy()
    }

    private fun requestShizuku() {
        if (!Shizuku.pingBinder()) return
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) return
        Shizuku.requestPermission(permissionRequest)
    }

    @Composable
    private fun NxoApp() {
        val scope = rememberCoroutineScope()
        var shizuku by remember { mutableStateOf(ShizukuShell.ready()) }
        var ffInstalled by remember { mutableStateOf(false) }
        var maxInstalled by remember { mutableStateOf(false) }
        var log by remember { mutableStateOf("Ready.") }
        var surface by remember { mutableStateOf("Not checked") }
        var daemon by remember { mutableStateOf("Idle") }
        var lastEvent by remember { mutableStateOf(NotificationState.last(this)) }
        var maxHz by remember { mutableStateOf(DeviceOptimizer.displayHz(this)) }
        var supportedHz by remember { mutableStateOf(DeviceOptimizer.allSupportedRefreshRates(this)) }
        var selectedHz by remember { mutableFloatStateOf(maxHz) }
        var autoMaxRefresh by remember { mutableStateOf(true) }
        var crosshair by remember { mutableStateOf("Classic") }
        var crosshairColor by remember { mutableStateOf(Color.White) }
        var monitoring by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            while (true) {
                shizuku = ShizukuShell.ready()
                if (shizuku) {
                    ffInstalled = withContext(Dispatchers.IO) { DeviceOptimizer.installed(DeviceOptimizer.FF) }
                    maxInstalled = withContext(Dispatchers.IO) { DeviceOptimizer.installed(DeviceOptimizer.FF_MAX) }
                    if (monitoring) {
                        val top = withContext(Dispatchers.IO) { topPackage() }
                        if (top == DeviceOptimizer.FF || top == DeviceOptimizer.FF_MAX) {
                            daemon = "GAME SESSION • ${if (top == DeviceOptimizer.FF_MAX) "FF MAX" else "FF"}"
                        }
                    }
                }
                delay(2500)
            }
        }

        DisposableEffect(Unit) {
            GameNotificationListener.listener = { event ->
                runOnUiThread {
                    lastEvent = event
                    if (event.startsWith(DeviceOptimizer.FF) || event.startsWith(DeviceOptimizer.FF_MAX)) {
                        daemon = "DEVICE + DAEMON MONITORING • GAME EVENT"
                    }
                }
            }
            onDispose { GameNotificationListener.listener = null }
        }

        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = Color(0xFF9CCBFF),
                secondary = Color(0xFFB8C8DA),
                background = Color(0xFF0D1115),
                surface = Color(0xFF151A20),
                surfaceVariant = Color(0xFF232A31)
            )
        ) {
            Surface(Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text("NXO DEBLOAT", fontSize = 31.sp, fontWeight = FontWeight.Bold)
                        Text("Shizuku • non-root • real device controls", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    item {
                        NxoCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Shizuku", fontWeight = FontWeight.Bold, fontSize = 19.sp)
                                    Text(if (shizuku) "Connected and authorized" else if (Shizuku.pingBinder()) "Running — permission required" else "Service not running", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StatusPill(if (shizuku) "READY" else "OFF")
                            }
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { requestShizuku() }, modifier = Modifier.fillMaxWidth()) {
                                Text(if (shizuku) "Shizuku connected" else "Request Shizuku permission")
                            }
                        }
                    }
                    item {
                        NxoCard {
                            Text("FF / FF MAX • JIT / AOT", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Android ART package compilation. No game-memory injection.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AssistChip(onClick = {}, label = { Text("FF ${if (ffInstalled) "✓" else "—"}") })
                                AssistChip(onClick = {}, label = { Text("MAX ${if (maxInstalled) "✓" else "—"}") })
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { compile(DeviceOptimizer.FF, "FF", false) { log = it } }, enabled = shizuku && ffInstalled, modifier = Modifier.weight(1f)) { Text("JIT/AOT FF") }
                                Button(onClick = { compile(DeviceOptimizer.FF_MAX, "FF MAX", false) { log = it } }, enabled = shizuku && maxInstalled, modifier = Modifier.weight(1f)) { Text("JIT/AOT MAX") }
                            }
                            OutlinedButton(onClick = { compileBgDexopt { log = it } }, enabled = shizuku, modifier = Modifier.fillMaxWidth()) { Text("Run background dexopt") }
                        }
                    }
                    item {
                        NxoCard {
                            Text("Auto cache cleanup", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Trims package caches; it does not wipe game data or login files.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(10.dp))
                            Button(onClick = { cleanCache { log = it } }, enabled = shizuku, modifier = Modifier.fillMaxWidth()) { Text("Clean cache now") }
                        }
                    }
                    item {
                        NxoCard {
                            Text("Refresh / SurfaceFlinger", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Detected: ${fmt(maxHz)} Hz • supported: ${supportedHz.joinToString { fmt(it) }}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(checked = autoMaxRefresh, onCheckedChange = { autoMaxRefresh = it })
                                Text("Auto max refresh rate")
                            }
                            if (autoMaxRefresh && shizuku) {
                                LaunchedEffect(maxHz, shizuku, autoMaxRefresh) {
                                    withContext(Dispatchers.IO) { DeviceOptimizer.setRefreshRate(maxHz) }
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                var expanded by remember { mutableStateOf(false) }
                                Box(Modifier.weight(1f)) {
                                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text("Max ${fmt(selectedHz)} Hz") }
                                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                        supportedHz.forEach { hz -> DropdownMenuItem(text = { Text(fmt(hz) + " Hz") }, onClick = { selectedHz = hz; expanded = false }) }
                                    }
                                }
                                Button(onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        val r = DeviceOptimizer.setRefreshRate(selectedHz)
                                        withContext(Dispatchers.Main) { log = if (r.ok) "Refresh-rate request applied: ${fmt(selectedHz)} Hz. OEM may ignore it." else r.text }
                                    }
                                }, enabled = shizuku) { Text("Apply") }
                            }
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(onClick = {
                                scope.launch(Dispatchers.IO) {
                                    val r = DeviceOptimizer.surfaceDiagnostics()
                                    withContext(Dispatchers.Main) { surface = r.text.ifBlank { "No SurfaceFlinger data returned." } }
                                }
                            }, enabled = shizuku, modifier = Modifier.fillMaxWidth()) { Text("SurfaceFlinger diagnostics") }
                            Text(surface.take(1200), modifier = Modifier.padding(top = 8.dp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    item {
                        NxoCard {
                            Text("FPS / VSync", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Max FPS target is shown from the display capability. The game engine may enforce its own cap.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Text("Display ceiling: ${fmt(maxHz)} FPS-class")
                            Text("VSync: capability check only — no fake 'disabled' state.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 10.dp)) {
                                OutlinedButton(onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        val r = DeviceOptimizer.vsyncCapability()
                                        withContext(Dispatchers.Main) { log = r.text.ifBlank { "No VSync/SurfaceFlinger property exposed." } }
                                    }
                                }, enabled = shizuku, modifier = Modifier.weight(1f)) { Text("Check VSync") }
                                OutlinedButton(onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        val pkg = if (maxInstalled) DeviceOptimizer.FF_MAX else DeviceOptimizer.FF
                                        val r = DeviceOptimizer.gfxDiagnostics(pkg)
                                        withContext(Dispatchers.Main) { log = r.text.ifBlank { "No gfxinfo data." } }
                                    }
                                }, enabled = shizuku && (ffInstalled || maxInstalled), modifier = Modifier.weight(1f)) { Text("FPS stats") }
                            }
                            OutlinedButton(onClick = {
                                scope.launch(Dispatchers.IO) {
                                    val pkg = if (maxInstalled) DeviceOptimizer.FF_MAX else DeviceOptimizer.FF
                                    val r = if (ffInstalled || maxInstalled) DeviceOptimizer.requestPerformanceMode(pkg) else ShizukuShell.Result(-1, stderr = "FF/FF MAX not installed")
                                    withContext(Dispatchers.Main) { log = if (r.ok) "Performance game mode requested for $pkg." else r.text }
                                }
                            }, enabled = shizuku && (ffInstalled || maxInstalled), modifier = Modifier.fillMaxWidth()) { Text("Request max-performance game mode") }
                        }
                    }
                    item {
                        NxoCard {
                            Text("Device + daemon monitor", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(daemon, fontWeight = FontWeight.SemiBold)
                            Text("Last notification: $lastEvent", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                                Switch(checked = monitoring, onCheckedChange = { monitoring = it })
                                Text("Monitor FF / FF MAX session")
                            }
                            OutlinedButton(onClick = { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }, modifier = Modifier.fillMaxWidth()) { Text("Open Notification Access") }
                        }
                    }
                    item {
                        NxoCard {
                            Text("Crosshair menu", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Preview/customization only. No game overlay or input injection.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Classic", "Dot", "Plus", "Circle").forEach { style -> FilterChip(selected = crosshair == style, onClick = { crosshair = style }, label = { Text(style) }) }
                            }
                            Spacer(Modifier.height(12.dp))
                            Box(Modifier.fillMaxWidth().height(120.dp).background(Color(0xFF080A0C), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                                CrosshairPreview(crosshair, crosshairColor)
                            }
                        }
                    }
                    item {
                        NxoCard {
                            Text("Activity log", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(log.take(3000), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    private fun topPackage(): String {
        val r = ShizukuShell.run("dumpsys activity activities | grep -m 1 -E 'mResumedActivity|ResumedActivity'")
        val line = r.text
        return when {
            line.contains(DeviceOptimizer.FF_MAX) -> DeviceOptimizer.FF_MAX
            line.contains(DeviceOptimizer.FF) -> DeviceOptimizer.FF
            else -> ""
        }
    }

    private fun compile(pkg: String, label: String, speed: Boolean, done: (String) -> Unit) {
        Thread {
            val r = if (speed) DeviceOptimizer.compileSpeed(pkg) else DeviceOptimizer.compileProfile(pkg)
            runOnUiThread { done(if (r.ok) "$label speed-profile AOT compilation completed.\n${r.text}" else "$label compile failed.\n${r.text}") }
        }.start()
    }
    private fun compileBgDexopt(done: (String) -> Unit) = Thread {
        val r = DeviceOptimizer.runBgDexopt(); runOnUiThread { done(if (r.ok) "Background dexopt completed.\n${r.text}" else r.text) }
    }.start()
    private fun cleanCache(done: (String) -> Unit) = Thread {
        val r = DeviceOptimizer.cleanCache(); runOnUiThread { done(if (r.ok) "Cache trim completed.\n${r.text}" else r.text) }
    }.start()

    @Composable private fun NxoCard(content: @Composable ColumnScope.() -> Unit) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(17.dp), content = content)
        }
    }
    @Composable private fun StatusPill(text: String) { AssistChip(onClick = {}, label = { Text(text) }) }
    @Composable private fun CrosshairPreview(style: String, color: Color) {
        Canvas(Modifier.size(70.dp)) {
            val c = Offset(size.width / 2, size.height / 2)
            when (style) {
                "Dot" -> drawCircle(color, radius = 5f, center = c)
                "Plus" -> { drawLine(color, Offset(c.x - 25, c.y), Offset(c.x + 25, c.y), strokeWidth = 4f); drawLine(color, Offset(c.x, c.y - 25), Offset(c.x, c.y + 25), strokeWidth = 4f) }
                "Circle" -> drawCircle(color, radius = 22f, center = c, style = Stroke(3f))
                else -> { drawLine(color, Offset(c.x - 24, c.y), Offset(c.x - 6, c.y), strokeWidth = 3f); drawLine(color, Offset(c.x + 6, c.y), Offset(c.x + 24, c.y), strokeWidth = 3f); drawLine(color, Offset(c.x, c.y - 24), Offset(c.x, c.y - 6), strokeWidth = 3f); drawLine(color, Offset(c.x, c.y + 6), Offset(c.x, c.y + 24), strokeWidth = 3f) }
            }
        }
    }

    private fun fmt(hz: Float) = String.format(Locale.US, "%.0f", hz)
}
