package com.official.senestro.core.classes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTTPMime {
    private static final String ASCII_ENCODING = "US-ASCII";
    private static final String MULTIPART_FORM_DATA_HEADER = "multipart/form-data";
    private static final String CONTENT_REGEX = "[ |\t]*([^/^ ^;^,]+/[^ ^;^,]+)";
    private static final Pattern MIME_PATTERN = Pattern.compile("[ |\t]*([^/^ ^;^,]+/[^ ^;^,]+)", 2);
    private static final String CHARSET_REGEX = "[ |\t]*(charset)[ |\t]*=[ |\t]*['|\"]?([^\"^'^;^,]*)['|\"]?";
    private static final Pattern CHARSET_PATTERN = Pattern.compile("[ |\t]*(charset)[ |\t]*=[ |\t]*['|\"]?([^\"^'^;^,]*)['|\"]?", 2);
    private static final String BOUNDARY_REGEX = "[ |\t]*(boundary)[ |\t]*=[ |\t]*['|\"]?([^\"^'^;^,]*)['|\"]?";
    private static final Pattern BOUNDARY_PATTERN = Pattern.compile("[ |\t]*(boundary)[ |\t]*=[ |\t]*['|\"]?([^\"^'^;^,]*)['|\"]?", 2);
    private final String mime;
    private final String mimeValue;
    private final String encoding;
    private final String boundary;
    // ========================== //

    public HTTPMime(String mime) {
        this.mime = mime;
        if (mime != null) {
            this.mimeValue = this.getDetailFromMime(mime, MIME_PATTERN, "", 1);
            this.encoding = this.getDetailFromMime(mime, CHARSET_PATTERN, (String) null, 2);
        } else {
            this.mimeValue = "";
            this.encoding = "UTF-8";
        }
        if ("multipart/form-data".equalsIgnoreCase(this.mimeValue)) {
            this.boundary = this.getDetailFromMime(mime, BOUNDARY_PATTERN, (String) null, 2);
        } else {
            this.boundary = null;
        }
    }

    public String getMime() {
        return this.mimeValue;
    }

    public String getMimeEncoding() {
        return this.encoding == null ? "US-ASCII" : this.encoding;
    }

    public String getBoundary() {
        return this.boundary;
    }

    public boolean isMimeMultipart() {
        return "multipart/form-data".equalsIgnoreCase(this.mimeValue);
    }

    public HTTPMime tryUTF8() {
        return this.encoding == null ? new HTTPMime(this.mime + "; charset=UTF-8") : this;
    }
    // ========================== //

    private String getDetailFromMime(String mime, Pattern pattern, String defaultValue, int group) {
        Matcher matcher = pattern.matcher(mime);
        return matcher.find() ? matcher.group(group) : defaultValue;
    }
}