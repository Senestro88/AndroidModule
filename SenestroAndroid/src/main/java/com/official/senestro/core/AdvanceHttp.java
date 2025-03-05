package com.official.senestro.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.official.senestro.core.callbacks.interfaces.AdvanceHttpCallback;
import com.official.senestro.core.utils.AdvanceUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AdvanceHttp {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36";
    private static final String LINE = "\r\n";

    /**
     * Private constructor to prevent instantiation.
     */
    private AdvanceHttp() {
    }

    /**
     * Sends a GET request to the specified URL with optional headers and
     * parameters.
     *
     * @param url        The target URL.
     * @param headers    Optional HTTP headers to include in the request.
     * @param parameters Optional query parameters to append to the URL.
     * @param callback   The callback to handle the response or errors.
     */
    public static void Get(@NonNull String url, @Nullable Map<String, String> headers, @Nullable Map<String, Object> parameters, @NonNull AdvanceHttpCallback callback) {
        AdvanceExecutorService.runInBackground(() -> {
            try {
                // Get trust manager that trust all certificates
                trustManager();
                // Build query string
                String query = buildQueryString(parameters);
                String $$url = getUrlWithoutQueryString(url) + query;
                // Make an HTTPS request
                URL $url = new URL(getUrlWithoutQueryString(url));
                HttpURLConnection connection = (HttpURLConnection) $url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", USER_AGENT);
                setRequestHeaders(connection, headers);
                setRequestParams(connection, parameters);
                // Get the response details
                int $code = connection.getResponseCode();
                String $message = connection.getResponseMessage();
                String $body = getResponseBody(connection);
                Map<String, String> $headers = getResponseHeaders(connection);
                callback.onResponse($code, $message, $headers, $body);
            } catch (Exception exception) {
                callback.onError(exception);
            }
        });
    }

    /**
     * Sends a POST request to the specified URL with optional headers and
     * parameters. Supports multipart form data for file uploads.
     *
     * @param url        The target URL.
     * @param headers    Optional HTTP headers to include in the request.
     * @param parameters Optional form data to include in the request body.
     * @param callback   The callback to handle the response or errors.
     */
    public static void Post(@NonNull String url, @Nullable Map<String, String> headers, @Nullable Map<String, Object> parameters, @NonNull AdvanceHttpCallback callback) {
        AdvanceExecutorService.runInBackground(() -> {
            try {
                // Get trust manager that trust all certificates
                trustManager();
                // The boundary string
                String boundary = "----WebKitFormBoundary" + System.currentTimeMillis() + UUID.randomUUID() + "----";
                // Make an HTTPS request
                URL $url = new URL(getUrlWithoutQueryString(url));
                HttpURLConnection connection = (HttpURLConnection) $url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("User-Agent", USER_AGENT);
                setRequestHeaders(connection, headers);
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                try (OutputStream os = connection.getOutputStream(); PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), true)) {
                    if (parameters != null) {
                        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                            String field = entry.getKey();
                            String value = entry.getValue().toString();
                            if (!AdvanceUtils.isFile(value)) {
                                // Add text form field
                                writer.append("--").append(boundary).append(LINE);
                                writer.append("Content-Disposition: form-data; name=\"").append(field).append("\"").append(LINE).append(LINE);
                                writer.append(value).append(LINE);
                            } else {
                                // Add file part
                                AdvanceFile file = new AdvanceFile(value);
                                String defaultMime = "application/octet-stream";
                                String mainMime = file.getMime();
                                String mimeFromExtension = file.getMimeFromFileExtension();
                                mimeFromExtension = mimeFromExtension == null ? defaultMime : mimeFromExtension;
                                String mime = mainMime == null ? mimeFromExtension : mainMime;
                                writer.append("--").append(boundary).append(LINE);
                                writer.append("Content-Disposition: form-data; name=\"").append(field).append("\"; filename=\"").append(file.getName()).append("\"").append(LINE);
                                writer.append("Content-Type: ").append(mime).append(LINE).append(LINE);
                                System.out.println(mime);
                                writer.flush();
                                // Write file content
                                try (FileInputStream fis = new FileInputStream(file)) {
                                    byte[] buffer = new byte[4096];
                                    int bytesRead;
                                    while ((bytesRead = fis.read(buffer)) != -1) {
                                        os.write(buffer, 0, bytesRead);
                                    }
                                    os.flush();
                                }
                                writer.append(LINE).flush();
                                writer.append("--").append(boundary).append("--").append(LINE);
                            }
                        }
                    }
                }
                // Get the response details
                int $code = connection.getResponseCode();
                String $message = connection.getResponseMessage();
                String $body = getResponseBody(connection);
                Map<String, String> $headers = getResponseHeaders(connection);
                callback.onResponse($code, $message, $headers, $body);
            } catch (Throwable exception) {
                callback.onError(exception);
            }
        });
    }

    /**
     * Sends a HEAD request to the specified URL with optional headers.
     *
     * @param url      The target URL.
     * @param headers  Optional HTTP headers to include in the request.
     * @param callback The callback to handle the response or errors.
     */
    public static void Head(@NonNull String url, @Nullable Map<String, String> headers, @NonNull AdvanceHttpCallback callback) {
        AdvanceExecutorService.runInBackground(() -> {
            try {
                // Get trust manager that trust all certificates
                trustManager();
                // Make an HTTPS request
                URL $url = new URL(getUrlWithoutQueryString(url));
                HttpURLConnection connection = (HttpURLConnection) $url.openConnection();
                connection.setRequestMethod("HEAD");
                connection.setRequestProperty("User-Agent", USER_AGENT);
                setRequestHeaders(connection, headers);
                // Get the response details
                int $code = connection.getResponseCode();
                String $message = connection.getResponseMessage();
                String $body = getResponseBody(connection);
                Map<String, String> $headers = getResponseHeaders(connection);
                callback.onResponse($code, $message, $headers, $body);
            } catch (IOException | GeneralSecurityException exception) {
                callback.onError(exception);
            }
        });
    }

    // PRIVATE METHODS

    /**
     * Configures the trust manager to trust all certificates.
     *
     * @throws NoSuchAlgorithmException If the TLS algorithm is not available.
     * @throws GeneralSecurityException If a security error occurs.
     */
    private static void trustManager() throws NoSuchAlgorithmException, GeneralSecurityException, IOException {
        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, new TrustManager[]{AdvanceUtils.unsafeTrustManager(null)}, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        // Optional: Disable hostname verification
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }

    /**
     * Reads the response body from the connection's input stream.
     *
     * @param connection The HTTP connection.
     * @return The response body as a string.
     * @throws IOException If an I/O error occurs.
     */
    private static String getResponseBody(@NonNull HttpURLConnection connection) throws IOException {
        StringBuilder builder;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            builder = new StringBuilder();
            while ((line = in.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    /**
     * Extracts the response headers from the connection.
     *
     * @param connection The HTTP connection.
     * @return A map containing the response headers.
     */
    private static Map<String, String> getResponseHeaders(@NonNull HttpURLConnection connection) {
        Map<String, String> headers = new HashMap<>();
        Map<String, List<String>> headerFields = connection.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
            String headerKey = entry.getKey();
            List<String> headerValues = entry.getValue();
            // Take the first value if the header has multiple values
            String headerValue = (headerValues != null && !headerValues.isEmpty()) ? headerValues.get(0) : "";
            headers.put(headerKey, headerValue);
        }
        return headers;
    }

    /**
     * Constructs a query string from the provided parameters map.
     *
     * @param parameters The parameters to include in the query string.
     * @return A query string.
     * @throws Exception If an encoding error occurs.
     */
    private static String buildQueryString(@Nullable Map<String, Object> parameters) throws Exception {
        if (parameters != null) {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().toString();
                builder.append((builder.length() == 0) ? "?" : "&").append(key).append("=").append(value);
            }
            return builder.toString();
        }
        return "";
    }

    /**
     * Gets' the URL by removing any query string.
     *
     * @param url The URL .
     * @return The URL without a query string.
     */
    private static String getUrlWithoutQueryString(@NonNull String url) {
        try {
            URL $url = new URL(url);
            String protocol = $url.getProtocol();
            String host = $url.getHost();
            String path = $url.getPath();
            return protocol + "://" + host + path;
        } catch (Throwable throwable) {
            return url.split("\\?", 2)[0];
        }
    }

    /**
     * Sets the HTTP headers for the connection from the provided map.
     *
     * @param connection The HTTP connection.
     * @param headers    The headers to set, or null if no headers are needed.
     */
    private static void setRequestHeaders(@NonNull HttpURLConnection connection, @Nullable Map<String, String> headers) {
        // Set headers from the map
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Sets the HTTP request parameters for the connection from the provided map.
     *
     * @param connection The HTTP connection.
     * @param parameters The parameters to set, or null if no parameters are needed.
     */
    private static void setRequestParams(@NonNull HttpURLConnection connection, @Nullable Map<String, Object> parameters) {
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().toString();
                connection.setRequestProperty(key, value);
            }
        }
    }
}