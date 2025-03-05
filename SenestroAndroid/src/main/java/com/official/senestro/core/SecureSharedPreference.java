package com.official.senestro.core;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import com.official.senestro.core.utils.AdvanceUtils;
import com.securepreferences.SecurePreferences;

public class SecureSharedPreference extends SecurePreferences {
    private final Context context;
    private final String password;
    private final String sharedPrefFilename;
    private final SharedPreferences sharedPreferences;
    @SuppressLint("StaticFieldLeak")
    private static volatile SecureSharedPreference instance;

    public SecureSharedPreference(@NonNull Context context, @NonNull String password, @NonNull String sharedPrefFilename) {
        super(context, password, sharedPrefFilename);
        this.context = context;
        this.password = password;
        this.sharedPrefFilename = sharedPrefFilename;
        this.sharedPreferences = context.getSharedPreferences(sharedPrefFilename, Activity.MODE_PRIVATE);
    }

    public static SecureSharedPreference getInstance(@NonNull Context context, @NonNull String password, @NonNull String sharedPrefFilename) {
        if (AdvanceUtils.isNull(instance)) {
            synchronized (SecureSharedPreference.class) {
                if (AdvanceUtils.isNull(instance)) {
                    instance = new SecureSharedPreference(context, password, sharedPrefFilename);
                }
            }
        }
        return instance;
    }
}
