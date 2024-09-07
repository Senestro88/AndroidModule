package com.official.senestro.core.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.Objects;

public class ClearDataUtils {
    private final String tag = ClearDataUtils.class.getName();

    private final Context context;

    public ClearDataUtils(@NonNull Context context) {
        this.context = context;
    }

    public void clearInternalData(boolean rootCleaning) {
        try {
            // Get the data directory for your application
            File dataDirFile = rootCleaning ? new File(this.context.getApplicationInfo().dataDir) : this.context.getFilesDir();
            // Call the method to delete all data files
            deleteFiles(dataDirFile);
        } catch (Exception e) {
            Log.e(tag, e.getMessage(), e);
        }
    }

    public void clearExternalData() {
        try {
            // Get the cache directory for your application
            File dataDirFile = this.context.getExternalFilesDir(null);
            // Call the method to delete all cache files
            assert dataDirFile != null;
            deleteFiles(dataDirFile);
        } catch (Exception e) {
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