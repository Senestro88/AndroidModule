package com.official.senestro.core.classes;

import androidx.annotation.NonNull;
import com.official.senestro.core.AdvanceHandlerThread;
import com.official.senestro.core.callbacks.interfaces.LogcatCallback;
import com.official.senestro.core.utils.AdvanceUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A utility class to capture Android Logcat output programmatically.
 * The logs are captured in the background and reported to the provided callback.
 */
public class Logcat {
    /**
     * A flag to control the log capturing process.
     * It ensures the process can be stopped gracefully.
     */
    private static volatile boolean isRunning = false;

    /**
     * Captures Logcat output and sends it to the provided callback in batches.
     *
     * @param callback A callback to handle captured logs or errors.
     */
    public static void capture(@NonNull LogcatCallback callback) {
        AdvanceHandlerThread.runInBackground(() -> {
            isRunning = true;
            Process process = null;
            BufferedReader reader = null;
            try {
                process = Runtime.getRuntime().exec("logcat");
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line;
                // Read log lines and trigger the callback after the specified number of lines
                while (isRunning && (line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                    // Trigger the callback with the captured logs
                    callback.onCaptured(builder.toString());
                    // Clear the StringBuilder
                    builder.setLength(0);
                }
            } catch (IOException exception) {
                // Notify the callback about an error
                callback.onError(exception);
            } finally {
                // Ensure isRunning is reset
                isRunning = false;
                // Cleanup resources
                AdvanceUtils.closeQuietly(reader);
                if (AdvanceUtils.notNull(process)) {
                    process.destroy();
                }
            }
        });
    }

    /**
     * Stops the log capturing process.
     * This sets the `isRunning` flag to false, which stops the log reading loop.
     */
    public static void stop() {
        isRunning = false;
    }
}
