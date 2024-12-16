package com.official.senestro.video.audio.ffmpeg.callbacks.interfaces;

public interface VideoTranscoderCallback {
    void onProgress(double progress);

    void onDone(boolean isSuccess, String message);
}