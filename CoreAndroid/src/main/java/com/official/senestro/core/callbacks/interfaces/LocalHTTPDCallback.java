package com.official.senestro.core.callbacks.interfaces;

import androidx.annotation.NonNull;

public interface LocalHTTPDCallback {
    void onBinded(@NonNull String address, int port, @NonNull String messageTag, @NonNull String message);

    void onUnbinded(@NonNull String messageTag, @NonNull String message);

    void onError(@NonNull String messageTag, @NonNull String message);

    void onMessage(@NonNull String messageTag, @NonNull String message);
}