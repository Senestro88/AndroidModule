package com.official.senestro.core.classes;

import java.util.HashMap;

public class HTTPStatus {
    private final int code;
    private final HashMap<Integer, String> statuses = new HashMap<>();

    // ========================== //

    // ========================== //
    public HTTPStatus(int statusCode) {
        this.code = statusCode;
        initHTTPStatus();
    }

    public String getMessage(boolean withStatusCode) {
        if (statuses.containsKey(code)) {
            String message = statuses.get(code);
            return withStatusCode ? code + " " + message : message;
        }
        return "";
    }

    public int getCode() {
        return code;
    }

    // ========================== //
    private void initHTTPStatus() {
        statuses.clear();
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
