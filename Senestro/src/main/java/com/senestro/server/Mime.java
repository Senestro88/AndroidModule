package com.senestro.server;

import com.senestro.Utils;
import com.senestro.annotations.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents and parses MIME (Multipurpose Internet Mail Extensions) type information.
 * Provides methods to extract MIME type, encoding, boundary, and check for multipart forms.
 *
 * @author Senestro
 */
public class Mime {

    // Constants for MIME-related patterns and default values
    private static final String ASCII_ENCODING = "US-ASCII";
    private static final String MULTIPART_FORM_DATA_HEADER = "multipart/form-data";
    private static final String CONTENT_REGEX = "[ |\t]*([^/^ ^;^,]+/[^ ^;^,]+)";
    private static final Pattern MIME_PATTERN = Pattern.compile(CONTENT_REGEX, Pattern.CASE_INSENSITIVE);
    private static final String CHARSET_REGEX = "[ |\t]*(charset)[ |\t]*=[ |\t]*['|\"]?([^\"^'^;^,]*)['|\"]?";
    private static final Pattern CHARSET_PATTERN = Pattern.compile(CHARSET_REGEX, Pattern.CASE_INSENSITIVE);
    private static final String BOUNDARY_REGEX = "[ |\t]*(boundary)[ |\t]*=[ |\t]*['|\"]?([^\"^'^;^,]*)['|\"]?";
    private static final Pattern BOUNDARY_PATTERN = Pattern.compile(BOUNDARY_REGEX, Pattern.CASE_INSENSITIVE);
    private static final String DEFUALT_ENCODING = "UTF-8";

    // Fields to store MIME details
    private final String mime;        // The full MIME string
    private final String value;       // The MIME type (e.g., "text/html")
    private final String encoding;    // The character encoding (e.g., "UTF-8")
    private final String boundary;    // The boundary string for multipart data (if applicable)

    /**
     * Constructs a {@link Mime} object and parses its details from the provided MIME string.
     *
     * @param mime The MIME string to parse. This should not be null.
     */
    public Mime(@NonNull String mime) {
        this.mime = mime;
        // Extract MIME type and encoding if MIME is not null; otherwise, set defaults.
        if (Utils.notNull(mime)) {
            value = getDetailFromMime(mime, MIME_PATTERN, DEFUALT_ENCODING, 1);
            encoding = getDetailFromMime(mime, CHARSET_PATTERN, DEFUALT_ENCODING, 2);
        } else {
            this.value = "";
            this.encoding = DEFUALT_ENCODING;
        }
        // Extract boundary if MIME type is "multipart/form-data"
        if (value.equalsIgnoreCase(MULTIPART_FORM_DATA_HEADER)) {
            boundary = this.getDetailFromMime(mime, BOUNDARY_PATTERN, DEFUALT_ENCODING, 2);
        } else {
            boundary = null;
        }
    }

    /**
     * Gets the parsed MIME type (e.g., "text/html").
     *
     * @return The MIME type as a string.
     */
    public String getMime() {
        return this.value;
    }

    /**
     * Gets the character encoding for the MIME.
     * Defaults to US-ASCII if no encoding is specified.
     *
     * @return The character encoding as a string.
     */
    public String getMimeEncoding() {
        return Utils.isNull(this.encoding) ? ASCII_ENCODING : this.encoding;
    }

    /**
     * Gets the boundary string for multipart MIME data.
     * This value is only relevant for "multipart/form-data" MIME types.
     *
     * @return The boundary string, or null if not applicable.
     */
    public String getBoundary() {
        return this.boundary;
    }

    /**
     * Checks if the MIME type is "multipart/form-data".
     *
     * @return True if the MIME type is "multipart/form-data"; otherwise, false.
     */
    public boolean isMimeMultipart() {
        return MULTIPART_FORM_DATA_HEADER.equalsIgnoreCase(this.value);
    }

    /**
     * Ensures the MIME has a UTF-8 charset.
     * If no charset is defined, appends "; charset=UTF-8" to the MIME string.
     *
     * @return A new {@link Mime} object with UTF-8 charset if it was missing.
     */
    public Mime tryUTF8() {
        return Utils.isNull(this.encoding) ? new Mime(this.mime + "; charset=UTF-8") : this;
    }

    /**
     * Extracts a specific detail from the MIME string using a regex pattern.
     *
     * @param mime         The full MIME string to parse.
     * @param pattern      The regex pattern to match against the MIME string.
     * @param defaultValue The default value to return if no match is found.
     * @param group        The regex group index to extract.
     * @return The extracted value if a match is found; otherwise, the default value.
     */
    private String getDetailFromMime(@NonNull String mime, @NonNull Pattern pattern, @NonNull String defaultValue, int group) {
        Matcher matcher = pattern.matcher(mime);
        return matcher.find() ? matcher.group(group) : defaultValue;
    }
}
