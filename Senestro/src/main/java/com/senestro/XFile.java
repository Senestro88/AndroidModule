package com.senestro;

import com.senestro.annotations.NonNull;
import com.senestro.annotations.Nullable;
import com.senestro.streams.Streamer;
import com.senestro.streams.TextStream;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Extends the functionality of the {@link File} class with additional utility
 * methods for working with files, such as retrieving MIME types, file
 * extensions, and content.
 *
 * This class includes custom constructors and a static comparator for sorting
 * {@link XFile} objects.
 *
 * @author Senestro
 */
public class XFile extends File {

    /**
     * Constructs a new {@code XFile} instance using a specified pathname.
     *
     * @param pathname the pathname of the file or directory (must not be
     * {@code null}).
     */
    public XFile(@NonNull String pathname) {
        super(pathname);
    }

    /**
     * Constructs a new {@code XFile} instance using a specified File object.
     *
     * @param file the file or directory object (must not be {@code null}).
     */
    public XFile(@NonNull File file) {
        super(getRealPath(file));
    }

    /**
     * Constructs a new {@code XFile} instance using a parent pathname and child
     * pathname string.
     *
     * @param parent the parent pathname string (nullable).
     * @param child the child pathname string (must not be {@code null}).
     */
    public XFile(@Nullable String parent, @NonNull String child) {
        super(parent, child);
    }

    /**
     * Constructs a new {@code XFile} instance using a parent {@link File}
     * object and child pathname string.
     *
     * @param parent the parent {@link File} object (nullable).
     * @param child the child pathname string (must not be {@code null}).
     */
    public XFile(@Nullable File parent, @NonNull String child) {
        super(parent, child);
    }

    /**
     * Constructs a new {@code XFile} instance using a parent {@link File}
     * object and child {@link File} object.
     *
     * @param parent the parent {@link File} object (nullable).
     * @param child the child {@link File} object (must not be {@code null}).
     */
    public XFile(@Nullable File parent, @NonNull File child) {
        super(parent, getRealPath(child));
    }

    /**
     * Constructs a new {@code XFile} instance using a URI.
     *
     * @param uri the URI representing the file (must not be {@code null}).
     */
    public XFile(@NonNull URI uri) {
        super(uri);
    }

    /**
     * Retrieves the MIME type of the file based on its name or extension.
     *
     * @return the MIME type as a string, or {@code null} if it cannot be
     * determined.
     */
    public String getMime() {
        return URLConnection.getFileNameMap().getContentTypeFor(getAbsolutePath());
    }

    /**
     * Retrieves the MIME type of the file based on its file extension using a
     * utility method.
     *
     * @return the MIME type as a string, or an empty string if the extension is
     * missing or unsupported.
     */
    public String getMimeFromFileExtension() {
        String extension = getExtension();
        return !extension.isEmpty() ? Utils.getMimeFromExtension(extension) : "";
    }

    /**
     * Extracts the file extension from the file name.
     *
     * @return the file extension as a lowercase string, or an empty string if
     * the file has no extension or is a directory.
     */
    public String getExtension() {
        try {
            return isFile() ? Utils.getExtension(getAbsolutePath()).toLowerCase() : "";
        } catch (Throwable e) {
            return "";
        }
    }

    /**
     * Retrieves the last modification date of the file in milliseconds since
     * the epoch.
     *
     * @return the last modified date, or {@code -1} if an error occurs.
     */
    public long dateModified() {
        try {
            return lastModified();
        } catch (Throwable e) {
            return -1;
        }
    }

    /**
     * Reads the file content as a byte array.
     *
     * @return a byte array containing the file content, or an empty array if
     * the file cannot be read or it's a directory.
     */
    public byte[] getBytes() {
        try {
            return isFile() && canRead() ? TextStream.readBinaryFileToBytes(getAbsolutePath()) : new byte[0];
        } catch (IOException ignored) {
            return new byte[0];
        }
    }

    /**
     * Retrieves the size of the file in bytes.
     *
     * @return the size of the file in bytes, or {@code 0} if the file does not
     * exist or is inaccessible.
     */
    public long getSize() {
        return Utils.getFileSize(getAbsolutePath());
    }

    /**
     * Retrieves a {@link Streamer} object for the current file.
     *
     * <p>
     * This method checks if the current file exists and can be read. If both
     * conditions are met, it creates and returns a {@link Streamer} instance
     * for the file, configured with read and write access. If the conditions
     * are not met, the method returns {@code null}.</p>
     *
     * @return a {@link Streamer} instance with read and write mode if the file
     * exists and is readable; otherwise, {@code null}.
     */
    public Streamer getStreamer() {
        return isFile() && canRead() ? new Streamer(getAbsolutePath(), Streamer.Mode.READ_AND_WRITE) : null;
    }

    /**
     * Lists {@link XFile} objects representing the files and directories within
     * this directory.
     *
     * <p>
     * This method checks if the current file is a directory and is readable. If
     * both conditions are met, it retrieves a list of {@link XFile} objects
     * from the directory. Optionally, the listing can be performed recursively
     * based on the input parameter. If the conditions are not met, the method
     * returns an empty list.</p>
     *
     * @param recursively a {@code boolean} indicating whether to include files
     * and directories in subdirectories recursively. {@code true} for recursive
     * listing, {@code false} for non-recursive listing.
     * @return a {@link List} of {@link XFile} objects representing the files
     * and directories within this directory if the conditions are met;
     * otherwise, an empty list.
     */
    public List<XFile> listXFiles(boolean recursively) {
        return isDirectory() && canRead() ? Utils.listDir(this, recursively) : new ArrayList<>();
    }

    /**
     * A static comparator for comparing {@code XFile} objects.
     * <p>
     * Directories are prioritized over files, and both are sorted
     * alphabetically by name (case-insensitive).
     */
    public static class XFileComparator implements Comparator<XFile> {

        /**
         * Compares two {@code XFile} objects for order.
         *
         * @param a the first {@code XFile} object.
         * @param b the second {@code XFile} object.
         * @return a negative integer, zero, or a positive integer if the first
         * argument is less than, equal to, or greater than the second.
         */
        @Override
        public int compare(XFile a, XFile b) {
            if (a.isDirectory() && !b.isDirectory()) {
                return -1; // Directory comes before file
            } else if (!a.isDirectory() && b.isDirectory()) {
                return 1; // File comes after directory
            } else {
                return a.getName().compareToIgnoreCase(b.getName());
            }
        }
    }

    // PRIVATE METHOD
    /**
     * Retrieves the the absolute/real path of the file object
     *
     * @param file The file object to get it's absolute pathname
     * @return the absolute pathname
     */
    private static String getRealPath(@NonNull File file) {
        return file.getAbsolutePath();
    }
}
