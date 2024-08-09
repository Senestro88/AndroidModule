package com.official.senestro.core.callbacks.interfaces;

import androidx.annotation.NonNull;

public interface VoiceRecorderCallback {
    void onError(@NonNull String message);

    void onMessage(@NonNull String message);

    /**
     * Call when any recording has started
     */
    void onStarted(@NonNull String filename);

    /**
     * Called when recording any voice
     */
    void onTimeUpdate(@NonNull String filename, int hours, int minutes, int seconds,  int milliseconds, @NonNull String formatted);

    /**
     * Called when recording any voice and the voice has amplitudes
     */
    void onAmplitudes(@NonNull String filename, int max, int normalized);

    /**
     * Called when any recording is paused
     */
    void onPaused();

    /**
     * Called when any recording is resumed
     */
    void onResumed();

    /**
     * Called when any recording is stooped
     */
    void onStopped(@NonNull String filename);

    /**
     * Called when an recording is stopped and action to broadcast is sent to the stop() method
     */
    void onBroadcast(@NonNull String filename);

    /**
     * Called when any recording is stopped and action to convert is sent to the stop() method (Conversion must be successful before this method can be called)
     */
    void onConverted(@NonNull String oldFilename, @NonNull String newFilename);
}