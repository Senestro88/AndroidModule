package com.official.senestro.core.callbacks.interfaces;

import com.official.senestro.core.classes.Storage;

import java.util.List;

public interface StorageVolumeCallback {
        void onList(List<Storage> storages);
}