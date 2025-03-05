package com.senestro.interfaces;

import com.senestro.annotations.NonNull;

import java.util.HashMap;

public interface OkHttpCallback {

    void onResponse(@NonNull String tag, @NonNull String response, @NonNull HashMap<String, Object> responseHeaders);

    void onErrorResponse(@NonNull String tag, @NonNull String message);
}
