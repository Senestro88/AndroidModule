package com.official.senestro.core.callbacks.interfaces;

import android.speech.tts.TextToSpeech;

public interface SpeechCallback {
    void onSuccess(TextToSpeech tts);

    void onError(String message);
}