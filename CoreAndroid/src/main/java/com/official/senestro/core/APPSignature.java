package com.official.senestro.core;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Objects;

public class APPSignature {
    private static final String tag = APPSignature.class.getName();

    private final Context context;

    public APPSignature(@NonNull Context context) {
        this.context = context;
    }

    public String get(String hash, boolean format) {
        return getFromPackageName(context.getPackageName(), hash, format);
    }

    public String getFromPackageName(String packageName, String hash, boolean format) {
        PackageInfo packageInfo = getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
        if (packageInfo != null) {
            return getFromPackageInfo(packageInfo, hash, format);
        }
        return null;
    }

    public String getFromAbsolutePath(String absolutePath, String hash, boolean format) {
        PackageInfo packageInfo = getPackageArchiveInfo(absolutePath, PackageManager.GET_SIGNATURES);
        if (packageInfo != null) {
            return getFromPackageInfo(packageInfo, hash, format);
        }
        return null;
    }

    // =============================================== //
    private PackageManager packageManager() {
        return context.getPackageManager();
    }

    private PackageInfo getPackageInfo(@NonNull String packageName, int flags) {
        try {
            PackageManager packageManager = packageManager();
            if (packageManager != null) {
                return packageManager.getPackageInfo(packageName, flags);
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private PackageInfo getPackageArchiveInfo(@NonNull String absolutePath, int flags) {
        try {
            PackageManager packageManager = packageManager();
            if (packageManager != null) {
                return packageManager.getPackageArchiveInfo(absolutePath, flags);
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexStringBuilder = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexStringBuilder.append('0');
            }
            hexStringBuilder.append(hex);
        }
        return hexStringBuilder.toString();
    }

    private String formatBytesToHex(byte[] bytes) {
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < bytes.length; ++i) {
            if (i > 0) {
                formatted.append(":");
            }
            formatted.append(String.format("%02X", bytes[i]));
        }
        return formatted.toString();
    }

    private HashMap<String, String> signatureHashes(byte[] signatureBytes, boolean format) {
        try {
            HashMap<String, String> signatures = new HashMap<>();
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(signatureBytes);
            String sha1;
            if (format) {
                sha1 = formatBytesToHex(digest.digest());
            } else {
                sha1 = bytesToHex(digest.digest());
            }
            signatures.put("SHA-1", sha1);
            digest = MessageDigest.getInstance("SHA-256");
            digest.update(signatureBytes);
            String sha256;
            if (format) {
                sha256 = formatBytesToHex(digest.digest());
            } else {
                sha256 = bytesToHex(digest.digest());
            }
            signatures.put("SHA-256", sha256);
            return signatures;
        } catch (Throwable e) {
            Log.e(tag, e.getMessage(), e);
        }
        return null;
    }

    private String getFromPackageInfo(@NonNull PackageInfo packageInfo, @NonNull String hash, boolean format) {
        android.content.pm.Signature[] signatures = packageInfo.signatures;
        for (android.content.pm.Signature signature : signatures) {
            HashMap<String, String> signatureHashes = signatureHashes(signature.toByteArray(), format);
            if (signatureHashes != null) {
                return signatureHashes.containsKey(hash.toUpperCase()) ? Objects.requireNonNull(signatureHashes.get(hash.toUpperCase())).toUpperCase() : null;
            }
        }
        return null;
    }
}