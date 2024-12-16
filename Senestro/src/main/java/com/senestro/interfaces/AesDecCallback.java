package com.senestro.interfaces;

import com.senestro.Aes;
import com.senestro.annotations.NonNull;

/**
 * Callback interface for handling AES decryption results. This interface
 * provides methods to handle successful decryption and errors performed by the
 * {@link com.senestro.Aes} class
 *
 * @author Senestro
 */
public interface AesDecCallback {

    /**
     * Called when an error occurs during the decryption process.
     *
     * @param message The error message describing the issue. This parameter is
     *                guaranteed to be non-null.
     */
    void onError(@NonNull String message);

    /**
     * Called when decryption is successful.
     *
     * @param resultBytes The decrypted data as a byte array.
     */
    void onDecrypted(@NonNull Aes.DecryptResult result);
}
