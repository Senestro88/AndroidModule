package com.official.senestro.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.official.senestro.core.callbacks.interfaces.LocalHTTPCallback;
import com.official.senestro.core.classes.LocalHTTPClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalHTTP {
    private static final String TAG = LocalHTTP.class.getSimpleName();
    private static final int MAX_PORT = 5000;
    private static final int MIN_PORT = 1000;
    private final Context context;
    private final AdvanceWifi wifi;
    private int port;
    private String address;
    private LocalHTTPCallback callback;
    private ServerSocket server;
    private final ArrayList<Socket> sockets = new ArrayList<>();
    private boolean isBinded = false;

    public LocalHTTP(@NonNull Context context) {
        this.context = context;
        this.wifi = new AdvanceWifi(context);
    }

    public boolean isBinded() {
        return isBinded;
    }

    public void unbind() {
        if (isBinded && server != null) {
            try {
                close();
                server.close();
                isBinded = false;
                onUnbinded("LocalHTTP was closed successfully");
            } catch (Throwable e) {
                onError(e.getMessage() == null ? "disconnect: Invalid exception message" : e.getMessage());
            }
        }
    }

    public void bind(@NonNull String address, int port, @NonNull LocalHTTPCallback callback) {
        this.callback = callback;
        this.address = address;
        this.port = port;
        startBinding();
    }

    public void bind(int port, @NonNull LocalHTTPCallback callback) {
        this.callback = callback;
        this.address = this.wifi.getAvailableIPAddress();
        this.port = port;
        startBinding();
    }

    public void bind(@NonNull LocalHTTPCallback callback) {
        this.callback = callback;
        this.address = this.wifi.getAvailableIPAddress();
        this.port = generateRandomPort();
        startBinding();
    }

    // PRIVATE
    private void startBinding() {
        if (!isBinded()) {
            ExecutorService executor = Executors.newFixedThreadPool(10);
            executor.submit(new StartServerRunnable(this));
        }
    }

    private int generateRandomPort() {
        return (int) (Math.random() * (MAX_PORT - MIN_PORT + 1) + MIN_PORT);
    }

    private void close() {
        for (Socket socket : sockets) {
            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void onBinded(@NonNull String address, int port, @NonNull String message) {
        postToCallback(callbacks -> callbacks.onBinded(address, port, LocalHTTP.TAG, message));
    }

    private void onUnbinded(@NonNull String message) {
        postToCallback(callbacks -> callbacks.onUnbinded(LocalHTTP.TAG, message));
    }

    private void onMessage(@NonNull String message) {
        postToCallback(callbacks -> callbacks.onMessage(TAG, message));
    }

    private void onError(@NonNull String errorMessage) {
        postToCallback(callbacks -> callbacks.onError(LocalHTTP.TAG, errorMessage));
    }

    private void onVisit(@NonNull LocalHTTPClient client) {
        postToCallback(callbacks -> callbacks.onVisit(client));
    }

    private void postOnMainThread(@NonNull Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    private void postToCallback(CallbackExecutor executor) {
        if (callback != null) {
            postOnMainThread(() -> executor.execute(callback));
        }
    }

    private static class StartServerRunnable implements Runnable {
        private final LocalHTTP instance;

        public StartServerRunnable(LocalHTTP instance) {
            this.instance = instance;
        }

        @Override
        public void run() {
            try {
                instance.server = new ServerSocket();
                instance.server.bind(new InetSocketAddress(instance.address, instance.port));
                instance.isBinded = true;
                instance.onBinded(instance.address, instance.port, "Server started and listening on: " + instance.address + ":" + instance.port);
                do {
                    Socket socket = instance.server.accept();
                    int socketIndex = instance.sockets.size();
                    instance.sockets.add(socketIndex, socket);
                    ExecutorService executor = Executors.newFixedThreadPool(10);
                    executor.submit(new StartClientRunnable(instance, socket, socketIndex));
                } while (!instance.server.isClosed());
            } catch (Throwable e) {
                instance.isBinded = false;
                instance.onError(e.getMessage() == null ? "Invalid exception message" : e.getMessage());
            }
        }
    }

    private static class StartClientRunnable implements Runnable {
        private final LocalHTTP instance;
        private final Socket socket;
        private final int socketIndex;
        private LocalHTTPClient client;

        public StartClientRunnable(@NonNull LocalHTTP instance, @NonNull Socket socket, int socketIndex) {
            this.instance = instance;
            this.socket = socket;
            this.socketIndex = socketIndex;
        }

        @Override
        public void run() {
            client = new LocalHTTPClient(instance, socket, socketIndex);
            instance.onVisit(client);
            try {
                String requestMethod = client.getMethod();
                handleRequestMethod(requestMethod);
            } catch (Throwable e) {
                e.printStackTrace();
                instance.onError(e.getMessage() == null ? "Invalid exception message" : e.getMessage());
            }
        }

        // PRIVATE
        private void handleRequestMethod(String requestMethod) {
            switch (requestMethod) {
                case "GET":
                    handleGETMethod();
                    break;
                case "POST":
                    handlePOSTMethod();
                    break;
                case "HEAD":
                    handleHEADMethod();
                    break;
                default:
                    client.sendStatus(403);
                    break;
            }
        }

        private void handleGETMethod() {
        }

        private void handlePOSTMethod() {
            // Implementation for POST method
        }

        private void handleHEADMethod() {
            // Implementation for HEAD method
        }
    }

    // INTERFACE
    @FunctionalInterface
    private interface CallbackExecutor {
        void execute(LocalHTTPCallback callbacks);
    }
}