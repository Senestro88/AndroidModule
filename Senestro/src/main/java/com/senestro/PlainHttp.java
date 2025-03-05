package com.senestro;

import com.senestro.annotations.NonNull;
import com.senestro.annotations.Nullable;

import javax.net.ssl.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class PlainHttp {
    // FINAL VARIABLES
    private final String TAG = PlainHttp.class.getName();
    private final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36";
    private final String host;
    private final String url;
    private final String boundary;

    private final String line = "\r\n";
    private final String space = " ";
    private final Charset charset = StandardCharsets.UTF_8;

    // NORMAL VARIABLES
    private int port = 80;
    private HashMap<String, String> headers = new HashMap<>();
    private HashMap<String, Object> parameters = new HashMap<>();
    private Object socket;
    private String plainBody;

    public PlainHttp(@NonNull String url) {
        this.url = getUrlWithoutQueryString(url);
        this.host = getHostFromUrl(url);
        this.boundary = "----WebKitFormBoundary" + System.currentTimeMillis() + UUID.randomUUID() + "----";
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHeaders(@NonNull HashMap<String, String> headers) {
        this.headers = headers;
    }

    public void setHeader(@NonNull String key, @NonNull String value) {
        this.headers.put(key, value);
    }

    public void setParameters(@NonNull HashMap<String, Object> parameters) {
        this.parameters = parameters;
    }

    public void setParameter(@NonNull String key, @NonNull Object value) {
        this.parameters.put(key, value);
    }

    public void execute(@NonNull RequestMethod method, boolean sslIgnore) {
        try {
            if (url.startsWith("https")) {
                executeFinal(method, sslIgnore);
            } else if (url.startsWith("https")) {
                executeFinal(method);
            } else {
                String message = "Unsupported: URL must start with \"http://\" or \"https://\" and the port must match a valid protocol.";
                throw new IllegalArgumentException(message);
            }
        } catch (Throwable throwable) {
            handleExecutorError(message(throwable));
        }
    }

    public void shutdown() {
        try {
            if (socket != null) {
                if (socket instanceof Socket $socket) {
                    // Close the socket
                    if (!$socket.isClosed()) {
                        $socket.close();
                    }
                } else if (socket instanceof SSLSocket $socket) {
                    // Signal that you are done receiving data
                    if (!$socket.isInputShutdown()) {
                        $socket.shutdownInput();
                    }
                    // Signal that you are done sending data
                    if (!$socket.isOutputShutdown()) {
                        $socket.shutdownOutput();
                    }
                    // Close the socket
                    if (!$socket.isClosed()) {
                        $socket.close();
                    }
                }
            }
        } catch (Throwable throwable) {
            logger().warning(message(throwable));
        } finally {
            socket = null;
        }
    }

    // PRIVATE METHODS
    private String getUrlWithoutQueryString(@NonNull String url) {
        try {
            URL $url = new URL(url);
            String protocol = $url.getProtocol();
            String host = $url.getHost();
            String path = $url.getPath();
            return protocol + "://" + host + path;
        } catch (Throwable throwable) {
            logger().warning(message(throwable));
            return url.split("\\?", 2)[0];
        }
    }

    private String buildQueryString(@Nullable HashMap<String, Object> parameters) throws Exception {
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

    private String getRequestPathFromUrl() {
        try {
            return new URL(url).getPath();
        } catch (Throwable throwable) {
            logger().warning(message(throwable));
        }
        return url;
    }

    private String getHostFromUrl(@NonNull String url) {
        try {
            return new URL(url).getHost();
        } catch (MalformedURLException throwable) {
            logger().warning(message(throwable));
        }
        return url;
    }

    private Logger logger() {
        return Logger.getLogger(TAG);
    }

    private String message(@NonNull Throwable throwable) {
        return throwable != null ? String.valueOf(throwable.getMessage()) : "";
    }

    private TrustManagerFactory trustManagerFactory() throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        // Load the default TrustStore
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        // Use default keystore (system CA certs)
        ks.load(null, null);
        factory.init(ks);
        return factory;
    }

    public X509TrustManager trustManager() throws GeneralSecurityException, IOException {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                // Do nothing - trust all clients
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                // Trust server certificate with custom certitificate if specified
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    private Socket createSocket() throws IOException {
        return new Socket(url, port);
    }

    private SSLSocketFactory createSslSocketFactory(boolean sslIgnore) throws GeneralSecurityException, IOException {
        // Create SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        SecureRandom secureRandom = new SecureRandom();
        if (sslIgnore) {
            X509TrustManager manager = trustManager();
            sslContext.init(null, new TrustManager[]{manager}, secureRandom);
        } else {
            TrustManagerFactory factory = trustManagerFactory();
            sslContext.init(null, factory.getTrustManagers(), secureRandom);
        }
        // Create SSLSocket
        return sslContext.getSocketFactory();
    }

    private void handleExecutorError(@Nullable String message) {
        message = message == null ? "An error has occurred" : message;
        logger().warning(message);
    }

    private void executeFinal(@NonNull RequestMethod method) {
        try {
            Socket socket = new Socket(host, port);
            // Get the streams
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();
            // Use ByteArrayOutputStream to collect the HTTP request
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Use PrintWriter to format and auto-flush data to ByteArrayOutputStream
            PrintWriter writer = new PrintWriter(baos, true, charset);
            // Save the socket
            this.socket = socket;
            executeFinal(in, out, writer, baos, method);
        } catch (Throwable throwable) {
            handleExecutorError(message(throwable));
        }
    }

    private void executeFinal(@NonNull RequestMethod method, boolean sslIgnore) {
        try {
            SSLSocketFactory factory = createSslSocketFactory(sslIgnore);
            SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
            socket.startHandshake();
            // If handshake is successful, the certificate is valid
            // Get the streams
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();
            // Use ByteArrayOutputStream to collect the HTTP request
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Use PrintWriter to format and auto-flush data to ByteArrayOutputStream
            PrintWriter writer = new PrintWriter(baos, true, charset);
            // Save the socket
            this.socket = socket;
            executeFinal(in, out, writer, baos, method);
        } catch (Throwable throwable) {
            handleExecutorError(message(throwable));
        }
    }

    private void executeFinal(@NonNull InputStream in, @NonNull OutputStream out, @NonNull PrintWriter writer, @NonNull ByteArrayOutputStream baos, @NonNull RequestMethod method) throws Exception {
        if (socket == null) {
            handleExecutorError("Invalid socket detected as null is received");
        } else {
            String requestPath = getRequestPathFromUrl();
            String rurlPath = method == RequestMethod.GET ? requestPath + buildQueryString(parameters) : requestPath;
            writer.append(method.name()).append(space).append(rurlPath).append(space).append("HTTP/1.1").append(line);
            writer.append("Host:").append(space).append(host).append(line);
            writer.append("User-Agent:").append(space).append(userAgent).append(line);
            writer.append("Connection:").append(space).append("close").append(line);
            setWriterHeaders(writer);
            writer.flush();
            if (method == RequestMethod.POST && !parameters.isEmpty()) {
                writer.append("Content-Type:").append(space).append("multipart/form-data; boundary=" + boundary).append(line);
                writer.append(line);
                writer.flush();
                setWriterBody(writer, baos);
            }
            baos.flush();
            byte[] requestByte = baos.toByteArray();
            String requestBody = new String(requestByte, charset);
            // Send the complete request (including headers and body) to the socket
            out.write(requestByte);
            // Ensure data is actually sent over the socket
            out.flush();
            // Read response from server
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String $line;
            StringBuilder builder = new StringBuilder();
            while (($line = reader.readLine()) != null) {
                builder.append($line).append(line);
            }
            String response = builder.toString();

            System.out.println("RESPONSE RECEIVED");
            System.out.println(response);

            // Close the socket and resources
            reader.close();
            writer.close();
            shutdown();
        }
    }

    private void setWriterHeaders(@NonNull PrintWriter writer) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            writer.append(key).append(":").append(space).append(value).append(line);
        }
    }

    private void setWriterBody(@NonNull PrintWriter writer, @NonNull ByteArrayOutputStream baos) {
        if (!parameters.isEmpty()) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                String field = entry.getKey();
                String value = entry.getValue().toString();
                if (!Utils.isFile(value)) {
                    // Add text form field
                    writer.append("--").append(boundary).append(line);
                    writer.append("Content-Disposition: form-data; name=\"").append(field).append("\"").append(line).append(line);
                    writer.append(value).append(line);
                    writer.flush();
                } else {
                    // Add file part
                    XFile file = new XFile(value);
                    String defaultMime = "application/octet-stream";
                    String mainMime = file.getMime();
                    String mimeFromExtension = file.getMimeFromFileExtension();
                    mimeFromExtension = mimeFromExtension == null ? defaultMime : mimeFromExtension;
                    String mime = mainMime == null ? mimeFromExtension : mainMime;
                    writer.append("--").append(boundary).append(line);
                    writer.append("Content-Disposition: form-data; name=\"").append(field).append("\"; filename=\"").append(file.getName()).append("\"").append(line);
                    writer.append("Content-Type:").append(space).append(mime).append(line).append(line);
                    writer.flush();
                    // Write file content
                    try (FileInputStream stream = new FileInputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = stream.read(buffer)) != -1) {
                            baos.write(buffer, 0, bytesRead);
                        }
                        baos.flush();
                    } catch (Throwable throwable) {
                        logger().warning(message(throwable));
                    }
                    writer.append(line).flush();
                }
            }
            writer.append("--").append(boundary).append("--").append(line);
            writer.flush();
        }
    }

    // PUBLIC ENUM
    public enum RequestMethod {
        GET, POST, HEAD
    }
}
