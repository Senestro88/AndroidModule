package com.official.senestro.core.utils;

import android.util.Base64;
import android.util.Log;
import androidx.annotation.NonNull;

/**
 * Utility class for encoding and decoding data using Base64.
 * This class wraps Android's Base64 encoding and decoding methods for ease of use.
 * It provides methods to encode and decode both strings and byte arrays.
 */
public class Base64Utils {
    private static final String tag = Base64Utils.class.getName();  // Tag used for logging

    /**
     * Encodes a string into Base64.
     * This method converts the input string into a byte array and then encodes it.
     *
     * @param data The input string to encode.
     * @return The Base64 encoded string.
     */
    public static String encode(final @NonNull String data) {
        return encode(data.getBytes());  // Convert string to bytes and then encode
    }

    /**
     * Encodes a string into Base64 with specific flags.
     * This method allows additional flags to be passed for encoding.
     *
     * @param data  The input string to encode.
     * @param flags The flags for encoding, as defined by Android's Base64.
     * @return The Base64 encoded string.
     */
    public static String encode(final @NonNull String data, int flags) {
        return encode(data.getBytes(), flags);  // Convert string to bytes and then encode with flags
    }

    /**
     * Encodes a byte array into Base64.
     * This method is used when the input is already in byte array format.
     *
     * @param bytes The byte array to encode.
     * @return The Base64 encoded string.
     */
    public static String encode(final @NonNull byte[] bytes) {
        return encode(bytes, Base64.DEFAULT);  // Use default encoding flags
    }

    /**
     * Encodes a byte array into Base64 with specific flags.
     * This method allows additional flags to be passed for encoding.
     *
     * @param bytes The byte array to encode.
     * @param flags The flags for encoding, as defined by Android's Base64.
     * @return The Base64 encoded string.
     */
    public static String encode(final @NonNull byte[] bytes, int flags) {
        String encoded = "";  // Initialize the encoded result
        try {
            // Use Android's Base64 utility to encode the byte array to a string
            encoded = android.util.Base64.encodeToString(bytes, flags);
        } catch (Throwable e) {
            // Log any error that occurs during encoding
            Log.e(tag, e.getMessage(), e);
        }
        return encoded;  // Return the encoded string
    }

    /**
     * Decodes a Base64 encoded string into the original data.
     * This method converts the input string into a byte array and then decodes it.
     *
     * @param data The Base64 encoded string to decode.
     * @return The decoded string.
     */
    public static String decode(final @NonNull String data) {
        return decode(data.getBytes());  // Convert string to bytes and then decode
    }

    /**
     * Decodes a Base64 encoded string into the original data with specific flags.
     * This method allows additional flags to be passed for decoding.
     *
     * @param data  The Base64 encoded string to decode.
     * @param flags The flags for decoding, as defined by Android's Base64.
     * @return The decoded string.
     */
    public static String decode(final @NonNull String data, int flags) {
        return decode(data.getBytes(), flags);  // Convert string to bytes and then decode with flags
    }

    /**
     * Decodes a Base64 encoded byte array into the original data.
     * This method is used when the input is already in byte array format.
     *
     * @param bytes The Base64 encoded byte array to decode.
     * @return The decoded string.
     */
    public static String decode(final @NonNull byte[] bytes) {
        return decode(bytes, android.util.Base64.DEFAULT);  // Use default decoding flags
    }

    /**
     * Decodes a Base64 encoded byte array into the original data with specific flags.
     * This method allows additional flags to be passed for decoding.
     *
     * @param bytes The Base64 encoded byte array to decode.
     * @param flags The flags for decoding, as defined by Android's Base64.
     * @return The decoded string.
     */
    public static String decode(final @NonNull byte[] bytes, int flags) {
        String decoded = "";  // Initialize the decoded result
        try {
            // Use Android's Base64 utility to decode the byte array and convert it back to a string
            decoded = new String(android.util.Base64.decode(bytes, flags));
        } catch (Throwable e) {
            // Log any error that occurs during decoding
            Log.e(tag, e.getMessage(), e);
        }
        return decoded;  // Return the decoded string
    }
}
