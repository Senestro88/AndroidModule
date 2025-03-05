package com.senestro.streams;

import com.senestro.Utils;
import com.senestro.annotations.NonNull;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for handling text and binary streams. Provides methods to
 * convert between strings and streams, and read/write binary data.
 * <p>
 * This class includes various utility methods for handling both text (String) and binary (byte array)
 * data in input and output streams. It supports common operations like reading from and writing to byte arrays
 * and streams, converting between byte streams and strings, and reading binary files into streams.
 * </p>
 *
 * @author Senestro
 */
public class TextStream {

    /**
     * Charset to use for encoding strings when converting to byte arrays.
     */
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    /**
     * Creates a {@link ByteArrayOutputStream} from the given string.
     * The string is encoded using UTF-8 charset.
     *
     * @param string The input string to write to the output stream. Must not be null.
     * @return A {@link ByteArrayOutputStream} containing the string's bytes.
     * @throws IllegalArgumentException if the input string is null.
     */
    public static ByteArrayOutputStream createByteArrayOutputStream(@NonNull String string) {
        if (string == null) {
            throw new IllegalArgumentException("String must not be null.");
        }
        return createByteArrayOutputStream(string.getBytes(CHARSET));
    }

    /**
     * Creates a {@link ByteArrayOutputStream} from the given byte array.
     *
     * @param bytes The byte array to write to the output stream. Must not be null.
     * @return A {@link ByteArrayOutputStream} containing the byte array.
     * @throws IllegalArgumentException if the input byte array is null.
     */
    public static ByteArrayOutputStream createByteArrayOutputStream(@NonNull byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("Byte array must not be null.");
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            os.write(bytes);
        } catch (IOException e) {
            e.printStackTrace(); // Log error, though typically rethrow or handle gracefully
        }
        return os;
    }

    /**
     * Creates a {@link ByteArrayInputStream} from the given string.
     *
     * @param string The input string to convert to an input stream. Must not be null.
     * @return A {@link ByteArrayInputStream} containing the string's bytes.
     * @throws IllegalArgumentException if the input string is null.
     */
    public static ByteArrayInputStream createByteArrayInputStream(@NonNull String string) {
        if (string == null) {
            throw new IllegalArgumentException("String must not be null.");
        }
        return createByteArrayInputStream(string.getBytes(CHARSET));
    }

    /**
     * Creates a {@link ByteArrayInputStream} from the given byte array.
     *
     * @param bytes The byte array to convert to an input stream. Must not be null.
     * @return A {@link ByteArrayInputStream} containing the byte array.
     * @throws IllegalArgumentException if the input byte array is null.
     */
    public static ByteArrayInputStream createByteArrayInputStream(@NonNull byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("Byte array must not be null.");
        }
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Reads the contents of a {@link ByteArrayInputStream} and converts it to a string.
     *
     * @param bais The {@link ByteArrayInputStream} to read from. Must not be null.
     * @return The string representation of the input stream's contents.
     * @throws IOException if an I/O error occurs during reading.
     * @throws IllegalArgumentException if the input stream is null.
     */
    public static String readFromByteArrayInputStream(@NonNull ByteArrayInputStream bais) throws IOException {
        if (bais == null) {
            throw new IllegalArgumentException("ByteArrayInputStream must not be null.");
        }

        int data;
        StringBuilder result = new StringBuilder();
        try {
            while ((data = bais.read()) != -1) {
                result.append((char) data);
            }
        } finally {
            bais.close(); // Ensure stream is closed after reading
        }
        return result.toString();
    }

    /**
     * Converts a {@link ByteArrayOutputStream} to a string using the default charset (UTF-8).
     *
     * @param baos The {@link ByteArrayOutputStream} to convert. Must not be null.
     * @return The string representation of the output stream's contents.
     * @throws IOException if an error occurs during conversion.
     * @throws IllegalArgumentException if the output stream is null.
     */
    public static String convertByteArrayOutputStream(@NonNull ByteArrayOutputStream baos) throws IOException {
        if (baos == null) {
            throw new IllegalArgumentException("ByteArrayOutputStream must not be null.");
        }

        return new String(baos.toByteArray(), CHARSET);
    }

    /**
     * Reads the contents of a binary file into a {@link ByteArrayOutputStream}.
     *
     * @param filename The path to the binary file. Must not be null or empty.
     * @return A {@link ByteArrayOutputStream} containing the file's binary data.
     * @throws IOException if an error occurs during file reading.
     * @throws IllegalArgumentException if the filename is null or empty.
     */
    public static ByteArrayOutputStream readBinaryFromFileToBytesArrayOutputStream(@NonNull String filename) throws IOException {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename must not be null or empty.");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (Utils.isFile(filename)) {
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename))) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
            }
        }
        return baos;
    }

    /**
     * Reads the contents of a binary file into a byte array.
     *
     * @param filename The path to the binary file. Must not be null or empty.
     * @return A byte array containing the file's binary data.
     * @throws IOException if an error occurs during file reading.
     * @throws IllegalArgumentException if the filename is null or empty.
     */
    public static byte[] readBinaryFileToBytes(@NonNull String filename) throws IOException {
        ByteArrayOutputStream baos = readBinaryFromFileToBytesArrayOutputStream(filename);
        return baos.toByteArray();
    }

    /**
     * Copies the contents of an {@link InputStream} to a {@link ByteArrayOutputStream}.
     *
     * @param is The {@link InputStream} to read from. Must not be null.
     * @param baos The {@link ByteArrayOutputStream} to write to. Must not be null.
     * @throws IOException If an I/O error occurs during the operation.
     * @throws IllegalArgumentException if the input stream or output stream is null.
     */
    public static void copyInputStreamToByteArrayOutputStream(@NonNull InputStream is, @NonNull ByteArrayOutputStream baos) throws IOException {
        if (is == null || baos == null) {
            throw new IllegalArgumentException("InputStream and ByteArrayOutputStream must not be null.");
        }

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
    }
}
