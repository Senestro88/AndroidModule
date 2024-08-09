package com.official.senestro.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;

import com.official.senestro.core.utils.AdvanceUtils;

public class FloatingVideo {
    private Context context;
    private String videoPath;
    private int startPos;
    private View view;
    private LinearLayout background;
    private VideoView video;
    private ImageView ic_open, ic_close, ic_play;
    private WindowManager manager;
    private WindowManager.LayoutParams params;
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;
    private boolean isVideoPrepared;
    private boolean isFloating;
    private boolean isVideoPlaying;

    public FloatingVideo(@NonNull Context context) {
        this.context = context;
    }

    public void setContext(@NonNull Context context) {
        this.context = context;
    }

    public void setPath(@NonNull String realPath) {
        this.videoPath = realPath;
    }

    public void setStartPos(int position) {
        this.startPos = position;
    }

    public boolean isVideoPlaying() {
        return this.isVideoPlaying;
    }

    public boolean isVideoPrepared() {
        return this.isVideoPrepared;
    }

    public boolean isFloating() {
        return this.isFloating;
    }

    public void start() {
        setVariables();
        setDesigns();
        setWindowManagerLayout();
        manager.addView(view, params);
        isFloating = true;
        setListeners();
        prepareVideo();
    }

    public void stop() {
        try {
            releaseAudioFocus();
            if (manager != null) {
                manager.removeView(view);
            }
            setWindowManagerLayout();
            if (isVideoPlaying()) {
                video.stopPlayback();
            }
            videoPath = "";
            startPos = 0;
            isFloating = false;
            isVideoPrepared = false;
            isVideoPlaying = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // PRIVATE
    @SuppressLint("InflateParams")
    private void setVariables() {
        manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.floating_video_layout, null, true);
        background = view.findViewById(R.id.floating_video_background);
        video = view.findViewById(R.id.floating_video_view);
        ic_open = view.findViewById(R.id.floating_ic_open);
        ic_close = view.findViewById(R.id.floating_ic_close);
        ic_play = view.findViewById(R.id.floating_ic_play);
    }

    private void setDesigns() {
        background.setBackground(new GradientDrawable() {
            public GradientDrawable getIns(int a, int b) {
                setCornerRadius(a);
                setColor(b);
                return this;
            }
        }.getIns(15, 0xFF1E1E1E));
        background.setElevation(8f);
    }

    private void setWindowManagerLayout() {
        int LAYOUT_TYPE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_TOAST;
        params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, LAYOUT_TYPE, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.END | Gravity.BOTTOM;
        params.x = 0;
        params.y = 0;
    }

    private void setListeners() {
        audioFocusChangeListener = focusChange -> {
            try {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        startPlayback();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    case AudioManager.AUDIOFOCUS_LOSS:
                        pausePlayback();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        ic_close.setOnClickListener(v -> stop());
        ic_play.setOnClickListener(v -> {
            if (isVideoPlaying()) {
                pausePlayback();
            } else {
                startPlayback();
            }
        });
        enableDragging();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void enableDragging() {
        background.setOnTouchListener(new View.OnTouchListener() {
            private int offsetX, offsetY;
            private boolean isDragging = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Calculate the offset from the initial touch point to the view's position
                        offsetX = (int) event.getRawX() - params.x;
                        offsetY = (int) event.getRawY() - params.y;
                        isDragging = true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!isDragging) break;
                        // Update view position based on the touch event and offset
                        params.x = (int) event.getRawX() - offsetX;
                        params.y = (int) event.getRawY() - offsetY;
                        // Update view layout
                        manager.updateViewLayout(view, params);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isDragging = false;
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
    }

    private void releaseAudioFocus() {
        if (audioFocusChangeListener != null) {
            ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE)).abandonAudioFocus(audioFocusChangeListener);
        }
    }

    private void prepareVideo() {
        if (!android.text.TextUtils.isEmpty(videoPath) && AdvanceUtils.isExist(videoPath)) {
            try {
                video.setVideoURI(Uri.parse(videoPath));
                video.seekTo(startPos);
                video.setOnPreparedListener(_mediaPlayer -> {
                    video.requestFocus();
                    isVideoPrepared = true;
                    startPlayback();
                });
                video.setOnErrorListener((mediaPlayer, what, extra) -> {
                    stop();
                    return false;
                });
                video.setOnCompletionListener(mediaPlayer -> stop());
            } catch (Exception e) {
                stop();
            }
        }
    }

    private void startPlayback() {
        if (isFloating && isVideoPrepared && !isVideoPlaying) {
            video.start();
            ic_play.setImageResource(R.mipmap.ic_pause_white);
            isVideoPlaying = true;
        }
    }

    private void pausePlayback() {
        if (isFloating && isVideoPrepared && isVideoPlaying) {
            video.pause();
            ic_play.setImageResource(R.mipmap.ic_play_arrow_white);
            isVideoPlaying = false;
        }
    }

    private void showMessage(@NonNull String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}