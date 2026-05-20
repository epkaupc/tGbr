package com.anonymous.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class NotificationGrabberModule extends ReactContextBaseJavaModule {
    private final ReactApplicationContext reactContext;

    public NotificationGrabberModule(ReactApplicationContext context) {
        super(context);
        this.reactContext = context;
        NotificationGrabberService.reactContext = context;
    }

    @NonNull
    @Override
    public String getName() {
        return "NotificationGrabber";
    }

    @ReactMethod
    public void openSettings() {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        reactContext.startActivity(intent);
    }

    @ReactMethod
    public void openOverlaySettings() {
        Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + reactContext.getPackageName())
        );

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        reactContext.startActivity(intent);
    }

    @ReactMethod
    public void startBubble() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(reactContext)) {
                openOverlaySettings();
                return;
            }
        }

        Intent intent = new Intent(reactContext, FloatingBubbleService.class);
        reactContext.startService(intent);
    }

    @ReactMethod
    public void stopBubble() {
        Intent intent = new Intent(reactContext, FloatingBubbleService.class);
        reactContext.stopService(intent);
    }
}