package com.official.senestro.core;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import androidx.annotation.ChecksSdkIntAtLeast;
import androidx.annotation.NonNull;

import com.official.senestro.core.callbacks.interfaces.StorageVolumeCallback;
import com.official.senestro.core.classes.EnvironmentSDCard;
import com.official.senestro.core.classes.Storage;
import com.official.senestro.core.classes.StorageFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StorageVolume {
    private static final String tag = StorageVolume.class.getName();

    @SuppressLint("StaticFieldLeak")
    private static volatile StorageVolume instance;
    private final Context context;
    private StorageVolumeCallback callback;
    private boolean receiverRegistered = false;
    private Receiver receiver;

    public StorageVolume(@NonNull Context context) {
        this.context = context;
    }

    public static synchronized StorageVolume getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (StorageVolume.class) {
                if (instance == null) {
                    instance = new StorageVolume(context);
                }
            }
        }
        return instance;
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.GINGERBREAD)
    @SuppressLint("ObsoleteSdkInt")
    public boolean isSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    @SuppressLint("ObsoleteSdkInt")
    public List<Storage> listStorages() {
        List<Storage> list = new ArrayList<>();
        if (isSupported()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                list = listStoragesForApi9To23();
            } else {
                list = listStoragesForApi24AndAbove();
            }
        }
        return list;
    }

    public void registerCallback(@NonNull StorageVolumeCallback callback) {
        if (!receiverRegistered) {
            this.callback = callback;
            receiver = new Receiver(this);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            intentFilter.addDataScheme("file");
            context.registerReceiver(receiver, intentFilter);
            receiverRegistered = true;
        }
    }

    public void unregisterCallback() {
        if (receiverRegistered) {
            context.unregisterReceiver(receiver);
            receiverRegistered = false;
        }
    }

    //PRIVATE
    @SuppressLint("ObsoleteSdkInt")
    private List<Storage> listStoragesForApi9To23() {
        List<Storage> list = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            StorageFile[] storages = EnvironmentSDCard.getDevices(context);
            for (StorageFile storage : storages) {
                if (storage.isAvailable()) {
                    File file = storage.getDir();
                    if (file.exists()) {
                        list.add(new Storage(file, storage.isPrimary(), storage.isRemovable(), storage.isEmulated(), storage.getLabel(), storage.getUuid()));
                    }
                }
            }
        }
        return list;
    }

    private List<Storage> listStoragesForApi24AndAbove() {
        List<Storage> storageList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                android.os.storage.StorageManager sm = (android.os.storage.StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
                String[] volumesPath = (String[]) sm.getClass().getMethod("getVolumePaths", new Class[0]).invoke(sm, new Object[0]);
                if (volumesPath != null) {
                    List<android.os.storage.StorageVolume> volumes = sm.getStorageVolumes();
                    for (int i = 0; i < volumesPath.length; i++) {
                        android.os.storage.StorageVolume volume = volumes.get(i);
                        String volumePath = volumesPath[i];
                        if (android.os.Environment.MEDIA_MOUNTED.equals(volume.getState())) {
                            File file = new java.io.File(volumePath);
                            if (file.exists()) {
                                storageList.add(new Storage(file, volume.isPrimary(), volume.isRemovable(), volume.isEmulated(), volume.getDescription(context), volume.getUuid()));
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                Log.e(tag, e.getMessage(), e);
            }
        }
        return storageList;
    }

    private static class Receiver extends BroadcastReceiver {
        private final StorageVolume instance;

        public Receiver(StorageVolume instance) {
            this.instance = instance;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (instance.callback != null) {
                instance.callback.onList(instance.listStorages());
            }
        }
    }
}