package com.official.senestro.core;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.official.senestro.core.callbacks.interfaces.AdvanceMediaRecorderCallback;
import com.official.senestro.core.classes.audio.AudioBitrate;
import com.official.senestro.core.classes.audio.AudioOutputFormat;
import com.official.senestro.core.classes.audio.AudioSamplingRate;
import com.official.senestro.core.classes.audio.AudioSource;
import com.official.senestro.core.utils.AdvanceUtils;
import com.official.senestro.core.views.AudioRecordWaveformView;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AdvanceMediaRecorder {
    private final String tag = AdvanceMediaRecorder.class.getName();
    private final Context context;
    private final Activity activity;
    private MediaRecorder mediaRecorder;
    private boolean startedRecording = false;
    private State state = State.INITIALIZED;
    private File dir = Environment.getExternalStorageDirectory(); // The recording/records directory
    private File file; // The recording file
    private long recordTime; // The time the recording was started, it will be updated when resumed
    private long onPauseTime; // The time the recording is paused, it will be reset when resumed or stooped
    private AudioRecordWaveformView recordWaveformView;
    private AdvanceMediaRecorderCallback callback;
    // The maximum duration in ms (if zero or negative, disables the duration limit)
    private int maxDuration = 0;
    // The maximum file size in bytes (if zero or negative, disables the limit)
    private int maxBytes = 0;
    private EndlessTask endlessTaskHandler;
    private AndroidBuildDetails buildDetails;

    public AdvanceMediaRecorder(@NonNull Context context, @NonNull Activity activity) {
        this.context = context;
        this.activity = activity;
        this.mediaRecorder = createMediaRecorder();
        this.endlessTaskHandler = new EndlessTask(100);
    }

    public void setCallback(@Nullable AdvanceMediaRecorderCallback callback) {
        this.callback = callback;
    }

    public boolean startedRecording() {
        return startedRecording;
    }

    public boolean isPaused() {
        return startedRecording && state == State.PAUSED;
    }

    public boolean isRecording() {
        return startedRecording && state == State.RECORDING;
    }

    /**
     * The maximum duration in ms (if zero or negative, disables the duration limit)
     */
    public void setMaxDuration(int durationMillis) {
        if (!startedRecording) {
            maxDuration = durationMillis;
        }
    }

    /**
     * The maximum file size in bytes (if zero or negative, disables the limit)
     */
    public void setMaxBytes(int sizeBytes) {
        if (!startedRecording) {
            maxBytes = sizeBytes;
        }
    }

    public void setDirectory(@NonNull String directory) {
        if (AdvanceUtils.isDirectory(directory)) {
            dir = new File(directory);
        }
    }

    public void setWaveformView(@NonNull FrameLayout waveformViewLayout) {
        newWaveformView(waveformViewLayout);
    }

    public void startRecord(@NonNull Source source, @NonNull OutputFormat format, @NonNull Bitrate bitrate) {
        AdvanceExecutorService.runInBackground(() -> {
            if (!startedRecording) {
                String filename = createFilename(format);
                if (AdvanceUtils.notNull(filename)) {
                    try {
                        AdvanceUtils.createFile(filename);
                        if (AdvanceUtils.isFile(filename)) {
                            startRecording(filename, source, format, bitrate);
                        } else {
                            postToCallback(listener -> listener.onError("Unable to create the record filename on: " + filename));
                        }
                    } catch (Throwable e) {
                        Log.e(tag, e.getMessage(), e);
                        String message = e.getMessage();
                        postToCallback(listener -> listener.onError(!AdvanceUtils.isNull(message) ? message : "An error has occurred while starting the voice record."));
                    }
                } else {
                    postToCallback(listener -> listener.onError("Unable to generate the record filename."));
                }
            } else {
                postToCallback(listener -> listener.onMessage("Can't start a voice record, an active record id found."));
            }
        });
    }

    public void startRecord(@NonNull OutputFormat format, @NonNull Bitrate bitrate) {
        startRecord(Source.MIC, format, bitrate);
    }

    public void startRecord(@NonNull Source source) {
        startRecord(source, OutputFormat.AAC, Bitrate.HIGH);
    }

    public void startRecord(@NonNull OutputFormat format) {
        startRecord(Source.MIC, format, Bitrate.HIGH);
    }

    public void startRecord(@NonNull Bitrate bitrate) {
        startRecord(Source.MIC, OutputFormat.AAC, bitrate);
    }

    public void startRecord(@NonNull Source source, @NonNull OutputFormat format) {
        startRecord(source, format, Bitrate.HIGH);
    }

    public void startRecord(@NonNull Source source, @NonNull Bitrate bitrate) {
        startRecord(source, OutputFormat.AAC, bitrate);
    }

    public void startRecord() {
        startRecord(Source.MIC, OutputFormat.AAC, Bitrate.HIGH);
    }

    public void pauseRecord() {
        AdvanceExecutorService.runInBackground(() -> {
            if (isRecording()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    try {
                        mediaRecorder.pause();
                        state = State.PAUSED;
                        onPauseTime = System.currentTimeMillis();
                        postToCallback(AdvanceMediaRecorderCallback::onPaused);
                    } catch (Throwable e) {
                        Log.e(tag, e.getMessage(), e);
                        String message = e.getMessage();
                        postToCallback(listener -> listener.onError(!AdvanceUtils.isNull(message) ? message : "An error has occurred while pausing the recorder."));
                    }
                } else {
                    postToCallback(listener -> listener.onMessage("You can not pause voice record on this device, your device API level is: " + Build.VERSION.SDK_INT + " (" + buildDetails.getVersionNameFromApiLevel(Build.VERSION.SDK_INT) + "). You must be using API level: " + Build.VERSION_CODES.N + " (" + buildDetails.getVersionNameFromApiLevel(Build.VERSION_CODES.N) + ") or greater."));
                }
            } else {
                postToCallback(listener -> listener.onMessage("You can't pause a voice record if no voice recording is started."));
            }
        });
    }

    public void resumeRecord() {
        AdvanceExecutorService.runInBackground(() -> {
            if (isPaused()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    try {
                        mediaRecorder.resume();
                        state = State.RECORDING;
                        recordTime += System.currentTimeMillis() - onPauseTime;
                        onPauseTime = 0;
                        postToCallback(AdvanceMediaRecorderCallback::onResumed);
                    } catch (Throwable e) {
                        Log.e(tag, e.getMessage(), e);
                        String message = e.getMessage();
                        postToCallback(listener -> listener.onError(!AdvanceUtils.isNull(message) ? message : "An error has occurred while resuming the voice record."));
                    }
                } else {
                    postToCallback(listener -> listener.onMessage("You can't resume voice recording on this device, your device API level is: " + Build.VERSION.SDK_INT + " (" + buildDetails.getCodeNameFromApiLevel(Build.VERSION.SDK_INT) + "). You must be using API level: " + Build.VERSION_CODES.N + " (" + buildDetails.getCodeNameFromApiLevel(Build.VERSION_CODES.N) + ") or greater."));
                }
            } else {
                postToCallback(listener -> listener.onMessage("You can't resume a voice record if no voice recording is started and paused."));
            }
        });
    }

    /**
     * Stops recording. Call this after startRecord(). Once recording is stopped, you will have to configure it again as if it has just been constructed.
     */
    public void stopRecord() {
        AdvanceExecutorService.runInBackground(() -> {
            if (startedRecording) {
                try {
                    mediaRecorder.stop();
                    mediaRecorder.reset();
                    mediaRecorder.release();
                    mediaRecorder = null;
                    startedRecording = false;
                    state = State.INITIALIZED;
                    RecordTimer timer = new RecordTimer(recordTime);
                    recordTime = onPauseTime = 0;
                    endlessTaskHandler.stop();
                    resetWaveform();
                    postToCallback(listener -> listener.onStopped(file, timer.hours(), timer.minutes(), timer.seconds(), timer.milliseconds(), timer.formatted()));
                } catch (Throwable e) {
                    Log.e(tag, e.getMessage(), e);
                    String message = e.getMessage();
                    postToCallback(listener -> listener.onError(!AdvanceUtils.isNull(message) ? message : "An error has occurred while stopping the voice record."));
                }
            } else {
                postToCallback(listener -> listener.onMessage("You can not stop a voice record if no voice record is started."));
            }
        });
    }

    // PRIVATE
    private void startRecording(@NonNull String filename, @NonNull Source source, @NonNull OutputFormat format, @NonNull Bitrate bitrate) throws Throwable {
        file = new File(filename);
        mediaRecorder = createMediaRecorder();
        BSVRMethods methods = new BSVRMethods();
        methods.source(mediaRecorder, source);
        methods.format(mediaRecorder, format);
        methods.maxDuration(mediaRecorder, maxDuration);
        methods.maxBytes(mediaRecorder, maxBytes);
        methods.encoder(mediaRecorder, format, bitrate);
        methods.bitrate(mediaRecorder, bitrate);
        methods.output(mediaRecorder, filename);
        onMediaRecorderError(mediaRecorder, filename);
        onMediaRecorderInfo(mediaRecorder, filename);
        mediaRecorder.prepare();
        mediaRecorder.start();
        startedRecording = true;
        state = State.RECORDING;
        recordTime = System.currentTimeMillis();
        postToCallback(listener -> listener.onStarted(file));
        endlessTaskHandler.start(() -> {
            if (startedRecording && isRecording()) {
                try {
                    RecordTimer millis = new RecordTimer(recordTime);
                    postToCallback(listener -> listener.onTimeUpdate(millis.hours(), millis.minutes(), millis.seconds(), millis.milliseconds(), millis.formatted()));
                    if (mediaRecorder != null) {
                        int maxAmplitude = mediaRecorder.getMaxAmplitude();
                        drawWaveform(maxAmplitude);
                        // Normalize the amplitude value to a percentage
                        int normalizedAmplitude = ((maxAmplitude * 100) / 32767);
                        postToCallback(listener -> listener.onAmplitudes(maxAmplitude, normalizedAmplitude));
                    }
                } catch (Throwable e) {
                    Log.e(tag, e.getMessage(), e);
                    String message = e.getMessage();
                    postToCallback(listener -> listener.onError(!AdvanceUtils.isNull(message) ? message : "An error has occurred on the record runnable."));
                }
            }
        });
    }

    private void reset() {
        mediaRecorder = null;
        startedRecording = false;
        state = State.INITIALIZED;
        dir = Environment.getExternalStorageDirectory();
        file = null;
        recordTime = onPauseTime = 0;
        recordWaveformView = null;
        callback = null;
    }

    @SuppressLint("InflateParams")
    public void newWaveformView(@Nullable FrameLayout waveformViewLayout) {
        if (AdvanceUtils.notNull(waveformViewLayout)) {
            recordWaveformView = new AudioRecordWaveformView(context);
            recordWaveformView.setWaveColor(Color.BLUE);
            recordWaveformView.setWaveDirection(AudioRecordWaveformView.Direction.RightToLeft);
            recordWaveformView.setWaveRoundedCorners(true);
            recordWaveformView.setWaveSoftTransition(true);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
            recordWaveformView.setLayoutParams(layoutParams);
            postOnMainThread(() -> {
                waveformViewLayout.removeAllViews();
                waveformViewLayout.addView(recordWaveformView);
            });
            postToCallback(listener -> listener.onMessage("Voice recording waveform view has been added."));
        }
    }

    @NonNull
    @Contract(" -> new")
    private MediaRecorder createMediaRecorder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new MediaRecorder(context);
        } else {
            return new MediaRecorder();
        }
    }

    private void onMediaRecorderError(@NonNull MediaRecorder mediaRecorder, @NonNull String filename) {
        mediaRecorder.setOnErrorListener((mr, what, extra) -> {
            switch (what) {
                case MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN:
                    postToCallback(listener -> listener.onError("Unspecified error message."));
                    break;
                case MediaRecorder.MEDIA_ERROR_SERVER_DIED:
                    stopRecord();
                    resetWaveform();
                    postToCallback(listener -> listener.onError("Media recorder server died. The record was auto stopped with no action."));
                    break;
                default:
                    break;
            }
        });
    }

    private void onMediaRecorderInfo(@NonNull MediaRecorder mediaRecorder, @NonNull String filename) {
        mediaRecorder.setOnInfoListener((mr, what, extra) -> {
            switch (what) {
                case MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN:
                    postToCallback(listener -> listener.onMessage("Unspecified info message."));
                    break;
                case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
                    stopRecord();
                    resetWaveform();
                    postToCallback(listener -> listener.onMessage("A maximum duration had been setup and has now been reached. The record was auto stopped with no action."));
                    break;
                case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
                    stopRecord();
                    resetWaveform();
                    postToCallback(listener -> listener.onMessage("A maximum file size had been setup and has now been reached. The record was auto stopped with no action."));
                    break;
                default:
                    break;
            }
        });
    }

    private void postOnMainThread(@NonNull Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    private void postToCallback(@NonNull CallbackExecutor executor) {
        if (callback != null) {
            postOnMainThread(() -> executor.execute(callback));
        }
    }

    @Nullable
    private String createFilename(@NonNull OutputFormat format) {
        if (AdvanceUtils.isDirectory(dir)) {
            String extension = getOutputFormatExtension(format);
            if (AdvanceUtils.notNull(extension)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US);
                return dir + File.separator + "voice_record_".concat(dateFormat.format(new Date()).concat(extension)).toLowerCase();
            }
        }
        return null;
    }

    /**
     * Get a voice record filename extension base on the format specified
     *
     * @param format The voice record format
     * @return A new filename extension or null if filename extension can not be generated
     */
    @Nullable
    @Contract(pure = true)
    private String getOutputFormatExtension(@NonNull OutputFormat format) {
        switch (format) {
            case M4A:
                return ".m4a";
            case AAC:
                return ".aac";
            case THREE_GPP:
                return ".3gp";
            default:
                return null;
        }
    }

    private void sendBroadcast(@NonNull String filename) {
        AdvanceExecutorService.runInBackground(() -> {
            if (AdvanceUtils.isFile(filename)) {
                File f = new File(filename);
                String name = f.getName();
                //Creating content values of size 4
                ContentValues values = new ContentValues(4);
                long current = System.currentTimeMillis();
                values.put(MediaStore.Audio.Media.TITLE, "audio" + name);
                values.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
                values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");
                values.put(MediaStore.Audio.Media.DATA, filename);
                //Create=ing content resolver and storing it in the external content uri
                ContentResolver contentResolver = context.getContentResolver();
                Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                Uri newUri = contentResolver.insert(base, values);
                //sending broadcast message to scan the media file so that it can be available
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, newUri));
                postToCallback(listener -> listener.onBroadcast(f));
            } else {
                postToCallback(listener -> listener.onMessage("Broadcast can't be sent as the filename isn't a valid file (" + filename + ")."));
            }
        });
    }

    private void resetWaveform() {
        if (AdvanceUtils.notNull(recordWaveformView)) {
            postOnMainThread(() -> recordWaveformView.recreate());
        }
    }

    private void drawWaveform(int maxAmplitude) {
        if (AdvanceUtils.notNull(recordWaveformView)) {
            postOnMainThread(() -> recordWaveformView.update(maxAmplitude));
        }
    }

    private LayoutInflater getLayoutInflater() {
        return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private void sleepThread() {
        try {
            Thread.sleep(100); // Sleep for 100ms
        } catch (InterruptedException e) {
            Log.e(tag, e.getMessage(), e);
        }
    }

    // PRIVATE CLASS

    /**
     * Before starting voice record methods
     */
    private class BSVRMethods {
        /**
         * Call source() only before format()
         */
        private void source(@NonNull MediaRecorder mediaRecorder, @NonNull Source source) {
            try {
                // Set the audio source
                // Call setAudioSource() only before setOutputFormat().
                mediaRecorder.setAudioSource(MediaValues.source(source));
            } catch (Throwable e) {
                Log.e(tag, e.getMessage(), e);
                String message = e.getMessage();
                postToCallback(listener -> listener.onError(!AdvanceUtils.isNull(message) ? message : "An error has occurred while setting the source."));
            }
        }

        /**
         * Call format() after source() but only before encoder()
         */
        private void format(@NonNull MediaRecorder mediaRecorder, @NonNull OutputFormat format) {
            try {
                // Set the format
                // Call setOutputFormat() after setAudioSource()/setVideoSource() but before prepare().
                mediaRecorder.setOutputFormat(MediaValues.format(format));
            } catch (Throwable e) {
                Log.e(tag, e.getMessage(), e);
                String message = e.getMessage();
                postToCallback(listener -> listener.onError(!AdvanceUtils.isNull(message) ? message : "An error has occurred while setting the format."));
            }
        }

        /**
         * Call encoder() after format()
         */
        private void encoder(@NonNull MediaRecorder mediaRecorder, @NonNull OutputFormat format, @NonNull Bitrate bitrate) {
            try {
                // Set the audio encoder based on audio bitrate
                // Call setAudioEncoder() after setOutputFormat() but before prepare().
                if (format == OutputFormat.THREE_GPP) {
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                } else {
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                }
            } catch (Throwable e) {
                Log.e(tag, e.getMessage(), e);
                String message = e.getMessage();
                postToCallback(listener -> listener.onError(!AdvanceUtils.isNull(message) ? message : "An error has occurred while setting the encoder."));
            }
        }

        /**
         * Call bitrate() after source(), format(), and encoder()
         */
        private void bitrate(@NonNull MediaRecorder mediaRecorder, @NonNull Bitrate bitrate) {
            try {
                // Set the Sampling rate and Encoding bitrate based on bitrate
                switch (bitrate) {
                    case LOW:
                        mediaRecorder.setAudioSamplingRate(AudioSamplingRate.RADIO);
                        mediaRecorder.setAudioEncodingBitRate(AudioBitrate.LOW);
                        break;
                    case MEDIUM_ONE:
                        mediaRecorder.setAudioSamplingRate(AudioSamplingRate.FM_RADIO);
                        mediaRecorder.setAudioEncodingBitRate(AudioBitrate.MEDIUM_ONE);
                        break;
                    case MEDIUM_TWO:
                        mediaRecorder.setAudioSamplingRate(AudioSamplingRate.CD);
                        mediaRecorder.setAudioEncodingBitRate(AudioBitrate.MEDIUM_TWO);
                        break;
                    default:
                        mediaRecorder.setAudioSamplingRate(AudioSamplingRate.PROFESSIONAL);
                        mediaRecorder.setAudioEncodingBitRate(AudioBitrate.HIGH);
                }
            } catch (Throwable e) {
                Log.e(tag, e.getMessage(), e);
                String message = e.getMessage();
                postToCallback(listener -> listener.onError(!AdvanceUtils.isNull(message) ? message : "An error has occurred while setting the bitrate."));
            }
        }

        /**
         * Call output() after format()
         */
        private void output(@NonNull MediaRecorder mediaRecorder, @NonNull String filename) {
            try {
                // Call this after setOutputFormat() but before prepare().
                mediaRecorder.setOutputFile(filename);
            } catch (Throwable e) {
                Log.e(tag, e.getMessage(), e);
                String message = e.getMessage();
                postToCallback(listener -> listener.onError(!AdvanceUtils.isNull(message) ? message : "An error has occurred while setting the output filename."));
            }
        }

        /**
         * Call maxDuration() after format()
         */
        private void maxDuration(@NonNull MediaRecorder mediaRecorder, int durationMillis) {
            try {
                mediaRecorder.setMaxDuration(durationMillis);
            } catch (Throwable e) {
                Log.e(tag, e.getMessage(), e);
                String message = e.getMessage();
                postToCallback(listener -> listener.onError(!AdvanceUtils.isNull(message) ? message : "An error has occurred while setting the output max duration."));
            }
        }

        /**
         * Call maxBytes() after format()
         */
        private void maxBytes(@NonNull MediaRecorder mediaRecorder, int sizeBytes) {
            try {
                mediaRecorder.setMaxFileSize(sizeBytes);
            } catch (Throwable e) {
                Log.e(tag, e.getMessage(), e);
                String message = e.getMessage();
                postToCallback(listener -> listener.onError(!AdvanceUtils.isNull(message) ? message : "An error has occurred while setting the output max file size."));
            }
        }
    }

    private static class MediaValues {
        private static int source(@NonNull Source source) {
            return source == Source.MIC ? AudioSource.MIC : AudioSource.DEFAULT;
        }

        private static int format(@NonNull OutputFormat format) {
            switch (format) {
                case M4A:
                    return AudioOutputFormat.M4A;
                case AAC:
                    return AudioOutputFormat.AAC;
                default:
                    return AudioOutputFormat.THREE_GPP;
            }
        }
    }

    private static class RecordTimer {
        private final long elapsedMilliseconds;
        private final long hours;
        private final long minutes;
        private final long seconds;
        private final long milliseconds;

        private RecordTimer(long recordTime) {
            elapsedMilliseconds = System.currentTimeMillis() - recordTime;
            hours = TimeUnit.MILLISECONDS.toHours(elapsedMilliseconds);
            minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMilliseconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedMilliseconds));
            seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMilliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedMilliseconds));
            milliseconds = elapsedMilliseconds - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(elapsedMilliseconds));
        }

        private int hours() {
            return (int) hours;
        }

        private int minutes() {
            return (int) minutes;
        }

        private int seconds() {
            return (int) seconds;
        }

        private int milliseconds() {
            return (int) milliseconds;
        }

        private String formatted() {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d:%d", hours, minutes, seconds, milliseconds);
        }
    }

    // INTERFACE
    @FunctionalInterface
    private interface CallbackExecutor {
        void execute(AdvanceMediaRecorderCallback listener);
    }

    // PUBLIC ENUMS
    public enum Source {
        MIC, DEFAULT
    }

    public enum OutputFormat {
        M4A, AAC, THREE_GPP
    }

    public enum State {
        INITIALIZED, RECORDING, PAUSED
    }

    public enum Bitrate {
        LOW, MEDIUM_ONE, MEDIUM_TWO, HIGH
    }
}