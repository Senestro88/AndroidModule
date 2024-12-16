package com.official.senestro.core.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {
    private static final String TAG = AESUtils.class.getSimpleName();
    private final Context context;
    private final String algo = "AES";
    private final String mode = "AES/CBC/PKCS7Padding";
    private final String hashAlgo = "SHA-256";
    private final Charset charset = StandardCharsets.UTF_8;
    private boolean enableDebugging = false;

    public AESUtils(@NonNull Context context) {
        this.context = context;
    }

    public void enableDebugging(boolean enable) {
        this.enableDebugging = enable;
    }

    public String encData(final @NonNull String data, final @NonNull String key) {
        try {
            Cipher cipher = Cipher.getInstance(mode);
            byte[] ivBytes = generateIVBytes();
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes(charset), algo), new IvParameterSpec(ivBytes));
            byte[] resultBytes = cipher.doFinal(data.getBytes(charset));
            log("Cipher result bytes", resultBytes);
            byte[] combinedBytes = combine(ivBytes, resultBytes);
            String hex = bytesToHex(combinedBytes);
            return hex;
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
    }

    public String decData(final @NonNull String data, final @NonNull String key) {
        try {
            byte[] dataBytes = hexToBytes(data);
            byte[] ivBytes = new byte[16]; // Assuming IV length is 16 bytes for AES/CBC mode
            System.arraycopy(dataBytes, 0, ivBytes, 0, 16);
            byte[] cipherBytes = new byte[dataBytes.length - 16]; // Subtract IV length
            System.arraycopy(dataBytes, 16, cipherBytes, 0, cipherBytes.length);
            log("Cipher bytes", cipherBytes);
            Cipher cipher = Cipher.getInstance(mode);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes(charset), algo), new IvParameterSpec(ivBytes));
            byte[] resultBytes = cipher.doFinal(cipherBytes);
            String result = new String(resultBytes, charset);
            return result;
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
    }

    // PRIVATE
    private byte[] generateIVBytes() {
        byte[] iv = new byte[16]; // AES block size is 128 bits (16 bytes) CBC (Cipher Block Chaining)
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return iv;
    }

    private String bytesToHex(@NonNull byte[] bytes) {
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

    private byte[] hexToBytes(@NonNull String string) {
        int len = string.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(string.charAt(i), 16) << 4) + Character.digit(string.charAt(i + 1), 16));
        }
        return bytes;
    }

    private byte[] combine(final byte[] bytes1, final byte[] bytes2) {
        byte[] combined = new byte[bytes1.length + bytes2.length];
        System.arraycopy(bytes1, 0, combined, 0, bytes1.length);
        System.arraycopy(bytes2, 0, combined, bytes1.length, bytes2.length);
        return combined;
    }

    private void log(@NonNull String a, @NonNull String b) {
        if (enableDebugging) {
            Log.d(TAG, a + " [length: " + b.length() + "] [value: " + b + "]");
        }
    }

    private void log(@NonNull String a, @NonNull byte[] b) {
        if (enableDebugging) {
            Log.d(TAG, a + " [length: " + b.length + "] [hex: " + bytesToHex(b) + "]");
        }
    }

    private void log(@NonNull String message) {
        if (enableDebugging) {
            Log.d(TAG, message);
        }
    }
}