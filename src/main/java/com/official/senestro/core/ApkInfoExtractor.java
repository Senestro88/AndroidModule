package com.official.senestro.core;

import androidx.annotation.NonNull;

import com.jaredrummler.apkparser.ApkParser;
import com.jaredrummler.apkparser.model.ApkMeta;

import java.io.File;
public class ApkInfoExtractor {
    private final File file;

    public ApkInfoExtractor(@NonNull File file){
        this.file = file;
    }

    public AppInfo getAppInfo() throws Exception {
        try (ApkParser apkParser = ApkParser.create(this.file)) {
            ApkMeta meta = apkParser.getApkMeta();
            String packageName = meta.packageName;
            String versionName = meta.versionName;
            long versionCode = meta.versionCode;
            return new AppInfo(packageName, String.valueOf(versionCode), versionName);
        }
    }

    // PUBLIC  CLASS
    public class AppInfo {
        public String packageName;
        public String versionCode;
        public String versionName;

        public AppInfo(String packageName, String versionCode, String versionName) {
            this.packageName = packageName;
            this.versionCode = versionCode;
            this.versionName = versionName;
        }
    }
}