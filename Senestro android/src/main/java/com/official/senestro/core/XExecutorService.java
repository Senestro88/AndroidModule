package com.official.senestro.core;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import com.official.senestro.core.callbacks.interfaces.XExecutorServiceCallback;
import com.official.senestro.core.callbacks.interfaces.XExecutorServiceToMainThreadCallback;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class XExecutorService {
    private static final String tag = XExecutorService.class.getName();

    private XExecutorService() {
    }

    public static void runInBackground(@NonNull XExecutorServiceCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            callback.run();
            shutdownExecutor(executor);
        });
    }

    public static void runInBackgroundToMainThread(@NonNull XExecutorServiceToMainThreadCallback callback, int killThreadIn) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            callback.run();
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(callback::done);
            handler.postDelayed(() -> shutdownExecutor(executor), killThreadIn);
        });
    }

    // PRIVATE

    private static void shutdownExecutor(@NonNull ExecutorService executor) {
        executor.shutdown();
    }
}