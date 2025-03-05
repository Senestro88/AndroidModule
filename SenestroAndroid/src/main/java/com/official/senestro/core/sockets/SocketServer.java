package com.official.senestro.core.sockets;

import androidx.annotation.NonNull;
import com.official.senestro.core.classes.SocketEventManager;
import com.official.senestro.core.utils.AdvanceUtils;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class SocketServer extends WebSocketServer {
    private final SocketEventManager em = new SocketEventManager();

    public SocketServer(@NonNull String hostname, int port) {
        super(new InetSocketAddress(hostname, port));
    }

    public SocketServer(@NonNull InetSocketAddress address) {
        super(address);
    }

    // PUBLIC FINAL METHODS (Final method can not be overrided)
    @Override
    public final void onOpen(WebSocket socket, ClientHandshake clientHandshake) {
        onClientOpen(socket, clientHandshake);
    }

    @Override
    public final void onClose(WebSocket socket, int i, String reason, boolean remote) {
        onClientClose(socket, i, reason, remote);
    }

    @Override
    public final void onMessage(WebSocket socket, String message) {
        if (AdvanceUtils.validJson(message)) {
            // Process custom events in JSON format
            JSONObject json = AdvanceUtils.Json(message);
            if (AdvanceUtils.notNull(json)) {
                Object event = AdvanceUtils.getDataFromJsonObject(json, "event");
                Object data = AdvanceUtils.getDataFromJsonObject(json, "data");
                if (AdvanceUtils.notNull(event) && AdvanceUtils.notNull(data)) {
                    // Emit event to the EventManager
                    em.emit((String) event, data);
                }
            }
        } else {
            onClientMessage(socket, message);
        }
    }

    @Override
    public final void onMessage(WebSocket socket, ByteBuffer buffer) {
        onClientByteBuffer(socket, buffer);
    }

    @Override
    public final void onError(WebSocket socket, Exception exception) {
        onClientError(socket, exception);
    }

    @Override
    public final void onStart() {
        onServerStarted();
    }

    // PUBLIC METHODS
    public void onClientOpen(@NonNull WebSocket socket, @NonNull ClientHandshake clientHandshake) {
    }

    public void onClientClose(@NonNull WebSocket socket, int i, @NonNull String reason, boolean remote) {
    }

    public void onClientMessage(@NonNull WebSocket socket, @NonNull String message) {
    }

    public void onClientByteBuffer(@NonNull WebSocket socket, @NonNull ByteBuffer buffer) {
    }

    public void onClientError(@NonNull WebSocket socket, @NonNull Exception exception) {
    }

    public void onServerStarted() {
    }

    @Override
    public void stop(int timeout) {
        try {
            super.stop(timeout);
        } catch (InterruptedException exception) {
        }
    }

    @Override
    public void stop(int timeout, String closeMessage) {
        try {
            super.stop(timeout, closeMessage);
        } catch (InterruptedException exception) {
        }
    }

    @Override
    public void stop() {
        try {
            super.stop();
        } catch (InterruptedException exception) {
        }
    }

    // Register event listeners
    public void on(String event, SocketEventManager.EventListener listener) {
        em.on(event, listener);
    }

    public void off(String event, SocketEventManager.EventListener listener) {
        em.off(event, listener);
    }

    // Send an event with custom data to the server
    public void emitToClient(@NonNull WebSocket socket, @NonNull String event, @NonNull Object data) {
        try {
            JSONObject json = new JSONObject();
            json.put("event", event);
            json.put("data", data);
            // Send the JSON-formatted event to the server
            socket.send(json.toString());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
