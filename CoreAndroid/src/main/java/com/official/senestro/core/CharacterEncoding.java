package com.official.senestro.core;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Pattern;

public class CharacterEncoding {
    private static final String tag = CharacterEncoding.class.getName();

    private CharacterEncoding() {
    }

    private static final Pattern CHARSET_PATTERN = Pattern.compile("[ |\t]*(charset)[ |\t]*=[ |\t]*['|\"]?([^\"^'^;^,]*)['|\"]?", 2);

    public static byte[] characterEncode(String originalString, String encoding) {
        if (validEncoding(encoding)) {
            try {
                return originalString.getBytes(encoding.toUpperCase());
            } catch (UnsupportedEncodingException e) {
                Log.e(tag, e.getMessage(), e);
            }
        }
        return null;
    }

    public static String characterDecode(byte[] encodingBytes, String encoding) {
        if (encodingBytes != null && validEncoding(encoding)) {
            try {
                return new String(encodingBytes, encoding.toUpperCase());
            } catch (UnsupportedEncodingException e) {
                Log.e(tag, e.getMessage(), e);
            }
        }
        return null;
    }

    public static BufferedReader characterStreamRead(String absolutePath, String encoding) {
        if (validEncoding(encoding)) {
            try (FileInputStream fileInputStream = new FileInputStream(absolutePath)) {
                return new BufferedReader(new InputStreamReader(fileInputStream, encoding.toUpperCase()));
            } catch (IOException e) {
                Log.e(tag, e.getMessage(), e);
            }
        }
        return null;
    }

    public static boolean isASCII(String input) {
        return input.matches("\\A\\p{ASCII}*\\z");
    }

    public static Map<String, Charset> availableCharsets() {
        return Charset.availableCharsets();
    }

    public static boolean encodingValid(String encoding) {
        return availableCharsets().containsKey(encoding.toLowerCase());
    }

    public static boolean validEncoding(String encoding) {
        return availableCharsets().containsKey(encoding.toUpperCase());
    }
}
