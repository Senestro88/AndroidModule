package com.official.senestro.core;

import android.animation.ObjectAnimator;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;

public class HorizontalProgressBarAnimator {
    private final ProgressBar progressBar;
    private boolean isRunning = false;
    private int lastProgressIndex = 0;

    public HorizontalProgressBarAnimator(@NonNull ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public void start(boolean restart) {
        AdvanceHandlerThread.runInBackground(() -> {
            if (isProgressBarHorizontal()) {
                int index = restart && lastProgressIndex > 0 ? 0 : lastProgressIndex;
                isRunning = true;
                while (!isHidden() && isRunning) {
                    // The expression (index + 1) increments the value of index.
                    // Using % 101 ensures that index wraps back to 0 when it reaches 100.
                    index = (index + 1) % 101;
                    lastProgressIndex = index;
                    if (index == 100) {
                        setProgressBarOnMainThread(0);
                    } else {
                        animateProgressBarOnMainThread(index);
                    }
                    sleepThread();
                }
            }
        });
    }

    public void stop() {
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }

    private void animateProgressBarOnMainThread(int toValue) {
        postOnMainThread(() -> {
            ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", progressBar.getProgress(), toValue);
            animation.setDuration(100);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        });
    }

    private void setProgressBarOnMainThread(int toValue) {
        postOnMainThread(() -> progressBar.setProgress(0));
    }

    // PRIVATE
    private void sleepThread() {
        try {
            // Simulate a delay to represent some loading task
            Thread.sleep(100);
        } catch (InterruptedException exception) {
            System.err.println(exception.getMessage());
        }
    }

    private boolean isHidden() {
        return progressBar.getVisibility() == View.GONE;
    }

    public boolean isProgressBarHorizontal() {
        // Check if it is determinate
        if (progressBar.isIndeterminate()) {
            // It's indeterminate, so not a horizontal determinate ProgressBar
            return false;
        } else {
            // Check if the drawable is a LayerDrawable (common for horizontal ProgressBars)
            Drawable drawable = progressBar.getProgressDrawable();
            if (drawable instanceof LayerDrawable) {
                // Likely a horizontal ProgressBar
                return true;
            } else {
                // Check for ShapeDrawable (some horizontal progress bars use this)
                if (drawable instanceof ShapeDrawable) {
                    return true;
                } else {
                    // Not a horizontal determinate ProgressBar
                    return false;
                }
            }
        }
    }

    private void postOnMainThread(@NonNull Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
