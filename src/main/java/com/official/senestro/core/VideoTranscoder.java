package com.official.senestro.core;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.Session;
import com.arthenica.ffmpegkit.Statistics;
import com.official.senestro.core.callbacks.interfaces.VideoTranscoderCallback;
import com.official.senestro.core.utils.AdvanceUtils;
import com.official.senestro.core.utils.StorageUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

public class VideoTranscoder {
    private final Context context;
    private final Activity activity;

    public VideoTranscoder(@NonNull Context context, @NonNull Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public void transcodeVideo(@NonNull String inputPath, @Nullable String outputPath, @Nullable VideoTranscoderCallback callback) {
        if (!AdvanceUtils.isFile(inputPath)) {
            handleTranscodeFailure(callback, "Invalid input file: " + inputPath);
            return;
        }
        if (outputPath == null) {
            outputPath = generateDefaultOutputPath(inputPath);
        }
        AdvanceUtils.delete(outputPath);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            transcodeWithFFmpeg(inputPath, outputPath, callback);
        } else {
            handleTranscodeFailure(callback, "Requires API level " + Build.VERSION_CODES.N + " or higher");
        }
    }


    // PRIVATE
    private String generateDefaultOutputPath(@NonNull String inputPath) {
        StorageUtils storageUtils = new StorageUtils(context);
        return storageUtils.getInternalApplicationCacheDirectory() + File.separator + "FFMPEG-TRANSCODE-" + Objects.requireNonNull(AdvanceUtils.generateRandomText(16)).toUpperCase() + "." + AdvanceUtils.getExtension(inputPath);
    }

    private void transcodeWithFFmpeg(@NonNull String inputPath, @NonNull String outputPath, VideoTranscoderCallback callback) {
        try {
            final String transcodePath = outputPath;
            HashMap<String, Object> metadata = AdvanceUtils.getMediaInformation(context, inputPath);
            String metadataDuration = (String) metadata.get("duration");
            final int duration = metadataDuration != null ? Integer.parseInt(metadataDuration) : 0;
            FFmpegKit.executeAsync("-err_detect ignore_err -i " + inputPath + " -c copy " + transcodePath + "", session -> {
                handleTranscodeResult(session, inputPath, transcodePath, duration, callback);
            }, null, statistics -> {
                handleTranscodeProgress(statistics, duration, callback);
            });
        } catch (Throwable e) {
            FFmpegKit.cancel();
            String message = e.getMessage();
            if (message != null) {
                handleTranscodeFailure(callback, message);
            }
        }
    }

    private void handleTranscodeResult(@NonNull Session session, @NonNull String inputPath, @NonNull String transcodePath, int duration, VideoTranscoderCallback callback) {
        boolean isVideoRepaired = ReturnCode.isSuccess(session.getReturnCode());
        if (isVideoRepaired) {
            AdvanceUtils.delete(inputPath);
            AdvanceUtils.renameFile(transcodePath, inputPath);
            postCallbackDone(callback, true, "Transcoding successful");
        } else {
            AdvanceUtils.delete(transcodePath);
            postCallbackDone(callback, false, "Failed to transcode: " + inputPath);
        }
    }

    private void handleTranscodeProgress(@NonNull Statistics statistics, int duration, VideoTranscoderCallback callback) {
        int progress = (int) Math.ceil((Float.parseFloat(String.valueOf(statistics.getTime())) / duration) * 100);
        if (progress >= 1) {
            postCallbackProgress(callback, progress);
        }
    }

    private void handleTranscodeFailure(VideoTranscoderCallback callback, @NonNull String errorMessage) {
        postCallbackDone(callback, false, errorMessage);
    }

    private void postCallbackDone(VideoTranscoderCallback callback, boolean isSuccess, @NonNull String message) {
        if (callback != null) {
            new Handler(Looper.getMainLooper()).post(() -> callback.onDone(isSuccess, message));
        }
    }

    private void postCallbackProgress(VideoTranscoderCallback callback, int progress) {
        if (callback != null) {
            new Handler(Looper.getMainLooper()).post(() -> callback.onProgress(progress));
        }
    }
}