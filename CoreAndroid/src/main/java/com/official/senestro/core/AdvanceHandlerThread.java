package com.official.senestro.core;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.official.senestro.core.callbacks.interfaces.AdvanceHandlerThreadCallback;
import com.official.senestro.core.callbacks.interfaces.AdvanceHandlerThreadToMainThreadCallback;

import java.util.concurrent.atomic.AtomicInteger;

public class AdvanceHandlerThread {
    private static final String tag = AdvanceHandlerThread.class.getName();

    private static final AtomicInteger identities = new AtomicInteger(1);

    private AdvanceHandlerThread() {
    }

    public static void runInBackground(@NonNull AdvanceHandlerThreadCallback callback) {
        runInBackground("", callback);
    }

    public static void runInBackground(@NonNull String name, @NonNull AdvanceHandlerThreadCallback callback) {
        name = name.isEmpty() ? "HandlerThread" : name;
        HandlerThread handlerThread = startHandlerThread(name.concat(":[" + identities.getAndIncrement() + "]"));
        new Handler(handlerThread.getLooper()).post(() -> {
            try {
                callback.run();
                handlerThread.quit();
            } catch (Throwable e) {
                Log.e(tag, e.getMessage(), e);
            }
        });
    }

    public static void runInBackgroundToMainThread(@NonNull AdvanceHandlerThreadToMainThreadCallback callback, int killThreadIn) {
        runInBackgroundToMainThread("", callback, killThreadIn);
    }

    public static void runInBackgroundToMainThread(@NonNull String name, @NonNull AdvanceHandlerThreadToMainThreadCallback callback, int killThreadIn) {
        name = name.isEmpty() ? "HandlerThread" : name;
        HandlerThread thread = startHandlerThread(name.concat(":[" + identities.getAndIncrement() + "]"));
        new Handler(thread.getLooper()).post(() -> {
            try {
                callback.onRun();
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        callback.onUICallback();
                        new Handler(Looper.getMainLooper()).postDelayed(thread::quit, killThreadIn);
                    } catch (Throwable e) {
                        Log.e(tag, e.getMessage(), e);
                    }
                });
            } catch (Throwable e) {
                Log.e(tag, e.getMessage(), e);
            }
        });
    }

    private static HandlerThread startHandlerThread(@NonNull String name) {
        HandlerThread handlerThread = new HandlerThread(name);
        handlerThread.start();
        return handlerThread;
    }
}