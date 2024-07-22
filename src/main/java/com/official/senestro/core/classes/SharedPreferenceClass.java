package com.official.senestro.core.classes;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class SharedPreferenceClass {
    private SharedPreferenceClass() {
    }

    public static SharedPreferences getCache(@NonNull Context context) {
        return context.getSharedPreferences("cache", Activity.MODE_PRIVATE);
    }
}