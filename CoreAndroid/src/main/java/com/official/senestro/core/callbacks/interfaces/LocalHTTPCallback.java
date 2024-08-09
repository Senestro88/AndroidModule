package com.official.senestro.core.callbacks.interfaces;

import androidx.annotation.NonNull;

import com.official.senestro.core.classes.LocalHTTPClient;

public interface LocalHTTPCallback {
    void onBinded(@NonNull String address, int port, @NonNull String messageTag, @NonNull String message);

    void onUnbinded(@NonNull String messageTag, @NonNull String message);

    void onError(@NonNull String messageTag, @NonNull String message);

    void onMessage(@NonNull String messageTag, @NonNull String message);

    void onVisit(LocalHTTPClient client);
}