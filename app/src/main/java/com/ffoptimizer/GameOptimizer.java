package com.ffoptimizer;

public final class GameOptimizer {
    public static final String FF = "com.dts.freefireth";
    public static final String FF_MAX = "com.dts.freefiremax";

    private GameOptimizer() {}

    public static String installedPackage(String pkg) {
        ShizukuShell.Result r = ShizukuShell.run(
                "pm path " + shellQuote(pkg) + " 2>/dev/null"
        );
        return r.ok() && !r.stdout.isEmpty() ? pkg : null;
    }

    public static ShizukuShell.Result compile(String pkg) {
        // Uses Android's package compiler. This does not modify game assets,
        // inject code, alter sensitivity, or bypass game protections.
        return ShizukuShell.run(
                "cmd package compile -m speed-profile -f " + shellQuote(pkg)
        );
    }

    public static ShizukuShell.Result diagnostics() {
        return ShizukuShell.run(
                "dumpsys input | grep -i -E 'touchscreen|touch screen|device|sources' | head -n 80"
        );
    }

    public static String shellQuote(String s) {
        return "'" + s.replace("'", "'\\''") + "'";
    }
}
