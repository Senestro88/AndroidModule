package com.official.senestro.core.utils;

import android.util.Log;
import androidx.annotation.NonNull;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class AESUtils {
    private static final String TAG = AESUtils.class.getSimpleName();
    private static final String algo = "AES";
    private static final String mode = "AES/CBC/PKCS7Padding";
    private static final Charset charset = StandardCharsets.UTF_8;

    /**
     * Encrypts the provided data using AES encryption.
     * The encryption is performed in CBC mode with an initialization vector (IV).
     * The IV is generated randomly for each encryption process to ensure secure encryption.
     * The encrypted data is returned as a byte array, which includes the IV followed by the encrypted data.
     *
     * @param dataBytes The data to be encrypted, represented as a byte array.
     * @param keyBytes  The encryption key, represented as a byte array. It should be of the correct size for AES.
     * @return A byte array containing the IV and the encrypted data. If encryption fails, returns null.
     */
    public static byte[] encData(final byte[] dataBytes, final byte[] keyBytes) {
        try {
            // Get the cipher instance for the specified mode and algorithm (e.g., AES)
            Cipher cipher = Cipher.getInstance(mode);
            // Generate a random Initialization Vector (IV) for encryption
            byte[] ivBytes = generateIvBytes();
            // Initialize the cipher in ENCRYPT_MODE with the key and IV
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, algo), new IvParameterSpec(ivBytes));
            // Perform the encryption and store the result
            byte[] resultBytes = cipher.doFinal(dataBytes);
            // Combine the IV and the encrypted result into a single byte array
            return combineBytes(ivBytes, resultBytes);
        } catch (Throwable e) {
            // Log the exception in case of an error
            Log.e(TAG, e.getMessage(), e);
            // Return null in case of failure
            return null;
        }
    }

    /**
     * Decrypts the provided encrypted data using AES decryption in CBC mode.
     * The encrypted data includes the IV (Initialization Vector) at the beginning,
     * which is extracted and used for decryption. The method assumes the IV is 16 bytes.
     *
     * @param encBytes The encrypted data, represented as a byte array. This includes the IV followed by the encrypted content.
     * @param keyBytes The decryption key, represented as a byte array. It should match the encryption key.
     * @return The decrypted data as a byte array. If decryption fails, returns null.
     */
    public static byte[] decData(final byte[] encBytes, final byte[] keyBytes) {
        try {
            // Extract the Initialization Vector (IV) from the beginning of the encrypted data
            byte[] ivBytes = new byte[16]; // Assuming IV length is 16 bytes for AES/CBC mode
            System.arraycopy(encBytes, 0, ivBytes, 0, 16); // Copy the first 16 bytes as the IV

            // Extract the actual encrypted cipher data (after the IV)
            byte[] cipherBytes = new byte[encBytes.length - 16]; // Subtract IV length
            System.arraycopy(encBytes, 16, cipherBytes, 0, cipherBytes.length); // Copy the cipher data

            // Get the cipher instance for decryption (AES in CBC mode)
            Cipher cipher = Cipher.getInstance(mode);

            // Initialize the cipher for decryption with the key and IV
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, algo), new IvParameterSpec(ivBytes));

            // Perform the decryption and return the result
            return cipher.doFinal(cipherBytes);

        } catch (Throwable e) {
            // Log the exception in case of an error
            Log.e(TAG, e.getMessage(), e);

            // Return null in case of failure
            return null;
        }
    }
    
    // PRIVATE
    private static byte[] generateIvBytes() {
        byte[] iv = new byte[16]; // AES block size is 128 bits (16 bytes) CBC (Cipher Block Chaining)
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return iv;
    }

    private static String bytesToHex(@NonNull byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                builder.append('0');
            }
            builder.append(hex);
        }
        return builder.toString();
    }

    private static byte[] hexToBytes(@NonNull String string) {
        int len = string.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(string.charAt(i), 16) << 4) + Character.digit(string.charAt(i + 1), 16));
        }
        return bytes;
    }

    private static byte[] combineBytes(final byte[] bytes1, final byte[] bytes2) {
        byte[] combined = new byte[bytes1.length + bytes2.length];
        System.arraycopy(bytes1, 0, combined, 0, bytes1.length);
        System.arraycopy(bytes2, 0, combined, bytes1.length, bytes2.length);
        return combined;
    }
}