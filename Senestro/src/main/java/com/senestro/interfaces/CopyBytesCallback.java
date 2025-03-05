package com.senestro.interfaces;

/**
 * A callback interface to track the progress of a byte-copying operation.
 * This can be used to monitor the transfer of data and provide feedback
 * on the number of bytes copied and the progress percentage.
 *
 * @author Senestro
 */
public interface CopyBytesCallback {

    /**
     * Called during a byte-copying operation to report progress.
     *
     * @param bytesToTotalBytes the number of bytes copied so far.
     * @param totalBytes        the total number of bytes to be copied.
     * @param progress          the current progress as a percentage (0-100).
     */
    void onBytes(int bytesToTotalBytes, int totalBytes, int progress);
}
