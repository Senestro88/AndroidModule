package com.senestro.server;

import com.senestro.Utils;
import com.senestro.annotations.NonNull;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Represents a client connected to the server, handling communication
 * through HTTP-like requests and responses. The class manages parsing of
 * HTTP headers, body, handling different HTTP methods (GET, POST, HEAD),
 * and sending responses.
 *
 * @author Senestro
 */
public class Client {

    // Instance of the server managing this client
    private final Server instance;
    // Socket representing the client connection
    private final Socket socket;
    // Buffers for reading and writing data
    private BufferedReader buffereReader;
    private BufferedWriter bufferedWriter;
    private InputStream inputStream;
    private OutputStream outputStream;
    // Maps to store headers and parameters of the request
    private final HashMap<String, String> requestHeaders = new HashMap<>();
    private final HashMap<String, String> requestParams = new HashMap<>();
    // Request body
    private String requestBody = null;
    // Indicates if the client connection is valid
    private boolean valid;
    // Client's address and hostname
    private String hostname;
    private String address;
    // Remote client's address and hostname
    private String remoteHostname;
    private String remoteAddress;

    /**
     * Constructor for initializing the client with the server instance and
     * socket. It starts the client connection setup.
     *
     * @param instance the server managing this client
     * @param socket the socket connection to the client
     */
    public Client(@NonNull Server instance, @NonNull Socket socket) {
        this.instance = instance;
        this.socket = socket;
        validate();  // Start the client connection
    }

    /**
     * Closes the client connection, flushing and closing streams and the
     * socket. This method runs asynchronously in a separate thread.
     */
    public void close() {
        Thread thread = new Thread(() -> {
            if (valid) {
                try {
                    getBufferedWriter().flush();  // Ensure data is written before closing
                    getBufferedWriter().close();
                    getBufferedReader().close();
                    socket.close();
                    valid = false;  // Mark client as invalid after closing
                } catch (IOException e) {
                    e.printStackTrace();  // Handle IOException
                }
            }
        });
        thread.start();  // Close asynchronously to avoid blocking
    }

    /**
     * Checks if the client connection is valid.
     *
     * @return true if the connection is valid, false otherwise
     */
    public boolean valid() {
        return valid;
    }

    // Getters for client information
    public String getRemoteAddress() {
        return remoteAddress;
    }

    public String getRemoteHostname() {
        return remoteHostname;
    }

    public String getAddress() {
        return address;
    }

    public String getHostname() {
        return hostname;
    }

    /**
     * Sets the date format in the format expected by HTTP headers.
     *
     * @param date the date object to format
     * @return the formatted date string
     */
    public String setDateFormat(@NonNull Object date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT+1'", Locale.UK);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+1"));
        return dateFormat.format(date);  // Format the date
    }

    // Getters for buffered readers and writers
    public BufferedReader getBufferedReader() {
        return buffereReader;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public Socket getSocket() {
        return socket;
    }

    public BufferedWriter getBufferedWriter() {
        return bufferedWriter;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Checks if a header exists for the given key.
     *
     * @param key the header key to check for
     * @return true if the header is present, false otherwise
     */
    public boolean inHeader(@NonNull String key) {
        return Utils.notNull(getHeader(key));
    }

    /**
     * Retrieves the value of a header by its key.
     *
     * @param key the header key
     * @return the value of the header or null if the header does not exist
     */
    public String getHeader(@NonNull String key) {
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

    /**
     * Retrieves the URI from the request header.
     *
     * @return the URI as a string
     */
    public String getUri() {
        return getHeader("uri");
    }

    /**
     * Retrieves the HTTP method (GET, POST, etc.) from the request header.
     *
     * @return the HTTP method
     */
    public String getMethod() {
        return getHeader("method");
    }

    /**
     * Retrieves the protocol (e.g., HTTP/1.1) from the request header.
     *
     * @return the protocol
     */
    public String getProtocol() {
        return getHeader("protocol");
    }

    /**
     * Retrieves the request body.
     *
     * @return the request body
     */
    public String requestBody() {
        return requestBody;
    }

    /**
     * Writes the HTTP response headers, including status code and other
     * headers.
     *
     * @param code the HTTP status code
     * @param headers the headers to include in the response
     */
    public void writeHeaders(int code, @NonNull HashMap<String, String> headers) {
        try {
            Status status = new Status(code);
            bufferedWriter.write("HTTP/1.1 " + status.getMessage(true));  // Write status line
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (!entry.getKey().equalsIgnoreCase("HTTP/1.1")) {
                    bufferedWriter.write(entry.getKey() + " " + entry.getValue() + "\r\n");  // Write headers
                }
            }
            bufferedWriter.write("\r\n");  // End of headers
        } catch (Throwable e) {
            e.printStackTrace();  // Handle any exceptions during writing
        }
    }

    /**
     * Writes the body content of the HTTP response.
     *
     * @param content the content to send in the response body
     */
    public void writeBody(@NonNull String content) {
        try {
            getBufferedWriter().write(content);  // Write body content
        } catch (IOException e) {
            e.printStackTrace();  // Handle IOException
        }
    }

    // Getters for query parameters and headers
    public HashMap<String, String> getQueryParams() {
        return requestParams;
    }

    public HashMap<String, String> getHeaders() {
        return requestHeaders;
    }

    /**
     * Sends a standard HTTP response with a status code.
     *
     * @param code the HTTP status code
     */
    public void sendResponse(int code) {
        if (valid()) {
            Mime mime = new Mime("text/html").tryUTF8();
            Status status = new Status(code);
            sendResponse(code, mime.getMime(), status.getMessage(true));
        }
    }

    /**
     * Sends a custom message as a response with status 200 OK.
     *
     * @param message the message content for the response
     */
    public void sendResponse(@NonNull String message) {
        if (valid()) {
            int code = 200;
            Mime mime = new Mime("text/html").tryUTF8();
            Status status = new Status(code);
            sendResponse(code, mime.getMime(), message);
        }
    }

    /**
     * Sends a custom HTTP response with a given status code, MIME type, and
     * content.
     *
     * @param code the HTTP status code
     * @param mime the MIME type for the response
     * @param content the content to send in the body
     */
    public void sendResponse(int code, @NonNull String mime, @NonNull String content) {
        if (valid()) {
            Mime serverMime = new Mime(mime).tryUTF8();
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Content-Type:", serverMime.getMime());
            headers.put("Content-Length:", String.valueOf(content.length()));
            headers.put("Date:", setDateFormat(new Date()));
            headers.put("Connection:", getHeader("Connection") != null ? (getHeader("Connection").equalsIgnoreCase("keep-alive") ? "keep-alive" : "close") : "close");
            headers.put("Server:", "HTTPForwarder");
            headers.put("Access-Control-Allow-Headers:", "*");
            headers.put("Access-Control-Allow-Methods:", "GET, POST, OPTIONS");
            writeHeaders(code, headers);
            writeBody(content);  // Write the response body
        }
    }

    // PRIVATE METHODS
    /**
     * Starts the client by initializing necessary streams and reading the
     * request.
     */
    private void validate() {
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            buffereReader = new BufferedReader(new InputStreamReader(inputStream));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            validationInitiliazer();
            this.address = ((InetSocketAddress) socket.getLocalSocketAddress()).getAddress().getHostAddress();
            this.hostname = ((InetSocketAddress) socket.getLocalSocketAddress()).getAddress().getHostName();
            this.remoteAddress = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().getHostAddress();
            this.remoteHostname = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().getHostName();
            valid = true;
        } catch (IOException e) {
            e.printStackTrace();  // Handle any input/output exceptions
        }
    }

    /**
     * Handles incoming HTTP requests by getting the HTTP methods.
     */
    private void handleRequests() {
        try {
            String requestMethod = getMethod();
            handleRequest(requestMethod);
        } catch (Throwable e) {
            e.printStackTrace();
            instance.onError(e.getMessage() == null ? "Invalid exception message" : e.getMessage());
        }
    }

    /**
     * Handles incoming HTTP requests from HTTP methods.
     */
    private void handleRequest(@NonNull String requestMethod) {
        // Identify the HTTP method (GET, POST, HEAD)
        String method = getMethod();
        switch (method) {
            case "GET":
                handleGet();
                break;
            case "POST":
                handlePost();
                break;
            case "HEAD":
                handleHead();
                break;
            default:
                sendResponse(405);  // Method Not Allowed
                break;
        }
    }

    /**
     * Handles GET requests.
     */
    private void handleGet() {
        // Implement GET request handling here
        sendResponse(200, "text/html", "<h1>GET Request Handled</h1>");
    }

    /**
     * Handles POST requests.
     */
    private void handlePost() {
        // Implement POST request handling here
        sendResponse(200, "text/html", "<h1>POST Request Handled</h1>");
    }

    /**
     * Handles HEAD requests.
     */
    private void handleHead() {
        // Implement HEAD request handling here
        sendResponse(200, "text/html", "");
    }

    /**
     * Initializes the client by reading and parsing the incoming HTTP request.
     * This includes reading the request line (method, URI, protocol), headers,
     * and any query parameters or request body (for POST requests).
     *
     * @throws IOException if an I/O error occurs while reading the request
     */
    private void validationInitiliazer() throws IOException {
        if (valid()) {  // Check if the client connection is valid
            // Clear any previous headers and body content
            requestHeaders.clear();
            requestBody = "";
            String method = null;  // HTTP method (GET, POST, etc.)
            String uri = null;  // URI from the request line
            String protocol;  // HTTP version
            // Build the raw header by reading lines from the reader
            StringBuilder headerBuilder = new StringBuilder();
            String headerLine;
            while ((headerLine = buffereReader.readLine()) != null && !headerLine.isEmpty()) {
                headerBuilder.append(headerLine).append("\r\n");  // Append header lines
            }
            // Convert the header into a string
            String rawHeader = headerBuilder.toString();
            // Split the raw header into individual lines
            String[] requestLines = rawHeader.trim().split("\r\n");
            // Split the request line into parts (method, URI, protocol)
            String[] requestParts = requestLines[0].trim().split("\\s+", 3);
            if (requestParts.length == 3) {
                method = requestParts[0];  // HTTP method (GET, POST, etc.)
                uri = requestParts[1];  // Requested URI
                protocol = requestParts[2];  // HTTP protocol version
                // Store method, URI, and protocol in the headers map
                requestHeaders.put("method", method.toUpperCase());
                requestHeaders.put("uri", uri);
                requestHeaders.put("protocol", protocol.toUpperCase());
            }
            // Process the remaining lines as headers (key-value pairs)
            for (int i = 1; i < requestLines.length; i++) {
                String[] headerParts = requestLines[i].split(": ", 2);
                if (headerParts.length == 2) {
                    // Store header key-value pairs in the map
                    requestHeaders.put(headerParts[0].toUpperCase(), headerParts[1]);
                }
            }
            // Extract and process query parameters from the URI (if any)
            String query = Utils.notNull(uri) && uri.contains("?") ? uri.substring(uri.indexOf('?') + 1) : "";
            if (!query.isEmpty()) {
                String[] params = query.split("&");  // Split query string into key-value pairs
                for (String param : params) {
                    String[] paramParts = param.split("=", 2);
                    if (paramParts.length == 2) {
                        // Store query parameters in the map
                        requestParams.put(paramParts[0].trim(), paramParts[1].trim());
                    }
                }
            }
            // If the method is POST, read the body content
            if (Utils.notNull(method) && method.equalsIgnoreCase("POST")) {
                // Get the content length from the headers
                int contentLength = Integer.parseInt(requestHeaders.containsKey("CONTENT-LENGTH")
                        ? Objects.requireNonNull(requestHeaders.get("CONTENT-LENGTH")) : "0");

                // If the content length is greater than 0, read the body
                if (contentLength > 0) {
                    StringBuilder bodyBuilder = new StringBuilder();
                    char[] buffer = new char[1024];
                    int bytesRead;
                    int totalBytesRead = 0;

                    // Read the body content until the entire content length is read
                    while (totalBytesRead < contentLength && (bytesRead = buffereReader.read(buffer)) != -1) {
                        bodyBuilder.append(buffer, 0, bytesRead);  // Append the body content
                        totalBytesRead += bytesRead;
                    }

                    // Store the body content in requestBody
                    requestBody = bodyBuilder.toString().trim();
                }
            }
        }
    }

}
