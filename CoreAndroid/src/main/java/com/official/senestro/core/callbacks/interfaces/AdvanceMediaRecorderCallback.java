package com.official.senestro.core.callbacks.interfaces;

import androidx.annotation.NonNull;

import java.io.File;

public interface AdvanceMediaRecorderCallback {
    void onError(@NonNull String message);

    void onMessage(@NonNull String message);

    void onStarted(@NonNull File file);

    void onTimeUpdate(int hours, int minutes, int seconds, int milliseconds, @NonNull String formatted);

    void onAmplitudes(int max, int normalized);

    void onPaused();

    void onResumed();

    void onStopped(@NonNull File file, int hours, int minutes, int seconds, int milliseconds, @NonNull String formatted);

    void onBroadcast(@NonNull File file);
}