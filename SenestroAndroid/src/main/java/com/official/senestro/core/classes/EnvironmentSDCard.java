package com.official.senestro.core.classes;

import android.content.Context;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

public class EnvironmentSDCard {
    public static final String MEDIA_UNKNOWN = "unknown";
    public static final String TYPE_PRIMARY = "primary";
    public static final String TYPE_INTERNAL = "internal";
    public static final String TYPE_SD = "MicroSD";
    public static final String TYPE_USB = "USB";
    public static final String TYPE_UNKNOWN = "unknown";
    public static final String WRITE_NONE = "none";
    public static final String WRITE_READONLY = "readonly";
    public static final String WRITE_APPONLY = "apponly";
    public static final String WRITE_FULL = "readwrite";

    private static StorageFile[] devices, externalStorage, storage;

    public static StorageFile[] getDevices(@NonNull Context context) {
        initDevices(context);
        return devices;
    }

    public static StorageFile[] getExternalStorage(@NonNull Context context) {
        initDevices(context);
        return externalStorage;
    }

    public static StorageFile[] getStorage(@NonNull Context context) {
        initDevices(context);
        return storage;
    }

    // PRIVATE
    private static void initDevices(@NonNull Context context) {
        android.os.storage.StorageManager sm = (android.os.storage.StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            Method method = sm.getClass().getMethod("getVolumeList");
            Object[] volumes = (Object[]) method.invoke(sm);
            if (volumes != null && volumes.length > 0) {
                StorageFile[] TStorages = new StorageFile[volumes.length];
                for (int i = 0; i < volumes.length; i++) {
                    String absolutePath = (String) volumes[i].getClass().getMethod("getPath").invoke(volumes[i]);
                    if (absolutePath != null) {
                        TStorages[i] = new StorageFile(absolutePath, volumes[i]);
                    }
                }
                StorageFile primaryStorage = getPrimaryStorage(TStorages);
                ArrayList<StorageFile> list1 = new ArrayList<>(Arrays.asList(TStorages));
                ArrayList<StorageFile> list2 = new ArrayList<>();
                ArrayList<StorageFile> list3 = new ArrayList<>();
                for (StorageFile device : TStorages) {
                    list1.add(device);
                    if (device.isAvailable()) {
                        list3.add(device);
                        list2.add(device);
                    }
                }
                StorageFile internal = new StorageFile(context);
                list2.add(0, internal);
                if (!primaryStorage.isEmulated) {
                    list1.add(0, internal);
                }
                devices = list1.toArray(new StorageFile[0]);
                storage = list2.toArray(new StorageFile[0]);
                externalStorage = list3.toArray(new StorageFile[0]);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static StorageFile getPrimaryStorage(StorageFile[] TStorages) {
        for (StorageFile device : TStorages) {
            if (device.isPrimary) {
                return device;
            }
            if (!device.isRemovable) {
                device.isPrimary = true;
                return device;
            }
        }
        return TStorages[0];
    }
}