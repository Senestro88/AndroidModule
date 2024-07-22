package com.official.senestro.core.callbacks.interfaces;

public interface VideoRepairCallback {
    void onDone(boolean isSuccess, String message);

    void onProgress(double progress);
}