package com.anonymous.app;

import android.app.Notification;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class NotificationGrabberService extends NotificationListenerService {

    public static ReactApplicationContext reactContext;

    private static final String TAG = "notification_grabber";

    public static String UltimoPacote = "";
    public static String UltimoTitulo = "";
    public static String UltimoTexto = "";

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.d(TAG, "NotificationListener conectado");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null || sbn.getNotification() == null) {
            Log.d(TAG, "Notificação nula recebida");
            return;
        }

        Notification notification = sbn.getNotification();
        Bundle extras = notification.extras;

        String packageName = sbn.getPackageName();
        String title = "";
        String text = "";

        if (extras != null) {
            CharSequence titleSeq = extras.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence textSeq = extras.getCharSequence(Notification.EXTRA_TEXT);
            CharSequence bigTextSeq = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);

            if (titleSeq != null) {
                title = titleSeq.toString();
            }

            if (bigTextSeq != null) {
                text = bigTextSeq.toString();
            } else if (textSeq != null) {
                text = textSeq.toString();
            }
        }

        UltimoPacote = packageName;
        UltimoTitulo = title;
        UltimoTexto = text;

        salvarNotificacao(packageName, title, text);

        Log.d(TAG, "Notificação salva: " + packageName + " | " + title + " | " + text);

        sendEvent(packageName, title, text);
    }

    private void salvarNotificacao(String packageName, String title, String text) {
        SharedPreferences prefs = getSharedPreferences("notificacoes", MODE_PRIVATE);

        prefs.edit()
                .putString("ultimo_pacote", packageName)
                .putString("ultimo_titulo", title)
                .putString("ultimo_texto", text)
                .putLong("ultimo_tempo", System.currentTimeMillis())
                .apply();
    }

    private void sendEvent(String packageName, String title, String text) {
        if (reactContext == null) {
            Log.d(TAG, "App fechado. reactContext NULL. Só salvou nativo.");
            return;
        }

        try {
            WritableMap map = Arguments.createMap();
            map.putString("packageName", packageName);
            map.putString("title", title);
            map.putString("text", text);

            reactContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("notification_received", map);

            Log.d(TAG, "Evento enviado pro React Native");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao enviar evento pro React Native", e);
        }
    }
}