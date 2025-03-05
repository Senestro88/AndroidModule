package com.official.senestro.core.sockets;

import androidx.annotation.NonNull;
import com.official.senestro.core.classes.SocketEventManager;
import com.official.senestro.core.utils.AdvanceUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class SocketClient extends WebSocketClient {
    private final SocketEventManager em;

    public SocketClient(@NonNull URI uri, @NonNull SocketEventManager eventManager) {
        super(uri);
        this.em = eventManager;
    }

    public SocketClient(@NonNull URI uri, @NonNull HashMap<String, String> headers, @NonNull SocketEventManager em) {
        super(uri, headers);
        this.em = em;
    }

    // PUBLIC FINAL METHODS (Final method can not be overrided)
    @Override
    public final void onOpen(ServerHandshake serverHandshake) {

    }

    @Override
    public final void onMessage(String message) {
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
            onServerMessage(message);
        }
    }

    @Override
    public final void onMessage(ByteBuffer buffer) {
        onServerByteBuffer(buffer);
    }

    @Override
    public final void onClose(int i, String reason, boolean remote) {
        onConnClose(i, reason, remote);
    }

    @Override
    public final void onError(Exception exception) {
        onConnError(exception);
    }

    // PUBLIC METHODS

    public void onConnOpen(@NonNull ServerHandshake serverHandshake) {
    }

    public void onConnClose(int i, @NonNull String reason, boolean remote) {
    }

    public void onServerMessage(@NonNull String message) {
    }

    public void onServerByteBuffer(@NonNull ByteBuffer buffer) {
    }

    public void onConnError(@NonNull Exception exception) {
    }

    // Send an event with custom data to the server
    public void emitToServer(@NonNull String event, @NonNull Object data) {
        try {
            JSONObject json = new JSONObject();
            json.put("event", event);
            json.put("data", data);
            // Send the JSON-formatted event to the server
            send(json.toString());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
