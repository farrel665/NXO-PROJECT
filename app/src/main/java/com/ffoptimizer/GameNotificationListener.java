package com.ffoptimizer;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class GameNotificationListener extends NotificationListenerService {

    public interface EventListener {
        void onEvent(String text);
    }

    private static volatile EventListener listener;

    public static void setListener(EventListener eventListener) {
        listener = eventListener;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        String title = "";
        try {
            if (sbn.getNotification().extras != null) {
                CharSequence t = sbn.getNotification().extras.getCharSequence("android.title");
                if (t != null) title = t.toString();
            }
        } catch (Exception ignored) {}

        publish(packageName + (title.isEmpty() ? "" : " • " + title));
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        publish("Removed: " + sbn.getPackageName());
    }

    private void publish(String text) {
        NotificationMonitorState.setLast(this, text);
        EventListener l = listener;
        if (l != null) {
            l.onEvent(text);
        }
    }
}
