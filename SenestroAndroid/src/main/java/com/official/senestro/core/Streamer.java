package com.official.senestro.core;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.official.senestro.core.utils.AdvanceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Logger;

public class Streamer implements AutoCloseable{

    private static final String TAG = Streamer.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(TAG);
    private static final String INIT_ERROR_MESSAGE = "Streamer is not initialized, or an error occurred during the initialization.";

    private File file;
    private Mode mode;
    private RandomAccessFile access;
    private Error error;

    /**
     * Constructs a {@code Streamer} object with the specified file name and
     * access mode.
     *
     * @param filename The name of the file to work with. Can be {@code null}.
     * @param mode The mode of file access (READ_ONLY or READ_AND_WRITE). Must
     * not be {@code null}.
     */
    public Streamer(@Nullable String filename, @NonNull Mode mode) {
        if (filename == null || filename.trim().isEmpty()) {
            handleError("Filename must not be null or empty.");
        } else if (!createFile(filename)) {
            handleError("File must exist or unable to create a new file.");
        } else {
            try {
                String option = (mode == Mode.READ_ONLY) ? "r" : "rw";
                this.file = new File(filename);
                this.mode = mode;
                this.access = new RandomAccessFile(this.file, option);
            } catch (FileNotFoundException exception) {
                reset();
                handleError(exception);
            }
        }
    }

    /**
     * Closes the underlying file stream and releases resources.
     *
     * @throws Exception If an error occurs during closing.
     */
    @Override
    public void close() throws Exception {
        try {
            if (access != null) {
                access.close();
            }
        } catch (IOException exception) {
            handleError(exception);
        }
    }

    /**
     * Skips the specified number of bytes in the file stream.
     *
     * @param length The number of bytes to skip.
     * @return {@code true} if the operation is successful; {@code false}
     * otherwise.
     */
    public boolean skipBytes(long length) {
        boolean skipped = false;
        if (!isValid()) {
            handleError(INIT_ERROR_MESSAGE);
        } else {
            try {
                if (length > 0 && length < access.length()) {
                    access.seek(access.getFilePointer() + length);
                    skipped = true;
                } else {
                    handleError("Invalid length provided in " + currentMethod());
                }
            } catch (IOException exception) {
                handleError(exception);
            }
        }
        return skipped;
    }

    /**
     * Reads bytes from the file into the provided byte array.
     *
     * @param bytes The byte array to store read data. Can be {@code null}.
     * @return The number of bytes read, or {@code -1} if an error occurs.
     */
    public int readBytes(@Nullable byte[] bytes) {
        if (!isValid()) {
            error = new Error(INIT_ERROR_MESSAGE);
        } else if (bytes == null) {
            handleError("Cannot read because the byte array is null provided in " + currentMethod());
        } else {
            try {
                return access.read(bytes);
            } catch (IOException exception) {
                handleError(exception);
            }
        }
        return -1;
    }

    /**
     * Reads a UTF-8 encoded string from the file.
     *
     * @return The read string, or {@code null} if an error occurs.
     */
    public String readUtf8() {
        if (!isValid()) {
            error = new Error(INIT_ERROR_MESSAGE);
        } else {
            try {
                return access.readUTF();
            } catch (IOException exception) {
                handleError(exception);
            }
        }
        return null;
    }

    /**
     * Reads a line of text from the file.
     *
     * @return The read line, or {@code null} if an error occurs.
     */
    public String readLine() {
        if (!isValid()) {
            handleError(INIT_ERROR_MESSAGE);
        } else {
            try {
                return access.readLine();
            } catch (IOException exception) {
                handleError(exception);
            }
        }
        return null;
    }

    /**
     * Writes the provided byte array to the file.
     *
     * @param bytes The byte array to write. Can be {@code null}.
     * @return {@code true} if the operation is successful; {@code false}
     * otherwise.
     */
    public boolean writeBytes(@Nullable byte[] bytes) {
        if (!isValid()) {
            handleError(INIT_ERROR_MESSAGE);
        } else if (mode == Mode.READ_ONLY) {
            handleError("Cannot write in read-only mode in " + currentMethod());
        } else if (bytes == null) {
            handleError("Cannot write because the byte array is null provided in " + currentMethod());
        } else {
            try {
                access.write(bytes);
                return true;
            } catch (IOException exception) {
                handleError(exception);
            }
        }
        return false;
    }

    /**
     * Writes a portion of the byte array to the file.
     *
     * @param bytes The byte array to write. Can be {@code null}.
     * @param offset The starting position in the array.
     * @param length The number of bytes to write.
     * @return {@code true} if the operation is successful; {@code false}
     * otherwise.
     */
    public boolean writeBytes(@Nullable byte[] bytes, int offset, int length) {
        if (!isValid()) {
            handleError(INIT_ERROR_MESSAGE);
        } else if (mode == Mode.READ_ONLY) {
            handleError("Cannot write in read-only mode in " + currentMethod());
        } else if (bytes == null) {
            handleError("Cannot write because the byte array is null provided in " + currentMethod());
        } else {
            try {
                access.write(bytes, offset, length);
                return true;
            } catch (IOException exception) {
                handleError(exception);
            }
        }
        return false;
    }

    /**
     * Writes the provided string data to the file as bytes.
     *
     * @param data The string to write. Can be {@code null}.
     * @return {@code true} if the operation is successful; {@code false}
     * otherwise.
     */
    public boolean writeData(@Nullable String data) {
        if (!isValid()) {
            error = new Error(INIT_ERROR_MESSAGE);
        } else if (mode == Mode.READ_ONLY) {
            handleError("Cannot write in read-only mode in " + currentMethod());
        } else if (data == null) {
            handleError("Cannot write because the data is null provided in " + currentMethod());
        } else {
            try {
                access.writeBytes(data);
                return true;
            } catch (IOException exception) {
                handleError(exception);
            }
        }
        return false;
    }

    /**
     * Returns the last encountered error.
     *
     * @return An {@code Error} object containing the error message.
     */
    public Error error() {
        return (error == null) ? new Error(null) : error;
    }

    /**
     * Logs and sets the error using an exception.
     *
     * @param exception The exception to handle. Can be {@code null}.
     */
    private void handleError(@Nullable Exception exception) {
        String message = (exception != null && exception.getMessage() != null)
                ? exception.getMessage()
                : "Unknown error";
        LOGGER.warning(message);
        error = new Error(message);
    }

    /**
     * Logs and sets the error using a custom message.
     *
     * @param message The error message to log and set.
     */
    private void handleError(@Nullable String message) {
        if (message != null) {
            LOGGER.warning(message);
            error = new Error(message);
        }
    }

    /**
     * Checks if the {@code Streamer} is properly initialized.
     *
     * @return {@code true} if valid; {@code false} otherwise.
     */
    private boolean isValid() {
        return file != null && mode != null && access != null;
    }

    /**
     * Attempts to create a file if it does not already exist.
     *
     * @param filename The name of the file.
     * @return {@code true} if the file exists or is successfully created;
     * {@code false} otherwise.
     */
    private boolean createFile(@NonNull String filename) {
        try {
            File file = new File(filename);
            return file.isFile() || file.createNewFile();
        } catch (IOException exception) {
            handleError(exception);
        }
        return false;
    }

    /**
     * Retrieves the name of the current method (for logging/debugging
     * purposes).
     *
     * @return The current method name.
     */
    private String currentMethod() {
        return AdvanceUtils.getCurrentMethodName();
    }
    
    /**
     * Resets the state of the {@code Streamer} object, clearing all fields.
     */
    private void reset() {
        file = null;
        mode = null;
        access = null;
        error = null;
    }

    /**
     * Enum representing file access modes.
     */
    public enum Mode {
        READ_ONLY, READ_AND_WRITE
    }

    /**
     * Inner class representing errors encountered during file operations.
     */
    public static class Error {

        private final String message;

        public Error(@Nullable String message) {
            this.message = (message == null) ? "" : message;
        }

        /**
         * Returns the error message.
         *
         * @return The error message.
         */
        public String message() {
            return message;
        }
    }
}
