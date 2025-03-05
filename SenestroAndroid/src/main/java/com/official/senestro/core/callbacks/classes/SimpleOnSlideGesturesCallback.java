package com.official.senestro.core.callbacks.classes;

import android.view.View;
import androidx.annotation.NonNull;
import com.official.senestro.core.callbacks.interfaces.OnSlideGesturesCallback;
import com.official.senestro.core.utils.GestureUtils;
import org.jetbrains.annotations.NotNull;

public class SimpleOnSlideGesturesCallback implements OnSlideGesturesCallback {

    @Override
    public void slideUp(@NonNull @NotNull GestureUtils instance, @NonNull @NotNull View view, float distance) {
    }

    @Override
    public void slideDown(@NonNull @NotNull GestureUtils instance, @NonNull @NotNull View view, float distance) {
    }

    @Override
    public void slideLeft(@NonNull @NotNull GestureUtils instance, @NonNull @NotNull View view, float distance) {
    }

    @Override
    public void slideRight(@NonNull @NotNull GestureUtils instance, @NonNull @NotNull View view, float distance) {
    }

    @Override
    public void slideUpLeft(@NonNull @NotNull GestureUtils instance, @NonNull @NotNull View view, float distance) {
    }

    @Override
    public void slideUpRight(@NonNull @NotNull GestureUtils instance, @NonNull @NotNull View view, float distance) {
    }

    @Override
    public void slideDownLeft(@NonNull @NotNull GestureUtils instance, @NonNull @NotNull View view, float distance) {
    }

    @Override
    public void slideDownRight(@NonNull @NotNull GestureUtils instance, @NonNull @NotNull View view, float distance) {
    }
}