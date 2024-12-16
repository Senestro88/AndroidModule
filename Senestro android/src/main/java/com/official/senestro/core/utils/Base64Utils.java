package com.official.senestro.core.utils;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

public class Base64Utils {
    private final String tag = Base64Utils.class.getName();

    private final Context context;

    public Base64Utils(@NonNull Context context) {
        this.context = context;
    }

    public String encode(final @NonNull String data) {
        return encode(data.getBytes());
    }

    public String encode(final @NonNull String data, int flags) {
        return encode(data.getBytes(), flags);
    }

    public String encode(final @NonNull byte[] bytes) {
        return encode(bytes, Base64.DEFAULT);
    }

    public String encode(final @NonNull byte[] bytes, int flags) {
        String encoded = "";
        try {
            encoded = android.util.Base64.encodeToString(bytes, flags);
        } catch (Throwable e) {
            Log.e(tag, e.getMessage(), e);
        }
        return encoded;
    }

    public String decode(final @NonNull String data) {
        return decode(data.getBytes());
    }

    public String decode(final @NonNull String data, int flags) {
        return decode(data.getBytes(), flags);
    }

    public String decode(final @NonNull byte[] bytes) {
        return decode(bytes, android.util.Base64.DEFAULT);
    }

    public String decode(final @NonNull byte[] bytes, int flags) {
        String decoded = "";
        try {
            decoded = new String(android.util.Base64.decode(bytes, flags));
        } catch (Throwable e) {
            Log.e(tag, e.getMessage(), e);
        }
        return decoded;
    }
}