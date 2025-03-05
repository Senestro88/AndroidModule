package com.official.senestro.video.audio.ffmpeg;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.Statistics;
import com.official.senestro.core.AdvanceHandlerThread;
import com.official.senestro.core.utils.AdvanceUtils;
import com.official.senestro.video.audio.ffmpeg.callbacks.interfaces.VideoTranscoderCallback;
import com.official.senestro.video.audio.ffmpeg.classes.Utils;

import java.util.HashMap;

public class VideoTranscoder {
    private final String tag = VideoTranscoder.class.getName();
    private final Context context;

    public VideoTranscoder(@NonNull Context context) {
        this.context = context;
    }

    public void transcode(@NonNull String input, @Nullable VideoTranscoderCallback callback) {
        if (!Utils.isFile(input) || !AdvanceUtils.canRead(input)) {
            onDone(callback, false, "File does not exist or can not read file: " + input);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            onDone(callback, false, "Transcode failed. API level " + Build.VERSION_CODES.N + " or higher is required");
        } else {
            String output = getOutputPath(input);
            Utils.delete(output);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Transcode(input, output, callback);
            } else {
                onDone(callback, false, "Transcode failed. API level " + Build.VERSION_CODES.N + " or higher is required");
            }
        }
    }

    // PRIVATE

    private String getOutputPath(@NonNull String input) {
        return AdvanceUtils.removeExtension(input).concat("-".concat(Utils.generateRandomText(16).concat(".".concat(AdvanceUtils.getExtension(input)))));
    }

    private void Transcode(@NonNull String input, @NonNull String output, VideoTranscoderCallback callback) {
        AdvanceHandlerThread.runInBackground(() -> {
            try {
                HashMap<String, Object> metadata = Utils.getMediaInformation(context, input);
                String metadataDuration = (String) metadata.get("duration");
                final int duration = AdvanceUtils.notNull(metadataDuration) ? Integer.parseInt(metadataDuration) : 0;
                Config.enableStatisticsCallback(statistics -> onProgress(statistics, duration, callback));
                FFmpeg.executeAsync("-err_detect ignore_err -i " + input + " -c copy " + output + "", (executionId, returncode) -> onResult(returncode, input, output, duration, callback));
            } catch (Throwable e) {
                FFmpeg.cancel();
                String message = e.getMessage();
                message = AdvanceUtils.notNull(message) ? message : "An error has occurred";
                onDone(callback, false, message);
            }
        });
    }

    private void onResult(int returncode, @NonNull String input, @NonNull String output, int duration, VideoTranscoderCallback callback) {
        boolean isVideoRepaired = returncode == Config.RETURN_CODE_SUCCESS;
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

    private void onDone(VideoTranscoderCallback callback, boolean success, @NonNull String message) {
        if (AdvanceUtils.notNull(callback)) {
            postOnMainThread(() -> callback.onDone(success, message));
        }
    }

    private void onProgress(VideoTranscoderCallback callback, int progress) {
        if (AdvanceUtils.notNull(callback)) {
            postOnMainThread(() -> callback.onProgress(progress));
        }
    }

    private void postOnMainThread(@NonNull Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}