package com.official.senestro.core.utils;

import android.content.Context;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import androidx.annotation.NonNull;

import com.official.senestro.core.callbacks.interfaces.AnimationCallback;

public class AnimationUtils {

    private AnimationUtils(){}

    // Alpha Animation
    public static void alphaAnimation(@NonNull View view, float startAlpha, float endAlpha, boolean repeat, boolean reverse, int duration, AnimationCallback callback) {
        AlphaAnimation animation = new AlphaAnimation(startAlpha, endAlpha);
        animation.setDuration(duration);
        if (repeat) {
            animation.setRepeatCount(Animation.INFINITE);
            if (reverse) {
                animation.setRepeatMode(Animation.REVERSE);
            }
        }
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (callback != null) {
                    callback.onAnimationStart();
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (callback != null) {
                    callback.onAnimationEnd();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                if (callback != null) {
                    callback.onAnimationRepeat();
                }
            }
        });
        view.startAnimation(animation);
    }

    // Rotate Animation
    public static void rotateAnimation(@NonNull View view, float startAngle, float endAngle, boolean repeat, boolean reverse, int duration, AnimationCallback callback) {
        RotateAnimation animation = new RotateAnimation(startAngle, endAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(duration);
        if (repeat) {
            animation.setRepeatCount(Animation.INFINITE);
            if (reverse) {
                animation.setRepeatMode(Animation.REVERSE);
            }
        }
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (callback != null) {
                    callback.onAnimationStart();
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (callback != null) {
                    callback.onAnimationEnd();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                if (callback != null) {
                    callback.onAnimationRepeat();
                }
            }
        });
        view.startAnimation(animation);
    }

    // Scale Animation
    public static void scaleAnimation(@NonNull View view, float fromX, float toX, float fromY, float toY, boolean repeat, boolean reverse, int duration, AnimationCallback callback) {
        ScaleAnimation animation = new ScaleAnimation(fromX, toX, fromY, toY, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(duration);
        if (repeat) {
            animation.setRepeatCount(Animation.INFINITE);
            if (reverse) {
                animation.setRepeatMode(Animation.REVERSE);
            }
        }
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (callback != null) {
                    callback.onAnimationStart();
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (callback != null) {
                    callback.onAnimationEnd();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                if (callback != null) {
                    callback.onAnimationRepeat();
                }
            }
        });
        view.startAnimation(animation);
    }

    // Translate Animation
    public static void translateAnimation(@NonNull View view, float fromXValue, float toXValue, float fromYValue, float toYValue, boolean repeat, boolean reverse, int duration, AnimationCallback callback) {
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, fromXValue, Animation.RELATIVE_TO_SELF, toXValue, Animation.RELATIVE_TO_SELF, fromYValue, Animation.RELATIVE_TO_SELF, toYValue);
        animation.setDuration(duration);
        if (repeat) {
            animation.setRepeatCount(Animation.INFINITE);
            if (reverse) {
                animation.setRepeatMode(Animation.REVERSE);
            }
        }
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (callback != null) {
                    callback.onAnimationStart();
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (callback != null) {
                    callback.onAnimationEnd();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                if (callback != null) {
                    callback.onAnimationRepeat();
                }
            }
        });
        view.startAnimation(animation);
    }

    // Slide in Animation
    public static void slideInAnimation(@NonNull View view, int direction, boolean repeat, boolean reverse, long duration, AnimationCallback callback) {

    }

    // Slide out Animation
    public static void slideOutAnimation(@NonNull View view, int direction, boolean repeat, boolean reverse, long duration, AnimationCallback callback) {

    }

    public static void xmlAnimation(@NonNull Context context, @NonNull View view, int animationXml, AnimationCallback callback) {
        Animation animation = android.view.animation.AnimationUtils.loadAnimation(context, animationXml);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (callback != null) {
                    callback.onAnimationStart();
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (callback != null) {
                    callback.onAnimationEnd();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                if (callback != null) {
                    callback.onAnimationRepeat();
                }
            }
        });
        view.startAnimation(animation);
    }
}