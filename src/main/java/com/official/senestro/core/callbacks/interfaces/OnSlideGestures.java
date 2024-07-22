package com.official.senestro.core.callbacks.interfaces;

import android.view.View;

import com.official.senestro.core.utils.GestureUtils;

public interface OnSlideGestures {
    void slideUp(GestureUtils instance, View view);

    void slideDown(GestureUtils instance, View view);

    void slideLeft(GestureUtils instance, View view);

    void slideRight(GestureUtils instance, View view);

    void slideUpLeft(GestureUtils instance, View view);

    void slideUpRight(GestureUtils instance, View view);

    void slideDownLeft(GestureUtils instance, View view);

    void slideDownRight(GestureUtils instance, View view);
}