package com.official.senestro.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

public class FloatingDialog {
    private final Context context;
    private WindowManager manager;
    private WindowManager.LayoutParams params;
    private View view;
    private LinearLayout background;
    private boolean isFloating;

    public FloatingDialog(@NonNull Context context) {
        this.context = context;
        init();
        setDesigns();
        setParams();
    }

    public void show(final boolean enableDragging) {
        manager.addView(view, params);
        isFloating = true;
        if (enableDragging) {
            enableDragging();
        }
    }

    public void dismiss() {
        if (manager != null && view != null) {
            manager.removeView(view);
            isFloating = false;
        }
    }

    public boolean isFloating() {
        return this.isFloating;
    }

    // PRIVATE
    @SuppressLint("InflateParams")
    private void init() {
        manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.floating_dialog_layout, null, true);
        background = view.findViewById(R.id.floating_dialog_background);
    }

    private void setDesigns() {
        background.setBackground(new GradientDrawable() {
            public GradientDrawable getIns(int a, int b) {
                setCornerRadius(a);
                setColor(b);
                return this;
            }
        }.getIns(15, 0xFF1E1E1E));
        background.setElevation(8f);
    }

    private void setParams() {
        int LAYOUT_TYPE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_TOAST;
        params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, LAYOUT_TYPE, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
        params.x = 0;
        params.y = 0;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void enableDragging() {
        if (background != null) {
            background.setOnTouchListener(new View.OnTouchListener() {
                private int offsetX, offsetY;
                private boolean isDragging = false;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            // Calculate the offset from the initial touch point to the view's position
                            offsetX = (int) event.getRawX() - params.x;
                            offsetY = (int) event.getRawY() - params.y;
                            isDragging = true;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (!isDragging) break;
                            // Update view position based on the touch event and offset
                            params.x = (int) event.getRawX() - offsetX;
                            params.y = (int) event.getRawY() - offsetY;
                            // Update view layout
                            manager.updateViewLayout(view, params);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            isDragging = false;
                            break;
                        default:
                            return false;
                    }
                    return true;
                }
            });
        }
    }
}