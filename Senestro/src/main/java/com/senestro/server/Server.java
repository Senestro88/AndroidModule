package com.senestro.server;

import com.senestro.Utils;
import com.senestro.annotations.NonNull;
import com.senestro.interfaces.ServerCallback;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple server implementation that listens for incoming client connections,
 * handles them via threads, and notifies registered callbacks about various events
 * like connection, message reception, error, and client visit.
 *
 * The server binds to a specific address and port, and uses callbacks to notify
 * about status changes, client visits, and errors.
 *
 * @author Senestro
 */
public class Server {

    private ServerSocket server;  // The server socket that listens for connections
    private String address;  // The address the server listens on
    private int port;  // The port the server listens on
    private boolean connected = false;  // The connection status of the server
    private ServerCallback callback;  // The callback interface to notify about server events

    // Default constructor for the Server
    public Server() {}

    /**
     * Returns whether the server is currently connected (listening for connections).
     *
     * @return True if the server is connected, otherwise false.
     */
    public boolean connected() {
        return connected;
    }

    /**
     * Starts the server and listens for incoming client connections on the specified address and port.
     * It also sets the callback interface for notifications.
     *
     * @param address The address to bind the server to.
     * @param port The port to bind the server to.
     * @param callback The callback interface to handle server events.
     */
    public void connect(@NonNull String address, int port, @NonNull ServerCallback callback) {
        if (!connected()) {
            this.address = address;
            this.port = port;
            this.callback = callback;
            // Start the server in a new thread to handle blocking operations
            Thread thread = new Thread(new ServerRunnable());
            thread.start();
        }
    }

    /**
     * Closes the server connection if it is currently connected.
     * Notifies the callback if the server is closed successfully or if an error occurs.
     */
    public void close() {
        if (connected() && Utils.notNull(server)) {
            try {
                server.close();  // Close the server socket
                connected = false;  // Mark server as disconnected
                onClosed("PortForwarder was closed successfully");
            } catch (IOException exception) {
                String message = exception.getMessage();
                onError(Utils.isNull(message) ? "disconnect: Invalid exception message" : message);
            }
        }
    }

    // PRIVATE METHODS

    /**
     * Executes a callback if the callback object is not null.
     *
     * @param executor The executor function that performs the callback operation.
     */
    private void callbackExecute(@NonNull CallbackExecutor executor) {
        if (Utils.notNull(callback)) {
            executor.execute(callback);
        }
    }

    // PROTECTED METHODS

    /**
     * Notifies the callback that the server has successfully connected to the specified address and port.
     *
     * @param address The address the server is bound to.
     * @param port The port the server is bound to.
     * @param message The success message.
     */
    protected void onConnected(@NonNull String address, int port, @NonNull String message) {
        callbackExecute(listener -> listener.onBounded(address, port, message));
    }

    /**
     * Notifies the callback that the server has been closed with the specified message.
     *
     * @param message The message to send when the server is closed.
     */
    protected void onClosed(@NonNull String message) {
        callbackExecute(listener -> listener.onClosed(message));
    }

    /**
     * Notifies the callback that a message has been received by the server.
     *
     * @param message The message received by the server.
     */
    protected void onMessage(@NonNull String message) {
        callbackExecute(listener -> listener.onMessage(message));
    }

    /**
     * Notifies the callback that an error occurred during the server operation.
     *
     * @param errorMessage The error message to notify.
     */
    protected void onError(@NonNull String errorMessage) {
        callbackExecute(listener -> listener.onError(errorMessage));
    }

    /**
     * Notifies the callback that a client has visited the server.
     *
     * @param client The client that has visited the server.
     */
    protected void onClientVisit(@NonNull Client client) {
        callbackExecute(listener -> listener.onClientVisit(client));
    }

    // PRIVATE INTERFACE

    /**
     * A functional interface to execute the callback methods.
     */
    @FunctionalInterface
    private interface CallbackExecutor {
        void execute(@NonNull ServerCallback listener);
    }

    // PRIVATE CLASSES

    /**
     * This class runs the server in a separate thread, listens for incoming client connections,
     * and manages client socket handling.
     */
    private class ServerRunnable implements Runnable {

        @Override
        public void run() {
            try {
                // Initialize the server socket and bind to the address and port
                server = new ServerSocket();
                server.bind(new InetSocketAddress(address, port));
                connected = true;
                onConnected(address, port, "Web server is started and listening on: " + address + ":" + port);
                // Keep the server running and accepting client connections
                do {
                    // Accept an incoming client connection
                    Socket socket = server.accept();
                    // Start a new thread for each client
                    // Thread thread = new Thread(new ClientRunnable(socket));
                    // thread.start();
                    // Thread pool to handle multiple clients
                    ExecutorService executor = Executors.newFixedThreadPool(100);
                    executor.submit(new ClientRunnable(socket));
                } while (server.isClosed());
            } catch (Exception exception) {
                connected = false;
                String message = exception.getMessage();
                onError(Utils.isNull(message) ? "Web server received invalid message " : message);
            }
        }
    }

    /**
     * This class handles each individual client connection in a separate thread.
     */
    private class ClientRunnable implements Runnable {
        private final Socket socket;
        private final Client client;

        public ClientRunnable(@NonNull Socket socket) {
            this.socket = socket;
            this.client = new Client(Server.this, socket);
        }

        @Override
        public void run() {
            if (client.valid()) {
                onClientVisit(client);  // Notify that a valid client has connected
            }
        }
    }
}
