package com.official.senestro.core.classes;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Objects;

public class Cache {
    // PUBLIC VARIABLES
    /* ----------------------------------------------------------------- */

    private Cache(){}

    // PRIVATE VARIABLES
    /* ----------------------------------------------------------------- */

    // PUBLIC METHODS
    /* ----------------------------------------------------------------- */
    public static boolean isCacheInSharedPreference(@NonNull Context context, @NonNull SharedPreferences preference, @NonNull String cachePath, @NonNull String name) {
        if (preference.contains(cachePath)) {
            String cacheContent = preference.getString(cachePath, "");
            if (!cacheContent.isEmpty()) {
                HashMap<String, Object> cache = GsonFromJson(cacheContent);
                return cache.containsKey(name);
            }
        }
        return false;
    }

    public static String getCacheInSharedPreference(@NonNull Context context, @NonNull SharedPreferences preference, @NonNull String cachePath, @NonNull String name) {
        String value = "";
        if (isCacheInSharedPreference(context, preference, cachePath, name)) {
            HashMap<String, Object> map = GsonFromJson(preference.getString(cachePath, ""));
            value = Objects.requireNonNull(map.get(name)).toString();
        }
        return value;
    }

    public static void saveCacheInSharedPreference(@NonNull Context context, @NonNull SharedPreferences preference, @NonNull String cachePath, @NonNull String name, @NonNull String value) {
        String cacheContent = preference.getString(cachePath, "");
        HashMap<String, Object> map;
        if (!cacheContent.isEmpty()) {
            map = GsonFromJson(preference.getString(cachePath, ""));
            if (map != null) {
                map.put(name, value);
            }
        } else {
            map = new HashMap<>();
            map.put(name, value);
        }
        assert map != null;
        preference.edit().putString(cachePath, GsonToJson(map)).apply();
    }

    public static void deleteCacheInSharedPreference(@NonNull Context context, @NonNull SharedPreferences preference, @NonNull String cachePath, @NonNull String name) {
        if (isCacheInSharedPreference(context, preference, cachePath, name)) {
            HashMap<String, Object> map = GsonFromJson(preference.getString(cachePath, ""));
            if (map != null && map.containsKey(name)) {
                map.remove(name);
                preference.edit().putString(cachePath, GsonToJson(map)).apply();
            }
        }
    }

    // PRIVATE METHODS
    /* ----------------------------------------------------------------- */
    private static HashMap<String, Object> GsonFromJson(@NonNull String content) {
        return new Gson().fromJson(content, new TypeToken<HashMap<String, Object>>() {
        }.getType());
    }

    private static String GsonToJson(@NonNull HashMap<String, Object> hashMap) {
        return new Gson().toJson(hashMap);
    }
}
