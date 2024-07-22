package com.official.senestro.core.callbacks.interfaces;

public interface VideoTranscoderCallback {
    void onProgress(double progress);

    void onDone(boolean isSuccess, String message);
}