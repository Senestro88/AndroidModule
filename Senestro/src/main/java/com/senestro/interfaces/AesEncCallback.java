package com.senestro.interfaces;

import com.senestro.Aes;
import com.senestro.annotations.NonNull;

/**
 * Callback interface for handling AES encryption results. This interface
 * provides methods to handle successful encryption and errors performed by the
 * {@link com.senestro.Aes} class
 *
 * @author Senestro
 */
public interface AesEncCallback {

    /**
     * Called when an error occurs during the encryption process.
     *
     * @param message The error message describing the issue. This parameter is
     *                guaranteed to be non-null.
     */
    void onError(@NonNull String message);

    /**
     * Called when encryption is successful.
     *
     * @param ivBytes     The initialization vector (IV) used for encryption.
     * @param resultBytes The encrypted data as a byte array.
     */
    void onEncrypted(@NonNull Aes.EncryptResult result);
}
