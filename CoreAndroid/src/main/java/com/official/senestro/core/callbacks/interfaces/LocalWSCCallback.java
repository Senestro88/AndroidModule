package com.official.senestro.core.callbacks.interfaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.ByteBuffer;

public interface LocalWSCCallback {

    void onConnecting();

    void onConnectMessage(@NonNull String message);

    void onError(@Nullable String message);

    void onClose(int code, String reason, boolean remote);

    void onMessage(String message);

    void onMessage(ByteBuffer bytes);

    void onOpen();
}