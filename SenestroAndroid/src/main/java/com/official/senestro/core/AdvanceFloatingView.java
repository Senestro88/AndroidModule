package com.official.senestro.core;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import androidx.annotation.NonNull;

public class AdvanceFloatingView {
    private static final String TAG = AdvanceFloatingVideo.class.getName();
    private final Context context;
    private final Activity activity;
    private final WindowManager manager;
    private final WindowManager.LayoutParams params;

    public AdvanceFloatingView(@NonNull Activity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.params = layoutParams();
        this.manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    // PRIVATE
    private WindowManager.LayoutParams layoutParams() {
        int LAYOUT_TYPE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_TOAST;
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, LAYOUT_TYPE, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
        params.x = 0;
        params.y = 0;
        return params;
    }

    private LayoutInflater layoutInflater() {
        return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private void addView(@NonNull View view) {
        manager.addView(view, params);
    }

    private boolean canOverlay() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context);
    }
}