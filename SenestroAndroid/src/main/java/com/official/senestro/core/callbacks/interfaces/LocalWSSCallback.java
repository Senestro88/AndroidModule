package com.official.senestro.core.callbacks.interfaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.ByteBuffer;

public interface LocalWSSCallback {
    void onClientOpen(@NonNull String clientId);

    void onClientClose(@NonNull String clientId, int code, @NonNull String reason, boolean remote);

    void onClientMessage(@NonNull String clientId, @NonNull String message);

    void onClientMessage(@NonNull String clientId, ByteBuffer message);

    void onClientError(@NonNull String clientId, @NonNull Throwable throwable);

    void onStarting();

    void onStartMessage(@NonNull String message);

    void onStarted(@NonNull String address, int port);

    void onError(@Nullable String message);

    void onEnded();
}