package com.official.senestro.core.callbacks.interfaces;

import androidx.annotation.NonNull;

public interface RootUtilsExecuteCommandCallback {
    void onOutputLine(@NonNull String line);

    void onErrorLine(@NonNull String line);

    void onExecuted();

    void onDenied();

    void onError(@NonNull String message);
}