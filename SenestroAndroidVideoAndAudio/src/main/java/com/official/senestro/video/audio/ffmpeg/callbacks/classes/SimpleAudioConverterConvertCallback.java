package com.official.senestro.video.audio.ffmpeg.callbacks.classes;

import androidx.annotation.NonNull;
import com.official.senestro.video.audio.ffmpeg.callbacks.interfaces.AudioConverterConvertCallback;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class SimpleAudioConverterConvertCallback implements AudioConverterConvertCallback {
    @Override
    public void onConverted(@NonNull @NotNull File output) {

    }

    @Override
    public void onFailure(@NonNull @NotNull Exception error) {

    }
}
