package com.official.senestro.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.official.senestro.core.callbacks.interfaces.LocalWSCCallback;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

public class LocalWSC {
    private static final String TAG = LocalWSS.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private static volatile LocalWSC instance;
    private final Context context;
    private LocalWSCCallback callback;
    private WebSocketClient client;
    private boolean isConnected;
    private boolean isConnecting;
    private int port;
    private String address;
    private static final int NORMAL_CLOSURE_STATUS = 1000;

    public LocalWSC(Context context) {
        this.context = context;
    }

    public static synchronized LocalWSC getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (LocalWSC.class) {
                if (instance == null) {
                    instance = new LocalWSC(context);
                }
            }
        }
        return instance;
    }

    public void setCallback(@NonNull LocalWSCCallback callback) {
        this.callback = callback;
    }

    public void unsetCallback() {
        this.callback = null;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void connect(@NonNull String address, int port) {
        if (!isConnected) {
            this.address = address;
            this.port = port;
            startConnecting();
        }
    }

    public void disconnect() {
        clientCallback(client -> client.close(NORMAL_CLOSURE_STATUS, "Connection closed by client"));
    }

    public void send(@NonNull String message) {
        clientCallback(client -> client.send(message));
    }

    private void send(@NonNull byte[] bytes) {
        clientCallback(client -> client.send(bytes));
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    // PRIVATE
    private void startConnecting() {
        if (isConnected) {
            postToCallback(callbacks -> callbacks.onConnectMessage("Client is already connected"));
        } else {
            if (isConnecting) {
                postToCallback(callbacks -> callbacks.onConnectMessage("Client is still opening connecting, please wait..."));
            } else {
                try {
                    URI serverUri = new URI("ws://" + address + ":" + port);
                    client = new SocketClientCallback(serverUri);
                    isConnecting = true;
                    postToCallback(LocalWSCCallback::onConnecting);
                    client.connect();
                } catch (Throwable e) {
                    isConnecting = false;
                    postToCallback(callbacks -> callbacks.onError(e.getMessage()));
                }
            }
        }
    }

    private void postOnMainThread(@NonNull Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    private void postToCallback(CallbackExecutor executor) {
        if (callback != null) {
            postOnMainThread(() -> executor.execute(callback));
        }
    }

    private void clientCallback(ClientExecutor executor) {
        if (isConnected && client != null) {
            executor.execute(client);
        }
    }

    // PRIVATE CLASS
    private final class SocketClientCallback extends WebSocketClient {
        private final URI serverUri;

        public SocketClientCallback(URI serverUri) {
            super(serverUri);
            this.serverUri = serverUri;
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
            isConnecting = false;
            isConnected = true;
            postToCallback(LocalWSCCallback::onOpen);
        }

        @Override
        public void onMessage(String message) {
            postToCallback(callbacks -> callbacks.onMessage(message));
        }

        @Override
        public void onMessage(ByteBuffer bytes) {
            super.onMessage(bytes);
            postToCallback(callbacks -> callbacks.onMessage(bytes));
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            isConnecting = false;
            isConnected = false;
            postToCallback(callbacks -> callbacks.onClose(code, reason, remote));
        }

        @Override
        public void onError(Exception ex) {
            postToCallback(callbacks -> callbacks.onError(ex.getMessage()));
        }
    }

    // INTERFACE
    @FunctionalInterface
    private interface CallbackExecutor {
        void execute(LocalWSCCallback callbacks);
    }

    @FunctionalInterface
    private interface ClientExecutor {
        void execute(WebSocketClient client);
    }
}