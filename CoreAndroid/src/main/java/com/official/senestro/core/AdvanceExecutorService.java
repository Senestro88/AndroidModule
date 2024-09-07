package com.official.senestro.core;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.official.senestro.core.callbacks.interfaces.AdvanceExecutorServiceCallback;
import com.official.senestro.core.callbacks.interfaces.AdvanceExecutorServiceToMainThreadCallback;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AdvanceExecutorService {
    private static final String tag = AdvanceExecutorService.class.getName();

    private AdvanceExecutorService() {
    }

    public static void runInBackground(@NonNull AdvanceExecutorServiceCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                callback.onRun();
                shutdownExecutor(executor);
            } catch (Throwable e) {
                Log.e(tag, e.getMessage(), e);
            }
        });
    }

    public static void runInBackgroundToMainThread(@NonNull AdvanceExecutorServiceToMainThreadCallback callback, int killThreadIn) {
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
                        Log.e(tag, e.getMessage(), e);
                    }
                });
            } catch (Throwable e) {
                Log.e(tag, e.getMessage(), e);
            }
        });
    }

    private static void shutdownExecutor(@NonNull ExecutorService executor) {
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