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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.official.senestro.core.callbacks.interfaces.VoiceRecorderCallback;
import com.official.senestro.core.callbacks.interfaces.VoiceRecorderConvertToMp3Callback;
import com.official.senestro.core.enums.VoiceRecorderBitrate;
import com.official.senestro.core.enums.VoiceRecorderConverterFormat;
import com.official.senestro.core.enums.VoiceRecorderFormat;
import com.official.senestro.core.enums.VoiceRecorderOnStopAction;
import com.official.senestro.core.enums.VoiceRecorderSource;
import com.official.senestro.core.enums.VoiceRecorderState;
import com.official.senestro.core.utils.AdvanceExecutorService;
import com.official.senestro.core.utils.AdvanceUtils;
import com.official.senestro.core.views.WaveformView;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class VoiceRecorder {
    private final Context context;
    private final Activity activity;
    private MediaRecorder recorder;
    private boolean startedRecording = false;
    private VoiceRecorderState state = VoiceRecorderState.notRecording;
    private File directory = Environment.getExternalStorageDirectory(); // The recording/records directory
    private File file; // The recording file
    private long startTime; // The time the recording wat started, it will be updated when resumed
    private long onPauseTime; // The time the recording is paused, it will be reset when resumed or stooped
    private Handler handler = new Handler(Looper.getMainLooper());
    private WaveformView waveformView;
    private VoiceRecorderCallback callback;
    private VoiceRecorderSource source = VoiceRecorderSource.MIC;
    private VoiceRecorderFormat format = VoiceRecorderFormat.AAC;
    private VoiceRecorderBitrate bitrate = VoiceRecorderBitrate.HIGH;
    /**
     * Android Build Details
     */
    private AndroidBuildDetails abd;
    /**
     * The maximum duration in ms (if zero or negative, disables the duration limit)
     */
    private int maxDuration = 0;
    /**
     * The maximum file size in bytes (if zero or negative, disables the limit)
     */
    private int maxBytes = 0;

    public VoiceRecorder(@NonNull Context context, @NonNull Activity activity) {
        this.context = context;
        this.activity = activity;
        this.recorder = newRecorder();
        this.abd = new AndroidBuildDetails();
    }

    public void setCallback(@Nullable VoiceRecorderCallback callback) {
        this.callback = callback;
    }

    public boolean startedRecording() {
        return startedRecording;
    }

    public boolean isPaused() {
        return startedRecording() && state == VoiceRecorderState.paused;
    }

    public boolean isRecording() {
        return startedRecording() && state == VoiceRecorderState.recording;
    }

    /**
     * The maximum duration in ms (if zero or negative, disables the duration limit)
     */
    public void setMaxDuration(int durationMillis) {
        this.maxDuration = durationMillis;
    }

    /**
     * The maximum file size in bytes (if zero or negative, disables the limit)
     */
    public void setMaxBytes(int sizeBytes) {
        this.maxBytes = sizeBytes;
    }

    public void startRecord(@Nullable File directory, @NonNull VoiceRecorderSource source, @NonNull VoiceRecorderFormat format, @NonNull VoiceRecorderBitrate bitrate, @Nullable FrameLayout waveformViewLayout) {
        AdvanceExecutorService.runInBackground(() -> {
            if (!startedRecording()) {
                String directoryPath = this.directory.getAbsolutePath();
                String filename = newFilename(directory == null ? directoryPath : (directory.isDirectory() ? directory.getAbsolutePath() : directoryPath), format);
                if (filename != null) {
                    try {
                        AdvanceUtils.createFile(filename);
                        if (AdvanceUtils.isFile(filename)) {
                            newRecording(filename, source, format, bitrate, waveformViewLayout);
                        } else {
                            postToCallback(listener -> listener.onError("The recording file doesn't exist, it must exist before starting a new recording."));
                        }
                    } catch (Throwable e) {
                        startedRecording = false;
                        recorder = null;
                        e.printStackTrace();
                        String message = e.getMessage();
                        postToCallback(listener -> listener.onError(message != null ? message : "An error has occurred while starting the voice recording."));
                    }
                } else {
                    postToCallback(listener -> listener.onError("Unable to generate the voice recording output filename."));
                }
            } else {
                postToCallback(listener -> listener.onMessage("A voice recording has already been started."));
            }
        });
    }

    public void startRecord(@NonNull VoiceRecorderSource source, @NonNull VoiceRecorderFormat format, @NonNull VoiceRecorderBitrate bitrate) {
        startRecord(null, source, format, bitrate, null);
    }

    public void startRecord(@NonNull VoiceRecorderFormat format, @NonNull VoiceRecorderBitrate bitrate) {
        startRecord(null, this.source, format, bitrate, null);
    }

    public void startRecord(@NonNull File directory) {
        startRecord(directory, this.source, this.format, this.bitrate, null);
    }

    public void startRecord(@NonNull VoiceRecorderSource source) {
        startRecord(null, source, this.format, this.bitrate, null);
    }

    public void startRecord(@NonNull VoiceRecorderFormat format) {
        startRecord(null, this.source, format, this.bitrate, null);
    }

    public void startRecord(@NonNull VoiceRecorderBitrate bitrate) {
        startRecord(null, this.source, this.format, bitrate, null);
    }

    public void startRecord(@NonNull FrameLayout layout) {
        startRecord(null, this.source, this.format, this.bitrate, layout);
    }

    public void startRecord(@NonNull File directory, @NonNull VoiceRecorderSource source) {
        startRecord(directory, source, this.format, this.bitrate, null);
    }

    public void startRecord(@NonNull File directory, @NonNull VoiceRecorderFormat format) {
        startRecord(directory, this.source, format, this.bitrate, null);
    }

    public void startRecord(@NonNull File directory, @NonNull VoiceRecorderBitrate bitrate) {
        startRecord(directory, this.source, this.format, bitrate, null);
    }

    public void startRecord(@NonNull VoiceRecorderSource source, @NonNull VoiceRecorderFormat format) {
        startRecord(null, source, format, this.bitrate, null);
    }

    public void startRecord(@NonNull VoiceRecorderSource source, @NonNull VoiceRecorderBitrate bitrate) {
        startRecord(null, source, this.format, bitrate, null);
    }

    public void startRecord() {
        startRecord(null, this.source, this.format, this.bitrate, null);
    }

    public void pauseRecord() {
        AdvanceExecutorService.runInBackground(() -> {
            if (isRecording()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    try {
                        recorder.pause();
                        state = VoiceRecorderState.paused;
                        onPauseTime = System.currentTimeMillis();
                        postToCallback(VoiceRecorderCallback::onPaused);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        String message = e.getMessage();
                        postToCallback(listener -> listener.onError(message != null ? message : "An error has occurred while pausing the recorder."));
                    }
                } else {
                    postToCallback(listener -> listener.onMessage("You can't pause voice recording on this device, your device API level is: " + Build.VERSION.SDK_INT + " (" + abd.getVersionNameFromApiLevel(Build.VERSION.SDK_INT) + "). You must be using API level: " + Build.VERSION_CODES.N + " (" + abd.getVersionNameFromApiLevel(Build.VERSION_CODES.N) + ") or greater."));
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
                        recorder.resume();
                        state = VoiceRecorderState.recording;
                        startTime += System.currentTimeMillis() - onPauseTime;
                        onPauseTime = 0;
                        postToCallback(VoiceRecorderCallback::onResumed);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        String message = e.getMessage();
                        postToCallback(listener -> listener.onError(message != null ? message : "An error has occurred while resuming the recorder."));
                    }
                } else {
                    postToCallback(listener -> listener.onMessage("You can't resume voice recording on this device, your device API level is: " + Build.VERSION.SDK_INT + " (" + abd.getCodeNameFromApiLevel(Build.VERSION.SDK_INT) + "). You must be using API level: " + Build.VERSION_CODES.N + " (" + abd.getCodeNameFromApiLevel(Build.VERSION_CODES.N) + ") or greater."));
                }
            } else {
                postToCallback(listener -> listener.onMessage("You can't resume a voice record if no voice recording is started and paused."));
            }
        });
    }

    public void stopRecord(@NonNull VoiceRecorderOnStopAction action) {
        AdvanceExecutorService.runInBackground(() -> {
            if (startedRecording()) {
                try {
                    recorder.stop();
                    recorder.reset();
                    recorder.release();
                    reset(false);
                    String filename = file.getAbsolutePath();
                    file = null;
                    postToCallback(listener -> listener.onStopped(filename));
                    executeAction(action, filename);
                    resetWaveform();
                } catch (Throwable e) {
                    e.printStackTrace();
                    String message = e.getMessage();
                    postToCallback(listener -> listener.onError(message != null ? message : "An error has occurred while stopping the recorder."));
                }
            } else {
                postToCallback(listener -> listener.onMessage("You can't stop a voice record if no voice recording is started."));
            }
        });
    }

    public void stopRecord() {
        stopRecord(VoiceRecorderOnStopAction.BROADCAST_RECORD);
    }

    // PRIVATE
    private void newRecording(@NonNull String createFilename, @NonNull VoiceRecorderSource source, @NonNull VoiceRecorderFormat format, @NonNull VoiceRecorderBitrate bitrate, @Nullable FrameLayout waveformViewLayout) throws Throwable {
        file = new File(createFilename);
        AdvanceUtils.createFile(file);
        recorder = newRecorder();
        BSVRMethods methods = new BSVRMethods();
        methods.source(recorder, source);
        methods.format(recorder, format);
        methods.maxDuration(recorder, maxDuration);
        methods.maxBytes(recorder, maxBytes);
        methods.encoder(recorder, format, bitrate);
        methods.bitrate(recorder, bitrate);
        methods.output(recorder, createFilename);
        handleRecorderError(recorder, createFilename);
        handleRecorderInfo(recorder, createFilename);
        recorder.prepare();
        recorder.start();
        startedRecording = true;
        state = VoiceRecorderState.recording;
        startTime = System.currentTimeMillis();
        postToCallback(listener -> listener.onStarted(createFilename));
        newWaveformView(waveformViewLayout);
        postOnMainThread(() -> handler.post(new RecorderRunnable(createFilename)));
    }

    private void reset(boolean withFile) {
        recorder = null;
        startedRecording = false;
        state = VoiceRecorderState.notRecording;
        handler.removeCallbacksAndMessages(null);
        startTime = onPauseTime = 0;
        if (withFile) {
            file = null;
        }
    }

    @SuppressLint("InflateParams")
    public void newWaveformView(@Nullable FrameLayout layout) {
        if (layout != null) {
            waveformView = new WaveformView(context);
            waveformView.setColor(Color.BLUE);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
            waveformView.setLayoutParams(layoutParams);
            postOnMainThread(() -> {
                layout.removeAllViews();
                layout.addView(waveformView);
            });
            postToCallback(listener -> listener.onMessage("Voice recording waveform view has been added to layout (FrameLayout)."));
        }
    }

    @NonNull
    @Contract(" -> new")
    private MediaRecorder newRecorder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new MediaRecorder(context);
        } else {
            return new MediaRecorder();
        }
    }

    private void handleRecorderError(@NonNull MediaRecorder mediaRecorder, @NonNull String filename) {
        mediaRecorder.setOnErrorListener((mr, what, extra) -> {
            switch (what) {
                case MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN:
                    postToCallback(listener -> listener.onError("Unspecified error message."));
                    break;
                case MediaRecorder.MEDIA_ERROR_SERVER_DIED:
                    mediaRecorder.release();
                    reset(true);
                    resetWaveform();
                    postToCallback(listener -> listener.onError("Media server died. The record was auto stopped with no action."));
                    postToCallback(listener -> listener.onStopped(filename));
                    break;
                default:
                    break;
            }
        });
    }

    private void handleRecorderInfo(@NonNull MediaRecorder mediaRecorder, @NonNull String filename) {
        mediaRecorder.setOnInfoListener((mr, what, extra) -> {
            switch (what) {
                case MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN:
                    postToCallback(listener -> listener.onMessage("Unspecified info message."));
                    break;
                case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
                    reset(true);
                    resetWaveform();
                    postToCallback(listener -> listener.onMessage("A maximum duration had been setup and has now been reached. The record was auto stopped with no action."));
                    postToCallback(listener -> listener.onStopped(filename));
                    break;
                case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
                    reset(true);
                    resetWaveform();
                    postToCallback(listener -> listener.onMessage("A maximum file size had been setup and has now been reached. The record was auto stopped with no action."));
                    postToCallback(listener -> listener.onStopped(filename));
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
    private String newConvertFilename(@NonNull String filename, @NonNull VoiceRecorderConverterFormat format) {
        if (AdvanceUtils.isFile(filename)) {
            String extension = getConvertExtension(format);
            if (extension != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US);
                return AdvanceUtils.removeExtension(filename).concat(extension).toLowerCase();
            }
        }
        return null;
    }

    @Nullable
    @Contract(pure = true)
    private String getConvertExtension(@NonNull VoiceRecorderConverterFormat format) {
        switch (format) {
            case MP3:
                return ".mp3";
            case WAV:
                return ".wav";
            case FLAC:
                return ".flac";
            case OGG:
                return ".ogg";
            default:
                return null;
        }
    }

    @Nullable
    private String newFilename(@NonNull String directory, @NonNull VoiceRecorderFormat format) {
        if (AdvanceUtils.isDirectory(directory)) {
            String extension = getExtension(format);
            if (extension != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US);
                return directory + File.separator + "voice_record_".concat(dateFormat.format(new Date()).concat(extension)).toLowerCase();
            }
        }
        return null;
    }

    @Nullable
    @Contract(pure = true)
    private String getExtension(@NonNull VoiceRecorderFormat format) {
        switch (format) {
            case M4A:
                return ".m4a";
            case AAC:
                return ".aac";
            case THREE_3GPP:
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
                postToCallback(listener -> listener.onBroadcast(filename));
            } else {
                postToCallback(listener -> listener.onMessage("Broadcast can't be sent as the filename isn't a valid file (" + filename + ")."));
            }
        });
    }

    private String getConvertCmd(@NonNull VoiceRecorderConverterFormat format, @NonNull String oldFilename, @NonNull String newFilename) {
        HashMap<VoiceRecorderConverterFormat, String> commands = new HashMap<>();
        commands.put(VoiceRecorderConverterFormat.MP3, String.format("-err_detect ignore_err -i %s -codec:a libmp3lame -qscale:a 2 %s", oldFilename, newFilename));
        commands.put(VoiceRecorderConverterFormat.OGG, String.format("-err_detect ignore_err -i %s -codec:a libvorbis -qscale:a 5 %s", oldFilename, newFilename));
        commands.put(VoiceRecorderConverterFormat.WAV, String.format("-err_detect ignore_err -i %s -codec:a pcm_s16le %s", oldFilename, newFilename));
        commands.put(VoiceRecorderConverterFormat.FLAC, String.format("-err_detect ignore_err -i %s -codec:a flac %s", oldFilename, newFilename));
        return commands.get(format);
    }

    private void convertRecord(@NonNull String filename, @Nullable VoiceRecorderConverterFormat format, @NonNull VoiceRecorderConvertToMp3Callback callback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            callback.onError("You must be running on " + abd.getVersionNameFromApiLevel(Build.VERSION_CODES.N) + " or greater to convert the recorded file.");
        } else if (format == null) {
            callback.onError("The format of conversion must not be null.");
        } else {
            String newFilename = newConvertFilename(filename, format);
            if (newFilename == null) {
                callback.onError("Failed to create the conversion filename.");
            } else {
                String command = getConvertCmd(format, filename, newFilename);
                if (command == null) {
                    callback.onError("Failed to get the conversion command.");
                } else {
                    FFmpegKit.executeAsync(command, session -> {
                        if (ReturnCode.isSuccess(session.getReturnCode())) {
                            callback.onConverted(filename, newFilename);
                        } else {
                            callback.onError("Failed to convert the recorded file.");
                        }
                    }, null, null);
                }
            }
        }
    }

    private void resetWaveform() {
        if (waveformView != null) {
            postOnMainThread(() -> waveformView.reset());
        }
    }

    private void drawWaveform(int maxAmplitude) {
        if (waveformView != null) {
            postOnMainThread(() -> waveformView.draw(maxAmplitude));
        }
    }

    private LayoutInflater getLayoutInflater() {
        return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Nullable
    @Contract(pure = true)
    private VoiceRecorderConverterFormat getConvertFormatFromAction(@NonNull VoiceRecorderOnStopAction action) {
        switch (action) {
            case CONVERT_TO_FLAC_AND_BROADCAST:
            case CONVERT_TO_FLAC:
                return VoiceRecorderConverterFormat.FLAC;
            case CONVERT_TO_OGG_AND_BROADCAST:
            case CONVERT_TO_OGG:
                return VoiceRecorderConverterFormat.OGG;
            case CONVERT_TO_WAV_AND_BROADCAST:
            case CONVERT_TO_WAV:
                return VoiceRecorderConverterFormat.WAV;
            case CONVERT_TO_MP3_AND_BROADCAST:
            case CONVERT_TO_MP3:
                return VoiceRecorderConverterFormat.MP3;
            default:
                return null;
        }
    }

    private void executeAction(@NonNull VoiceRecorderOnStopAction action, @NonNull String filename) {
        switch (action) {
            case BROADCAST_RECORD:
                sendBroadcast(filename);
                break;
            case DELETE_RECORD:
                AdvanceUtils.delete(filename);
                break;
            case CONVERT_TO_FLAC_AND_BROADCAST:
            case CONVERT_TO_OGG_AND_BROADCAST:
            case CONVERT_TO_WAV_AND_BROADCAST:
            case CONVERT_TO_MP3_AND_BROADCAST:
                convertRecord(filename, getConvertFormatFromAction(action), new ConvertToMp3Callback(true));
                break;
            case CONVERT_TO_FLAC:
            case CONVERT_TO_OGG:
            case CONVERT_TO_WAV:
            case CONVERT_TO_MP3:
                convertRecord(filename, getConvertFormatFromAction(action), new ConvertToMp3Callback(false));
                break;
            default:
                break;
        }
    }

    // PRIVATE CLASS
    private class RecorderRunnable implements Runnable {
        private final String filename;

        private RecorderRunnable(@NonNull String filename) {
            this.filename = filename;
        }

        private String format2Digits(int number) {
            return String.format(Locale.US, "%02d", number);
        }

        @Override
        public void run() {
            if (isRecording()) {
                try {
                    long elapsed = System.currentTimeMillis() - startTime;
                    TimeMillis millis = new TimeMillis(elapsed);
                    int hours = millis.hours();
                    int minutes = millis.minutes();
                    int seconds = millis.seconds();
                    int milliseconds = millis.milliseconds();
                    String formatted = format2Digits(hours) + ":" + format2Digits(minutes) + ":" + format2Digits(seconds) + ":" + format2Digits(milliseconds);
                    postToCallback(listener -> listener.onTimeUpdate(filename, hours, minutes, seconds, milliseconds, formatted));
                    if (recorder != null) {
                        int maxAmplitude = recorder.getMaxAmplitude();
                        drawWaveform(maxAmplitude);
                        // Normalize the amplitude value to a percentage
                        int normalizedAmplitude = ((maxAmplitude * 100) / 32767);
                        postToCallback(listener -> listener.onAmplitudes(filename, maxAmplitude, normalizedAmplitude));
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    String message = e.getMessage();
                    postToCallback(listener -> listener.onError(message != null ? message : "An error has occurred on the record runnable."));
                }
            }
            handler.postDelayed(this, 100);
        }
    }

    /**
     * Before starting voice record methods
     */
    private class BSVRMethods {
        /**
         * Call source() only before format()
         */
        private void source(@NonNull MediaRecorder mediaRecorder, @NonNull VoiceRecorderSource source) {
            try {
                // Set the audio source
                // Call setAudioSource() only before setOutputFormat().
                switch (source) {
                    case CAMCORDER:
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                        break;
                    case MIC:
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        break;
                    case VOICE_CALL:
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
                        break;
                    case VOICE_RECOGNITION:
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
                        break;
                    default:
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
                        break;
                }
            } catch (Throwable e) {
                e.printStackTrace();
                String message = e.getMessage();
                postToCallback(listener -> listener.onError(message != null ? message : "An error has occurred while setting the source."));
            }
        }

        /**
         * Call format() after source() but only before encoder()
         */
        private void format(@NonNull MediaRecorder mediaRecorder, @NonNull VoiceRecorderFormat format) {
            try {
                // Set the format
                // Call setOutputFormat() after setAudioSource()/setVideoSource() but before prepare().
                switch (format) {
                    case M4A:
                        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                        break;
                    case AAC:
                        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
                        break;
                    default:
                        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                String message = e.getMessage();
                postToCallback(listener -> listener.onError(message != null ? message : "An error has occurred while setting the format."));
            }
        }

        /**
         * Call encoder() after format()
         */
        private void encoder(@NonNull MediaRecorder mediaRecorder, @NonNull VoiceRecorderFormat format, @NonNull VoiceRecorderBitrate bitrate) {
            try {
                // Set the audio encoder based on audio bitrate
                // Call setAudioEncoder() after setOutputFormat() but before prepare().
                if (format == VoiceRecorderFormat.THREE_3GPP) {
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                } else {
                    switch (bitrate) {
                        case VERY_HIGH:
                        case VERY_VERY_HIGH:
                            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
                            break;
                        default:
                            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                            break;
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
                String message = e.getMessage();
                postToCallback(listener -> listener.onError(message != null ? message : "An error has occurred while setting the encoder."));
            }
        }

        /**
         * Call bitrate() after source(), format(), and encoder()
         */
        private void bitrate(@NonNull MediaRecorder mediaRecorder, @NonNull VoiceRecorderBitrate bitrate) {
            try {
                // Set the Sampling rate and Encoding bitrate based on bitrate
                switch (bitrate) {
                    case VERY_LOW:
                        mediaRecorder.setAudioSamplingRate(SamplingRate.RADIO);
                        mediaRecorder.setAudioEncodingBitRate(Bitrate.VERY_LOW);
                        break;
                    case LOW:
                        mediaRecorder.setAudioSamplingRate(SamplingRate.RADIO);
                        mediaRecorder.setAudioEncodingBitRate(Bitrate.LOW);
                        break;
                    case MEDIUM_ONE:
                        mediaRecorder.setAudioSamplingRate(SamplingRate.FM_RADIO);
                        mediaRecorder.setAudioEncodingBitRate(Bitrate.MEDIUM_ONE);
                        break;
                    case MEDIUM_TWO:
                        mediaRecorder.setAudioSamplingRate(SamplingRate.CD);
                        mediaRecorder.setAudioEncodingBitRate(Bitrate.MEDIUM_TWO);
                        break;
                    case HIGH:
                        mediaRecorder.setAudioSamplingRate(SamplingRate.PROFESSIONAL);
                        mediaRecorder.setAudioEncodingBitRate(Bitrate.HIGH);
                        break;
                    case VERY_HIGH:
                        mediaRecorder.setAudioSamplingRate(SamplingRate.HIGH);
                        mediaRecorder.setAudioEncodingBitRate(Bitrate.VERY_HIGH);
                        break;
                    default:
                        mediaRecorder.setAudioSamplingRate(SamplingRate.VERY_HIGH);
                        mediaRecorder.setAudioEncodingBitRate(Bitrate.VERY_VERY_HIGH);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                String message = e.getMessage();
                postToCallback(listener -> listener.onError(message != null ? message : "An error has occurred while setting the bitrate."));
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
                e.printStackTrace();
                String message = e.getMessage();
                postToCallback(listener -> listener.onError(message != null ? message : "An error has occurred while setting the output filename."));
            }
        }

        /**
         * Call maxDuration() after format()
         */
        private void maxDuration(@NonNull MediaRecorder mediaRecorder, int durationMillis) {
            try {
                mediaRecorder.setMaxDuration(durationMillis);
            } catch (Throwable e) {
                e.printStackTrace();
                String message = e.getMessage();
                postToCallback(listener -> listener.onError(message != null ? message : "An error has occurred while setting the output max duration."));
            }
        }

        /**
         * Call maxBytes() after format()
         */
        private void maxBytes(@NonNull MediaRecorder mediaRecorder, int sizeBytes) {
            try {
                mediaRecorder.setMaxFileSize(sizeBytes);
            } catch (Throwable e) {
                e.printStackTrace();
                String message = e.getMessage();
                postToCallback(listener -> listener.onError(message != null ? message : "An error has occurred while setting the output max file size."));
            }
        }
    }

    private class TimeMillis {
        private final long elapsed;

        private TimeMillis(long elapsed) {
            this.elapsed = elapsed;
        }

        /**
         * Divide the elapsed time milliseconds by 1 hour ((1000 * 60) * 60 = 3,600,000)
         */
        private int hours() {
            return startedRecording() && elapsed >= 1 ? (int) (elapsed / ((1000 * 60) * 60)) : 0;
        }

        /**
         * Divide the elapsed time milliseconds by 1 minute (1000 * 60 = 60,000) and make it a round of 60 minutes (60,000 % 60)
         * That's the minutes reset it counting for every 60 minutes
         */
        private int minutes() {
            return startedRecording() && elapsed >= 1 ? (int) ((elapsed / (1000 * 60)) % 60) : 0;
        }

        /**
         * Divide the elapsed time milliseconds by 1 second (1000) and make it a round of 60 seconds (1000 % 60)
         * That's the seconds reset it counting for every 60 seconds
         */
        private int seconds() {
            return startedRecording() && elapsed >= 1 ? (int) ((elapsed / 1000) % 60) : 0;
        }

        /**
         * Rounds the elapsed time milliseconds by 1 milliseconds (1000)
         */
        private int milliseconds() {
            return startedRecording() && elapsed >= 1 ? (int) (elapsed % 1000) : 0;
        }
    }

    private static class Bitrate {
        /**
         * 64 kbps
         */
        private final static int VERY_LOW = 64000;

        /**
         * 96 kbps
         */
        private final static int LOW = 96000;

        /**
         * 128 kbps
         */
        private final static int MEDIUM_ONE = 128000;

        /**
         * 160 kbps
         */
        private final static int MEDIUM_TWO = 160000;

        /**
         * 192 kbps
         */
        private final static int HIGH = 192000;

        /**
         * 256 kbps
         */
        private final static int VERY_HIGH = 256000;

        /**
         * 320 kbps
         */
        private final static int VERY_VERY_HIGH = 320000;
    }

    private static class SamplingRate {
        /**
         * 8000 Hz (Telephone quality)
         */
        private final static int TELEPHONE = 8000;

        /**
         * 11025 Hz (Low quality voice)
         */
        private final static int LOW_Q_VOICE = 11025;

        /**
         * 16000 Hz (Wideband voice)
         */
        private final static int BETTER_TELEPHONE = 16000;

        /**
         * 16000 Hz (Radio voice)
         */
        private final static int RADIO = 22050;

        /**
         * 16000 Hz (FM radio voice)
         */
        private final static int FM_RADIO = 32000;

        /**
         * 44100 Hz (CD quality)
         */
        private final static int CD = 44100;

        /**
         * 48000 Hz (Professional audio/video quality)
         */
        private final static int PROFESSIONAL = 48000;

        /**
         * 96000 Hz (High-resolution audio quality)
         */
        private final static int HIGH = 96000;

        /**
         * 96000 Hz (Very high-resolution audio quality)
         */
        private final static int VERY_HIGH = 192000;
    }

    private class ConvertToMp3Callback implements VoiceRecorderConvertToMp3Callback {
        private final boolean broadcast;

        private ConvertToMp3Callback(boolean broadcast) {
            this.broadcast = broadcast;
        }

        private void broadcast(@NonNull String filename) {
            if (broadcast) {
                sendBroadcast(filename);
            }
        }

        @Override
        public void onError(@NonNull String message) {
            postToCallback(listener -> listener.onError(message));
        }

        @Override
        public void onConverted(@NonNull String oldFilename, @NonNull String newFilename) {
            broadcast(oldFilename);
            broadcast(newFilename);
            postToCallback(listener -> listener.onConverted(oldFilename, newFilename));
        }
    }

    // INTERFACE
    @FunctionalInterface
    private interface CallbackExecutor {
        void execute(VoiceRecorderCallback listener);
    }
}