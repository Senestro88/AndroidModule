package com.official.senestro.core.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.official.senestro.core.callbacks.interfaces.StaticHandlerThreadCallback;
import com.official.senestro.core.callbacks.interfaces.StaticHandlerThreadToUICallback;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A utility class providing static methods for executing tasks using HandlerThreads.
 */
public class AdvanceHandlerThread {
    private static final AtomicInteger taskId = new AtomicInteger(1);

    private AdvanceHandlerThread(){}

    /**
     * Executes a task in a background HandlerThread.
     *
     * @param callback The callback to be executed in the background thread.
     */
    public static void runInBackground(StaticHandlerThreadCallback callback) {
        if (callback != null) {
            HandlerThread handlerThread = startHandlerThread("HandlerThread:" + taskId.getAndIncrement());
            new Handler(handlerThread.getLooper()).post(() -> {
                try {
                    callback.run();
                    handlerThread.quit();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * Executes a task in a background HandlerThread and then posts the result to the UI thread.
     *
     * @param callback The callback to be executed.
     * @param killThreadIn The delay in milliseconds after which the background thread should be terminated.
     */
    public static void runInBackgroundToUi(StaticHandlerThreadToUICallback callback, int killThreadIn) {
        if (callback != null) {
            HandlerThread thread = startHandlerThread("HandlerThread:" + taskId.getAndIncrement());
            new Handler(thread.getLooper()).post(() -> {
                try {
                    callback.onRun();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        try {
                            callback.onUICallback();
                            new Handler(Looper.getMainLooper()).postDelayed(thread::quit, killThreadIn);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    });
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * Starts a new HandlerThread with the given name.
     *
     * @param name The name of the HandlerThread.
     * @return The started HandlerThread.
     */
    private static HandlerThread startHandlerThread(String name) {
        HandlerThread handlerThread = new HandlerThread(name);
        handlerThread.start();
        return handlerThread;
    }
}