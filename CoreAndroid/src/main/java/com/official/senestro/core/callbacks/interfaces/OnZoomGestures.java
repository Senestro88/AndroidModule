package com.official.senestro.core.callbacks.interfaces;

import android.view.View;

import com.official.senestro.core.utils.GestureUtils;

public interface OnZoomGestures {
    void zoomingIn(GestureUtils instance, View view, float scale);

    void zoomingOut(GestureUtils instance, View view, float scale);

    void zoomChanged(GestureUtils instance, View view, boolean zoomingIn);
}