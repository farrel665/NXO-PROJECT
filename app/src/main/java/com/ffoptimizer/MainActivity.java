package com.ffoptimizer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ffoptimizer.databinding.ActivityMainBinding;

import rikka.shizuku.Shizuku;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int SHIZUKU_REQUEST = 1001;
    private static final int NOTIFICATION_REQUEST = 1002;

    private ActivityMainBinding binding;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final Shizuku.OnRequestPermissionResultListener permissionListener =
            (requestCode, grantResult) -> runOnUiThread(() -> {
                if (requestCode == SHIZUKU_REQUEST) {
                    refreshShizuku();
                    log(grantResult == PackageManager.PERMISSION_GRANTED
                            ? "Shizuku permission granted."
                            : "Shizuku permission denied.");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Shizuku.addRequestPermissionResultListener(permissionListener);
        GameNotificationListener.setListener(text -> runOnUiThread(() -> {
            binding.tvLastNotification.setText("Last event: " + text);
            binding.tvMonitor.setText("Notification monitor: receiving events");
        }));

        binding.btnShizuku.setOnClickListener(v -> requestShizuku());
        binding.btnCompileFF.setOnClickListener(v -> compile(GameOptimizer.FF, "Free Fire"));
        binding.btnCompileMax.setOnClickListener(v -> compile(GameOptimizer.FF_MAX, "Free Fire MAX"));
        binding.btnRefreshTouch.setOnClickListener(v -> refreshTouchDiagnostics());

        binding.btnNotificationSettings.setOnClickListener(v -> {
            try {
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            } catch (Exception e) {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            }
        });

        if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_REQUEST
            );
        }

        refreshShizuku();
        refreshPackages();
        refreshTouchDiagnostics();
        refreshNotificationState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshShizuku();
        refreshNotificationState();
    }

    private void requestShizuku() {
        if (!Shizuku.pingBinder()) {
            Toast.makeText(this, "Start Shizuku first.", Toast.LENGTH_LONG).show();
            log("Shizuku is not running. Start it, then press this button again.");
            return;
        }

        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            refreshShizuku();
            return;
        }

        Shizuku.requestPermission(SHIZUKU_REQUEST);
    }

    private void refreshShizuku() {
        boolean running = Shizuku.pingBinder();
        boolean granted = running &&
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;

        binding.chipShizuku.setText(granted ? "READY" : running ? "AUTH" : "OFF");
        binding.tvShizukuStatus.setText(
                granted ? "Connected with shell-level Shizuku access."
                        : running ? "Shizuku is running; permission required."
                        : "Shizuku is not running."
        );
        binding.btnShizuku.setText(granted ? "Shizuku connected" : "Request Shizuku permission");
    }

    private void refreshPackages() {
        executor.execute(() -> {
            boolean ff = GameOptimizer.installedPackage(GameOptimizer.FF) != null;
            boolean max = GameOptimizer.installedPackage(GameOptimizer.FF_MAX) != null;

            runOnUiThread(() -> binding.tvPackageStatus.setText(
                    "FF: " + (ff ? "installed" : "not found") +
                    "  •  MAX: " + (max ? "installed" : "not found")
            ));
        });
    }

    private void compile(String pkg, String label) {
        if (!hasShizuku()) {
            Toast.makeText(this, "Shizuku permission is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.tvLog.setText("Compiling " + label + "…");
        executor.execute(() -> {
            ShizukuShell.Result result = GameOptimizer.compile(pkg);
            runOnUiThread(() -> {
                String text = result.ok()
                        ? label + " compile completed.\n" + result.display()
                        : label + " compile failed.\n" + result.display();
                binding.tvLog.setText(text);
                Toast.makeText(this,
                        result.ok() ? "Compile completed" : "Compile failed",
                        Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void refreshTouchDiagnostics() {
        if (!hasShizuku()) {
            binding.tvTouchDevice.setText("Touch device: Shizuku required");
            binding.tvTouchEvents.setText("Touch monitor: waiting for Shizuku");
            return;
        }

        executor.execute(() -> {
            ShizukuShell.Result r = GameOptimizer.diagnostics();
            runOnUiThread(() -> {
                if (r.ok() && !r.stdout.isEmpty()) {
                    binding.tvTouchDevice.setText("Touch diagnostics: data available");
                    binding.tvTouchInfo.setText(r.stdout);
                    binding.tvTouchEvents.setText(
                            "Real-time event injection/sampling-rate changes are not claimed."
                    );
                } else {
                    binding.tvTouchDevice.setText("Touch diagnostics: restricted/unavailable");
                    binding.tvTouchInfo.setText(r.display());
                }
            });
        });
    }

    private boolean hasShizuku() {
        return Shizuku.pingBinder() &&
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
    }

    private void refreshNotificationState() {
        boolean enabled = NotificationMonitorState.isEnabled(this);
        binding.tvMonitor.setText(enabled
                ? "Notification monitor: connected"
                : "Notification monitor: access not granted");
        binding.tvLastNotification.setText(
                "Last event: " + NotificationMonitorState.getLast(this)
        );
    }

    public void onNotificationEvent(String text) {
        NotificationMonitorState.setLast(this, text);
        runOnUiThread(this::refreshNotificationState);
    }

    private void log(String s) {
        binding.tvLog.setText(s);
    }

    @Override
    protected void onDestroy() {
        GameNotificationListener.setListener(null);
        Shizuku.removeRequestPermissionResultListener(permissionListener);
        executor.shutdownNow();
        super.onDestroy();
    }
}
