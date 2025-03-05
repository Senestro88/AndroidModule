package com.official.senestro.core.classes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

public class Storage {
    File directory;
    String description, uuid;
    boolean isPrimary, isRemovable, isEmulated;

    public Storage(@NonNull File directory, boolean isPrimary, boolean isRemovable, boolean isEmulated, @NonNull String description, @Nullable String uuid) {
        this.directory = directory;
        this.isPrimary = isPrimary;
        this.isRemovable = isRemovable;
        this.isEmulated = isEmulated;
        this.description = description;
        this.uuid = uuid;
    }

    public boolean isPrimary() {
        return this.isPrimary;
    }

    public boolean isRemovable() {
        return this.isRemovable;
    }

    public boolean isEmulated() {
        return this.isEmulated;
    }

    public java.io.File getDirectory() {
        return this.directory;
    }

    public String getDescription() {
        return this.description;
    }

    public String getUuid() {
        return this.uuid;
    }

    public long getFreeSpace() {
        return this.directory.getFreeSpace();
    }

    public long getTotalSpace() {
        return this.directory.getTotalSpace();
    }

    public long getUsedSpace() {
        return getTotalSpace() - getFreeSpace();
    }
}