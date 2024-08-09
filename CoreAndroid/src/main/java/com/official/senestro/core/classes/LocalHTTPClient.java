package com.official.senestro.core.classes;

import androidx.annotation.NonNull;

import com.official.senestro.core.LocalHTTP;
import com.official.senestro.core.utils.AdvanceHandlerThread;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class LocalHTTPClient {

    private LocalHTTP serverInstance;
    private Socket socket;
    private int socketIndex;
    private BufferedReader reader;
    private BufferedWriter writer;
    private final HashMap<String, String> requestHeaders = new HashMap<>();
    private final HashMap<String, String> requestQueryParams = new HashMap<>();
    private String postRequestBody = null;
    private boolean isValid;
    private String hostname;
    private String ip;
    private String remoteHostname;
    private String remoteIp;

    public LocalHTTPClient(@NonNull LocalHTTP serverInstance, @NonNull Socket socket, int socketIndex) {
        try {
            this.serverInstance = serverInstance;
            this.socket = socket;
            this.socketIndex = socketIndex;
            this.isValid = true;
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            initialize();
            AdvanceHandlerThread.runInBackground(() -> {
                ip = ((InetSocketAddress) socket.getLocalSocketAddress()).getAddress().getHostAddress();
                hostname = ((InetSocketAddress) socket.getLocalSocketAddress()).getAddress().getHostName();
                remoteIp = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().getHostAddress();
                remoteHostname = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().getHostName();
            });
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void closeAll() {
        AdvanceHandlerThread.runInBackground(() -> {
            if (isValid) {
                try {
                    getWriter().flush();
                    getWriter().close();
                    getReader().close();
                    socket.close();
                    isValid = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean isValid() {
        return isValid;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public String getRemoteHostname() {
        return remoteHostname;
    }

    public String getIp() {
        return ip;
    }

    public String getHostname() {
        return hostname;
    }

    public String setDateFormat(Object date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT+1'", Locale.UK);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+1"));
        return dateFormat.format(date);
    }

    public BufferedReader getReader() {
        return reader;
    }

    public Socket getSocket() {
        return socket;
    }

    public BufferedWriter getWriter() {
        return writer;
    }

    public boolean inHeader(String key) {
        return getHeader(key) != null;
    }

    public String getHeader(String key) {
        if (!requestHeaders.isEmpty()) {
            for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                String headerKey = entry.getKey();
                if (headerKey.equalsIgnoreCase(key)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    public String getUri() {
        return getHeader("uri");
    }

    public String getMethod() {
        return getHeader("method");
    }

    public String getProtocol() {
        return getHeader("protocol");
    }

    public String getPostRequestBody() {
        return postRequestBody;
    }

    public void writeHeaders(int statusCode, HashMap<String, String> headers) {
        try {
            HTTPStatus status = new HTTPStatus(statusCode);
            writer.write("HTTP/1.1 " + status.getMessage(true));
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (!entry.getKey().equalsIgnoreCase("HTTP/1.1")) {
                    writer.write(entry.getKey() + " " + entry.getValue() + "\r\n");
                }
            }
            writer.write("\r\n");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void writeBody(String content) {
        try {
            getWriter().write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, String> getQueryParams() {
        return requestQueryParams;
    }

    public HashMap<String, String> getHeaders() {
        return requestHeaders;
    }

    public void sendStatus(int statusCode) {
        if (isValid) {
            HTTPMime mime = new HTTPMime("text/html").tryUTF8();
            HTTPStatus status = new HTTPStatus(statusCode);
            sendContent(statusCode, mime.getMime(), status.getMessage(true));
        }
    }

    public void sendContent(int statusCode, String mime, String content) {
        if (isValid) {
            HTTPMime serverMime = new HTTPMime(mime).tryUTF8();
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Content-Type:", serverMime.getMime());
            headers.put("Content-Length:", String.valueOf(content.length()));
            headers.put("Date:", setDateFormat(new Date()));
            headers.put("Connection:", getHeader("Connection") != null ? (getHeader("Connection").equalsIgnoreCase("keep-alive") ? "keep-alive" : "close") : "close");
            headers.put("Server:", "WebServer");
            headers.put("Access-Control-Allow-Headers:", "*");
            headers.put("Access-Control-Allow-Methods:", "GET, POST, HEAD");
            headers.put("Access-Control-Allow-Origin:", "*");
            writeHeaders(statusCode, headers);
            try {
                writeBody(content);
                closeAll();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    // PRIVATE
    private void initialize() {
        requestHeaders.clear();
        postRequestBody = "";
        String method = null;
        String uri = null;
        String protocolVersion;
        try {
            StringBuilder headerBuilder = new StringBuilder();
            String headerLine;
            while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
                headerBuilder.append(headerLine).append("\r\n");
            }
            String rawHeader = headerBuilder.toString();
            String[] requestLines = rawHeader.trim().split("\r\n");
            String[] requestParts = requestLines[0].trim().split("\\s+", 3);
            if (requestParts.length == 3) {
                method = requestParts[0];
                uri = requestParts[1];
                protocolVersion = requestParts[2];
                requestHeaders.put("method", method.toUpperCase());
                requestHeaders.put("uri", uri);
                requestHeaders.put("protocol", protocolVersion.toUpperCase());
            }
            for (int i = 1; i < requestLines.length; i++) {
                String[] headerLines = requestLines[i].split(": ", 2);
                if (headerLines.length == 2) {
                    requestHeaders.put(headerLines[0].toUpperCase(), headerLines[1]);
                }
            }
            String query = uri != null && uri.contains("?") ? uri.substring(uri.indexOf('?') + 1) : "";
            if (!query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] paramsParts = param.split("=", 2);
                    if (paramsParts.length == 2) {
                        requestQueryParams.put(paramsParts[0].trim(), paramsParts[1].trim());
                    }
                }
            }
            if (method != null && method.equalsIgnoreCase("POST")) {
                int contentLength = Integer.parseInt(requestHeaders.containsKey("CONTENT-LENGTH") ? Objects.requireNonNull(requestHeaders.get("CONTENT-LENGTH")) : String.valueOf(0));
                if (contentLength > 0) {
                    try {
                        StringBuilder bodyBuilder = new StringBuilder();
                        char[] buffer = new char[1024];
                        int bytesRead;
                        int totalBytesRead = 0;
                        while (totalBytesRead < contentLength && (bytesRead = reader.read(buffer)) != -1) {
                            bodyBuilder.append(buffer, 0, bytesRead);
                            totalBytesRead += bytesRead;
                        }
                        postRequestBody = bodyBuilder.toString().trim();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}