package com.official.senestro.core.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.Objects;

public class StorageUtils {

    private final Context context;

    public StorageUtils(@NonNull Context context) {
        this.context = context;
    }

    public String getVolumeLabel(@NonNull File storageVolume) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StorageVolume volume = getStorageVolume(storageVolume);
            return (volume != null) ? volume.getDescription(context) : getVolumeLabelFromPath(storageVolume.getAbsolutePath());
        }
        return getVolumeLabelFromPath(storageVolume.getAbsolutePath());
    }

    public boolean isPrimary(@NonNull File storageVolume) {
        if (Build.VERSION.SDK_INT >= 30) {
            StorageVolume primaryVolume = getPrimaryStorageVolume();
            File primaryFile = (primaryVolume != null) ? primaryVolume.getDirectory() : Environment.getExternalStorageDirectory();
            return storageVolume.equals(primaryFile);
        }
        return storageVolume.equals(Environment.getExternalStorageDirectory());
    }

    public boolean isRemovable(@NonNull File storageVolume) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StorageVolume volume = getStorageVolume(storageVolume);
            return (volume != null) ? volume.isRemovable() : isPathRemovable(storageVolume.getAbsolutePath());
        }
        return isPathRemovable(storageVolume.getAbsolutePath());
    }

    public boolean isEmulated(@NonNull File storageVolume) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StorageVolume volume = getStorageVolume(storageVolume);
            return (volume != null) ? volume.isEmulated() : isPathEmulated(storageVolume.getAbsolutePath());
        }
        return isPathEmulated(storageVolume.getAbsolutePath());
    }

    public String getVolumeId(@NonNull File storageVolume) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StorageVolume volume = getStorageVolume(storageVolume);
            return (volume != null) ? volume.getUuid() : null;
        }
        return null;
    }

    public String getInternalApplicationFilesDirectory() {
        return context.getFilesDir().getAbsolutePath();
    }

    public String getInternalApplicationCacheDirectory() {
        return context.getCacheDir().getAbsolutePath();
    }

    public String getExternalApplicationFilesDirectory() {
        return Objects.requireNonNull(context.getExternalFilesDir(null)).getAbsolutePath();
    }

    public String getExternalApplicationCacheDirectory() {
        return Objects.requireNonNull(context.getExternalCacheDir()).getAbsolutePath();
    }

    public String getExternalStorageDirectory() {
        return getExternalStorageDirectoryFile().getAbsolutePath();
    }

    public File getExternalStorageDirectoryFile() {
        return Environment.getExternalStorageDirectory();
    }

    public long getTotalInternalStorageSize() {
        return getTotalStorageSize(Environment.getDataDirectory());
    }

    public long getFreeInternalStorageSize() {
        return getFreeStorageSize(Environment.getDataDirectory());
    }

    public long getUsedInternalStorageSize() {
        return getTotalInternalStorageSize() - getFreeInternalStorageSize();
    }

    public long getUsedExternalStorageSize() {
        return isExternalStorageAvailable() ? getFreeStorageSize(getExternalStorageDirectoryFile()) : 0;
    }

    public boolean isExternalStorageAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    // PRIVATE
    private StorageVolume getStorageVolume(@NonNull File storageVolume) {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return (storageManager != null) ? storageManager.getStorageVolume(storageVolume) : null;
        }
        return null;
    }

    private StorageVolume getPrimaryStorageVolume() {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return (storageManager != null) ? storageManager.getPrimaryStorageVolume() : null;
        }
        return null;
    }

    private boolean isPathRemovable(@NonNull String path) {
        String lowerCasePath = path.toLowerCase();
        return lowerCasePath.contains("sdcard") || lowerCasePath.contains("microsd");
    }

    private boolean isPathEmulated(@NonNull String path) {
        return path.toLowerCase().contains("emulated");
    }

    private String getVolumeLabelFromPath(@NonNull String path) {
        if (path.toLowerCase().contains("emulated") || path.toLowerCase().contains("sdcard0")) {
            return "Internal Storage";
        } else if (path.toLowerCase().contains("sdcard") || path.toLowerCase().contains("microsd")) {
            return "MicroSD Card";
        } else {
            String[] segments = path.split("/");
            return segments[segments.length - 1];
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private long getTotalStorageSize(@NonNull File path) {
        StatFs stat = new StatFs(path.getPath());
        // Get the block size (in bytes)
        long blockSize = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ? stat.getBlockSizeLong() : (long) stat.getBlockSize();
        // Get the total block size (in bytes)
        long totalBlocks = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ? stat.getBlockCountLong() : (long) stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    @SuppressLint("ObsoleteSdkInt")
    private long getFreeStorageSize(@NonNull File path) {
        // Create a StatFs object to query the filesystem
        StatFs stat = new StatFs(path.getPath());
        // Get the block size (in bytes)
        long blockSize = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ? stat.getBlockSizeLong() : (long) stat.getBlockSize();
        // Get the number of available blocks
        long availableBlocks = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ? stat.getAvailableBlocksLong() : (long) stat.getAvailableBlocks();
        // Calculate the free space in bytes
        return availableBlocks * blockSize;
    }
}