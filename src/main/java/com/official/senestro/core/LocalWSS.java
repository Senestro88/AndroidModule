package com.official.senestro.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.official.senestro.core.callbacks.interfaces.LocalWSSCallback;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LocalWSS {
    private static final String TAG = LocalWSS.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private static volatile LocalWSS instance;
    private static final int MAX_PORT = 5000;
    private static final int MIN_PORT = 1000;
    private final @NonNull Context context;
    private @Nullable LocalWSSCallback callback;
    private final @NonNull AdvanceWifi wifi;
    private WebSocketServer server;
    private boolean isStarted;
    private boolean isStarting;
    private int port;
    private String address;
    private final HashMap<WebSocket, String> clientsId;

    public LocalWSS(@NonNull Context context) {
        this.context = context;
        this.wifi = new AdvanceWifi(context);
        this.clientsId = new HashMap<>();
        this.address = wifi.getAvailableIPAddress();
        this.port = generateRandomPort();
    }

    public static synchronized LocalWSS getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (LocalWSS.class) {
                if (instance == null) {
                    instance = new LocalWSS(context);
                }
            }
        }
        return instance;
    }

    public void setCallback(@NonNull LocalWSSCallback callback) {
        this.callback = callback;
    }

    public void unsetCallback() {
        this.callback = null;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void start(@NonNull String address, int port) {
        this.address = address;
        this.port = port;
        startBinding();
    }

    public void start(int port) {
        this.address = wifi.getAvailableIPAddress();
        this.port = port;
        startBinding();
    }

    public void start() {
        this.address = wifi.getAvailableIPAddress();
        this.port = generateRandomPort();
        startBinding();
    }

    public void stop() {
        if (isStarted) {
            try {
                isStarting = false;
                server.stop(0);
                isStarted = false;
                postToCallbacks(LocalWSSCallback::onEnded);
            } catch (Throwable e) {
                postToCallbacks(callbacks -> callbacks.onError(e.getMessage()));
            }
        }
    }

    public void send(@NonNull String message) {
        postToServer(server -> server.broadcast(message));
    }

    public void send(byte[] bytes) {
        postToServer(server -> server.broadcast(ByteBuffer.wrap(bytes)));
    }

    public void send(@NonNull String clientId, @NonNull String message) {
        WebSocket client = getKeyByValue(clientId);
        if (client != null) {
            try {
                client.send(message);
            } catch (Throwable e) {
                postToCallbacks(callbacks -> callbacks.onClientError(clientId, e));
            }
        }
    }

    public void send(@NonNull String connectionId, byte[] bytes) {
        WebSocket client = getKeyByValue(connectionId);
        if (client != null) {
            client.send(ByteBuffer.wrap(bytes));
        }
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    // PRIVATE
    private void startBinding() {
        if (isStarted) {
            postToCallbacks(callbacks -> callbacks.onStartMessage("Server is already started"));
        } else {
            if (isStarting) {
                postToCallbacks(callbacks -> callbacks.onStartMessage("Server is still starting, please wait..."));
            } else {
                try {
                    server = new ServerCallback(this, new InetSocketAddress(address, port));
                    isStarting = true;
                    postToCallbacks(LocalWSSCallback::onStarting);
                    server.start();
                } catch (Throwable e) {
                    isStarting = false;
                    postToCallbacks(callbacks -> callbacks.onError(e.getMessage()));
                }
            }
        }
    }

    private void postOnMainThread(@NonNull Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    private void postToCallbacks(CallbackExecutor executor) {
        if (callback != null) {
            postOnMainThread(() -> executor.execute(callback));
        }
    }

    private void postToServer(ServerExecutor executor) {
        if (isStarted && server != null) {
            executor.execute(server);
        }
    }

    private int generateRandomPort() {
        return (int) (Math.random() * (MAX_PORT - MIN_PORT + 1) + MIN_PORT);
    }

    private long generateRandomNumber() {
        long min = 100_000_000L;
        long max = 900_000_000L;
        Random random = new Random();
        return min + (long) (random.nextDouble() * (max - min + 1));
    }

    private String generateClientId() {
        return "Client_" + generateRandomNumber();
    }

    private String getClientId(@NonNull WebSocket conn) {
        String clientId = clientsId.get(conn);
        return clientId != null ? clientId : "";
    }

    private void setClientId(@NonNull WebSocket conn, @NonNull String connectionId) {
        if (clientsId.get(conn) == null) {
            clientsId.put(conn, connectionId);
        }
    }

    private void deleteClientId(@NonNull WebSocket conn) {
        clientsId.remove(conn);
    }

    private WebSocket getKeyByValue(@NonNull String connectionId) {
        for (Map.Entry<WebSocket, String> entry : clientsId.entrySet()) {
            if (connectionId.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    // PUBLIC CLASS
    public final class ServerCallback extends WebSocketServer {
        private final @NonNull LocalWSS instance;

        public ServerCallback(@NonNull LocalWSS instance, @NonNull InetSocketAddress socketAddress) {
            super(socketAddress);
            this.instance = instance;
            setTcpNoDelay(false);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            String clientId = generateClientId();
            clientsId.put(conn, clientId);
            postToCallbacks(callbacks -> callbacks.onClientOpen(clientId));
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            String clientId = getClientId(conn);
            deleteClientId(conn);
            postToCallbacks(callbacks -> callbacks.onClientClose(clientId, code, reason, remote));
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            postToCallbacks(callbacks -> callbacks.onClientMessage(getClientId(conn), message));
        }

        @Override
        public void onMessage(WebSocket conn, ByteBuffer message) {
            postToCallbacks(callbacks -> callbacks.onClientMessage(getClientId(conn), message));
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            postToCallbacks(callbacks -> callbacks.onClientError(getClientId(conn), ex));
        }

        @Override
        public void onStart() {
            isStarting = false;
            isStarted = true;
            postToCallbacks(callbacks -> callbacks.onStarted(instance.getAddress(), instance.getPort()));
        }
    }

    // INTERFACE
    @FunctionalInterface
    private interface CallbackExecutor {
        void execute(LocalWSSCallback callbacks);
    }

    @FunctionalInterface
    private interface ServerExecutor {
        void execute(WebSocketServer server);
    }
}