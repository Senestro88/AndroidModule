package com.official.senestro.core.callbacks.interfaces;

import androidx.annotation.NonNull;

import java.util.Map;

public interface XHttpCallback {
    void onResponse(int code, @NonNull String message, @NonNull Map<String, String> headers, @NonNull String body);

    void onError(@NonNull Throwable throwable);
}
