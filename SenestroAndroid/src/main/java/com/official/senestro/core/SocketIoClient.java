package com.official.senestro.core;

import androidx.annotation.NonNull;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;

import java.net.URISyntaxException;

public class SocketIoClient {
    private final Socket socket;

    public SocketIoClient(@NonNull String endpoint) throws URISyntaxException {
        IO.Options options = new IO.Options();
        options.reconnectionAttempts = 5;
        options.reconnectionDelay = 1000; // 1 second
        options.transports = new String[]{WebSocket.NAME};
        socket = IO.socket(endpoint, options);
        setSocketEvents();
        setCustomEvents();
    }

    public Socket getSocket() {
        return socket;
    }

    public void connect() {
        socket.connect();
    }

    public void disconnect() {
        socket.disconnect();
        socket.close();
    }

    // PRIVATE METHODS

    private void setSocketEvents() {
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                // Handle reconnection
            }
        });
        socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                // Handle reconnection
            }
        });
        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                // Handle disconnection
            }
        });
    }

    private void setCustomEvents() {
        socket.on("message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                // Handle reconnection
            }
        });
    }
}
