package com.senestro.server;

import java.util.HashMap;

/**
 * Represents an HTTP status code and provides methods for retrieving the corresponding
 * message for that code. It also initializes a predefined set of HTTP status codes.
 *
 * This class is useful for mapping HTTP status codes to their textual representations
 * and includes functionality for retrieving status messages with or without the status code.
 *
 * @author Senestro
 */
public class Status {

    private final int code;  // HTTP status code
    private final HashMap<Integer, String> statuses = new HashMap<>();  // Map of HTTP status codes to their messages

    /**
     * Constructor to initialize the Status object with a specific HTTP status code.
     *
     * @param code The HTTP status code to be used for this Status object.
     */
    public Status(int code) {
        this.code = code;
        initHTTPStatus();  // Initialize the map of HTTP status codes and messages
    }

    /**
     * Retrieves the HTTP status message for the given status code.
     *
     * @param include_code If true, the status code is included in the returned string.
     * @return The HTTP status message, optionally including the status code.
     */
    public String getMessage(boolean include_code) {
        if (statuses.containsKey(code)) {  // Check if the code exists in the status map
            String message = statuses.get(code);  // Get the status message for the code
            return include_code ? code + " " + message : message;  // Return message with or without code
        }
        return "";  // Return an empty string if the status code is not found
    }

    /**
     * Retrieves the HTTP status code.
     *
     * @return The HTTP status code.
     */
    public int getCode() {
        return code;  // Return the status code
    }

    // PRIVATE METHODS

    /**
     * Initializes the map of HTTP status codes and their corresponding messages.
     * This method populates the `statuses` map with standard HTTP status codes and messages.
     */
    private void initHTTPStatus() {
        statuses.clear();  // Clear any existing entries in the map
        // Informational 1xx
        statuses.put(100, "Continue");
        statuses.put(101, "Switching Protocols");
        statuses.put(102, "Processing");
        // Successful 2xx
        statuses.put(200, "OK");
        statuses.put(201, "Created");
        statuses.put(202, "Accepted");
        statuses.put(203, "Non-Authoritative Information");
        statuses.put(204, "No Content");
        statuses.put(205, "Reset Content");
        statuses.put(206, "Partial Content");
        statuses.put(207, "Multi-Status");
        // Redirection 3xx
        statuses.put(300, "Multiple Choices");
        statuses.put(301, "Moved Permanently");
        statuses.put(302, "Found");
        statuses.put(303, "See Other");
        statuses.put(304, "Not Modified");
        statuses.put(305, "Use Proxy");
        statuses.put(307, "Temporary Redirect");
        statuses.put(308, "Permanent Redirect");
        // Client Error 4xx
        statuses.put(400, "Bad Request");
        statuses.put(401, "Unauthorized");
        statuses.put(402, "Payment Required");
        statuses.put(403, "Forbidden");
        statuses.put(404, "Not Found");
        statuses.put(405, "Method Not Allowed");
        statuses.put(406, "Not Acceptable");
        statuses.put(407, "Proxy Authentication Required");
        statuses.put(408, "Request Timeout");
        statuses.put(409, "Conflict");
        statuses.put(410, "Gone");
        statuses.put(411, "Length Required");
        statuses.put(412, "Precondition Failed");
        statuses.put(413, "Payload Too Large");
        statuses.put(414, "URI Too Long");
        statuses.put(415, "Unsupported Media Type");
        statuses.put(416, "Range Not Satisfiable");
        statuses.put(417, "Expectation Failed");
        statuses.put(418, "I'm a teapot");
        statuses.put(421, "Misdirected Request");
        statuses.put(422, "Unprocessable Entity");
        statuses.put(423, "Locked");
        statuses.put(424, "Failed Dependency");
        statuses.put(425, "Too Early");
        statuses.put(426, "Upgrade Required");
        statuses.put(428, "Precondition Required");
        statuses.put(429, "Too Many Requests");
        statuses.put(431, "Request Header Fields Too Large");
        statuses.put(451, "Unavailable For Legal Reasons");
        // Server Error 5xx
        statuses.put(500, "Internal Server Error");
        statuses.put(501, "Not Implemented");
        statuses.put(502, "Bad Gateway");
        statuses.put(503, "Service Unavailable");
        statuses.put(504, "Gateway Timeout");
        statuses.put(505, "HTTP Version Not Supported");
        statuses.put(506, "Variant Also Negotiates");
        statuses.put(507, "Insufficient Storage");
        statuses.put(508, "Loop Detected");
        statuses.put(510, "Not Extended");
        statuses.put(511, "Network Authentication Required");
    }
}
