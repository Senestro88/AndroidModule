package com.official.senestro.video.audio.ffmpeg;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.official.senestro.core.utils.AdvanceUtils;
import com.official.senestro.video.audio.ffmpeg.callbacks.interfaces.AudioConverterConvertCallback;
import com.official.senestro.video.audio.ffmpeg.classes.Utils;
import com.official.senestro.video.audio.ffmpeg.enums.AudioConverterAudioFormat;

import java.io.File;
import java.io.IOException;

public class AudioConverter {
    private final String tag = VideoPlaybackFixer.class.getName();
    private final Context context;

    public AudioConverter(@NonNull Context context) {
        this.context = context;
    }

    public void convert(@NonNull File input, @NonNull AudioConverterAudioFormat format, int sampleRate, int channelsCount, @NonNull AudioConverterConvertCallback callback) {
        if (!input.exists() || !input.canRead()) {
            postOnMainThread(() -> callback.onFailure(new IOException("File does not exist or can not read file")));
        } else {
            try {
                final File output = getOuputFile(input, format);
                final String command = getConvertCommand(input, output, format.getFormat(), sampleRate, channelsCount);
                System.out.println("Conversion command: " + command);
                FFmpeg.executeAsync(command, (executionId, returnCode) -> {
                    if (returnCode == Config.RETURN_CODE_SUCCESS) {
                        postOnMainThread(() -> callback.onConverted(output));
                    } else if (returnCode == Config.RETURN_CODE_CANCEL) {
                        postOnMainThread(() -> callback.onFailure(new Exception("FFmpeg command execution cancelled by user.")));
                    } else {
                        postOnMainThread(() -> callback.onFailure(new Exception("An error has occurred while converting the file.")));
                    }
                });
            } catch (Exception e) {
                postOnMainThread(() -> callback.onFailure(e));
            }
        }
    }

    // PRIVATE STATIC METHODS

    private File getOuputFile(@NonNull File input, @NonNull AudioConverterAudioFormat format) {
        String output = AdvanceUtils.removeExtension(input.getPath()).concat(".".concat(format.getFormat()));
        return input.getPath().equals(output) ? new File(AdvanceUtils.removeExtension(input.getPath()).concat("-".concat(Utils.generateRandomText(16).concat(".".concat(AdvanceUtils.getExtension(input.getPath())))))) : new File(output);
    }

    private String getConvertCommand(@NonNull File input, @NonNull File output, @NonNull String format, int sampleRate, int channelsCount) {
        String command;
        sampleRate = sampleRate < 1 ? 44100 : sampleRate;
        channelsCount = channelsCount < 1 ? 2 : channelsCount;
        switch (format) {
            case "AAC":
            case "M4A":
                command = "-f  s16le -ar " + sampleRate + " -ac " + channelsCount + " -i " + input.getPath() + " -c:a aac " + output.getPath();
                break;
            case "MP3":
                command = "-f  s16le -ar " + sampleRate + " -ac " + channelsCount + " -i " + input.getPath() + " -c:a libmp3lame " + output.getPath();
                break;
            case "WMA":
                command = "-f  s16le -ar " + sampleRate + " -ac " + channelsCount + " -i " + input.getPath() + " -c:a wmav2 " + output.getPath();
                break;
            case "WAV":
                command = "-f  s16le -ar " + sampleRate + " -ac " + channelsCount + " -i " + input.getPath() + " " + output.getPath();
                break;
            default:
                command = "-f  s16le -ar " + sampleRate + " -ac " + channelsCount + " -i " + input.getPath() + " -c:a flac " + output.getPath();
        }
        return command;
    }

    private void postOnMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}