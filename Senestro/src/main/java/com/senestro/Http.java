package com.senestro;

import com.senestro.annotations.NonNull;
import com.senestro.annotations.Nullable;
import com.senestro.interfaces.HttpCallback;
import kong.unirest.core.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * A utility class for making HTTP GET and POST requests with configurable SSL
 * contexts and callback handling for responses and errors.
 *
 * <p>
 * This class leverages the Unirest library for HTTP operations and supports
 * optional SSL verification, custom headers, query parameters, and file uploads
 * for POST requests.</p>
 *
 * @author Senestro
 */
public class Http {

    /**
     * Prevent instance initialization
     */
    private Http() {
    }

    /**
     * Executes an HTTP GET request.
     *
     * @param url The target URL (must not be null).
     * @param headers Optional headers to include in the request.
     * @param parameters Optional query parameters to include in the request.
     * @param callback Callback for handling the response or error (must not be
     * null).
     */
    public static void Get(@NonNull String url, @Nullable Map<String, String> headers, @Nullable Map<String, Object> parameters, @NonNull HttpCallback callback) {
        try {
            configureUnirest();
            GetRequest request = Unirest.get(url);
            setHeaders(request, headers);
            setParams(request, parameters);
            HttpResponse<String> response = request.asString();
            processResponse(response, callback);
        } catch (Exception exception) {
            callback.onError(exception);
        }
    }

    /**
     * Executes an HTTP POST request.
     *
     * @param url The target URL (must not be null).
     * @param headers Optional headers to include in the request.
     * @param parameters Optional form fields to include in the request.
     * @param files Optional files to upload as part of the request.
     * @param callback Callback for handling the response or error (must not be
     * null).
     */
    public static void Post(@NonNull String url, @Nullable Map<String, String> headers, @Nullable Map<String, Object> parameters, @Nullable Map<String, XFile> files, @NonNull HttpCallback callback) {
        try {
            configureUnirest();
            HttpRequestWithBody request = Unirest.post(url);
            setHeaders(request, headers);
            setParams(request, parameters);
            setFiles(request, files);
            HttpResponse<String> response = request.asString();
            processResponse(response, callback);
        } catch (Exception exception) {
            callback.onError(exception);
        }
    }

    /**
     * Executes an HTTP HEAD request.
     *
     * @param url The target URL (must not be null).
     * @param headers Optional headers to include in the request.
     * @param callback Callback for handling the response or error (must not be
     * null).
     */
    public static void Head(@NonNull String url, @Nullable Map<String, String> headers, @NonNull HttpCallback callback) {
        try {
            configureUnirest();
            GetRequest request = Unirest.head(url);
            setHeaders(request, headers);
            HttpResponse<String> response = request.asString();
            processResponse(response, callback);
        } catch (Exception exception) {
            callback.onError(exception);
        }
    }

    // Configures Unirest with SSL settings and timeouts.
    private static void configureUnirest() throws GeneralSecurityException, IOException {
        Config config = Unirest.config();
        config.requestTimeout(10000).connectTimeout(10000); // 10 seconds
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{Utils.trustManager(null)}, new SecureRandom());
        config.sslContext(sslContext).verifySsl(false);
    }

    // Processes the HTTP response and triggers the callback.
    private static void processResponse(@NonNull HttpResponse<String> response, @NonNull HttpCallback callback) {
        boolean success = response.isSuccess();
        int code = response.getStatus();
        String message = response.getStatusText();
        String body = response.getBody();
        HashMap<String, String> headers = new HashMap<>();
        for (Header header : response.getHeaders().all()) {
            headers.put(header.getName(), header.getValue());
        }
        callback.onResponse(success, code, message, headers, body);
    }

    // Sets headers for the request if provided.
    private static void setHeaders(@NonNull GetRequest request, @Nullable Map<String, String> headers) {
        if (Utils.notNull(headers)) {
            request.headersReplace(headers);
        }
    }

    private static void setHeaders(@NonNull HttpRequestWithBody request, @Nullable Map<String, String> headers) {
        if (Utils.notNull(headers)) {
            request.headersReplace(headers);
        }
    }

    // Sets query parameters for GET requests.
    private static void setParams(GetRequest request, @Nullable Map<String, Object> parameters) {
        if (Utils.notNull(parameters)) {
            request.queryString(parameters);
        }
    }

    // Sets form fields for POST requests.
    private static void setParams(HttpRequestWithBody request, @Nullable Map<String, Object> parameters) {
        if (Utils.notNull(parameters)) {
            request.fields(parameters);
        }
    }

    // Sets files for POST requests.
    private static void setFiles(HttpRequestWithBody request, @Nullable Map<String, XFile> files) {
        if (Utils.notNull(files)) {
            files.forEach(request::field);
        }
    }
}
