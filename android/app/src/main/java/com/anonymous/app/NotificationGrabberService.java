package com.anonymous.app;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.app.Notification;
import android.os.Bundle;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class NotificationGrabberService extends NotificationListenerService {
    public static ReactApplicationContext reactContext;

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null || sbn.getNotification() == null) return;

        Notification notification = sbn.getNotification();
        Bundle extras = notification.extras;

        String packageName = sbn.getPackageName();
        String title = "";
        String text = "";

        if (extras != null) {
            CharSequence titleSeq = extras.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence textSeq = extras.getCharSequence(Notification.EXTRA_TEXT);

            if (titleSeq != null) title = titleSeq.toString();
            if (textSeq != null) text = textSeq.toString();
        }
        if (title.equals("com.ubercab")) {
            
        }
        sendEvent(packageName, title, text);
    }

    private void sendEvent(String packageName, String title, String text) {
        if (reactContext == null) return;

        WritableMap map = Arguments.createMap();
        map.putString("packageName", packageName);
        map.putString("title", title);
        map.putString("text", text);

        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit("notification_received", map);
    }
}