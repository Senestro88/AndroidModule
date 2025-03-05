package com.official.senestro.core.callbacks.classes;

import android.view.View;
import com.official.senestro.core.callbacks.interfaces.OnZoomGesturesCallback;
import com.official.senestro.core.utils.GestureUtils;

public class SimpleOnZoomGesturesCallback implements OnZoomGesturesCallback {
    @Override
    public void zoomingIn(GestureUtils instance, View view, float scaleFactor) {

    }

    @Override
    public void zoomingOut(GestureUtils instance, View view, float scaleFactor) {

    }

    @Override
    public void zoomChanged(GestureUtils instance, View view, boolean isZoomingIn, float scaleFactor) {

    }
}