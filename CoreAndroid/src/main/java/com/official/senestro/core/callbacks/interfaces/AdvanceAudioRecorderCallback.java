package com.official.senestro.core.callbacks.interfaces;

import androidx.annotation.NonNull;

import java.io.File;

public interface AdvanceAudioRecorderCallback {
    void onError(@NonNull String message);

    void onMessage(@NonNull String message);

    void onStarted(@NonNull File filename);

    void onAmplitude(double max, double normalized);

    void onTimeUpdate(int hours, int minutes, int seconds, int milliseconds, @NonNull String formatted);

    void onPcmByte(byte[] bytes);

    void onPaused();

    void onResumed();

    void onStopped(@NonNull File filename, int sampleRate, int channelsCount, int bufferSize, int bitsPerSample, int bitRate, int hours, int minutes, int seconds, int milliseconds, @NonNull String formatted);
}
