package com.official.senestro.core.callbacks.classes;

import androidx.annotation.NonNull;

import com.official.senestro.core.callbacks.interfaces.RootUtilsExecuteCommandCallback;

public class SimpleRootUtilsExecuteCommandCallback implements RootUtilsExecuteCommandCallback {
    @Override
    public void onOutputLine(@NonNull String line) {

    }

    @Override
    public void onErrorLine(@NonNull String line) {

    }

    @Override
    public void onExecuted() {

    }

    @Override
    public void onDenied() {

    }

    @Override
    public void onError(@NonNull String message) {

    }
}