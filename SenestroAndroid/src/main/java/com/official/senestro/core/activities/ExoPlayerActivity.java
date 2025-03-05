package com.official.senestro.core.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.official.senestro.core.AdvanceProgressDialog;
import com.official.senestro.core.R;
import com.official.senestro.core.callbacks.classes.SimpleOnSlideGesturesCallback;
import com.official.senestro.core.callbacks.classes.SimpleOnTouchGesturesCallback;
import com.official.senestro.core.callbacks.interfaces.ExoPlayerLisener;
import com.official.senestro.core.enums.ExoPlayerState;
import com.official.senestro.core.utils.AdvanceUtils;
import com.official.senestro.core.utils.GestureUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class ExoPlayerActivity extends AppCompatActivity implements ExoPlayerLisener {
    // The context and activity are required
    private Context context;
    private Activity activity;
    private ArrayList<String> errors = new ArrayList<>();
    private Events events;
    private ExoPlayer exoPlayer;
    private StyledPlayerView styledPlayerView;
    private FrameLayout playerFrame;
    private LinearLayout playerFrameTitleAndTimerLayout;
    private LinearLayout playerFrameControlsLayout;
    private TextView title;
    private TextView elapsedTime;
    private TextView totalTime;
    private SeekBar seekbar;
    private ImageView playImage;
    private ImageView rewindImage;
    private ImageView forwardImage;
    private ExoPlayerState stateEnum;
    private boolean isInitialized;
    private boolean viewsAllocated;
    private boolean askingFromLastPosition;
    private boolean setPositionOnProgressChangedFromUser = false;

    // PROTECTED INHERITED METHODS (Final method can not be overridden)

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getApplicationContext();
        this.activity = this;
        this.events = new Events();
        this.stateEnum = ExoPlayerState.UNPREPARED;
        setContentView(R.layout.advance_video_player);
        initPlayerVariables();
        initPlayerViews();
        onCreated(savedInstanceState);
    }

    @Override
    protected final void onDestroy() {
        super.onDestroy();
        release();
        onDestroyed();
    }

    // PUBLIC INHERITED METHODS (Final method can not be overridden)

    @Override
    public final void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }

    @Override
    public final void setContentView(View view) {
        super.setContentView(view);
    }

    @Override
    public final void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
    }

    // PUBLIC METHODS

    @Override
    public void onPlayerError(@NonNull @NotNull String message) {
    }

    @Override
    public void onPlayerMessage(@NonNull @NotNull String message) {
    }

    @Override
    public void onPlayerPlaying() {
    }

    @Override
    public void onPlayerPaused() {
    }

    @Override
    public void onPlayerEnded() {
    }

    public void onCreated(@Nullable Bundle savedInstanceState) {
    }

    public void onDestroyed() {
    }

    // PUBLIC FINAL METHOD (Final method can not be overridden)

    public final void setPlayerLayoutParams(boolean useDisplayPixels) {
        if (viewsAllocated) {
            int width = AdvanceUtils.getDisplayWidthPixels(context);
            int height = AdvanceUtils.getDisplayHeightPixels(context);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(useDisplayPixels ? width : FrameLayout.LayoutParams.MATCH_PARENT, useDisplayPixels ? height : FrameLayout.LayoutParams.MATCH_PARENT);
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
            styledPlayerView.setLayoutParams(layoutParams);
            playerFrame.setLayoutParams(layoutParams);
        }
    }

    public final void initializePlayer(@NonNull ExoPlayer player, @NonNull MediaSource mediaSource) {
        if (!isInitialized && viewsAllocated && AdvanceUtils.notNull(context) && AdvanceUtils.notNull(activity)) {
            try {
                exoPlayer = player;
                styledPlayerView.setPlayer(exoPlayer);
                exoPlayer.setMediaSource(mediaSource);
                exoPlayer.prepare();
                showMainPlayerView();
                showPlayerFrameView();
                stateEnum = ExoPlayerState.PREPARED;
                isInitialized = true;
                setTotalTime();
                events.initialize();
            } catch (Throwable throwable) {
                isInitialized = false;
                String msg = throwable.getMessage();
                String message = AdvanceUtils.isNull(msg) ? "Player initialize error" : msg;
                onPlayerError(message);
            }
        }
    }

    public final boolean isPlayerInitialized() {
        return isInitialized;
    }

    public final void setPlayerTitle(@NonNull String titleText) {
        if (viewsAllocated) {
            title.setText(titleText);
        }
    }

    public final void setPlayerFrameBackground(int color) {
        createPlayerFrameBackground(color);
    }

    public final void setPlayerFrameBackground(@NonNull String color) {
        createPlayerFrameBackground(Color.parseColor(color));
    }

    public final void setSeekbarColors(int backgroundColor, int progressColor, int thumbColor) {
        createSeekbarColors(backgroundColor, progressColor, thumbColor);
    }

    public final void setSeekbarColors(@NonNull String backgroundColor, @NonNull String progressColor, @NonNull String thumbColor) {
        createSeekbarColors(Color.parseColor(backgroundColor), Color.parseColor(progressColor), Color.parseColor(thumbColor));
    }

    public final boolean isPlayerPlaying() {
        return isInitialized && exoPlayer.isPlaying();
    }

    public final ExoPlayerState getPlayerState() {
        return stateEnum;
    }

    public final ArrayList<String> getPlayerErrors() {
        return errors;
    }

    public final boolean playerHasError() {
        return !errors.isEmpty();
    }

    public final String getLastPlayerError() {
        return playerHasError() ? errors.get(errors.size() - 1) : null;
    }

    public final void startPlayer(boolean startDirect) {
        if (isInitialized && viewsAllocated && !exoPlayer.isPlaying()) {
            if (startDirect) {
                exoPlayer.setPlayWhenReady(true);
            } else {
                if (hasPosition() && !askingFromLastPosition) {
                    askingFromLastPosition = true;
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setCancelable(false);
                    builder.setMessage("Continue from where you stopped. ");
                    builder.setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                        askingFromLastPosition = false;
                        clearPosition();
                        exoPlayer.setPlayWhenReady(true);
                    });
                    builder.setPositiveButton("Yes", (dialog, which) -> {
                        dialog.dismiss();
                        askingFromLastPosition = false;
                        exoPlayer.seekTo(lastPosition());
                        clearPosition();
                        exoPlayer.setPlayWhenReady(true);
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.setCanceledOnTouchOutside(false);
                    postOnMainThread(alertDialog::show);
                }
            }
        } else {
            onPlayerMessage("Can not start player, possibles reason are: Player isn't initialized, views aren't allocated, or already playing!");
        }
    }

    public final void pausePlayer() {
        if (isInitialized && exoPlayer.isPlaying()) {
            exoPlayer.setPlayWhenReady(false);
        }
    }

    public final void stopPlayer() {
        if (isInitialized && exoPlayer.isPlaying()) {
            exoPlayer.stop();
        }
    }

    public final void setPositionOnProgressChangedFromUser(boolean setPosition) {
        this.setPositionOnProgressChangedFromUser = setPosition;
    }

    public final void hideAllPlayerViews() {
        if (viewsAllocated) {
            styledPlayerView.setVisibility(View.GONE);
            playerFrame.setVisibility(View.GONE);
        }
    }

    public final void showAllPlayerViews() {
        showMainPlayerView();
        showPlayerFrameView();
    }

    public final void hideMainPlayerView() {
        if (viewsAllocated) {
            styledPlayerView.setVisibility(View.GONE);
        }
    }

    public final void showMainPlayerView() {
        if (viewsAllocated) {
            styledPlayerView.setVisibility(View.VISIBLE);
        }
    }

    public final void hidePlayerFrameView() {
        if (viewsAllocated) {
            playerFrame.setVisibility(View.GONE);
        }
    }

    public final void showPlayerFrameView() {
        if (viewsAllocated) {
            playerFrame.setVisibility(View.VISIBLE);
        }
    }

    // PRIVATE METHODS

    private void initPlayerVariables() {
    }

    private void initPlayerViews() {
        styledPlayerView = findViewById(R.id.advance_video_player_styled_view);
        playerFrame = findViewById(R.id.advance_video_player_frame_view);
        playerFrameTitleAndTimerLayout = findViewById(R.id.advance_video_player_frame_title_and_timers);
        playerFrameControlsLayout = findViewById(R.id.advance_video_player_frame_controls_layout);
        title = findViewById(R.id.advance_video_player_frame_player_title);
        elapsedTime = findViewById(R.id.advance_video_player_frame_elapsed_time);
        totalTime = findViewById(R.id.advance_video_player_frame_total_time);
        seekbar = findViewById(R.id.advance_video_player_frame_seekbar);
        rewindImage = findViewById(R.id.advance_video_player_frame_rewind_image);
        playImage = findViewById(R.id.advance_video_player_frame_play_image);
        forwardImage = findViewById(R.id.advance_video_player_frame_forward_image);
        viewsAllocated = true;
    }

    private void addError(@NonNull String error) {
        errors.add(error);
    }

    private boolean isVisible(@NonNull View view) {
        return view.getVisibility() == View.VISIBLE;
    }

    private HashMap<String, Object> geMediaInformation(@NonNull String vidPath) {
        try {
            return AdvanceUtils.isFile(vidPath) ? AdvanceUtils.getMediaInformation(context, vidPath) : new HashMap<>();
        } catch (Throwable throwable) {
            return new HashMap<>();
        }
    }

    private void setTotalTime() {
        if (isInitialized && viewsAllocated) {
            String duration = null;
            if (AdvanceUtils.notNull(duration) && !duration.isEmpty()) {
                String[] split = duration.split("\\.");
                int intDuration = duration.contains(".") ? Integer.parseInt(split[0]) : Integer.parseInt(duration);
                totalTime.setText(AdvanceUtils.millisecondsToTime(intDuration));
            }
        }
    }

    private void createPlayerFrameBackground(int color) {
        if (viewsAllocated) {
            playerFrameTitleAndTimerLayout.setBackgroundColor(color);
            playerFrameControlsLayout.setBackgroundColor(color);
        }
    }

    private void createSeekbarColors(int backgroundColor, int progressColor, int thumbColor) {
        if (viewsAllocated) {
            // Set the background color
            LayerDrawable layerDrawable = (LayerDrawable) seekbar.getProgressDrawable();
            GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.findDrawableByLayerId(android.R.id.background);
            gradientDrawable.setColor(backgroundColor);
            // Set the progress color
            ClipDrawable progressClip = (ClipDrawable) layerDrawable.findDrawableByLayerId(android.R.id.progress);
            progressClip.setColorFilter(progressColor, PorterDuff.Mode.SRC_IN);
            // Apply to seekbar
            seekbar.getThumb().setColorFilter(thumbColor, PorterDuff.Mode.SRC_IN);
        }
    }

    private SharedPreferences getPositionSharedPreference() {
        return getPositionSharedPreference(context);
    }

    private void savePosition() {
        if (isInitialized) {
            SharedPreferences preferences = getPositionSharedPreference();
            if (AdvanceUtils.notNull(preferences)) {
                long milliseconds = exoPlayer.getCurrentPosition();
                // preferences.edit().putLong("", milliseconds).apply();
            }
        }
    }

    private long lastPosition() {
        if (isInitialized) {
            SharedPreferences preferences = getPositionSharedPreference();
            if (AdvanceUtils.notNull(preferences)) {
                // return preferences.getLong("", 0);
            }
        }
        return 0;
    }

    private void clearPosition() {
        if (isInitialized) {
            SharedPreferences preferences = getPositionSharedPreference();
            if (AdvanceUtils.notNull(preferences)) {
                // preferences.edit().remove("").apply();
            }
        }
    }

    private boolean hasPosition() {
        if (isInitialized) {
            SharedPreferences preferences = getPositionSharedPreference();
            // return AdvanceUtils.notNull(preferences) && preferences.contains("");
        }
        return false;
    }

    private void release() {
        if (isInitialized) {
            savePosition();
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
            isInitialized = false;
        }
    }

    // PRIVATE STATIC METHODS

    private static SharedPreferences getPositionSharedPreference(@NonNull Context context) {
        return context.getSharedPreferences("movies-last-positions", Activity.MODE_PRIVATE);
    }

    private static void postOnMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    // PUBLIC STATIC METHODS

    public static void savePosition(@NonNull Context context, @NonNull String absolutePath, int currentPosition) {
        SharedPreferences sharedPreferences = getPositionSharedPreference(context);
        if (AdvanceUtils.notNull(sharedPreferences)) {
            sharedPreferences.edit().putLong(absolutePath, currentPosition).apply();
        }
    }

    public static long lastPosition(@NonNull Context context, @NonNull String absolutePath) {
        SharedPreferences sharedPreferences = getPositionSharedPreference(context);
        if (AdvanceUtils.notNull(sharedPreferences)) {
            return sharedPreferences.getLong(absolutePath, 0);
        }
        return 0;
    }

    public static void clearPosition(@NonNull Context context, @NonNull String absolutePath) {
        SharedPreferences sharedPreferences = getPositionSharedPreference(context);
        if (AdvanceUtils.notNull(sharedPreferences)) {
            sharedPreferences.edit().remove(absolutePath).apply();
        }
    }

    public static boolean hasPosition(@NonNull Context context, @NonNull String absolutePath) {
        SharedPreferences sharedPreferences = getPositionSharedPreference(context);
        if (AdvanceUtils.notNull(sharedPreferences)) {
            return sharedPreferences.contains(absolutePath);
        }
        return false;
    }

    // PRIVATE CLASSES
    private class Events {
        private final long SEEK_LENGTH = 5000;
        private final long ANIMATION_DELAY = 4000;
        private final long ANIMATION_DURATION = 500;
        private final Handler ANIMATION_HANDLER = new Handler(Looper.getMainLooper());
        private final Handler GLOBAL_HANDLER = new Handler(Looper.getMainLooper());
        private boolean isKeepScreenOn = false;
        private boolean isFullscreenOn = false;
        private AdvanceProgressDialog bufferProgressDialog;

        private void initialize() {
            onAnimationHandler();
            setBufferProgressDialog();
            playImage.setOnClickListener(v -> eventPauseAndPlay());
            rewindImage.setOnClickListener(v -> eventSeekBackward());
            forwardImage.setOnClickListener(v -> eventSeekForward());
            exoPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    Player.Listener.super.onPlayerStateChanged(playWhenReady, playbackState);
                    eventPlayerStateChanged(playWhenReady, playbackState);
                }

                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    Player.Listener.super.onIsPlayingChanged(isPlaying);
                    eventOnIsPlayingChanged(isPlaying);
                }

                @Override
                public void onPlayerError(@NonNull PlaybackException error) {
                    Player.Listener.super.onPlayerError(error);
                    String msg = error.getMessage();
                    String message = AdvanceUtils.isNull(msg) ? "Player error" : msg;
                    eventPlayerError(error.getMessage());
                }

                @Override
                public void onIsLoadingChanged(boolean isLoading) {
                    Player.Listener.super.onIsLoadingChanged(isLoading);
                    eventOnIsLoadingChanged(isLoading);
                }
            });
            seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    eventSeekbarProgressChanged(seekBar, progress, fromUser);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    eventStartTrackingTouch(seekBar);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    eventStopTrackingTouch(seekBar);
                }
            });
            GestureUtils gesture = setGestureUtils();
            gesture.commit();
        }

        private @NotNull GestureUtils setGestureUtils() {
            GestureUtils gesture = new GestureUtils(styledPlayerView);
            gesture.setSwipeThreshold(20);
            gesture.setSwipeVelocityThreshold(20);
            gesture.onTouch(new SimpleOnTouchGesturesCallback() {
                @Override
                public void doubleTouch(@NonNull GestureUtils instance, @NonNull View view) {
                    eventPlayerDoubleTouched();
                }

                @Override
                public void singleTouch(@NonNull GestureUtils instance, @NonNull View view) {
                    eventPlayerTouched();
                }
            });
            gesture.onSlide(new SimpleOnSlideGesturesCallback() {
                @Override
                public void slideUp(@NonNull @NotNull GestureUtils instance, @NonNull @NotNull View view, float distance) {
                    setBrightnessFromSwipeUpAndDownDistance(distance);
                }

                @Override
                public void slideDown(@NonNull @NotNull GestureUtils instance, @NonNull @NotNull View view, float distance) {
                    setBrightnessFromSwipeUpAndDownDistance(-distance);
                }

                @Override
                public void slideLeft(@NonNull @NotNull GestureUtils instance, @NonNull @NotNull View view, float distance) {
                    seekBackwardFromSwipeDistance(distance);
                }

                @Override
                public void slideRight(@NonNull @NotNull GestureUtils instance, @NonNull @NotNull View view, float distance) {
                    seekForwardFromSwipeDistance(distance);
                }
            });
            return gesture;
        }

        // MAIN EVENTS
        private void eventPauseAndPlay() {
            if (!exoPlayer.isPlaying() || stateEnum == ExoPlayerState.ENDED) {
                eventPlay();
            } else {
                eventPause();
            }
        }

        private void eventSeekBackward() {
            showPlayerFrame();
            long currentPosition = Math.max(0, exoPlayer.getCurrentPosition());
            long seekPosition = currentPosition - SEEK_LENGTH;
            // Ensure not to go below 0
            exoPlayer.seekTo(Math.max(0, seekPosition));
        }

        private void eventSeekForward() {
            showPlayerFrame();
            long currentPosition = exoPlayer.getCurrentPosition();
            long totalDuration = exoPlayer.getDuration();
            long seekPosition = currentPosition + SEEK_LENGTH;
            // Ensure it doesn't go beyond total totalDuration
            exoPlayer.seekTo(Math.min(seekPosition, totalDuration));
        }

        private void eventPlayerStateChanged(boolean playWhenReady, int playbackState) {
            boolean isIdle = playbackState == Player.STATE_IDLE;
            boolean isBuffering = playbackState == Player.STATE_BUFFERING;
            boolean isReady = playbackState == Player.STATE_READY;
            boolean hasEnded = playbackState == Player.STATE_ENDED;
            if (isBuffering && !askingFromLastPosition) {
                postOnMainThread(() -> bufferProgressDialog.show());
            } else if (isReady) {
                postOnMainThread(() -> bufferProgressDialog.dismiss());
                if (playWhenReady) {
                    keepScreenOn();
                    onGlobalHandler();
                    stateEnum = ExoPlayerState.PLAYING;
                    setPauseAndPlayImage();
                    onPlayerPlaying();
                } else {
                    keepScreenOff();
                    offGlobalHandler();
                    setPauseAndPlayImage();
                    stateEnum = ExoPlayerState.PAUSED;
                    onPlayerPaused();
                }
            } else if (hasEnded) {
                offEventHandlers();
                keepScreenOff();
                clearPosition();
                stateEnum = ExoPlayerState.ENDED;
                onPlayerEnded();
            }
        }

        private void eventOnIsLoadingChanged(boolean isLoading) {
        }

        private void eventOnIsPlayingChanged(boolean isPlaying) {
        }

        private void eventPlayerError(@NonNull String message) {
            cancelAnimationHandler();
            onPlayerError(message);
        }

        private void eventSeekbarProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser && setPositionOnProgressChangedFromUser) {
                exoPlayer.seekTo(getPositionFromSeekbarProgress());
            }
        }

        private void eventStartTrackingTouch(SeekBar seekBar) {
        }

        private void eventStopTrackingTouch(SeekBar seekBar) {
            if (!setPositionOnProgressChangedFromUser) {
                exoPlayer.seekTo(getPositionFromSeekbarProgress());
            }
        }

        private void eventPlayerDoubleTouched() {
            if (exoPlayer.isPlaying()) {
                eventPause();
            } else {
                eventPlay();
            }
        }

        private void eventPlayerTouched() {
            if (isPlayerFrameHidden()) {
                showPlayerFrame();
            } else {
                hidePlayerFrame();
            }
        }

        // OTHER EVENTS

        private void eventPlay() {
            if (!exoPlayer.isPlaying()) {
                exoPlayer.setPlayWhenReady(true);
            }
        }

        private void eventPause() {
            if (exoPlayer.isPlaying()) {
                exoPlayer.setPlayWhenReady(false);
            }
        }

        private void setFullscreenOn() {
            if (!isFullscreenOn) {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                isFullscreenOn = true;
            }
        }

        private void setFullscreenOff() {
            if (isFullscreenOn) {
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                isFullscreenOn = false;
            }
        }

        private void keepScreenOn() {
            if (!isKeepScreenOn) {
                AdvanceUtils.keepScreen(activity, true);
                isKeepScreenOn = true;
            }
        }

        private void keepScreenOff() {
            if (isKeepScreenOn) {
                AdvanceUtils.keepScreen(activity, false);
                isKeepScreenOn = false;
            }
        }

        private void hidePlayerFrame() {
            cancelAnimationHandler();
            setFullscreenOn();
            playerFrame.setVisibility(View.GONE);
        }

        private void showPlayerFrame() {
            cancelAnimationHandler();
            setFullscreenOff();
            playerFrame.setVisibility(View.VISIBLE);
            setPauseAndPlayImage();
            onAnimationHandler();
        }

        private void setPauseAndPlayImage() {
            if (!isPlayerFrameHidden()) {
                if (exoPlayer.isPlaying()) {
                    playImage.setImageResource(R.mipmap.ic_pause_white);
                } else {
                    playImage.setImageResource(R.mipmap.ic_play_white);
                }
            }
        }

        private void onGlobalHandler() {
            GLOBAL_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    setProgressAndElapseTime();
                    GLOBAL_HANDLER.postDelayed(this, 1000);
                }
            });
        }

        private void offGlobalHandler() {
            GLOBAL_HANDLER.removeCallbacksAndMessages(null);
        }

        private void onAnimationHandler() {
            ANIMATION_HANDLER.postDelayed(animationRunnable(), ANIMATION_DELAY);
        }

        private void offAnimationHandler() {
            ANIMATION_HANDLER.removeCallbacksAndMessages(null);
        }

        private void setProgressAndElapseTime() {
            if (!isPlayerFrameHidden()) {
                seekbar.setProgress((int) getSeekbarProgressFromPosition());
                elapsedTime.setText(AdvanceUtils.millisecondsToTime((int) exoPlayer.getCurrentPosition()));
            }
        }

        private long getPositionFromSeekbarProgress() {
            return (exoPlayer.getDuration() * seekbar.getProgress()) / seekbar.getMax();
        }

        private long getSeekbarProgressFromPosition() {
            return (seekbar.getMax() * exoPlayer.getCurrentPosition()) / exoPlayer.getDuration();
        }

        private Bitmap getBitmapFromPosition() {
            // return AdvanceUtils.extractVideoFrame("", (int) exoPlayer.getCurrentPosition());
            return null;
        }

        private boolean isPlayerFrameHidden() {
            return playerFrame.getVisibility() == View.GONE;
        }

        private void cancelAnimationHandler() {
            offAnimationHandler();
            playerFrame.clearAnimation();
        }

        private void offEventHandlers() {
            offGlobalHandler();
            offAnimationHandler();
        }

        private Runnable animationRunnable() {
            return new Runnable() {
                @Override
                public void run() {
                    AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
                    animation.setDuration(ANIMATION_DURATION);
                    animation.setAnimationListener(new SimpleAlphaAnimationListener() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            super.onAnimationEnd(animation);
                            hidePlayerFrame();
                        }
                    });
                    playerFrame.startAnimation(animation);
                }
            };
        }

        private void setBrightnessFromSwipeUpAndDownDistance(float distance) {
            float maxBrightness = 1.0f; // Full brightness
            float minBrightness = 0.1f; // Minimum brightness
            float currentBrightness = getWindow().getAttributes().screenBrightness;
            // Calculate new brightness level
            float newBrightness = currentBrightness + (distance / 1000.0f); // Normalize the distance
            newBrightness = Math.max(minBrightness, Math.min(newBrightness, maxBrightness));
            // Apply the brightness
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.screenBrightness = newBrightness;
            getWindow().setAttributes(layoutParams);
            // The bightness perventage
            int brightnessPercentage = (int) (newBrightness * 100);
        }

        // Calculate seek based on swipe distance
        private void seekForwardFromSwipeDistance(float distance) {
            long currentPosition = exoPlayer.getCurrentPosition();
            long totalDuration = exoPlayer.getDuration(); // Get the total duration of the video
            long seekDelta = getSeekDeltaFromSwipeDistance(distance); // Calculate seek delta from swipe distance
            long seekPosition = Math.min(currentPosition + seekDelta, totalDuration); // Ensure it does not exceed total duration
            int seekSeconds = (int) (seekDelta / 1000); // For debugging or display purposes
            exoPlayer.seekTo(seekPosition);
        }

        private void seekBackwardFromSwipeDistance(float distance) {
            long currentPosition = exoPlayer.getCurrentPosition();
            long seekDelta = getSeekDeltaFromSwipeDistance(distance); // Calculate seek delta from swipe distance
            long seekPosition = Math.max(currentPosition - seekDelta, 0); // Ensure it does not go below 0
            int seekSeconds = (int) (seekDelta / 1000); // For debugging or display purposes
            exoPlayer.seekTo(seekPosition);
        }

        // Converts swipe distance to a seek delta in milliseconds
        private long getSeekDeltaFromSwipeDistance(float distance) {
            // Normalize distance for seek calculation
            // Assume 1000 pixels equals 10 seconds
            float pixelsPerSecond = 100.0f; // 100 pixels = 1 second
            return (long) (distance / pixelsPerSecond * 1000); // Convert to milliseconds
        }

        private void setBufferProgressDialog() {
            bufferProgressDialog = new AdvanceProgressDialog(activity);
            bufferProgressDialog.setTitle("Buffering...");
        }
    }

    private static class SimpleAlphaAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }
}