package com.official.senestro.core.callbacks.interfaces;

public interface CopyBytesChangedCallback {
    void onChanged(int bytesToTotalBytes, int totalBytes, int progress);
}