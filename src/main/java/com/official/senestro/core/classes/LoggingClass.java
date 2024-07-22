package com.official.senestro.core.classes;

import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.official.senestro.core.utils.AdvanceUtils;
import com.official.senestro.core.utils.DateUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LoggingClass {
    private static String directory;

    private LoggingClass() {
    }

    public static void setPath(@NonNull String directory) {
        LoggingClass.directory = directory;
    }

    public static void message(@NonNull String message, @Nullable Throwable throwable) {
        save(forma(message, throwable), "messages");
    }

    public static void error(@NonNull String message, @Nullable Throwable throwable) {
        save(forma(message, throwable), "errors");
    }

    // ----------------------------------------------------------------------------------------------------- //
    private static String forma(@NonNull String message, @Nullable Throwable throwable) {
        DateUtils dateUtils = new DateUtils();
        String date = dateUtils.getLongDayNameOfWeek() + ", " + dateUtils.getMonthLongName() + " " + dateUtils.getDayInMonth() + ", " + dateUtils.getYear();
        String time = dateUtils.get12Hour(true) + ":" + dateUtils.getMinutes() + ":" + dateUtils.getSeconds() + " " + dateUtils.getDayPeriod();
        String formated = "====>>> LOG DATE AND TIME  " + date.concat(" @ " + time) + "\n\t\tMessage >> " + message + (throwable != null ? "\n\t\tStack trace >> " + Log.getStackTraceString(throwable) : "") + "\n\n";
        return formated;
    }

    private static void save(@NonNull String message, @NonNull String basename) {
        String directoryPath = directory != null ? directory : Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "logs";
        if (!AdvanceUtils.isDirectory(directoryPath)) {
            AdvanceUtils.createDirectory(directoryPath);
            if (AdvanceUtils.isDirectory(directoryPath)) {
                save(message, basename);
            }
        } else {
            try {
                FileWriter writer = new FileWriter(new File(directoryPath, basename.concat(".txt")), true);
                writer.append(message);
                writer.append("\n\n");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}