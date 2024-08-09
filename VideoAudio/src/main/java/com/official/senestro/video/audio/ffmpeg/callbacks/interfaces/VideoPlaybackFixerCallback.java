package com.official.senestro.video.audio.ffmpeg.callbacks.interfaces;

public interface VideoPlaybackFixerCallback {
    void onDone(boolean isSuccess, String message);

    void onProgress(double progress);
}