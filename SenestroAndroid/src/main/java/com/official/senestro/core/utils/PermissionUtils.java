package com.official.senestro.core.utils;

import android.Manifest;
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

    public boolean isExternalStorageGranted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return isGranted(android.Manifest.permission.READ_EXTERNAL_STORAGE) && isGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        return false;
    }

    public void requestExternalStorage(int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !isExternalStorageGranted()) {
            request(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
        }
    }

    public boolean isReadPhoneStateGranted() {
        return isGranted(android.Manifest.permission.READ_PHONE_STATE);
    }

    public void requestReadPhoneState(int requestCode) {
        if (!isReadPhoneStateGranted()) {
            request(new String[]{android.Manifest.permission.READ_PHONE_STATE}, requestCode);
        }
    }

    public boolean isRecordAudioGranted() {
        return isGranted(android.Manifest.permission.RECORD_AUDIO);
    }

    public void requestRecordAudio(int requestCode) {
        if (!isRecordAudioGranted()) {
            request(new String[]{android.Manifest.permission.RECORD_AUDIO}, requestCode);
        }
    }

    public boolean isCameraGranted() {
        return isGranted(android.Manifest.permission.CAMERA);
    }

    public void requestCamera(int requestCode) {
        if (!isCameraGranted()) {
            request(new String[]{android.Manifest.permission.CAMERA}, requestCode);
        }
    }

    public boolean isAccessFineLocationGranted() {
        return isGranted(android.Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public void requestAccessFineLocation(int requestCode) {
        if (!isAccessFineLocationGranted()) {
            request(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
        }
    }

    public boolean isAccessNetworkStateGranted() {
        return isGranted(android.Manifest.permission.ACCESS_NETWORK_STATE);
    }

    public void requestAccessNetworkState(int requestCode) {
        if (!isAccessNetworkStateGranted()) {
            request(new String[]{android.Manifest.permission.ACCESS_NETWORK_STATE}, requestCode);
        }
    }

    public boolean isAccessWifiStateGranted() {
        return isGranted(Manifest.permission.ACCESS_WIFI_STATE);
    }

    public void requestAccessWifiState(int requestCode) {
        if (!isAccessWifiStateGranted()) {
            request(new String[]{android.Manifest.permission.ACCESS_WIFI_STATE}, requestCode);
        }
    }

    public boolean isAccessCoarseLocationGranted() {
        return isGranted(Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    public void requestAccessCoarseLocation(int requestCode) {
        if (!isAccessCoarseLocationGranted()) {
            request(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, requestCode);
        }
    }

    public boolean isBatteryStatsGranted() {
        return isGranted(Manifest.permission.BATTERY_STATS);
    }

    public void requestBatteryStats(int requestCode) {
        if (!isBatteryStatsGranted()) {
            request(new String[]{android.Manifest.permission.BATTERY_STATS}, requestCode);
        }
    }

    public boolean isChangeNetworkStateGranted() {
        return isGranted(Manifest.permission.CHANGE_NETWORK_STATE);
    }

    public void requestChangeNetworkState(int requestCode) {
        if (!isChangeNetworkStateGranted()) {
            request(new String[]{android.Manifest.permission.CHANGE_NETWORK_STATE}, requestCode);
        }
    }

    public boolean isChangeWifiStateGranted() {
        return isGranted(Manifest.permission.CHANGE_WIFI_STATE);
    }

    public void requestChangeWifiState(int requestCode) {
        if (!isChangeWifiStateGranted()) {
            request(new String[]{android.Manifest.permission.CHANGE_WIFI_STATE}, requestCode);
        }
    }

    public boolean isClearAppCacheGranted() {
        return isGranted(Manifest.permission.CLEAR_APP_CACHE);
    }

    public void requestClearAppCache(int requestCode) {
        if (!isClearAppCacheGranted()) {
            request(new String[]{android.Manifest.permission.CLEAR_APP_CACHE}, requestCode);
        }
    }

    public boolean isDeleteCacheFilesGranted() {
        return isGranted(Manifest.permission.DELETE_CACHE_FILES);
    }

    public void requestDeleteCacheFiles(int requestCode) {
        if (!isDeleteCacheFilesGranted()) {
            request(new String[]{android.Manifest.permission.DELETE_CACHE_FILES}, requestCode);
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

    private boolean isGranted(String permission) {
        // Api level 6+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            // Permissions are granted during installation for versions below Android 6
            return true;
        }
    }

    private void request(String[] permissions, int requestCode) {
        List<String> requestLists = new ArrayList<>();
        for (String permission : permissions) {
            if (!isGranted(permission)) {
                requestLists.add(permission);
            }
        }
        if (!requestLists.isEmpty()) {
            ActivityCompat.requestPermissions(activity, requestLists.toArray(new String[0]), requestCode);
        }
    }
}
