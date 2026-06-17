package com.anonymous.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

public class NotificationModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    public NotificationModule(ReactApplicationContext context) {
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
        Intent intent = new Intent(reactContext, FloatingBubbleService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        reactContext.startService(intent);
    }

    @ReactMethod
    public void getUltimaNotificacao(Promise promise) {
        try {
            SharedPreferences prefs = reactContext.getSharedPreferences("notificacoes", ReactApplicationContext.MODE_PRIVATE);

            WritableMap map = Arguments.createMap();
            map.putString("packageName", prefs.getString("ultimo_pacote", ""));
            map.putString("title", prefs.getString("ultimo_titulo", ""));
            map.putString("text", prefs.getString("ultimo_texto", ""));
            map.putDouble("time", prefs.getLong("ultimo_tempo", 0));

            promise.resolve(map);
        } catch (Exception e) {
            promise.reject("ERRO_NOTIFICACAO", e);
        }
    }

    @ReactMethod
    public void addListener(String eventName) {
        // Necessário para NativeEventEmitter no React Native novo
    }

    @ReactMethod
    public void removeListeners(Integer count) {
        // Necessário para NativeEventEmitter no React Native novo
    }
}