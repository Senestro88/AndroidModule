package com.official.senestro.core.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;

public class ClearRecentUtils {
    private final String tag = ClearRecentUtils.class.getName();

    private final Context context;

    public ClearRecentUtils(@NonNull Context context) {
        this.context = context;
    }

    public void clearAppFromRecent() {
        try {
            ActivityManager activityManager = (ActivityManager) this.context.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager != null) {
                // Get the app task
                List<ActivityManager.AppTask> appTasks = activityManager.getAppTasks();
                // Finish each task, effectively removing the app from the recent apps list
                for (ActivityManager.AppTask appTask : appTasks) {
                    appTask.finishAndRemoveTask();
                }
            }
        } catch (Exception e) {
            Log.e(tag, e.getMessage(), e);
        }
    }
}