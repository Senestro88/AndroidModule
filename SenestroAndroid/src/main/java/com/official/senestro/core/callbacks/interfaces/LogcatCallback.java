package com.official.senestro.core.callbacks.interfaces;

import androidx.annotation.NonNull;

import java.io.IOException;

public interface LogcatCallback {
    void onCaptured(@NonNull String log);

    void onError(@NonNull IOException exception);
}
