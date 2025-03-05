package com.official.senestro.core.callbacks.interfaces;

import android.view.View;
import androidx.annotation.NonNull;
import com.official.senestro.core.utils.GestureUtils;

public interface OnSlideGesturesCallback {
    void slideUp(@NonNull GestureUtils instance, @NonNull View view, float distance);

    void slideDown(@NonNull GestureUtils instance, @NonNull View view, float distance);

    void slideLeft(@NonNull GestureUtils instance, @NonNull View view, float distance);

    void slideRight(@NonNull GestureUtils instance, @NonNull View view, float distance);

    void slideUpLeft(@NonNull GestureUtils instance, @NonNull View view, float distance);

    void slideUpRight(@NonNull GestureUtils instance, @NonNull View view, float distance);

    void slideDownLeft(@NonNull GestureUtils instance, @NonNull View view, float distance);

    void slideDownRight(@NonNull GestureUtils instance, @NonNull View view, float distance);
}