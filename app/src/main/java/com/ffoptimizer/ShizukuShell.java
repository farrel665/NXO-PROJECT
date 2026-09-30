package com.ffoptimizer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuRemoteProcess;

public final class ShizukuShell {
    private ShizukuShell() {}

    public static Result run(String command) {
        if (!Shizuku.pingBinder()) {
            return new Result(-1, "", "Shizuku service is not running.");
        }

        if (Shizuku.checkSelfPermission() != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return new Result(-1, "", "Shizuku permission is not granted.");
        }

        ShizukuRemoteProcess process = null;
        try {
            process = Shizuku.newProcess(
                    new String[]{"sh", "-c", command},
                    null,
                    null
            );

            final StringBuilder out = new StringBuilder();
            final StringBuilder err = new StringBuilder();

            BufferedReader stdout = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            BufferedReader stderr = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));

            Thread t1 = new Thread(() -> {
                try {
                    String line;
                    while ((line = stdout.readLine()) != null) {
                        synchronized (out) {
                            out.append(line).append('\n');
                        }
                    }
                } catch (Exception ignored) {}
            });

            Thread t2 = new Thread(() -> {
                try {
                    String line;
                    while ((line = stderr.readLine()) != null) {
                        synchronized (err) {
                            err.append(line).append('\n');
                        }
                    }
                } catch (Exception ignored) {}
            });

            t1.start();
            t2.start();

            int code = process.waitFor();
            t1.join(3000);
            t2.join(3000);

            return new Result(code, out.toString().trim(), err.toString().trim());
        } catch (Exception e) {
            return new Result(-1, "", e.toString());
        } finally {
            if (process != null) {
                try { process.destroy(); } catch (Exception ignored) {}
            }
        }
    }

    public static final class Result {
        public final int code;
        public final String stdout;
        public final String stderr;

        public Result(int code, String stdout, String stderr) {
            this.code = code;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public boolean ok() {
            return code == 0;
        }

        public String display() {
            if (!stdout.isEmpty() && !stderr.isEmpty()) {
                return stdout + "\n" + stderr;
            }
            return stdout.isEmpty() ? stderr : stdout;
        }
    }
}
