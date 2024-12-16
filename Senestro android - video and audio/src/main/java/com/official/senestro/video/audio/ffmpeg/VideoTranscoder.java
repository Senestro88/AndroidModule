package com.official.senestro.video.audio.ffmpeg;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.Statistics;
import com.official.senestro.core.utils.AdvanceUtils;
import com.official.senestro.video.audio.ffmpeg.callbacks.interfaces.VideoTranscoderCallback;
import com.official.senestro.video.audio.ffmpeg.classes.Utils;

import java.util.HashMap;

public class VideoTranscoder {
    private final String tag = VideoTranscoder.class.getName();
    private final Context context;
    private final Activity activity;

    public VideoTranscoder(@NonNull Context context, @NonNull Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public void transcode(@NonNull String input, @Nullable VideoTranscoderCallback callback) {
        if (!Utils.isFile(input) || !AdvanceUtils.canRead(input)) {
            onFailed(callback, "File does not exist or can not read file: " + input);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            onFailed(callback, "Transcode failed. API level " + Build.VERSION_CODES.N + " or higher is required");
        } else {
            String output = getOutputPath(input);
            Utils.delete(output);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Transcode(input, output, callback);
            } else {
                onFailed(callback, "Transcode failed. API level " + Build.VERSION_CODES.N + " or higher is required");
            }
        }
    }

    // PRIVATE

    private String getOutputPath(@NonNull String input) {
        return AdvanceUtils.removeExtension(input).concat("-".concat(Utils.generateRandomText(16).concat(".".concat(AdvanceUtils.getExtension(input)))));
    }

    private void Transcode(@NonNull String input, @NonNull String output, VideoTranscoderCallback callback) {
        try {
            HashMap<String, Object> metadata = Utils.getMediaInformation(context, input);
            String metadataDuration = (String) metadata.get("duration");
            final int duration = metadataDuration != null ? Integer.parseInt(metadataDuration) : 0;
            Config.enableStatisticsCallback(statistics -> onProgress(statistics, duration, callback));
            FFmpeg.executeAsync("-err_detect ignore_err -i " + input + " -c copy " + output + "", (executionId, returnCode) -> onResult(returnCode, input, output, duration, callback));
        } catch (Throwable e) {
            FFmpeg.cancel();
            String message = e.getMessage();
            if (message != null) {
                onFailed(callback, message);
            }
        }
    }

    private void onResult(int returnCode, @NonNull String input, @NonNull String output, int duration, VideoTranscoderCallback callback) {
        boolean isVideoRepaired = returnCode == Config.RETURN_CODE_SUCCESS;
        if (isVideoRepaired) {
            Utils.delete(input);
            Utils.renameFile(output, input);
            onDone(callback, true, "Transcoding successful");
        } else {
            Utils.delete(output);
            onDone(callback, false, "Failed to transcode: " + input);
        }
    }

    private void onProgress(@NonNull Statistics statistics, int duration, VideoTranscoderCallback callback) {
        int progress = (int) Math.ceil((Float.parseFloat(String.valueOf(statistics.getTime())) / duration) * 100);
        if (progress >= 1) {
            onProgress(callback, progress);
        }
    }

    private void onFailed(VideoTranscoderCallback callback, @NonNull String message) {
        onDone(callback, false, message);
    }

    private void onDone(VideoTranscoderCallback callback, boolean success, @NonNull String message) {
        if (callback != null) {
            new Handler(Looper.getMainLooper()).post(() -> callback.onDone(success, message));
        }
    }

    private void onProgress(VideoTranscoderCallback callback, int progress) {
        if (callback != null) {
            new Handler(Looper.getMainLooper()).post(() -> callback.onProgress(progress));
        }
    }
}