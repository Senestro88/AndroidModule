package com.official.senestro.core;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import androidx.annotation.NonNull;
import com.official.senestro.core.callbacks.interfaces.AdvanceHandlerThreadCallback;
import com.official.senestro.core.callbacks.interfaces.AdvanceHandlerThreadToMainThreadCallback;
import com.official.senestro.core.utils.AdvanceUtils;

public class AdvanceHandlerThread {
    private static final String tag = AdvanceHandlerThread.class.getName();

    private AdvanceHandlerThread() {
    }

    public static void runInBackground(@NonNull AdvanceHandlerThreadCallback callback) {
        runInBackground(generateRandomThreadName(), callback);
    }

    public static void runInBackground(@NonNull String name, @NonNull AdvanceHandlerThreadCallback callback) {
        name = name.isEmpty() ? generateRandomThreadName() : name;
        HandlerThread handlerThread = startHandlerThread(name);
        Handler looperHandler = new Handler(handlerThread.getLooper());
        looperHandler.post(() -> {
            callback.run();
            quitHandlerThread(handlerThread);
        });
    }

    public static void runInBackgroundToMainThread(@NonNull AdvanceHandlerThreadToMainThreadCallback callback, int killThreadIn) {
        runInBackgroundToMainThread(generateRandomThreadName(), callback, killThreadIn);
    }

    public static void runInBackgroundToMainThread(@NonNull String name, @NonNull AdvanceHandlerThreadToMainThreadCallback callback, int killThreadIn) {
        name = name.isEmpty() ? generateRandomThreadName() : name;
        Handler uiHandler = new Handler(Looper.getMainLooper());
        HandlerThread handlerThread = startHandlerThread(name);
        Handler looperHandler = new Handler(handlerThread.getLooper());
        looperHandler.post(() -> {
            callback.run();
            uiHandler.post(callback::done);
            looperHandler.postDelayed(() -> quitHandlerThread(handlerThread), killThreadIn);
        });
    }

    // PRIVATE

    private static HandlerThread startHandlerThread(@NonNull String name) {
        HandlerThread handlerThread = new HandlerThread(name);
        handlerThread.start();
        return handlerThread;
    }

    private static String generateRandomThreadName() {
        String numberText = String.valueOf(AdvanceUtils.getRandom(10000, 99999));
        return AdvanceUtils.generateRandomText(4) + numberText;
    }

    private static void quitHandlerThread(@NonNull HandlerThread handlerThread) {
        handlerThread.quitSafely();
    }
}