package com.official.senestro.core.callbacks.classes;

import android.view.View;

import com.official.senestro.core.callbacks.interfaces.OnZoomGestures;
import com.official.senestro.core.utils.GestureUtils;

public class SimpleOnZoomGestures implements OnZoomGestures {
    @Override
    public void zoomingIn(GestureUtils instance, View view, float scale) {

    }

    @Override
    public void zoomingOut(GestureUtils instance, View view, float scale) {

    }

    @Override
    public void zoomChanged(GestureUtils instance, View view, boolean zoomingIn) {

    }
}