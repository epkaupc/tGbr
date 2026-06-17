package com.anonymous.app;
import android.util.Log;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FloatingBubbleService extends Service {
    private WindowManager windowManager;
    private TextView bubble;
    private LinearLayout panel;
    private TextView subtitle;

    private WindowManager.LayoutParams bubbleParams;
    private WindowManager.LayoutParams panelParams;

    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;

    private boolean panelVisible = false;
    private boolean moved = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Sem permissão de overlay", Toast.LENGTH_LONG).show();
            stopSelf();
            return;
        }

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        createBubble();
        createPanel();

        windowManager.addView(bubble, bubbleParams);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (windowManager != null) {
            try {
                if (bubble != null) {
                    windowManager.removeView(bubble);
                }
            } catch (Exception ignored) {}

            try {
                if (panel != null && panelVisible) {
                    windowManager.removeView(panel);
                }
            } catch (Exception ignored) {}
        }

        bubble = null;
        panel = null;
    }
    private void killApp() {
        if (windowManager != null) {
            try {
                if (panelVisible && panel != null) {
                    windowManager.removeView(panel);
                }
            } catch (Exception ignored) {}

            try {
                if (bubble != null) {
                    windowManager.removeView(bubble);
                }
            } catch (Exception ignored) {}
        }

        panelVisible = false;
        stopSelf();
    }
    private void createBubble() {
        bubble = new TextView(this);
        bubble.setText("Tgb");
        bubble.setTextSize(36);
        bubble.setTextColor(Color.WHITE);
        bubble.setGravity(Gravity.CENTER);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(Color.parseColor("#2563EB"));
        bubble.setBackground(bg);

        bubbleParams = new WindowManager.LayoutParams(
                140,
                140,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );

        bubbleParams.gravity = Gravity.TOP | Gravity.START;
        bubbleParams.x = 400;
        bubbleParams.y = 400;

        bubble.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = bubbleParams.x;
                    initialY = bubbleParams.y;
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    moved = false;
                    return true;

                case MotionEvent.ACTION_MOVE:
                    int dx = (int) (event.getRawX() - initialTouchX);
                    int dy = (int) (event.getRawY() - initialTouchY);

                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                        moved = true;
                    }

                    bubbleParams.x = initialX + dx;
                    bubbleParams.y = initialY + dy;
                    windowManager.updateViewLayout(bubble, bubbleParams);

                    if (panelVisible && panel != null) {
                        panelParams.x = bubbleParams.x + 160;
                        panelParams.y = bubbleParams.y;
                        windowManager.updateViewLayout(panel, panelParams);
                    }

                    return true;

                case MotionEvent.ACTION_UP:
                    if (!moved) {
                        togglePanel();
                    }
                    return true;
            }

            return false;
        });
    }
   private void reFresha() {
    if (subtitle != null) {
        subtitle.setText(
                "\nApp: " + NotificationGrabberService.UltimoPacote +
                "\nTítulo: " + NotificationGrabberService.UltimoTitulo +
                "\nTexto: " + NotificationGrabberService.UltimoTexto
        );
    }

    if (windowManager != null && panel != null && panelVisible) {
        try {
            windowManager.updateViewLayout(panel, panelParams);
            Log.d("Bubble", "Painel atualizado");
        } catch (Exception e) {
            Log.e("Bubble", "Erro ao atualizar painel", e);
        }
    }
}     
    private void createPanel() {
        panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(24, 20, 24, 20);

        GradientDrawable panelBg = new GradientDrawable();
        panelBg.setColor(Color.parseColor("#1F2937"));
        panelBg.setCornerRadius(24);
        panel.setBackground(panelBg);

        TextView title = new TextView(this);
        title.setText("Painel ativo");
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        title.setGravity(Gravity.START);

        subtitle = new TextView(this);;
        subtitle.setText(
            "\nApp: " + NotificationGrabberService.UltimoPacote +
            "\nTítulo: " + NotificationGrabberService.UltimoTitulo +
            "\nTexto: " + NotificationGrabberService.UltimoTexto
        );
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(14);

        TextView close = new TextView(this);
        close.setText("Minimizar");
        close.setTextColor(Color.parseColor("#60A5FA"));
        close.setTextSize(16);
        close.setPadding(0, 18, 0, 0);

        TextView kill = new TextView(this);
        kill.setText("Fechar bolha");
        kill.setTextColor(Color.parseColor("#EF4444"));
        kill.setTextSize(16);
        kill.setPadding(0, 18, 0, 0);
        
        TextView refresh = new TextView(this);
        refresh.setText("Atualizar");
        refresh.setTextColor(Color.parseColor("#10B981"));
        refresh.setTextSize(16);
        refresh.setPadding(0, 18, 0, 0);

        close.setOnClickListener(v -> togglePanel());
        refresh.setOnClickListener(v -> reFresha());
        kill.setOnClickListener(v -> killApp());
        

        panel.addView(title);
        panel.addView(subtitle);
        panel.addView(close);
        panel.addView(refresh);
        panel.addView(kill);

        panelParams = new WindowManager.LayoutParams(
                520,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );

        panelParams.gravity = Gravity.TOP | Gravity.START;
        panelParams.x = bubbleParams.x + 160;
        panelParams.y = bubbleParams.y;
    }

    private void togglePanel() {
        if (panelVisible) {
            try {
                windowManager.removeView(panel);
            } catch (Exception ignored) {}

            panelVisible = false;
        } else {
            panelParams.x = bubbleParams.x + 160;
            panelParams.y = bubbleParams.y;

            try {
                windowManager.addView(panel, panelParams);
                panelVisible = true;
            } catch (Exception e) {
                Toast.makeText(this, "Erro ao abrir painel: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}