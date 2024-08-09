package com.official.senestro.core.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {
    private final Context context;
    private final Activity activity;

    public PermissionUtils(@NonNull Context context, @NonNull Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public boolean isStoragePermissionGranted() {
        return isPermissionGranted(android.Manifest.permission.READ_EXTERNAL_STORAGE) && isPermissionGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public void requestStoragePermission(int requestCode) {
        if (!isStoragePermissionGranted()) {
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
        }
    }

    public boolean isReadPhoneStatePermissionGranted() {
        return isPermissionGranted(android.Manifest.permission.READ_PHONE_STATE);
    }

    public void requestReadPhoneStatePermission(int requestCode) {
        if (!isReadPhoneStatePermissionGranted()) {
            requestPermissions(new String[]{android.Manifest.permission.READ_PHONE_STATE}, requestCode);
        }
    }

    public boolean isPermissionResultsGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    // PRIVATE

    private boolean isPermissionGranted(String permission) {
        // Api level 6+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            // Permissions are granted during installation for versions below Android 6
            return true;
        }
    }

    private void requestPermissions(String[] permissions, int requestCode) {
        List<String> requestLists = new ArrayList<>();
        for (String permission : permissions) {
            if (!isPermissionGranted(permission)) {
                requestLists.add(permission);
            }
        }
        if (!requestLists.isEmpty()) {
            ActivityCompat.requestPermissions(activity, requestLists.toArray(new String[0]), requestCode);
        }
    }
}
