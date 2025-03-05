package com.official.senestro.video.audio.ffmpeg.callbacks.interfaces;

import androidx.annotation.NonNull;

import java.io.File;

public interface AudioConverterConvertCallback {

    void onConverted(@NonNull File output);

    void onFailure(@NonNull Exception error);
}
