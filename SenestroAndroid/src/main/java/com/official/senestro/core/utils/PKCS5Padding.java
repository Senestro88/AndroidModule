package com.official.senestro.core.utils;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class PKCS5Padding {
    private static int blockSize = 16; // AES block size is 16 bytes

    private PKCS5Padding(){}

    public static String addPadding(@NonNull String data) {
        if (needsPadding(data)) {
            StringBuilder builder = new StringBuilder(data);
            int length = blockSize - (data.length() % blockSize);
            for (int i = 0; i < length; i++) {
                builder.append((char) length);
            }
            return builder.toString();
        }
        return data;
    }

    public static byte[] addPadding(@NonNull byte[] data) {
        int length = blockSize - (data.length % blockSize);
        byte value = (byte) length;
        byte[] paddedData = Arrays.copyOf(data, data.length + length);
        Arrays.fill(paddedData, data.length, paddedData.length, value);
        return paddedData;
    }

    public static String removePadding(@NonNull String data) {
        if (needsPaddingRemoval(data)) {
            int length = data.charAt(data.length() - 1);
            return data.substring(0, data.length() - length);
        }
        return data;
    }

    public static byte[] removePadding(@NonNull byte[] data) {
        if (needsPaddingRemoval(data)) {
            int length = data[data.length - 1];
            return Arrays.copyOf(data, data.length - length);
        }
        return data;
    }

    public static boolean needsPadding(@NonNull String data) {
        int length = data.length();
        int lenght = blockSize - (length % blockSize);
        return lenght != blockSize; // Padding is needed if length is not equal to blockSize
    }

    public static boolean needsPaddingRemoval(@NonNull byte[] data) {
        if (data.length == 0) {
            return false; // If data is empty, no padding to remove
        }
        int paddingValue = data[data.length - 1];
        return paddingValue > 0 && data.length > paddingValue;
    }

    public static boolean needsPaddingRemoval(@NonNull String data) {
        if (data.isEmpty()) {
            return false; // If data is empty, no padding to remove
        }
        int paddingValue = data.charAt(data.length() - 1);
        return paddingValue > 0 && data.length() > paddingValue;
    }
}