package com.official.senestro.core.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.official.senestro.core.callbacks.interfaces.RootUtilsExecuteCommandCallback;
import com.scottyab.rootbeer.RootBeer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class RootUtils {
    private static final String TAG = RootUtils.class.getSimpleName();
    private final Context context;
    private boolean loggingEnabled = false;
    private final RootBeer rootBeer;

    public RootUtils(@NonNull Context context) {
        this.context = context;
        this.rootBeer = new RootBeer(context);
    }

    public void enableLogging(boolean enableLogging) {
        this.loggingEnabled = enableLogging;
    }

    public boolean isRooted() {
        return rootBeer.isRooted() || checkRootAccessMethod1() || checkRootAccessMethod2() || checkRootAccessMethod3();
    }

    public boolean requestRootAccess() {
        String result = AdvanceUtils.executeCommand("su -c echo Requesting root access");
        return result != null;
    }

    public void executeCommand(@NonNull String command, @Nullable RootUtilsExecuteCommandCallback callback) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);
            // Obtain input, output, and error streams
            OutputStream outputStream = process.getOutputStream();
            InputStream inputStream = process.getInputStream();
            InputStream errorStream = process.getErrorStream();
            // Read output lines
            BufferedReader outputReader = new BufferedReader(new InputStreamReader(inputStream));
            String outputLine;
            while ((outputLine = outputReader.readLine()) != null) {
                if (callback != null) {
                    callback.onOutputLine(outputLine);
                }
            }
            // Read error lines
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                if (callback != null) {
                    callback.onErrorLine(errorLine);
                }
            }
            // Wait for the process to finish
            int exitCode = process.waitFor();
            // Check if permission was granted
            if (exitCode == 0) {
                if (callback != null) {
                    callback.onExecuted();
                }
            } else {
                if (callback != null) {
                    callback.onDenied();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            if (callback != null) {
                callback.onError("Error: " + e.getMessage());
            }
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    public boolean isMagiskInstalled() {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo("com.topjohnwu.magisk", PackageManager.GET_ACTIVITIES);
            return true; // Magisk is installed
        } catch (PackageManager.NameNotFoundException e) {
            return false; // Magisk is not installed
        }
    }

    // PRIVATE
    private void debug(@Nullable String message) {
        if (message != null && loggingEnabled) {
            Log.d(TAG, message);
        }
    }

    private void error(@Nullable String message) {
        if (message != null && loggingEnabled) {
            Log.e(TAG, message);
        }
    }

    private boolean checkRootAccessMethod1() {
        String[] binPaths = {"/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su", "/system/su"};
        for (String binPath : binPaths) {
            if (AdvanceUtils.isExist(binPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 1. Checks if the value of buildTags contains the substring "test-keys". This is commonly associated with rooted devices.
     * 2. Checks if the device fingerprint contains the pattern "generic.*test-keys". This pattern is often present in emulator or test environment fingerprints.
     * 3. Checks if the product name contains the substring "generic", which is common in emulator or test devices.
     * 4.Checks if the product name contains the substring "sdk", which is common in emulator or development devices.
     * 5.Checks if the hardware information contains the substring "goldfish", which is indicative of an emulator environment.
     * 6. Checks if the display information contains the pattern ".*test-keys". This pattern is often present in rooted devices.
     */
    private boolean checkRootAccessMethod2() {
        String buildTags = android.os.Build.TAGS;
        String fingerprint = Build.FINGERPRINT;
        String product = Build.PRODUCT;
        String hardware = Build.HARDWARE;
        String display = Build.DISPLAY;
        return (buildTags != null) && (buildTags.contains("test-keys") || fingerprint.contains("generic.*test-keys") || product.contains("generic") || product.contains("sdk") || hardware.contains("goldfish") || display.contains(".*test-keys")
        );
    }

    private boolean checkRootAccessMethod3() {
        return AdvanceUtils.executeCommand("which su") != null;
    }
}