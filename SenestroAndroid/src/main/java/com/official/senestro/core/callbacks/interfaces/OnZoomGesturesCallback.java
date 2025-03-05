package com.official.senestro.core.callbacks.interfaces;

import android.view.View;
import com.official.senestro.core.utils.GestureUtils;

public interface OnZoomGesturesCallback {
    void zoomingIn(GestureUtils instance, View view, float scaleFactor);

    void zoomingOut(GestureUtils instance, View view, float scaleFactor);

    void zoomChanged(GestureUtils instance, View view, boolean isZoomingIn, float scaleFactor);
}