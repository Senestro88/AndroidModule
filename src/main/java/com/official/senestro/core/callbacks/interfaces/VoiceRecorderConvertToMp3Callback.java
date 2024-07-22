package com.official.senestro.core.callbacks.interfaces;

import androidx.annotation.NonNull;

public interface VoiceRecorderConvertToMp3Callback {

    void onError(@NonNull String message);

    void onConverted(@NonNull String oldFilename, @NonNull String newFilename);
}