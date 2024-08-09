package com.official.senestro.core.utils;

import android.os.Handler;
import android.os.Looper;

import com.official.senestro.core.callbacks.interfaces.AdvanceExecutorServiceCallback;
import com.official.senestro.core.callbacks.interfaces.AdvanceExecutorServiceToUICallback;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A utility class providing static methods for executing tasks in background threads.
 */
public class AdvanceExecutorService {

    private AdvanceExecutorService(){}

    /**
     * Executes a task in a background thread.
     *
     * @param callback The callback to be executed in the background thread.
     */
    public static void runInBackground(AdvanceExecutorServiceCallback callback) {
        if (callback != null) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    callback.run();
                    shutdownExecutor(executor);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * Executes a task in a background thread and then posts the result to the UI thread.
     *
     * @param callback The callback to be executed.
     * @param killThreadIn The delay in milliseconds after which the background thread should be terminated.
     */
    public static void runInBackgroundToUi(AdvanceExecutorServiceToUICallback callback, int killThreadIn) {
        if (callback != null) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    callback.onRun();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        try {
                            callback.onDone();
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                shutdownExecutor(executor);
                            }, killThreadIn);
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
     * Shuts down an ExecutorService gracefully, waiting for a specified timeout.
     *
     * @param executor The ExecutorService to shut down.
     */
    private static void shutdownExecutor(ExecutorService executor) {
        try {
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}