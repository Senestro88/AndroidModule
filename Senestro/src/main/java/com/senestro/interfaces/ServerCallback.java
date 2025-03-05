package com.senestro.interfaces;

import com.senestro.annotations.NonNull;
import com.senestro.server.Client;

/**
 * Interface that defines callback methods for server events.
 * This interface is intended to be implemented by classes that handle
 * specific server lifecycle events such as binding, closing, errors,
 * receiving messages, and client visits.
 * <p>
 * The methods in this interface should be invoked by the server
 * when the corresponding events occur.
 *
 * @author Senestro
 */
public interface ServerCallback {

    /**
     * Called when the server successfully binds to the specified address and port.
     *
     * @param address The address the server is bound to (e.g., "localhost").
     * @param port    The port the server is bound to (e.g., 8080).
     * @param message A message or status related to the bind operation.
     */
    void onBounded(@NonNull String address, int port, @NonNull String message);

    /**
     * Called when the server is closed or shut down.
     *
     * @param message A message or status related to the server closure.
     */
    void onClosed(@NonNull String message);

    /**
     * Called when an error occurs on the server.
     *
     * @param message The error message describing the issue.
     */
    void onError(@NonNull String message);

    /**
     * Called when the server receives a message.
     *
     * @param message The content of the received message.
     */
    void onMessage(@NonNull String message);

    /**
     * Called when a client visits or connects to the server.
     *
     * @param client The client that visited or connected to the server.
     */
    void onClientVisit(@NonNull Client client);
}
