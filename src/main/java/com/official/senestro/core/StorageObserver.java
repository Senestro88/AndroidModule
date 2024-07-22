package com.official.senestro.core;

import android.content.Context;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.official.senestro.core.utils.AdvanceUtils;

import java.io.File;

public class StorageObserver {
    private final Context context;
    private static boolean isObserving = false;
    private static FileObserver observer;

    public StorageObserver(@NonNull Context context) {
        this.context = context;
    }

    public boolean isObserving() {
        return isObserving;
    }

    public void observer(@NonNull String path, @NonNull Callback callback) {
        if (!isObserving() && AdvanceUtils.isDirectory(path)) {
            observer = new ChangeObserver(path, callback);
            observer.startWatching();
            isObserving = true;
        }
    }

    public void unObserve() {
        if (isObserving()) {
            observer.stopWatching();
            isObserving = false;
        }
    }

    public static void postOnMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public interface Callback {
        void onFileCreated(@NonNull String absPath);

        void onFileModified(@NonNull String absPath);

        void onFileDeleted(@NonNull String absPath);
    }

    public static class SimpleCallback implements Callback {
        @Override
        public void onFileCreated(@NonNull String absPath) {

        }

        @Override
        public void onFileModified(@NonNull String absPath) {

        }

        @Override
        public void onFileDeleted(@NonNull String absPath) {

        }
    }

    public static class ChangeObserver extends FileObserver {
        private final @NonNull Callback callback;
        private final @NonNull String observedPath;

        public ChangeObserver(@NonNull String path, @NonNull Callback callback) {
            super(path, FileObserver.ALL_EVENTS);
            this.callback = callback;
            this.observedPath = path;
        }

        @Override
        public void onEvent(int event, @Nullable String path) {
            if (path != null) {
                String absPath = observedPath + File.separator + path;
                switch (event & FileObserver.ALL_EVENTS) {
                    case FileObserver.CREATE:
                        postOnMainThread(() -> callback.onFileCreated(absPath));
                        break;
                    case FileObserver.MODIFY:
                        postOnMainThread(() -> callback.onFileModified(absPath));
                        break;
                    case FileObserver.DELETE:
                        postOnMainThread(() -> callback.onFileDeleted(absPath));
                        break;
                    default:
                        break;
                }
            }
        }
    }
}