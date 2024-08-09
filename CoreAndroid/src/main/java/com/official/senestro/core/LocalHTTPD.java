package com.official.senestro.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.official.senestro.core.callbacks.interfaces.LocalHTTPDCallback;

import fi.iki.elonen.NanoHTTPD;

public class LocalHTTPD {
    private static final String TAG = LocalHTTPD.class.getSimpleName();
    private static final int MAX_PORT = 5000;
    private static final int MIN_PORT = 1000;
    private final Context context;
    private LocalHTTPDCallback callback;
    private final AdvanceWifi wifi;
    private NanoHTTPD nano;
    private boolean isBinded;
    private int port;
    private String address;

    public LocalHTTPD(@NonNull Context context) {
        this.context = context;
        this.wifi = new AdvanceWifi(context);
    }

    public boolean isBinded() {
        return isBinded;
    }

    public void bind(@NonNull String address, int port, @NonNull LocalHTTPDCallback callback) {
        this.address = address;
        this.port = port;
        this.callback = callback;
        startBinding();
    }

    public void bind(int port, @NonNull LocalHTTPDCallback callback) {
        this.address = wifi.getAvailableIPAddress();
        this.port = port;
        this.callback = callback;
        startBinding();
    }

    public void bind(@NonNull LocalHTTPDCallback callback) {
        this.address = wifi.getAvailableIPAddress();
        this.port = generateRandomPort();
        this.callback = callback;
        startBinding();
    }

    public void unbind() {
        if (isBinded && nano != null) {
            nano.stop();
            isBinded = false;
            nano = null;
            onUnbinded("LocalHTTP was closed successfully");
        }
    }

    // PRIVATE
    private void startBinding() {
        try {
            nano = new Nano(this.address, this.port);
            nano.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            isBinded = true;
            onBinded(this.address, this.port, "Server started and listening on: " + this.address + ":" + this.port);
        } catch (Throwable e) {
            isBinded = false;
            String message = e.getMessage();
            onError(message == null ? "An error has occurred" : message);
        }
    }

    private void onBinded(@NonNull String address, int port, @NonNull String message) {
        postToCallback(callbacks -> callbacks.onBinded(address, port, LocalHTTPD.TAG, message));
    }

    private void onUnbinded(@NonNull String message) {
        postToCallback(callbacks -> callbacks.onUnbinded(LocalHTTPD.TAG, message));
    }

    private void onMessage(@NonNull String message) {
        postToCallback(callbacks -> callbacks.onMessage(TAG, message));
    }

    private void onError(@NonNull String message) {
        postToCallback(callbacks -> callbacks.onError(LocalHTTPD.TAG, message));
    }

    private void postOnMainThread(@NonNull Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    private int generateRandomPort() {
        return (int) (Math.random() * (MAX_PORT - MIN_PORT + 1) + MIN_PORT);
    }

    private void postToCallback(CallbackExecutor executor) {
        if (callback != null) {
            postOnMainThread(() -> executor.execute(callback));
        }
    }

    // PUBLIC CLASS
    public static final class Nano extends NanoHTTPD {

        public Nano(int port) {
            super(port);
        }

        public Nano(String hostname, int port) {
            super(hostname, port);
        }

        @Override
        public Response serve(IHTTPSession session) {
            return newFixedLengthResponse("OK");
        }
    }

    // INTERFACE
    @FunctionalInterface
    private interface CallbackExecutor {
        void execute(LocalHTTPDCallback callbacks);
    }
}