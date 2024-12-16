package com.official.senestro.core;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import androidx.annotation.NonNull;
import com.official.senestro.core.callbacks.interfaces.XHandlerThreadCallback;
import com.official.senestro.core.callbacks.interfaces.XHandlerThreadToMainThreadCallback;
import com.official.senestro.core.utils.XUtils;

public class XHandlerThread {
    private static final String tag = XHandlerThread.class.getName();

    private XHandlerThread() {
    }

    public static void runInBackground(@NonNull XHandlerThreadCallback callback) {
        runInBackground(generateRandomThreadName(), callback);
    }

    public static void runInBackground(@NonNull String name, @NonNull XHandlerThreadCallback callback) {
        name = name.isEmpty() ? generateRandomThreadName() : name;
        HandlerThread handlerThread = startHandlerThread(name);
        Handler looperHandler = new Handler(handlerThread.getLooper());
        looperHandler.post(() -> {
            callback.run();
            quitHandlerThread(handlerThread);
        });
    }

    public static void runInBackgroundToMainThread(@NonNull XHandlerThreadToMainThreadCallback callback, int killThreadIn) {
        runInBackgroundToMainThread(generateRandomThreadName(), callback, killThreadIn);
    }

    public static void runInBackgroundToMainThread(@NonNull String name, @NonNull XHandlerThreadToMainThreadCallback callback, int killThreadIn) {
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
        String numberText = String.valueOf(XUtils.getRandom(10000, 99999));
        return XUtils.generateRandomText(4) + numberText;
    }

    private static void quitHandlerThread(@NonNull HandlerThread handlerThread) {
        handlerThread.quitSafely();
    }
}