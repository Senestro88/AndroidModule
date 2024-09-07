package com.official.senestro.core;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.FileOutputStream;
import java.io.IOException;

public class FOStream implements AutoCloseable {
    private static final String tag = FOStream.class.getName();

    private final String absolutePath;
    private FileOutputStream stream;

    public FOStream(@NonNull String path, boolean append) {
        this.absolutePath = path;
        this.setStream(append);
    }

    public void writeInt(int integer) {
        if (this.stream != null) {
            try {
                this.stream.write(integer);
            } catch (IOException e) {
                Log.e(tag, e.getMessage(), e);
            }
        }
    }

    public void writeBytes(@NonNull byte[] bytes) {
        if (this.stream != null) {
            try {
                this.stream.write(bytes);
            } catch (IOException e) {
                Log.e(tag, e.getMessage(), e);
            }
        }
    }

    public void writeBytes(@NonNull byte[] bytes, int offset, int length) {
        if (this.stream != null) {
            try {
                this.stream.write(bytes, offset, length);
            } catch (IOException e) {
                Log.e(tag, e.getMessage(), e);
            }
        }
    }

    public void writeString(@NonNull String string, int offset, int length) {
        writeBytes(string.getBytes(), offset, length);
    }

    public void writeString(@NonNull String string) {
        writeBytes(string.getBytes());
    }

    @Override
    public void close() {
        if (this.stream != null) {
            try {
                this.stream.close();
            } catch (IOException e) {
                Log.e(tag, e.getMessage(), e);
            } finally {
                this.stream = null;
            }
        }
    }

    // PRIVATE
    private void setStream(boolean append) {
        try {
            this.stream = new FileOutputStream(this.absolutePath, append);
        } catch (IOException e) {
            this.stream = null;
            Log.e(tag, e.getMessage(), e);
        }
    }
}