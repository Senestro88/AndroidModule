package com.official.senestro.video.audio.ffmpeg;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.official.senestro.core.utils.AdvanceUtils;
import com.official.senestro.video.audio.ffmpeg.callbacks.interfaces.PcmEncoderCallback;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class PcmEncoder {
    private final File file;
    private final int sampleRate;
    private final int channelsCount;
    private final int bufferSize;
    private final int bitsPerSample;
    private final int bitRate;

    private static final String AAC_MIME_TYPE = "audio/mp4a-latm";

    public PcmEncoder(int sampleRate, int channelsCount, int bufferSize, int bitsPerSample, int bitRate, @NonNull File file) {
        this.sampleRate = sampleRate;
        this.channelsCount = channelsCount; // Number of audio channelsCount (1 for mono, 2 for stereo)
        this.bufferSize = bufferSize;
        this.file = file;
        this.bitsPerSample = bitsPerSample; // Bits per sample (usually 16 for CD quality)
        this.bitRate = bitRate; // in bits per second
    }

    // Encode PCM to WAV
    public void encodeToWav(@NonNull PcmEncoderCallback callback) {
        File output = getOuputFile(file, "wav");
        try (OutputStream os = new FileOutputStream(output)) {
            // WAVE RIFF header
            writeToWavOutput(os, "RIFF"); // Chunk ID
            writeToWavOutput(os, (int) (36 + file.length())); // Chunk size
            writeToWavOutput(os, "WAVE"); // Format
            // SUB CHUNK 1 (FORMAT)
            writeToWavOutput(os, "fmt "); // Sub chunk 1 ID
            writeToWavOutput(os, 16); // Sub chunk 1 size (16 for PCM)
            writeToWavOutput(os, (short) 1); // Audio format (1 for PCM)
            writeToWavOutput(os, (short) channelsCount); // Number of channels count
            writeToWavOutput(os, sampleRate); // Sample rate;
            writeToWavOutput(os, bitRate); // Bitrate
            writeToWavOutput(os, (short) (channelsCount * bitsPerSample / 8)); // Block align
            writeToWavOutput(os, (short) bitsPerSample); // Bits per sample
            // SUB CHUNK 2 (AUDIO DATA)
            writeToWavOutput(os, "data"); // Sub chunk 2 ID;
            writeToWavOutput(os, (int) file.length()); // Sub chunk 2 size
            // Write PCM Data
            writeToWavOutputStream(new FileInputStream(file), os, 1024);
            postOnMainThread(() -> callback.onEncoded(output));
        } catch (Throwable e) {
            postOnMainThread(() -> callback.onFailure(new Exception(e)));
        }
    }

    // PRIVATE METHODS

    private void writeToWavOutput(@NonNull OutputStream os, int value) throws IOException {
        os.write(value & 0xFF);
        os.write((value >> 8) & 0xFF);
        os.write((value >> 16) & 0xFF);
        os.write((value >> 24) & 0xFF);
    }

    private void writeToWavOutput(@NonNull OutputStream os, short value) throws IOException {
        os.write(value & 0xFF);
        os.write((value >> 8) & 0xFF);
    }

    private void writeToWavOutput(@NonNull OutputStream output, @NonNull String data) throws IOException {
        for (int i = 0; i < data.length(); i++) {
            output.write(data.charAt(i));
        }
    }

    private void writeToWavOutputStream(@NonNull InputStream input, @NonNull OutputStream output, int bufferSize) throws IOException {
        long read = 0L;
        byte[] buffer = new byte[bufferSize];
        for (int n; (n = input.read(buffer)) != -1; read += n) {
            output.write(buffer, 0, n);
        }
    }

    private File getOuputFile(@NonNull File input, @NonNull String extension) {
        String output = AdvanceUtils.removeExtension(input.getPath()).concat(".".concat(extension));
        return input.getPath().equals(output) ? new File(AdvanceUtils.removeExtension(input.getPath()).concat("-".concat(AdvanceUtils.generateRandomText(16).concat(".".concat(AdvanceUtils.getExtension(input.getPath())))))) : new File(output);
    }

    // PRIVATE STATIC METHODS

    private static void postOnMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
