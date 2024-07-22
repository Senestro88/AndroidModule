package com.official.senestro.core;

import android.content.Context;

import androidx.annotation.NonNull;

import com.official.senestro.core.utils.Base64Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipManager {
    private final Context context;

    public GzipManager(@NonNull Context context) {
        this.context = context;
    }

    // Function to gzip encode data
    public static byte[] gzipEncode(@NonNull String data) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(data.getBytes(StandardCharsets.UTF_8));
        }
        return byteArrayOutputStream.toByteArray();
    }

    // Function to gzip decode data
    public static String gzipDecode(@NonNull byte[] data) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream)) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            return new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    public static String gzipEncrypt(@NonNull Context context, @NonNull String data, @NonNull String password) throws Exception {
        // Compress the data
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(data.getBytes(StandardCharsets.UTF_8));
        }
        // Generate salt and derive the key
        byte[] salt = deriveBinByteToken(16);
        byte[] key = deriveBinByteKey(password, salt, 32); // 32 bytes key for AES-256
        // Encrypt the compressed data
        byte[] iv = new byte[16];
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
        byte[] compressedData = byteArrayOutputStream.toByteArray();
        byte[] encryptedData = cipher.doFinal(compressedData);
        // Return the encrypted data, the IV, and the salt
        byte[] combinedData = new byte[salt.length + iv.length + encryptedData.length];
        System.arraycopy(salt, 0, combinedData, 0, salt.length);
        System.arraycopy(iv, 0, combinedData, salt.length, iv.length);
        System.arraycopy(encryptedData, 0, combinedData, salt.length + iv.length, encryptedData.length);
        return new Base64Utils(context).encode(combinedData);
    }

    public static String gzipDecrypt(@NonNull Context context, @NonNull String encryptedData, @NonNull String password) throws Exception {
        // Decode the base64 encoded data
        byte[] combinedData = new Base64Utils(context).decode(encryptedData).getBytes();
        // Extract the salt, IV, and encrypted data
        byte[] salt = new byte[16];
        System.arraycopy(combinedData, 0, salt, 0, salt.length);
        byte[] iv = new byte[16];
        System.arraycopy(combinedData, salt.length, iv, 0, iv.length);
        byte[] encryptedDataBytes = new byte[combinedData.length - salt.length - iv.length];
        System.arraycopy(combinedData, salt.length + iv.length, encryptedDataBytes, 0, encryptedDataBytes.length);
        // Derive the key from the password and salt
        byte[] key = deriveBinByteKey(password, salt, 32); // 32 bytes key for AES-256
        // Decrypt the data
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
        byte[] compressedData = cipher.doFinal(encryptedDataBytes);
        // Decompress the data
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedData);
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    // Method to derive a binary key from a password and a binary token (salt)
    public static byte[] deriveBinByteKey(String password, byte[] token, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
        int iterations = 10000; // Number of iterations
        char[] passwordChars = password.toCharArray(); // Convert password to char array
        // Create PBEKeySpec with password, token (salt), iterations, and key length (in bits)
        PBEKeySpec spec = new PBEKeySpec(passwordChars, token, iterations, keyLength * 8);
        // Get SecretKeyFactory instance for PBKDF2WithHmacSHA256
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        // Generate the derived key
        byte[] key = keyFactory.generateSecret(spec).getEncoded();
        // Return the key
        return key;
    }

    // Method to generate a random binary token (salt)
    public static byte[] deriveBinByteToken(int length) {
        // Create SecureRandom instance
        SecureRandom random = new SecureRandom();
        // Create byte array for token (salt)
        byte[] token = new byte[length];
        // Generate random bytes for token (salt)
        random.nextBytes(token);
        // Return the token
        return token;
    }
}