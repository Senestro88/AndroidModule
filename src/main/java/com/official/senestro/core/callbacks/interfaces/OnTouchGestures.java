package com.official.senestro.core.callbacks.interfaces;

import android.view.View;

import com.official.senestro.core.utils.GestureUtils;

public interface OnTouchGestures {
    void doubleTouch(GestureUtils instance, View view);

    void singleTouch(GestureUtils instance, View view);

    void longTouch(GestureUtils instance, View view);
}
