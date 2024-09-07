package com.official.senestro.video.audio.ffmpeg;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.official.senestro.core.utils.AdvanceUtils;
import com.official.senestro.video.audio.ffmpeg.callbacks.interfaces.VideoPlaybackFixerCallback;
import com.official.senestro.video.audio.ffmpeg.classes.Utils;

import java.util.HashMap;
import java.util.Objects;

public class VideoPlaybackFixer {
    private final String tag = VideoPlaybackFixer.class.getName();

    private final Context context;
    private final Activity activity;

    public VideoPlaybackFixer(@NonNull Context context, @NonNull Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public void repairPlayback(@NonNull String input, @NonNull VideoPlaybackFixerCallback callback) {
        if (!Utils.isFile(input) || !AdvanceUtils.canRead(input)) {
            onFailed(callback, "File does not exist or can not read file: " + input);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            onFailed(callback, "Transcode failed. API level " + Build.VERSION_CODES.N + " or higher is required");
        } else {
            try {
                String output = getOutputPath(input);
                HashMap<String, Object> metadata = Utils.getMediaInformation(context, input);
                String metadataDuration = (String) metadata.get("duration");
                final int duration = metadataDuration != null ? Integer.parseInt(metadataDuration) : 0;
                String command = "-err_detect ignore_err -i " + input + " -c copy " + output + "";
                Config.enableStatisticsCallback(statistics -> onProgress(callback, duration, (float) statistics.getTime()));
                FFmpeg.executeAsync(command, (executionId, returnCode) -> {
                    boolean isSuccess = returnCode == Config.RETURN_CODE_SUCCESS;
                    onResult(callback, isSuccess, output, input);
                });
            } catch (Throwable e) {
                FFmpeg.cancel();
                Log.e(tag, e.getMessage(), e);
                onFailed(callback, Objects.requireNonNull(e.getMessage()));
            }
        }
    }

    // PRIVATE METHODS

    private String getOutputPath(@NonNull String input) {
        return AdvanceUtils.removeExtension(input).concat("-".concat(Utils.generateRandomText(16).concat(".".concat(AdvanceUtils.getExtension(input)))));
    }

    private void onResult(@NonNull VideoPlaybackFixerCallback callback, boolean isSuccess, @NonNull String cacheTrailerPath, @NonNull String videoPath) {
        String message = isSuccess ? "success" : "failed";
        if (isSuccess) {
            Utils.delete(videoPath);
            Utils.renameFile(cacheTrailerPath, videoPath);
        } else {
            Utils.delete(cacheTrailerPath);
        }
        onDone(callback, isSuccess, message);
    }

    private void onProgress(@NonNull VideoPlaybackFixerCallback callback, int duration, float time) {
        int progress = (int) Math.ceil((time / duration) * 100);
        if (progress >= 1) {
            onProgress(callback, (double) progress);
        }
    }

    private void onFailed(@NonNull VideoPlaybackFixerCallback callback, @NonNull String errorMessage) {
        onDone(callback, false, errorMessage);
    }

    private void onDone(@NonNull VideoPlaybackFixerCallback callback, boolean isSuccess, @NonNull String message) {
        if (callback != null) {
            new Handler(Looper.getMainLooper()).post(() -> callback.onDone(isSuccess, message));
        }
    }

    private void onProgress(@NonNull VideoPlaybackFixerCallback callback, double progress) {
        if (callback != null) {
            new Handler(Looper.getMainLooper()).post(() -> callback.onProgress(progress));
        }
    }
}
