package com.official.senestro.core;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.ConsumerIrManager;

import androidx.annotation.NonNull;

public class IRBlaster {
    @SuppressLint("StaticFieldLeak")
    private static volatile IRBlaster instance;
    private final Context context;
    private final Activity activity;

    public IRBlaster(@NonNull Context context, @NonNull Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public static synchronized IRBlaster getInstance(@NonNull Context context, @NonNull Activity activity) {
        if (instance == null) {
            synchronized (IRBlaster.class) {
                if (instance == null) {
                    instance = new IRBlaster(context, activity);
                }
            }
        }
        return instance;
    }

    // Check if the IR blaster is available on the device
    public boolean hasFeature() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CONSUMER_IR);
    }

    // Check if the IR blaster is enabled
    public boolean isEnabled() {
        ConsumerIrManager manager = getConsumerIrManager();
        return manager != null && manager.hasIrEmitter();
    }

    // Transmit the IR signal
    public void transmit(int frequency, int[] pattern) {
        if (hasFeature() && isEnabled()) {
            ConsumerIrManager manager = getConsumerIrManager();
            if (manager != null) {
                manager.transmit(frequency, pattern);
            }
        }
    }

    // PRIVATE
    private PackageManager getPackageManager() {
        return context.getPackageManager();
    }

    private ConsumerIrManager getConsumerIrManager() {
        return (ConsumerIrManager) context.getSystemService(Context.CONSUMER_IR_SERVICE);
    }
}