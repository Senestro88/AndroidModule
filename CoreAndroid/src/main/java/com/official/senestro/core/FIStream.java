package com.official.senestro.core;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FIStream implements AutoCloseable {
    private static final String tag = FIStream.class.getName();

    private final String absolutePath;
    private long startPos;
    private long endPos;
    private FileInputStream stream;

    public FIStream(@NonNull String path) {
        this.absolutePath = path;
        this.startPos = 0;
        this.setEndPos();
        this.setStream();
    }

    public void skipBytes(long bytes) {
        if (this.stream != null) {
            try {
                long skipped = this.stream.skip(bytes);
                this.startPos += skipped;
            } catch (IOException e) {
                Log.e(tag, e.getMessage(), e);
            }
        }
    }

    public long getStartPos() {
        return this.startPos;
    }

    public long getEndPos() {
        return this.endPos;
    }

    public long readBytes(@Nullable byte[] bytes) {
        if (bytes != null && this.stream != null) {
            try {
                int bytesRead = this.stream.read(bytes);
                if (bytesRead != -1) {
                    this.startPos += bytesRead;
                    return bytesRead;
                }
            } catch (IOException e) {
                Log.e(tag, e.getMessage(), e);
            }
        }
        return -1;
    }

    public long readByte() {
        if (this.stream != null) {
            try {
                int byteRead = this.stream.read();
                if (byteRead != -1) {
                    this.startPos++;
                    return byteRead;
                }
            } catch (IOException e) {
                Log.e(tag, e.getMessage(), e);
            }
        }
        return -1;
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
                this.startPos = 0;
            }
        }
    }

    // PRIVATE
    private void setStream() {
        try {
            this.stream = new FileInputStream(this.absolutePath);
        } catch (IOException e) {
            this.stream = null;
            Log.e(tag, e.getMessage(), e);
        }
    }

    private void setEndPos() {
        this.endPos = new File(this.absolutePath).length();
    }
}