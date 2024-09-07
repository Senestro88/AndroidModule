package com.official.senestro.core.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.net.URLConnection;
import java.util.Comparator;

public class FileUtils extends File {
    private final String tag = FileUtils.class.getName();

    public FileUtils(@NonNull String absolutePath) {
        super(absolutePath);
    }

    public String getMime() {
        return URLConnection.getFileNameMap().getContentTypeFor(super.getAbsolutePath());
    }

    public String getMimeFromFileExtension() {
        String extension = getExtension();
        return !extension.isEmpty() ? AdvanceUtils.getMimeFromExtension(extension) : "";
    }

    public long dateModified() {
        try {
            return super.lastModified();
        } catch (Throwable e) {
            Log.e(tag, e.getMessage(), e);
            return -1;
        }
    }

    public String getContent() {
        try {
            return super.isFile() ? AdvanceUtils.getFileContent(getPath()) : "";
        } catch (Throwable e) {
            Log.e(tag, e.getMessage(), e);
            return "";
        }
    }

    public String getExtension() {
        try {
            return super.isFile() ? AdvanceUtils.getExtension(super.getAbsolutePath()).toLowerCase() : "";
        } catch (Throwable e) {
            Log.e(tag, e.getMessage(), e);
            return "";
        }
    }

    // PRIVATE INNER CLASSES
    /* ----------------------------------------------------------------- */
    public static class FileUtilsComparator implements Comparator<FileUtils> {
        @Override
        public int compare(FileUtils a, FileUtils b) {
            // Directories come first, followed by files, both sorted by name
            if (a.isDirectory() && !b.isDirectory()) {
                return -1; // Directory comes before file
            } else if (!a.isDirectory() && b.isDirectory()) {
                return 1; // File comes after directory
            } else {
                return a.getName().compareToIgnoreCase(b.getName());
            }
        }
    }
}
