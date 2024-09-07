package com.official.senestro.core;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TextStream {
    private static final String tag = TextStream.class.getName();

    private TextStream() {
    }

    public static ByteArrayOutputStream createByteArrayOutputStreamFromText(@NonNull String content) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            os.write(content.getBytes());
        } catch (IOException e) {
            Log.e(tag, e.getMessage(), e);
        }
        return os;
    }

    public static ByteArrayOutputStream createByteArrayOutputStreamFromByte(@NonNull byte[] bytes) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            os.write(bytes);
        } catch (IOException e) {
            Log.e(tag, e.getMessage(), e);
        }
        return os;
    }

    public static ByteArrayInputStream createByteArrayInputStreamFromText(@NonNull String content) {
        return new ByteArrayInputStream(content.getBytes());
    }

    public static ByteArrayInputStream createByteArrayInputStreamFromByte(@NonNull byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

    public static String readTextFromByteArrayInputStream(ByteArrayInputStream inputStream) {
        int data;
        StringBuilder result = new StringBuilder();
        try {
            while ((data = inputStream.read()) != -1) {
                result.append((char) data);
            }
            inputStream.close();
        } catch (IOException e) {
            Log.e(tag, e.getMessage(), e);
        }
        return result.toString();
    }

    public static String convertByteArrayOutputStreamToText(ByteArrayOutputStream outputStream) {
        String result = null;
        try {
            result = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            outputStream.close();
        } catch (IOException e) {
            Log.e(tag, e.getMessage(), e);
        }
        return result;
    }

    public static ByteArrayOutputStream readBinaryFileToBytesArrayOutputStream(@NonNull String filename) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename))) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
        } catch (Throwable e) {
            Log.e(tag, e.getMessage(), e);
        }
        return baos;
    }

    public static byte[] readBinaryFileToBytes(@NonNull String filename) {
        ByteArrayOutputStream baos = readBinaryFileToBytesArrayOutputStream(filename);
        return baos.toByteArray();
    }

    private static void copyInputStreamToByteArrayOutputStream(@NonNull InputStream in, @NonNull ByteArrayOutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
    }
}