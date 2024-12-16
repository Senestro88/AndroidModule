package com.official.senestro.video.audio.ffmpeg.classes;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class Utils {

    public static boolean isFile(@Nullable String path) {
        return notNull(path) && isFile(new File(path));
    }

    public static boolean isFile(@Nullable File path) {
        return notNull(path) && path.isFile();
    }

    public static boolean notNull(@Nullable Object arg) {
        return !isNull(arg);
    }

    public static boolean isNull(@Nullable Object arg) {
        return arg == null;
    }

    public static String getBasename(@NonNull String absolutePath) {
        int lastIndex = absolutePath.lastIndexOf(File.separator);
        if (lastIndex != -1) {
            return absolutePath.substring(lastIndex + 1);
        }
        return absolutePath;
    }

    public static void delete(@Nullable String path) {
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                if (file.isFile()) {
                    boolean delete = file.delete();
                } else if (file.isDirectory()) {
                    File[] lists = Objects.requireNonNull(file.listFiles());
                    for (File list : lists) {
                        if (list.isDirectory()) {
                            delete(list.getAbsolutePath());
                        } else if (list.isFile()) {
                            boolean delete = list.delete();
                        }
                    }
                    boolean delete = file.delete();
                }
            }
        }
    }

    public static void delete(@Nullable File absoluteFile) {
        if (absoluteFile != null) {
            delete(absoluteFile.getAbsolutePath());
        }
    }

    public static String generateRandomText(int length) {
        int textLength = Math.max(1, Math.min(length, 32));
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            builder.append(characters.charAt(index));
        }
        return builder.toString();
    }

    public static String getExtension(@NonNull String name) {
        String extension = "";
        int lastIndex = name.lastIndexOf(".");
        if (lastIndex != -1) {
            return name.substring(lastIndex + 1);
        }
        return extension;
    }

    public static HashMap<String, Object> getMediaInformation(@NonNull Context context, @NonNull String absolutePath) {
        HashMap<String, Object> information = new HashMap<>();
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, Uri.parse(absolutePath));
            information.put("width", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            information.put("height", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            information.put("duration", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            information.put("genre", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
            information.put("title", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            retriever.release();
            retriever.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return information;
    }

    public static void renameFile(@Nullable String source, @Nullable String destination) {
        if (source != null && destination != null && isFile(source)) {
            try {
                boolean renamed = new File(source).renameTo(new File(destination));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}