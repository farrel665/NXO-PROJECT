package com.nxo.debloat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
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

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    // Button dengan efek scale saat ditekan
    @Composable
    private fun PressBtn(
        onClick: () -> Unit,
        enabled: Boolean = true,
        modifier: Modifier = Modifier,
        content: @Composable RowScope.() -> Unit
    ) {
        val src = remember { MutableInteractionSource() }
        val pressed by src.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (pressed) 0.95f else 1f,
            label = "btn_scale"
        )
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale },
            interactionSource = src,
            content = content
        )
    }

    // OutlinedButton dengan efek scale saat ditekan
    @Composable
    private fun PressOutBtn(
        onClick: () -> Unit,
        enabled: Boolean = true,
        modifier: Modifier = Modifier,
        content: @Composable RowScope.() -> Unit
    ) {
        val src = remember { MutableInteractionSource() }
        val pressed by src.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (pressed) 0.95f else 1f,
            label = "outbtn_scale"
        )
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale },
            interactionSource = src,
            content = content
        )
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

        // State untuk dialog clean cache
        var showCleanDialog by remember { mutableStateOf(false) }
        var cleanRunning by remember { mutableStateOf(false) }
        var cleanResult by remember { mutableStateOf("") }

        // Dialog proses clean cache
        if (showCleanDialog) {
            AlertDialog(
                onDismissRequest = { if (!cleanRunning) showCleanDialog = false },
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text("Auto Cache Cleanup", fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        if (cleanRunning) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Sedang membersihkan cache semua package...\nMohon tunggu sebentar.",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        } else {
                            Text(
                                cleanResult,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                confirmButton = {
                    if (!cleanRunning) {
                        Button(onClick = { showCleanDialog = false }) {
                            Text("OK")
                        }
                    }
                }
            )
        }

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

                    // ── Header ──────────────────────────────────────────────
                    item {
                        Text("NXO DEBLOAT", fontSize = 31.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Shizuku • non-root • real device controls",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // ── Shizuku ─────────────────────────────────────────────
                    item {
                        NxoCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Shizuku", fontWeight = FontWeight.Bold, fontSize = 19.sp)
                                    Text(
                                        when {
                                            shizuku -> "Connected and authorized"
                                            Shizuku.pingBinder() -> "Running — permission required"
                                            else -> "Service not running"
                                        },
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusPill(if (shizuku) "READY" else "OFF")
                            }
                            Spacer(Modifier.height(12.dp))
                            PressBtn(
                                onClick = {
                                    requestShizuku()
                                    toast(if (shizuku) "Shizuku sudah terhubung" else "Meminta izin Shizuku...")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (shizuku) "Shizuku connected" else "Request Shizuku permission")
                            }
                        }
                    }

                    // ── JIT / AOT ────────────────────────────────────────────
                    item {
                        NxoCard {
                            Text("FF / FF MAX • JIT / AOT", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Android ART package compilation. No game-memory injection.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AssistChip(onClick = {}, label = { Text("FF ${if (ffInstalled) "✓" else "—"}") })
                                AssistChip(onClick = {}, label = { Text("MAX ${if (maxInstalled) "✓" else "—"}") })
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                PressBtn(
                                    onClick = {
                                        toast("Memulai kompilasi JIT/AOT Free Fire...")
                                        compile(DeviceOptimizer.FF, "FF", false) { log = it }
                                    },
                                    enabled = shizuku && ffInstalled,
                                    modifier = Modifier.weight(1f)
                                ) { Text("JIT/AOT FF") }
                                PressBtn(
                                    onClick = {
                                        toast("Memulai kompilasi JIT/AOT FF MAX...")
                                        compile(DeviceOptimizer.FF_MAX, "FF MAX", false) { log = it }
                                    },
                                    enabled = shizuku && maxInstalled,
                                    modifier = Modifier.weight(1f)
                                ) { Text("JIT/AOT MAX") }
                            }
                            PressOutBtn(
                                onClick = {
                                    toast("Menjalankan background dexopt...")
                                    compileBgDexopt { log = it }
                                },
                                enabled = shizuku,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Run background dexopt") }
                        }
                    }

                    // ── Clean Cache ──────────────────────────────────────────
                    item {
                        NxoCard {
                            Text("Auto cache cleanup", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Trims package caches; it does not wipe game data or login files.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(10.dp))
                            PressBtn(
                                onClick = {
                                    showCleanDialog = true
                                    cleanRunning = true
                                    cleanResult = ""
                                    cleanCache { result ->
                                        cleanRunning = false
                                        cleanResult = result
                                        log = result
                                    }
                                },
                                enabled = shizuku,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Clean cache now") }
                        }
                    }

                    // ── Refresh / SurfaceFlinger ─────────────────────────────
                    item {
                        NxoCard {
                            Text("Refresh / SurfaceFlinger", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Detected: ${fmt(maxHz)} Hz • supported: ${supportedHz.joinToString { fmt(it) }}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = autoMaxRefresh,
                                    onCheckedChange = {
                                        autoMaxRefresh = it
                                        toast(if (it) "Auto max refresh rate aktif" else "Auto max refresh rate nonaktif")
                                    }
                                )
                                Text("Auto max refresh rate")
                            }
                            if (autoMaxRefresh && shizuku) {
                                LaunchedEffect(maxHz, shizuku, autoMaxRefresh) {
                                    withContext(Dispatchers.IO) { DeviceOptimizer.setRefreshRate(maxHz) }
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                var expanded by remember { mutableStateOf(false) }
                                Box(Modifier.weight(1f)) {
                                    PressOutBtn(
                                        onClick = { expanded = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Max ${fmt(selectedHz)} Hz") }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        supportedHz.forEach { hz ->
                                            DropdownMenuItem(
                                                text = { Text(fmt(hz) + " Hz") },
                                                onClick = { selectedHz = hz; expanded = false }
                                            )
                                        }
                                    }
                                }
                                PressBtn(
                                    onClick = {
                                        toast("Menerapkan refresh rate ${fmt(selectedHz)} Hz...")
                                        scope.launch(Dispatchers.IO) {
                                            val r = DeviceOptimizer.setRefreshRate(selectedHz)
                                            withContext(Dispatchers.Main) {
                                                log = if (r.ok) "Refresh-rate request applied: ${fmt(selectedHz)} Hz. OEM may ignore it." else r.text
                                                toast(if (r.ok) "Refresh rate ${fmt(selectedHz)} Hz diterapkan!" else "Gagal menerapkan refresh rate")
                                            }
                                        }
                                    },
                                    enabled = shizuku
                                ) { Text("Apply") }
                            }
                            Spacer(Modifier.height(8.dp))
                            PressOutBtn(
                                onClick = {
                                    toast("Mengambil data SurfaceFlinger...")
                                    scope.launch(Dispatchers.IO) {
                                        val r = DeviceOptimizer.surfaceDiagnostics()
                                        withContext(Dispatchers.Main) {
                                            surface = r.text.ifBlank { "No SurfaceFlinger data returned." }
                                            toast("Data SurfaceFlinger diperoleh")
                                        }
                                    }
                                },
                                enabled = shizuku,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("SurfaceFlinger diagnostics") }
                            Text(
                                surface.take(1200),
                                modifier = Modifier.padding(top = 8.dp),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // ── FPS / VSync ──────────────────────────────────────────
                    item {
                        NxoCard {
                            Text("FPS / VSync", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Max FPS target is shown from the display capability. The game engine may enforce its own cap.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("Display ceiling: ${fmt(maxHz)} FPS-class")
                            Text(
                                "VSync: capability check only — no fake 'disabled' state.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 10.dp)
                            ) {
                                PressOutBtn(
                                    onClick = {
                                        toast("Memeriksa kemampuan VSync...")
                                        scope.launch(Dispatchers.IO) {
                                            val r = DeviceOptimizer.vsyncCapability()
                                            withContext(Dispatchers.Main) {
                                                log = r.text.ifBlank { "No VSync/SurfaceFlinger property exposed." }
                                                toast("Pemeriksaan VSync selesai")
                                            }
                                        }
                                    },
                                    enabled = shizuku,
                                    modifier = Modifier.weight(1f)
                                ) { Text("Check VSync") }
                                PressOutBtn(
                                    onClick = {
                                        toast("Mengambil statistik FPS...")
                                        scope.launch(Dispatchers.IO) {
                                            val pkg = if (maxInstalled) DeviceOptimizer.FF_MAX else DeviceOptimizer.FF
                                            val r = DeviceOptimizer.gfxDiagnostics(pkg)
                                            withContext(Dispatchers.Main) {
                                                log = r.text.ifBlank { "No gfxinfo data." }
                                                toast("Data FPS diperoleh")
                                            }
                                        }
                                    },
                                    enabled = shizuku && (ffInstalled || maxInstalled),
                                    modifier = Modifier.weight(1f)
                                ) { Text("FPS stats") }
                            }
                            PressOutBtn(
                                onClick = {
                                    toast("Meminta mode game performa tinggi...")
                                    scope.launch(Dispatchers.IO) {
                                        val pkg = if (maxInstalled) DeviceOptimizer.FF_MAX else DeviceOptimizer.FF
                                        val r = if (ffInstalled || maxInstalled)
                                            DeviceOptimizer.requestPerformanceMode(pkg)
                                        else
                                            ShizukuShell.Result(-1, stderr = "FF/FF MAX not installed")
                                        withContext(Dispatchers.Main) {
                                            log = if (r.ok) "Performance game mode requested for $pkg." else r.text
                                            toast(if (r.ok) "Mode performa tinggi aktif!" else "Gagal mengaktifkan mode performa")
                                        }
                                    }
                                },
                                enabled = shizuku && (ffInstalled || maxInstalled),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Request max-performance game mode") }
                        }
                    }

                    // ── Device + Daemon Monitor ──────────────────────────────
                    item {
                        NxoCard {
                            Text("Device + daemon monitor", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(daemon, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Last notification: $lastEvent",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Switch(
                                    checked = monitoring,
                                    onCheckedChange = {
                                        monitoring = it
                                        toast(if (it) "Monitoring aktif" else "Monitoring nonaktif")
                                    }
                                )
                                Text("Monitor FF / FF MAX session")
                            }
                            PressOutBtn(
                                onClick = {
                                    toast("Membuka pengaturan akses notifikasi...")
                                    startActivity(Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Open Notification Access") }
                        }
                    }

                    // ── Crosshair ────────────────────────────────────────────
                    item {
                        NxoCard {
                            Text("Crosshair menu", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Preview/customization only. No game overlay or input injection.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Classic", "Dot", "Plus", "Circle").forEach { style ->
                                    FilterChip(
                                        selected = crosshair == style,
                                        onClick = {
                                            crosshair = style
                                            toast("Crosshair: $style")
                                        },
                                        label = { Text(style) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .background(Color(0xFF080A0C), RoundedCornerShape(18.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                CrosshairPreview(crosshair, crosshairColor)
                            }
                        }
                    }

                    // ── Activity Log ─────────────────────────────────────────
                    item {
                        NxoCard {
                            Text("Activity log", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(
                                log.take(3000),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
            runOnUiThread {
                done(if (r.ok) "$label speed-profile AOT compilation completed.\n${r.text}" else "$label compile failed.\n${r.text}")
                toast(if (r.ok) "$label kompilasi selesai!" else "$label kompilasi gagal")
            }
        }.start()
    }

    private fun compileBgDexopt(done: (String) -> Unit) = Thread {
        val r = DeviceOptimizer.runBgDexopt()
        runOnUiThread {
            done(if (r.ok) "Background dexopt completed.\n${r.text}" else r.text)
            toast(if (r.ok) "Background dexopt selesai!" else "Background dexopt gagal")
        }
    }.start()

    private fun cleanCache(done: (String) -> Unit) = Thread {
        val r = DeviceOptimizer.cleanCache()
        runOnUiThread {
            done(if (r.ok) "Cache trim completed.\n${r.text}" else r.text)
            toast(if (r.ok) "Cache berhasil dibersihkan!" else "Gagal membersihkan cache")
        }
    }.start()

    @Composable
    private fun NxoCard(content: @Composable ColumnScope.() -> Unit) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(17.dp), content = content)
        }
    }

    @Composable
    private fun StatusPill(text: String) {
        AssistChip(onClick = {}, label = { Text(text) })
    }

    @Composable
    private fun CrosshairPreview(style: String, color: Color) {
        Canvas(Modifier.size(70.dp)) {
            val c = Offset(size.width / 2, size.height / 2)
            when (style) {
                "Dot" -> drawCircle(color, radius = 5f, center = c)
                "Plus" -> {
                    drawLine(color, Offset(c.x - 25, c.y), Offset(c.x + 25, c.y), strokeWidth = 4f)
                    drawLine(color, Offset(c.x, c.y - 25), Offset(c.x, c.y + 25), strokeWidth = 4f)
                }
                "Circle" -> drawCircle(color, radius = 22f, center = c, style = Stroke(3f))
                else -> {
                    drawLine(color, Offset(c.x - 24, c.y), Offset(c.x - 6, c.y), strokeWidth = 3f)
                    drawLine(color, Offset(c.x + 6, c.y), Offset(c.x + 24, c.y), strokeWidth = 3f)
                    drawLine(color, Offset(c.x, c.y - 24), Offset(c.x, c.y - 6), strokeWidth = 3f)
                    drawLine(color, Offset(c.x, c.y + 6), Offset(c.x, c.y + 24), strokeWidth = 3f)
                }
            }
        }
    }

    private fun fmt(hz: Float) = String.format(Locale.US, "%.0f", hz)
}
