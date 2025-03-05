package com.senestro;

import com.senestro.annotations.NonNull;
import com.senestro.interfaces.AesDecCallback;
import com.senestro.interfaces.AesEncCallback;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.function.Consumer;

/**
 * Utility class for AES encryption and decryption. This class provides methods
 * to perform AES encryption and decryption operations using the
 * AES/CBC/PKCS5Padding transformation. It supports 16-byte and 32-byte keys.
 *
 * @author Senestro
 */
public class Aes {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Aes() {
    }

    private static final String TAG = Aes.class.getSimpleName(); // Tag for logging purposes
    private static final String AES_ALGORITHM = "AES"; // AES encryption algorithm
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding"; // Cipher transformation for AES
    private static final Charset CHARSET = StandardCharsets.UTF_8; // Default character set

    /**
     * Encrypts the given data asynchronously using AES encryption and provides
     * the result to a callback.
     *
     * @param data The plaintext data to encrypt.
     * @param key The encryption key. Must be 16 or 32 bytes.
     * @param callback Callback interface to handle encryption success or
     * failure.
     */
    public static void encrypt(@NonNull byte[] data, @NonNull byte[] key, @NonNull AesEncCallback callback) {
        try {
            callback.onEncrypted(encrypt(data, key));
        } catch (Exception exception) {
            handleException(exception, callback::onError);
        }
    }

    /**
     * Encrypts the given data synchronously using AES encryption.
     *
     * @param dataBytes The plaintext data to encrypt.
     * @param keyBytes The encryption key. Must be 16 or 32 bytes.
     * @return EncryptResult containing the IV and encrypted data.
     * @throws Exception If an error occurs during encryption.
     */
    public static EncryptResult encrypt(@NonNull byte[] dataBytes, @NonNull byte[] keyBytes) throws Exception {
        validateInputs(dataBytes, keyBytes);
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        byte[] ivBytes = generateIvBytes(cipher);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, AES_ALGORITHM), new IvParameterSpec(ivBytes));
        byte[] resultBytes = cipherExecute(cipher, dataBytes);
        return new EncryptResult(ivBytes, resultBytes);
    }

    /**
     * Decrypts the given data asynchronously using AES decryption and provides
     * the result to a callback.
     *
     * @param dataBytes The encrypted data to decrypt.
     * @param keyBytes The decryption key. Must be 16 or 32 bytes.
     * @param ivBytes The initialization vector (IV) used during encryption.
     * @param callback Callback interface to handle decryption success or
     * failure.
     */
    public static void decrypt(@NonNull byte[] dataBytes, @NonNull byte[] keyBytes, @NonNull byte[] ivBytes, @NonNull AesDecCallback callback) {
        try {
            callback.onDecrypted(decrypt(dataBytes, keyBytes, ivBytes));
        } catch (Exception exception) {
            handleException(exception, callback::onError);
        }
    }

    /**
     * Decrypts the given data synchronously using AES decryption.
     *
     * @param dataBytes The encrypted data to decrypt.
     * @param keyBytes The decryption key. Must be 16 or 32 bytes.
     * @param ivBytes The initialization vector (IV) used during encryption.
     * @return DecryptResult containing the decrypted data.
     * @throws Exception If an error occurs during decryption.
     */
    public static DecryptResult decrypt(@NonNull byte[] dataBytes, @NonNull byte[] keyBytes, @NonNull byte[] ivBytes) throws Exception {
        validateInputs(dataBytes, keyBytes);
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, AES_ALGORITHM), new IvParameterSpec(ivBytes));
        byte[] resultBytes = cipherExecute(cipher, dataBytes);
        return new DecryptResult(resultBytes);
    }

    // PRIVATE METHODS
    /**
     * Validates the input data and encryption key for AES operations.
     *
     * @param dataBytes The input data.
     * @param keyBytes The encryption key.
     * @throws IllegalArgumentException If the data is empty or the key size is
     * invalid.
     */
    private static void validateInputs(@NonNull byte[] dataBytes, @NonNull byte[] keyBytes) {
        if (dataBytes.length < 1 || (keyBytes.length != 16 && keyBytes.length != 32)) {
            throw new IllegalArgumentException("Invalid input: Data cannot be empty, and key must be 16 or 32 bytes.");
        }
    }

    /**
     * Generates a random initialization vector (IV) for AES encryption.
     *
     * @param cipher The cipher instance to determine the block size.
     * @return A byte array containing the generated IV.
     */
    private static byte[] generateIvBytes(@NonNull Cipher cipher) {
        byte[] iv = new byte[cipher.getBlockSize()];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    /**
     * Executes encryption or decryption using the given cipher and input data.
     *
     * @param cipher The initialized Cipher instance.
     * @param bytes The input data to process.
     * @return A byte array containing the processed data.
     * @throws Exception If an error occurs during the cipher operation.
     */
    private static byte[] cipherExecute(@NonNull Cipher cipher, byte[] bytes) throws Exception {
        // Get the block size of the cipher (e.g., 16 bytes for AES).
        int blockSize = cipher.getBlockSize();
        // Create a buffer large enough to hold the output of the cipher operation.
        // The size accounts for possible padding in the final output.
        byte[] outputBuffer = new byte[cipher.getOutputSize(bytes.length)];
        // Keep track of the total number of bytes written to the output buffer.
        int length = 0;
        // Process the input byte array in chunks of size equal to the cipher's block size.
        for (int i = 0; i < bytes.length; i += blockSize) {
            // Determine the size of the current chunk.
            // For the last chunk, it will be smaller if there are fewer remaining bytes.
            int chunkSize = Math.min(blockSize, bytes.length - i);
            // Update the cipher with the current chunk and write the processed bytes to the output buffer.
            length += cipher.update(bytes, i, chunkSize, outputBuffer, length);
        }
        // Finalize the cipher operation, processing any remaining bytes and handling padding.
        length += cipher.doFinal(outputBuffer, length);
        // Create an exact-sized array for the final output and copy the processed bytes into it.
        byte[] outputBytes = new byte[length];
        System.arraycopy(outputBuffer, 0, outputBytes, 0, length);
        // Return the processed output bytes.
        return outputBytes;
    }

    /**
     * Handles exceptions during AES encryption or decryption.
     *
     * @param exception The exception that occurred.
     * @param callback A callback to handle the error message.
     */
    private static void handleException(Exception exception, @NonNull Consumer<String> callback) {
        exception.printStackTrace();
        String message = exception.getMessage();
        callback.accept(message == null ? "An error has occurred" : message);
    }

    // PUBLIC CLASSES
    /**
     * Represents the result of an AES encryption operation, including the
     * initialization vector (IV) and the encrypted data.
     */
    public static class EncryptResult {

        private final byte[] ivBytes; // The initialization vector used in encryption
        private final byte[] resultBytes; // The encrypted data

        /**
         * Constructs an EncryptResult instance.
         *
         * @param ivBytes The initialization vector.
         * @param resultBytes The encrypted data.
         */
        public EncryptResult(byte[] ivBytes, byte[] resultBytes) {
            this.ivBytes = ivBytes;
            this.resultBytes = resultBytes;
        }

        /**
         * Gets the initialization vector (IV).
         *
         * @return A byte array containing the IV.
         */
        public byte[] getIvBytes() {
            return ivBytes;
        }

        /**
         * Gets the encrypted data.
         *
         * @return A byte array containing the encrypted data.
         */
        public byte[] getResultBytes() {
            return resultBytes;
        }
    }

    /**
     * Represents the result of an AES decryption operation, including the
     * decrypted data.
     */
    public static class DecryptResult {

        private final byte[] resultBytes; // The decrypted data

        /**
         * Constructs a DecryptResult instance.
         *
         * @param resultBytes The decrypted data.
         */
        public DecryptResult(byte[] resultBytes) {
            this.resultBytes = resultBytes;
        }

        /**
         * Gets the decrypted data.
         *
         * @return A byte array containing the decrypted data.
         */
        public byte[] getResultBytes() {
            return resultBytes;
        }
    }
}
