package com.official.senestro.core.classes;

import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.official.senestro.core.utils.AdvanceUtils;
import com.official.senestro.core.utils.DateUtils;

import java.io.File;
import java.io.FileWriter;

public class Logging {
    private static String directory = Environment.getExternalStorageDirectory().getAbsolutePath();

    private Logging() {
    }

    public static void setDirectory(@NonNull String directory) {
        if (AdvanceUtils.isDirectory(directory)) {
            Logging.directory = directory;
        }
    }

    public static String getDirectory() {
        return directory;
    }

    public static void d(@NonNull String message, @Nullable Throwable throwable) {
        saveFormattedFormated(setFormattedMessage(message, throwable), "debugs");
    }

    public static void e(@NonNull String message, @Nullable Throwable throwable) {
        saveFormattedFormated(setFormattedMessage(message, throwable), "errors");
    }

    public static void i(@NonNull String message, @Nullable Throwable throwable) {
        saveFormattedFormated(setFormattedMessage(message, throwable), "info");
    }

    // ----------------------------------------------------------------------------------------------------- //
    private static String setFormattedMessage(@NonNull String message, @Nullable Throwable throwable) {
        DateUtils dateUtils = new DateUtils();
        String date = dateUtils.getLongDayNameOfWeek() + ", " + dateUtils.getMonthLongName() + " " + dateUtils.getDayInMonth() + ", " + dateUtils.getYear();
        String time = dateUtils.get12Hour(true) + ":" + dateUtils.getMinutes() + ":" + dateUtils.getSeconds() + " " + dateUtils.getDayPeriod();
        return "====>>> LOG DATE AND TIME  " + date.concat(" @ " + time) + "\n\t\tMESSAGE >> " + message + (throwable != null ? "\n\t\tSTACK TRACE >> " + Log.getStackTraceString(throwable) : "") + "\n\n";
    }

    private static void saveFormattedFormated(@NonNull String formatted, @NonNull String basename) {
        if (!AdvanceUtils.isDirectory(directory)) {
            AdvanceUtils.createDirectory(directory);
            if (AdvanceUtils.isDirectory(directory)) {
                saveFormattedFormated(formatted, basename);
            }
        } else {
            try {
                // Write to file
                FileWriter writer = new FileWriter(new File(directory, basename.concat(".txt")), true);
                writer.append(formatted);
                writer.append("\n\n");
                writer.close();
            } catch (Throwable ignored) {
            }
        }
    }
}