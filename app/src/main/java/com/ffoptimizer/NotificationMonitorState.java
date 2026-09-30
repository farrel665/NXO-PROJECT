package com.ffoptimizer;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

public final class NotificationMonitorState {
    private static final String PREF = "monitor";
    private static final String LAST = "last";

    private NotificationMonitorState() {}

    public static boolean isEnabled(Context context) {
        String enabled = Settings.Secure.getString(
                context.getContentResolver(),
                "enabled_notification_listeners"
        );
        if (enabled == null) return false;

        String expected = new ComponentName(
                context, GameNotificationListener.class
        ).flattenToString();

        for (String item : enabled.split(":")) {
            if (expected.equalsIgnoreCase(item)) return true;
        }
        return false;
    }

    public static void setLast(Context context, String text) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .putString(LAST, text)
                .apply();
    }

    public static String getLast(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getString(LAST, "—");
    }
}
