package com.official.senestro.video.audio.ffmpeg;

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
import com.official.senestro.video.audio.ffmpeg.callbacks.interfaces.VideoTranscoderCallback;

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

    public void transcode(@NonNull String inputPath, @Nullable String outputPath, @Nullable VideoTranscoderCallback callback) {
        if (!Utils.isFile(inputPath)) {
            tanscodeFailure(callback, "Invalid input file: " + inputPath);
            return;
        }
        if (outputPath == null) {
            outputPath = generateDefaultOutputPath(inputPath);
        }
        Utils.delete(outputPath);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FFmpegTranscode(inputPath, outputPath, callback);
        } else {
            tanscodeFailure(callback, "Requires API level " + Build.VERSION_CODES.N + " or higher");
        }
    }

    // PRIVATE
    private String generateDefaultOutputPath(@NonNull String inputPath) {
        return context.getCacheDir().getAbsolutePath() + File.separator + "FFMPEG-TRANSCODE-" + Objects.requireNonNull(Utils.generateRandomText(16)).toUpperCase() + "." + Utils.getExtension(inputPath);
    }

    private void FFmpegTranscode(@NonNull String input, @NonNull String output, VideoTranscoderCallback callback) {
        try {
            final String transcodePath = output;
            HashMap<String, Object> metadata = Utils.getMediaInformation(context, input);
            String metadataDuration = (String) metadata.get("duration");
            final int duration = metadataDuration != null ? Integer.parseInt(metadataDuration) : 0;
            FFmpegKit.executeAsync("-err_detect ignore_err -i " + input + " -c copy " + transcodePath + "", session -> {
                transcodeResult(session, input, transcodePath, duration, callback);
            }, null, statistics -> {
                tanscodeProgress(statistics, duration, callback);
            });
        } catch (Throwable e) {
            FFmpegKit.cancel();
            String message = e.getMessage();
            if (message != null) {
                tanscodeFailure(callback, message);
            }
        }
    }

    private void transcodeResult(@NonNull Session session, @NonNull String inputPath, @NonNull String transcodePath, int duration, VideoTranscoderCallback callback) {
        boolean isVideoRepaired = ReturnCode.isSuccess(session.getReturnCode());
        if (isVideoRepaired) {
            Utils.delete(inputPath);
            Utils.renameFile(transcodePath, inputPath);
            onDone(callback, true, "Transcoding successful");
        } else {
            Utils.delete(transcodePath);
            onDone(callback, false, "Failed to transcode: " + inputPath);
        }
    }

    private void tanscodeProgress(@NonNull Statistics statistics, int duration, VideoTranscoderCallback callback) {
        int progress = (int) Math.ceil((Float.parseFloat(String.valueOf(statistics.getTime())) / duration) * 100);
        if (progress >= 1) {
            onProgress(callback, progress);
        }
    }

    private void tanscodeFailure(VideoTranscoderCallback callback, @NonNull String errorMessage) {
        onDone(callback, false, errorMessage);
    }

    private void onDone(VideoTranscoderCallback callback, boolean isSuccess, @NonNull String message) {
        if (callback != null) {
            new Handler(Looper.getMainLooper()).post(() -> callback.onDone(isSuccess, message));
        }
    }

    private void onProgress(VideoTranscoderCallback callback, int progress) {
        if (callback != null) {
            new Handler(Looper.getMainLooper()).post(() -> callback.onProgress(progress));
        }
    }
}