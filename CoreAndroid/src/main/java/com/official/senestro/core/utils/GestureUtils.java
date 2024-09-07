package com.official.senestro.core.utils;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;

import com.official.senestro.core.callbacks.interfaces.OnSlideGestures;
import com.official.senestro.core.callbacks.interfaces.OnTouchGestures;
import com.official.senestro.core.callbacks.interfaces.OnZoomGestures;

import java.util.ArrayList;
import java.util.List;

public class GestureUtils {

    @SuppressLint("StaticFieldLeak")
    final private GestureUtils instance;
    @SuppressLint("StaticFieldLeak")
    final private View view;

    private final List<GestureDetector> gestureDetectors;
    private final List<ScaleGestureDetector> scaleGestureDetectors;

    private final float MIN_SCALE_FACTOR = 1.0f;
    private final float MAX_SCALE_FACTOR = 10.f;
    private float SCALE_FACTOR = 1.0f;
    private float PREVIOUS_SCALE_FACTOR = SCALE_FACTOR;

    public GestureUtils(@NonNull final View view) {
        instance = this;
        this.view = view;
        gestureDetectors = new ArrayList<>();
        scaleGestureDetectors = new ArrayList<>();
    }

    public void setZomGestures(@NonNull OnZoomGestures listener) {
        final ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(view.getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                SCALE_FACTOR *= detector.getScaleFactor();
                SCALE_FACTOR = Math.max(MIN_SCALE_FACTOR, Math.min(SCALE_FACTOR, MAX_SCALE_FACTOR));
                if (PREVIOUS_SCALE_FACTOR != SCALE_FACTOR) {
                    boolean zoomingIn = SCALE_FACTOR > PREVIOUS_SCALE_FACTOR;
                    PREVIOUS_SCALE_FACTOR = SCALE_FACTOR;
                    listener.zoomChanged(instance, view, zoomingIn);
                    if (zoomingIn) {
                        listener.zoomingIn(instance, view, SCALE_FACTOR);
                    } else {
                        listener.zoomingOut(instance, view, SCALE_FACTOR);
                    }
                }
                view.setScaleX(SCALE_FACTOR);
                view.setScaleY(SCALE_FACTOR);
                return true;
            }
        });

        scaleGestureDetectors.add(scaleGestureDetector);
    }

    public void setSlideGestures(@NonNull OnSlideGestures listener) {
        final GestureDetector gestureDetector = new GestureDetector(view.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                int SWIPE_THRESHOLD = 100;
                int SWIPE_VELOCITY_THRESHOLD = 100;
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            listener.slideRight(instance, view);
                        } else {
                            listener.slideLeft(instance, view);
                        }
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            listener.slideDown(instance, view);
                        } else {
                            listener.slideUp(instance, view);
                        }
                    }
                }
                // Diagonal swipes
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD && Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        if (diffY > 0) {
                            listener.slideDownRight(instance, view);
                        } else {
                            listener.slideUpRight(instance, view);
                        }
                    } else {
                        if (diffY > 0) {
                            listener.slideDownLeft(instance, view);
                        } else {
                            listener.slideUpLeft(instance, view);
                        }
                    }
                }
                return true;
            }
        });

        gestureDetectors.add(gestureDetector);
    }

    public void setTouchGestures(@NonNull OnTouchGestures listener) {
        final GestureDetector gestureDetector = new GestureDetector(view.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                listener.doubleTouch(instance, view);
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                listener.singleTouch(instance, view);
                return true;
            }

            @Override
            public void onLongPress(@NonNull MotionEvent e) {
                listener.longTouch(instance, view);
            }
        });
        gestureDetectors.add(gestureDetector);
    }

    public void resetZoom() {
        ValueAnimator animator = ValueAnimator.ofFloat(view.getScaleX(), 1.0f);
        animator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            view.setScaleX(scale);
            view.setScaleY(scale);
            SCALE_FACTOR = 1.0f;
            PREVIOUS_SCALE_FACTOR = SCALE_FACTOR;
        });
        animator.setDuration(200);
        animator.start();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void commit() {
        view.setOnTouchListener((v, event) -> {
            for (GestureDetector gestureDetector : gestureDetectors) {
                gestureDetector.onTouchEvent(event);
            }
            for (ScaleGestureDetector scaleGestureDetector : scaleGestureDetectors) {
                scaleGestureDetector.onTouchEvent(event);
            }
            return true;
        });
    }
}
