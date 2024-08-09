package com.official.senestro.core;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;

public class TextStream {
    private TextStream() {
    }

    public static ByteArrayOutputStream createByteArrayOutputStreamFromText(String content) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            os.write(content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return os;
    }

    public static ByteArrayInputStream createByteArrayInputStreamFromText(String content) {
        return new ByteArrayInputStream(content.getBytes());
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
            e.printStackTrace();
        }
        return result.toString();
    }

    public static String convertByteArrayOutputStreamToText(ByteArrayOutputStream outputStream) {
        String result = null;
        try {
            result = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return baos;
    }

    public static byte[] readBinaryFileToBytes(@NonNull String filename) {
        ByteArrayOutputStream baos = readBinaryFileToBytesArrayOutputStream(filename);
        return baos.toByteArray();
    }

    private static void copyInputStreamToByteArrayOutputStream(InputStream in, ByteArrayOutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
    }
}