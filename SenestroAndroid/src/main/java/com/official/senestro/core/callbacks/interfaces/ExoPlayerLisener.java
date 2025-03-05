package com.official.senestro.core.callbacks.interfaces;

import androidx.annotation.NonNull;

public interface ExoPlayerLisener {
    void onPlayerError(@NonNull String message);

    void onPlayerMessage(@NonNull String message);

    void onPlayerPlaying();

    void onPlayerPaused();

    void onPlayerEnded();
}
