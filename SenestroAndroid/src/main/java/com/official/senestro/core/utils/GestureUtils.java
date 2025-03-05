package com.official.senestro.core.utils;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import androidx.annotation.NonNull;
import com.official.senestro.core.callbacks.interfaces.OnSlideGesturesCallback;
import com.official.senestro.core.callbacks.interfaces.OnTouchGesturesCallback;
import com.official.senestro.core.callbacks.interfaces.OnZoomGesturesCallback;

import java.util.ArrayList;
import java.util.List;

public class GestureUtils {

    // Instance and View to bind gesture utilities
    @SuppressLint("StaticFieldLeak")
    private final GestureUtils instance;
    @SuppressLint("StaticFieldLeak")
    private final View view;

    // Lists to store gesture detectors
    private final List<GestureDetector> gestureDetectors;
    private final List<ScaleGestureDetector> scaleGestureDetectors;

    // Zoom parameters


    private static final float MIN_ZOOM_SCALE_FACTOR = 1.0f; //  The smallest scale allowed for zooming a View.
    private static final float MAX_ZOOM_SCALE_FACTOR = 10.0f; // The largest scale allowed for zooming a View.
    private static float SWIPE_THRESHOLD = 100.0f; // The minimum distance a touch must travel (in pixels) for the system to consider it a swipe.
    private static float SWIPTE_THRESHOLD_VELOCITY_PER_SECOND = 100.0f; // The minimum velocity (in pixels per second) required for a swipe to be registered.
    private float ZOOM_SCALE_FACTOR = 1.0f;
    private float LAST_ZOOM_SCALE_FACTOR = ZOOM_SCALE_FACTOR;

    // Constructor initializes the view and gesture detector lists
    public GestureUtils(@NonNull final View view) {
        instance = this;
        this.view = view;
        gestureDetectors = new ArrayList<>();
        scaleGestureDetectors = new ArrayList<>();
    }

    public void setSwipeThreshold(int threshold) {
        this.SWIPE_THRESHOLD = threshold;
    }

    public void setSwipeVelocityThreshold(int threshold) {
        this.SWIPTE_THRESHOLD_VELOCITY_PER_SECOND = threshold;
    }

    // Sets up zoom gestures with the provided listener
    public void onZoom(@NonNull OnZoomGesturesCallback listener) {
        scaleGestureDetectors.add(new ScaleGestureDetector(view.getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                ZOOM_SCALE_FACTOR *= detector.getScaleFactor();
                ZOOM_SCALE_FACTOR = Math.max(MIN_ZOOM_SCALE_FACTOR, Math.min(ZOOM_SCALE_FACTOR, MAX_ZOOM_SCALE_FACTOR));
                if (!(LAST_ZOOM_SCALE_FACTOR == ZOOM_SCALE_FACTOR)) {
                    boolean isZoomingIn = ZOOM_SCALE_FACTOR > LAST_ZOOM_SCALE_FACTOR;
                    LAST_ZOOM_SCALE_FACTOR = ZOOM_SCALE_FACTOR;
                    // Notify listener of zoom changes
                    listener.zoomChanged(instance, view, isZoomingIn, ZOOM_SCALE_FACTOR);
                    if (isZoomingIn) {
                        listener.zoomingIn(instance, view, ZOOM_SCALE_FACTOR);
                    } else {
                        listener.zoomingOut(instance, view, ZOOM_SCALE_FACTOR);
                    }
                }
                // Apply scale to the view
                view.setScaleX(ZOOM_SCALE_FACTOR);
                view.setScaleY(ZOOM_SCALE_FACTOR);
                return true;
            }
        }));
    }

    // Sets up slide gestures with the provided listener
    public void onSlide(@NonNull OnSlideGesturesCallback listener) {
        GestureDetector gestureDetector = new GestureDetector(view.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent firstEvent, @NonNull MotionEvent secondEvent, float velocityX, float velocityY) {
                float DIFFX = secondEvent.getX() - firstEvent.getX(); // Difference in X-axis
                float DIFFY = secondEvent.getY() - firstEvent.getY(); // Difference in Y-axis
                float SWIPTE_DISTANCE = (float) Math.sqrt(DIFFX * DIFFX + DIFFY * DIFFY); // Total slide distance
                // Check if the total slide distance meets the threshold
                if (SWIPTE_DISTANCE > SWIPE_THRESHOLD && Math.max(Math.abs(velocityX), Math.abs(velocityY)) > SWIPTE_THRESHOLD_VELOCITY_PER_SECOND) {
                    // Check horizontal direction
                    if (Math.abs(DIFFX) > Math.abs(DIFFY)) {
                        if (DIFFX > 0) {
                            listener.slideRight(instance, view, SWIPTE_DISTANCE);
                        } else {
                            listener.slideLeft(instance, view, SWIPTE_DISTANCE);
                        }
                    }
                    // Check vertical direction
                    else {
                        if (DIFFY > 0) {
                            listener.slideDown(instance, view, SWIPTE_DISTANCE);
                        } else {
                            listener.slideUp(instance, view, SWIPTE_DISTANCE);
                        }
                    }
                    // Check diagonal swipes
                    if (Math.abs(DIFFX) > SWIPE_THRESHOLD && Math.abs(DIFFY) > SWIPE_THRESHOLD) {
                        if (DIFFX > 0) {
                            if (DIFFY > 0) {
                                listener.slideDownRight(instance, view, SWIPTE_DISTANCE);
                            } else {
                                listener.slideUpRight(instance, view, SWIPTE_DISTANCE);
                            }
                        } else {
                            if (DIFFY > 0) {
                                listener.slideDownLeft(instance, view, SWIPTE_DISTANCE);
                            } else {
                                listener.slideUpLeft(instance, view, SWIPTE_DISTANCE);
                            }
                        }
                    }
                }
                return true;
            }
        });

        gestureDetectors.add(gestureDetector);
    }

    // Sets up touch gestures like single tap, double tap, and long press
    public void onTouch(@NonNull OnTouchGesturesCallback listener) {
        GestureDetector gestureDetector = new GestureDetector(view.getContext(), new GestureDetector.SimpleOnGestureListener() {
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

    // Resets the view zoom to its default scale
    public void resetZoom() {
        ValueAnimator animator = ValueAnimator.ofFloat(view.getScaleX(), 1.0f);
        animator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            view.setScaleX(scale);
            view.setScaleY(scale);
            ZOOM_SCALE_FACTOR = 1.0f;
            LAST_ZOOM_SCALE_FACTOR = ZOOM_SCALE_FACTOR;
        });
        animator.setDuration(200);
        animator.start();
    }

    // Attaches the touch listener to the view to process gestures
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
