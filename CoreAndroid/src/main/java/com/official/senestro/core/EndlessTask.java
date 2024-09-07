package com.official.senestro.core;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.official.senestro.core.callbacks.interfaces.EndlessTaskCallback;

public class EndlessTask {
    private final Handler handler;
    private final long interval;
    private boolean isRunning;

    public EndlessTask(long intervalMillis) {
        this.interval = intervalMillis;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void start(@NonNull EndlessTaskCallback callback) {
        // Prevent multiple starts
        if (!isRunning) {
            isRunning = true;
            // Initial execution
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (isRunning) {
                        // Execute your task here
                        callback.onTask();
                        // Schedule the next execution
                        handler.postDelayed(this, interval);
                    }
                }
            });
        }
    }

    public void stop() {
        if (isRunning) {
            isRunning = false;
            handler.removeCallbacksAndMessages(null);
        }
    }
}