package com.official.senestro.core.utils;

import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Calendar;

public class CryptoUtils {
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";

    public static KeyPair getKeyPairFromAndroidKeyStore(@NonNull Context context, @NonNull String keyAlias) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        // API 21-22 (Lollipop)
        if (!keyStore.containsAlias(keyAlias)) {
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            // Valid for 10 years
            end.add(Calendar.YEAR, 10);
            KeyPairGeneratorSpec keyPairGeneratorSpec = new KeyPairGeneratorSpec.Builder(context).setAlias(keyAlias).setKeySize(2048) // RSA keys should be at least 2048 bits
                    .setSubject(new X500Principal("CN=" + keyAlias)).setSerialNumber(BigInteger.valueOf(System.currentTimeMillis())) // Unique serial number
                    .setStartDate(start.getTime()).setEndDate(end.getTime()).build();
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
            keyPairGenerator.initialize(keyPairGeneratorSpec);
            keyPairGenerator.generateKeyPair();
        }
        Certificate cert = keyStore.getCertificate(keyAlias);
        PublicKey publicKey = cert.getPublicKey();
        // Retrieve the private key
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyAlias, null);
        return new KeyPair(publicKey, privateKey);
    }

    @RequiresApi(api = 23)
    public static SecretKey getSecretKeyFromAndroidKeyStore(@NonNull String keyAlias) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        // API 23+ (Marshmallow and above)
        // Android 6+ (API 23+)
        if (!keyStore.containsAlias(keyAlias)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", ANDROID_KEYSTORE);
            keyGenerator.init(new KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_CTR) // Valid AES block mode
                    .setKeySize(2048) // AES supports only 128, 192, or 256 bits
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE) // No padding
                    .build());
            // Generates and stores the AES key in Keystore
            keyGenerator.generateKey();
        }
        return ((KeyStore.SecretKeyEntry) keyStore.getEntry(keyAlias, null)).getSecretKey();
    }

    public static byte[] encryptRSA(byte[] data, @NonNull String keyAlias) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(keyAlias));
        return cipher.doFinal(data);
    }

    public static byte[] decryptRSA(byte[] encryptedData, @NonNull String keyAlias) throws Exception {
        PrivateKey privateKey = getPrivateKey(keyAlias);
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedData);
    }

    public static byte[] encryptAES(byte[] data, @NonNull SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] iv = cipher.getIV();
        byte[] cipherText = cipher.doFinal(data);
        // Concatenate IV and CipherText (IV is required for decryption)
        byte[] encryptedData = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, encryptedData, 0, iv.length);
        System.arraycopy(cipherText, 0, encryptedData, iv.length, cipherText.length);
        return encryptedData;
    }

    public static byte[] encryptAES(byte[] data, @NonNull PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] iv = cipher.getIV();
        byte[] cipherText = cipher.doFinal(data);
        // Concatenate IV and CipherText (IV is required for decryption)
        byte[] encryptedData = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, encryptedData, 0, iv.length);
        System.arraycopy(cipherText, 0, encryptedData, iv.length, cipherText.length);
        return encryptedData;
    }

    public static byte[] decryptAES(byte[] encryptedData, @NonNull SecretKey secretKey) throws Exception {
        // Extract IV (first 16 bytes)
        byte[] iv = Arrays.copyOfRange(encryptedData, 0, 16);
        // Remaining bytes are cipher text
        byte[] cipherText = Arrays.copyOfRange(encryptedData, 16, encryptedData.length);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        return cipher.doFinal(cipherText);
    }

    public static byte[] decryptAES(byte[] encryptedData, @NonNull PrivateKey privateKey) throws Exception {
        // Extract IV (first 16 bytes)
        byte[] iv = Arrays.copyOfRange(encryptedData, 0, 16);
        // Remaining bytes are cipher text
        byte[] cipherText = Arrays.copyOfRange(encryptedData, 16, encryptedData.length);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey, new IvParameterSpec(iv));
        return cipher.doFinal(cipherText);
    }

    // PRIVATE METHODS

    private static PublicKey getPublicKey(@NonNull String keyAlias) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        return keyStore.getCertificate(keyAlias).getPublicKey();
    }

    private static PrivateKey getPrivateKey(@NonNull String keyAlias) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        return (PrivateKey) keyStore.getKey(keyAlias, null);
    }
}
