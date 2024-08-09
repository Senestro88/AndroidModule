package com.official.senestro.video.audio.ffmpeg;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.official.senestro.video.audio.ffmpeg.callbacks.interfaces.VideoPlaybackFixerCallback;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

public class VideoPlaybackFixer {
    private final Context context;
    private final Activity activity;

    public VideoPlaybackFixer(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void repairPlayback(@NonNull String videoPath, @NonNull VideoPlaybackFixerCallback callback) {
        if (Utils.isFile(videoPath)) {
            String cacheTrailerPath = context.getCacheDir().getAbsolutePath() + File.separator + "FFMPEG-REPAIR-" + Objects.requireNonNull(Utils.generateRandomText(16)).toUpperCase() + "." + Utils.getExtension(videoPath);
            Utils.delete(cacheTrailerPath);
            try {
                HashMap<String, Object> metadata = Utils.getMediaInformation(context, videoPath);
                String metadataDuration = (String) metadata.get("duration");
                final int duration = metadataDuration != null ? Integer.parseInt(metadataDuration) : 0;
                String command = "-err_detect ignore_err -i " + videoPath + " -c copy " + cacheTrailerPath + "";
                FFmpegKit.executeAsync(command, session -> {
                    boolean isSuccess = ReturnCode.isSuccess(session.getReturnCode());
                    repairResult(callback, isSuccess, cacheTrailerPath, videoPath);
                }, null, statistics -> {
                    repairProgress(callback, duration, (float) statistics.getTime());
                });
            } catch (Throwable e) {
                FFmpegKit.cancel();
                e.printStackTrace();
                repairFailure(callback, Objects.requireNonNull(e.getMessage()));
            }
        } else {
            repairFailure(callback, "The Context and videoPath must be valid");
        }
    }

    // PRIVATE METHODS
    private void repairResult(@NonNull VideoPlaybackFixerCallback callback, boolean isSuccess, @NonNull String cacheTrailerPath, @NonNull String videoPath) {
        String message = isSuccess ? "success" : "failed";
        if (isSuccess) {
            Utils.delete(videoPath);
            Utils.renameFile(cacheTrailerPath, videoPath);
        } else {
            Utils.delete(cacheTrailerPath);
        }
        onDone(callback, isSuccess, message);
    }

    private void repairProgress(@NonNull VideoPlaybackFixerCallback callback, int duration, float time) {
        int progress = (int) Math.ceil((time / duration) * 100);
        if (progress >= 1) {
            onProgress(callback, (double) progress);
        }
    }

    private void repairFailure(@NonNull VideoPlaybackFixerCallback callback, @NonNull String errorMessage) {
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
