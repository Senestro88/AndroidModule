package com.official.senestro.video.audio.ffmpeg.callbacks.interfaces;

import androidx.annotation.NonNull;

import java.io.File;

public interface PcmEncoderCallback {
    void onEncoded(@NonNull File output);

    void onFailure(@NonNull Exception e);
}
