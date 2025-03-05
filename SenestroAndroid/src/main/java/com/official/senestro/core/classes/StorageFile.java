package com.official.senestro.core.classes;

import static com.official.senestro.core.classes.EnvironmentSDCard.MEDIA_UNKNOWN;
import static com.official.senestro.core.classes.EnvironmentSDCard.TYPE_INTERNAL;
import static com.official.senestro.core.classes.EnvironmentSDCard.TYPE_PRIMARY;
import static com.official.senestro.core.classes.EnvironmentSDCard.TYPE_SD;
import static com.official.senestro.core.classes.EnvironmentSDCard.TYPE_UNKNOWN;
import static com.official.senestro.core.classes.EnvironmentSDCard.TYPE_USB;
import static com.official.senestro.core.classes.EnvironmentSDCard.WRITE_APPONLY;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.Objects;

public class StorageFile extends File {
    private final String tag = StorageFile.class.getName();

    public String userLabel, uuid, state, writeState, type;
    public boolean isPrimary, isRemovable, isEmulated;

    public StorageFile(@NonNull Context context) {
        super(Environment.getDataDirectory().getAbsolutePath());
        state = Environment.MEDIA_MOUNTED;
        type = TYPE_INTERNAL;
        writeState = WRITE_APPONLY;
    }

    public StorageFile(@NonNull String absolutePath, @NonNull Object storage) {
        super(absolutePath);
        try {
            userLabel = (String) storage.getClass().getMethod("getUserLabel").invoke(storage);
            uuid = (String) storage.getClass().getMethod("getUuid").invoke(storage);
            state = (String) storage.getClass().getMethod("getState").invoke(storage);
            isRemovable = (Boolean) Objects.requireNonNull(storage.getClass().getMethod("isRemovable").invoke(storage));
            isPrimary = (Boolean) Objects.requireNonNull(storage.getClass().getMethod("isPrimary").invoke(storage));
            isEmulated = (Boolean) Objects.requireNonNull(storage.getClass().getMethod("isEmulated").invoke(storage));
            state = state == null ? getState() : state;
            if (isPrimary) {
                type = TYPE_PRIMARY;
            } else {
                String n = getAbsolutePath().toLowerCase();
                if (n.indexOf("sd") > 0) {
                    type = TYPE_SD;
                } else if (n.indexOf("usb") > 0) {
                    type = TYPE_USB;
                } else {
                    type = TYPE_UNKNOWN + " " + getAbsolutePath();
                }
            }
        } catch (Throwable e) {
            Log.e(tag, e.getMessage(), e);
        }
    }

    public boolean isAvailable() {
        String s = getState();
        return (android.os.Environment.MEDIA_MOUNTED.equals(s) || android.os.Environment.MEDIA_MOUNTED_READ_ONLY.equals(s));
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public boolean isRemovable() {
        return isRemovable;
    }

    public boolean isEmulated() {
        return isEmulated;
    }

    public String getLabel() {
        return userLabel;
    }

    public File getDir() {
        return new File(this, "");
    }

    public String getUuid() {
        return uuid;
    }

    public String getType() {
        return type;
    }

    // @SuppressLint("ObsoleteSdkInt")
    @SuppressLint("ObsoleteSdkInt")
    public String getState() {
        if (isRemovable || state == null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                state = android.os.Environment.getExternalStorageState(this);
            else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
                state = android.os.Environment.getStorageState(this);
            else if (canRead() && getTotalSpace() > 0)
                state = android.os.Environment.MEDIA_MOUNTED;
            else if (state == null || android.os.Environment.MEDIA_MOUNTED.equals(state))
                state = MEDIA_UNKNOWN;
        }
        return state;
    }
}