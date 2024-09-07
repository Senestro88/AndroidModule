package com.official.senestro.video.audio.ffmpeg.enums;

public enum AudioConverterAudioFormat {
    AAC,
    MP3,
    M4A,
    WMA,
    WAV,
    FLAC;

    public String getFormat() {
        return toString().toLowerCase();
    }
}
