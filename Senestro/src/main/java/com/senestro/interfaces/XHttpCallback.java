package com.senestro.interfaces;

import com.senestro.annotations.NonNull;

import java.util.Map;

/**
 * A callback interface for handling HTTP responses and errors.
 *
 * <p>
 * This interface is used to process the result of HTTP operations performed by
 * the {@link com.senestro.XHttp} class.</p>
 *
 * @author Senestro
 */
public interface XHttpCallback {

    /**
     * Called when the HTTP request completes successfully.
     *
     * @param code    The HTTP status code of the response.
     * @param message A descriptive message associated with the status code.
     * @param headers A map of response headers (key-value pairs).
     * @param body    The body of the response as a string.
     */
    void onResponse(int code, @NonNull String message, @NonNull Map<String, String> headers, @NonNull String body);

    /**
     * Called when an error occurs during the HTTP request.
     *
     * @param exception The exception that was thrown during the HTTP operation.
     */
    void onError(@NonNull Exception exception);
}
