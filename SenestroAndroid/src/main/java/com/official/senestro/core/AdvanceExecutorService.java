package com.official.senestro.core;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import com.official.senestro.core.callbacks.interfaces.AdvanceExecutorServiceCallback;
import com.official.senestro.core.callbacks.interfaces.AdvanceExecutorServiceToMainThreadCallback;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdvanceExecutorService {
    private static final String tag = AdvanceExecutorService.class.getName();

    private AdvanceExecutorService() {
    }

    public static void runInBackground(@NonNull AdvanceExecutorServiceCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            callback.run();
            shutdownExecutor(executor);
        });
    }

    public static void runInBackgroundToMainThread(@NonNull AdvanceExecutorServiceToMainThreadCallback callback, int killThreadIn) {
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