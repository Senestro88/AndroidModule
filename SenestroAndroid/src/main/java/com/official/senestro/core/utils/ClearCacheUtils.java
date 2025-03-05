package com.official.senestro.core.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.Objects;

public class ClearCacheUtils {
    private final String tag = ClearCacheUtils.class.getName();

    private final Context context;

    public ClearCacheUtils(@NonNull Context context) {
        this.context = context;
    }

    public void clearInternalCache() {
        try {
            // Get the cache directory for your application
            File cacheDirFile = this.context.getCacheDir();
            // Call the method to delete all cache files
            deleteFiles(cacheDirFile);
        } catch (Throwable e) {
            Log.e(tag, e.getMessage(), e);
        }
    }

    public void clearExternalCache() {
        try {
            // Get the cache directory for your application
            File cacheDirFile = this.context.getExternalCacheDir();
            // Call the method to delete all cache files
            assert cacheDirFile != null;
            deleteFiles(cacheDirFile);
        } catch (Throwable e) {
            Log.e(tag, e.getMessage(), e);
        }
    }

    /* ------------------------------------------- */

    private void deleteFiles(File path) {
        if (path.isDirectory()) {
            // If it's a directory, recursively delete its content
            for (File child : Objects.requireNonNull(path.listFiles())) {
                deleteFiles(child);
            }
        }
        // Delete the file or empty directory
        boolean delete = path.delete();
    }
}