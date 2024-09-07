package com.official.senestro.core;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;

import com.official.senestro.core.utils.AdvanceUtils;

import java.lang.reflect.Method;

public class PackageLauncher {
    private final String tag = PackageLauncher.class.getName();

    private final Context context;
    private final Activity activity;
    private final String packageName;
    // From android.app.ActivityManager.StackId
    private static final int FULLSCREEN_WORKSPACE_STACK_ID = 1;
    private static final int FREEFORM_WORKSPACE_STACK_ID = 2;
    // From android.app.WindowConfiguration
    private static final int WINDOWING_MODE_FULLSCREEN = 1;
    private static final int WINDOWING_MODE_FREEFORM = 5;

    public PackageLauncher(Context context, Activity activity, String packageName) {
        this.context = context;
        this.activity = activity;
        this.packageName = packageName;
    }

    public void launchApplicationInFreeFormMode() {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("android.activity.windowingMode", 5); // 5 for free-form mode
            int screenWidth = AdvanceUtils.getDisplayWidthPixels(context);
            int screenHeight = AdvanceUtils.getDisplayHeightPixels(context);
            int width = screenWidth - 120;
            int height = screenHeight - 120;
            ActivityOptions options = createActivityOptions(screenWidth, screenHeight, width, height);
            if (options != null) {
                try {
                    Method method = options.getClass().getMethod(getWindowingModeMethodName(), int.class);
                    method.invoke(options, getFreeformWindowModeId());
                    activity.startActivity(intent, options.toBundle());
                } catch (Exception e) {
                    Log.e(tag, e.getMessage(), e);
                    activity.startActivity(intent);
                }
            } else {
                activity.startActivity(intent);
            }
        }
    }

    // PRIVATE
    private void allowReflection() {
        // Android 9+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

        }
    }

    private ActivityOptions createActivityOptions(int screenWidth, int screenHeight, int width, int height) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            int left = (screenWidth - width) / 2;
            int top = (screenHeight - height) / 2;
            ActivityOptions options = ActivityOptions.makeBasic();
            options.setLaunchBounds(new Rect(left, top, left + width, top + height));
            return options;
        }
        return null;
    }

    private int getFreeformWindowModeId() {
        if (getCurrentApiVersion() >= Build.VERSION_CODES.P) return WINDOWING_MODE_FREEFORM;
        else return FREEFORM_WORKSPACE_STACK_ID;
    }

    private String getWindowingModeMethodName() {
        if (getCurrentApiVersion() >= Build.VERSION_CODES.P) return "setLaunchWindowingMode";
        else return "setLaunchStackId";
    }

    private int getCurrentApiVersion() {
        return Build.VERSION.SDK_INT;
    }
}
