package com.official.senestro.core.callbacks.interfaces;

public interface CopyBytesChangedCallback {
    /**
     * Called when bytes are copied.
     *
     * @param bytesCopied The number of bytes copied so far.
     * @param totalBytes  The total bytes to copy.
     * @param progress    The copy progress as a percentage (0–100).
     */
    void onChanged(long bytesToTotalBytes, long totalBytes, int progress);
}