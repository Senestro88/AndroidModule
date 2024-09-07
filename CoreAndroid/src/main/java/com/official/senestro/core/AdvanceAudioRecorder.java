package com.official.senestro.core;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.official.senestro.core.callbacks.interfaces.AdvanceAudioRecorderCallback;
import com.official.senestro.core.classes.audio.AudioSamplingRate;
import com.official.senestro.core.classes.audio.AudioSource;
import com.official.senestro.core.utils.AdvanceUtils;
import com.official.senestro.core.views.AudioRecordWaveformView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AdvanceAudioRecorder {
    private final String tag = AdvanceAudioRecorder.class.getName();
    private final Context context;
    private final Activity activity;
    private AudioRecord audioRecord;
    private boolean startedRecording = false;
    private State state = State.INITIALIZED;
    private File dir = Environment.getExternalStorageDirectory(); // The recording/records directory
    private File file; // The recording file
    private EndlessTask endlessTaskHandler;
    private AdvanceAudioRecorderCallback callback;
    private AudioRecordWaveformView recordWaveformView;
    private long recordTime; // The time the recording was started, it will be updated when resumed
    private long onPauseTime; // The time the recording is paused, it will be reset when resumed or stooped

    private int samplingRateInt;
    private int channelConfigInt;
    private int encodingFormatInt;
    private int bufferBytesInt;

    private Source sourceEnum;
    private Channel channelEnum;
    private SamplingRate samplingRateEnum;
    private EncodingFormat encodingFormatEnum;

    public AdvanceAudioRecorder(@NonNull Context context, @NonNull Activity activity) {
        this.context = context;
        this.activity = activity;
        this.endlessTaskHandler = new EndlessTask(100);
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

    public void setCallback(@Nullable AdvanceAudioRecorderCallback callback) {
        this.callback = callback;
    }

    public void setDirectory(@NonNull String directory) {
        if (AdvanceUtils.isDirectory(directory)) {
            dir = new File(directory);
        }
    }

    public void setWaveformView(@NonNull FrameLayout waveformViewLayout) {
        newWaveformView(waveformViewLayout);
    }

    public void startRecord(@NonNull Source source, @NonNull Channel channel, @NonNull SamplingRate samplingRate, @NonNull EncodingFormat encodingFormat) {
        AdvanceExecutorService.runInBackground(() -> {
            if (!startedRecording) {
                try {
                    String filename = createPcmFilename();
                    if (AdvanceUtils.notNull(filename)) {
                        sourceEnum = source;
                        channelEnum = channel;
                        samplingRateEnum = samplingRate;
                        encodingFormatEnum = encodingFormat;
                        samplingRateInt = AudioValues.samplingRate(samplingRate); // Sample rate in Hz
                        channelConfigInt = AudioValues.channel(channel);
                        encodingFormatInt = AudioValues.encodingFormat(encodingFormat);
                        bufferBytesInt = AudioRecord.getMinBufferSize(samplingRateInt, channelConfigInt, encodingFormatInt);
                        audioRecord = new AudioRecord(AudioValues.source(source), samplingRateInt, channelConfigInt, encodingFormatInt, bufferBytesInt);
                        audioRecord.startRecording();
                        startedRecording = true;
                        state = State.RECORDING;
                        recordTime = System.currentTimeMillis();
                        file = new File(filename);
                        postToCallback(listener -> listener.onStarted(file));
                        endlessTaskHandler.start(() -> {
                            if (startedRecording && isRecording()) {
                                byte[] audioData = new byte[bufferBytesInt];
                                int readSize = audioRecord.read(audioData, 0, bufferBytesInt);
                                if (readSize > 0) {
                                    // Amplitude
                                    double amplitude = calculateAmplitude(audioData, readSize);
                                    double normalized = normalizedAmplitude(amplitude);
                                    drawWaveform((int) amplitude);
                                    postToCallback(listener -> listener.onAmplitude(amplitude, normalized));
                                    // PCM Byte
                                    savePcmByte(filename, audioData);
                                    postToCallback(listener -> listener.onPcmByte(audioData));
                                    // Time
                                    RecordTimer timer = new RecordTimer(recordTime);
                                    postToCallback(listener -> listener.onTimeUpdate(timer.hours(), timer.minutes(), timer.seconds(), timer.milliseconds(), timer.formatted()));
                                }
                            }
                        });
                    } else {
                        postToCallback(listener -> listener.onError("Unable to generate the record filename."));
                    }
                } catch (Throwable e) {
                    Log.e(tag, e.getMessage(), e);
                    String message = e.getMessage();
                    postToCallback(listener -> listener.onError(!AdvanceUtils.isNull(message) ? message : "An error has occurred while starting the audio record."));
                }
            } else {
                postToCallback(listener -> listener.onMessage("Can't start a audio record, an active record id found."));
            }
        });
    }

    public void startRecord(@NonNull Source source, @NonNull Channel channel, @NonNull SamplingRate samplingRate) {
        startRecord(source, channel, samplingRate, EncodingFormat.PCM_16_BIT);
    }

    public void startRecord(@NonNull Source source, @NonNull Channel channel) {
        startRecord(source, channel, SamplingRate.CD, EncodingFormat.PCM_16_BIT);
    }

    public void startRecord(@NonNull Source source) {
        startRecord(source, Channel.MONO, SamplingRate.CD, EncodingFormat.PCM_16_BIT);
    }

    public void startRecord() {
        startRecord(Source.MIC, Channel.MONO, SamplingRate.CD, EncodingFormat.PCM_16_BIT);
    }

    public void stopRecord() {
        AdvanceExecutorService.runInBackground(() -> {
            if (startedRecording) {
                try {
                    audioRecord.stop();
                    audioRecord.release();
                    audioRecord = null;
                    startedRecording = false;
                    state = State.INITIALIZED;
                    RecordTimer timer = new RecordTimer(recordTime);
                    recordTime = onPauseTime = 0;
                    endlessTaskHandler.stop();
                    resetWaveform();
                    int channelsCount = getChannelsCount(channelEnum);
                    int bitsPerSample = getBitPerSample(encodingFormatEnum);
                    int bitRate = samplingRateInt * channelsCount * bitsPerSample;
                    postToCallback(listener -> listener.onStopped(file, samplingRateInt, channelsCount, bufferBytesInt, bitsPerSample, bitRate, timer.hours(), timer.minutes(), timer.seconds(), timer.milliseconds(), timer.formatted()));
                } catch (Throwable e) {
                    Log.e(tag, e.getMessage(), e);
                    String message = e.getMessage();
                    postToCallback(listener -> listener.onError(!AdvanceUtils.isNull(message) ? message : "An error has occurred while stopping the audio record."));
                }
            } else {
                postToCallback(listener -> listener.onMessage("You can not stop a audio record if no audio record is started."));
            }
        });
    }

    public void pauseRecord() {
        AdvanceExecutorService.runInBackground(() -> {
            if (isRecording()) {
                state = State.PAUSED;
                onPauseTime = System.currentTimeMillis();
                postToCallback(AdvanceAudioRecorderCallback::onPaused);
            } else {
                postToCallback(listener -> listener.onMessage("You can't pause a audio record if no audio recording is started."));
            }
        });
    }

    public void resumeRecord() {
        AdvanceExecutorService.runInBackground(() -> {
            if (isPaused()) {
                state = State.RECORDING;
                recordTime += System.currentTimeMillis() - onPauseTime;
                onPauseTime = 0;
                postToCallback(AdvanceAudioRecorderCallback::onResumed);
            } else {
                postToCallback(listener -> listener.onMessage("You can't resume a audio record if no audio recording is started and paused."));
            }
        });
    }

    // PRIVATE METHODS

    private void postOnMainThread(@NonNull Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    private void postToCallback(@NonNull CallbackExecutor executor) {
        if (callback != null) {
            postOnMainThread(() -> executor.execute(callback));
        }
    }

    private void reset() {
        audioRecord = null;
        startedRecording = false;
        state = State.INITIALIZED;
        dir = Environment.getExternalStorageDirectory();
        file = null;
        callback = null;
        recordTime = onPauseTime = 0;
        recordWaveformView = null;
        samplingRateInt = channelConfigInt = encodingFormatInt = bufferBytesInt = -1;
        sourceEnum = null;
        channelEnum = null;
        samplingRateEnum = null;
    }

    private double calculateAmplitude(byte[] audioData, int readSize) {
        double sum = 0;
        if (encodingFormatInt == 8) {
            for (int i = 0; i < readSize; i++) {
                // Convert unsigned byte to signed value (centered around 0)
                int sample = audioData[i] & 0xFF;  // Convert byte to unsigned int
                sample -= 128;  // Convert to signed 8-bit range [-128, 127]
                sum += sample * sample;
            }
            return Math.sqrt(sum / readSize);
        } else {
            for (int i = 0; i < readSize / 2; i++) {
                // Convert 2 bytes to a 16-bit signed integer (little-endian)
                int lowByte = audioData[2 * i];
                int highByte = audioData[2 * i + 1];
                int sample = (highByte << 8) | (lowByte & 0xFF);  // Combine bytes
                // Adjust for signed 16-bit PCM (ensure correct sign)
                if (sample > 32767) {
                    sample -= 65536;  // Adjust for negative values in 16-bit PCM
                }
                sum += sample * sample;
            }
            return Math.sqrt(sum / ((double) readSize / 2));
        }
    }

    private double normalizedAmplitude(byte[] audioData, int readSize) {
        double amplitude = calculateAmplitude(audioData, readSize);
        if (encodingFormatInt == 8) {
            // Normalize RMS value to range [0, 1]
            return amplitude / 128.0;  // Maximum possible amplitude for 8-bit PCM
        } else {
            // Normalize RMS value to range [0, 1]
            return amplitude / 32768.0;  // Maximum possible amplitude for 16-bit PCM
        }
    }

    private double normalizedAmplitude(double amplitude) {
        if (encodingFormatInt == 8) {
            // Normalize RMS value to range [0, 1]
            return amplitude / 128.0;  // Maximum possible amplitude for 8-bit PCM
        } else {
            // Normalize RMS value to range [0, 1]
            return amplitude / 32768.0;  // Maximum possible amplitude for 16-bit PCM
        }
    }

    private byte[] getPcmByte(@NonNull short[] buffer, int read) {
        byte[] bufferByte = new byte[read * 2];
        for (int i = 0; i < read; i++) {
            bufferByte[i * 2] = (byte) (buffer[i] & 0x00FF);
            bufferByte[i * 2 + 1] = (byte) ((buffer[i] & 0xFF00) >> 8);
        }
        return bufferByte;
    }

    private void savePcmByte(@NonNull String filename, @NonNull byte[] pcmByte) {
        try (FileOutputStream stream = new FileOutputStream(filename, true)) {
            // Write the byte array to the file
            stream.write(pcmByte);
        } catch (Throwable e) {
            Log.e(tag, e.getMessage(), e);
            String message = e.getMessage();
            postToCallback(listener -> listener.onError(!AdvanceUtils.isNull(message) ? message : "An error has occurred while saving raw pcm data to: " + filename));
        }
    }

    private void newWaveformView(@Nullable FrameLayout waveformViewLayout) {
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
            postToCallback(listener -> listener.onMessage("Waveform view has been added."));
        }
    }

    private void resetWaveform() {
        if (AdvanceUtils.notNull(recordWaveformView)) {
            postOnMainThread(() -> recordWaveformView.recreate());
        }
    }

    private void drawWaveform(int amplitude) {
        if (AdvanceUtils.notNull(recordWaveformView)) {
            postOnMainThread(() -> recordWaveformView.update(amplitude));
        }
    }

    @Nullable
    private String createPcmFilename() {
        if (AdvanceUtils.isDirectory(dir)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US);
            return dir + File.separator + "voice_record_".concat(dateFormat.format(new Date()).concat(".".concat("pcm"))).toLowerCase();
        }
        return null;
    }

    private String getStringValue(@Nullable String string, @NonNull String defaultValue) {
        return AdvanceUtils.isNull(string) ? defaultValue : string;
    }

    private String getStringValue(@Nullable String string) {
        return AdvanceUtils.isNull(string) ? "" : string;
    }

    private String getPath(@Nullable File file) {
        return AdvanceUtils.notNull(file) && AdvanceUtils.isFile(file) ? file.getPath() : "";
    }

    private int getChannelsCount(@NonNull Channel channel) {
        // Mono is 1, while stereo is 2
        return channel == Channel.MONO ? 1 : 2;
    }

    private int getBitPerSample(@NonNull EncodingFormat encodingFormat) {
        return encodingFormat == EncodingFormat.PCM_8_BIT ? 8 : 16;
    }

    // INTERFACE
    @FunctionalInterface
    private interface CallbackExecutor {
        void execute(AdvanceAudioRecorderCallback listener);
    }

    // PRIVATE CLASS

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

    // PUBLIC CLASS

    public static class AudioValues {
        public static int source(@NonNull Source source) {
            return source == Source.MIC ? AudioSource.MIC : AudioSource.DEFAULT;
        }

        public static int channel(@NonNull Channel channel) {
            return channel == Channel.MONO ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
        }

        public static int samplingRate(@NonNull SamplingRate samplingRate) {
            return AudioSamplingRate.CD;
        }

        public static int encodingFormat(@NonNull EncodingFormat encodingFormat) {
            return encodingFormat == EncodingFormat.PCM_8_BIT ? AudioFormat.ENCODING_PCM_8BIT : AudioFormat.ENCODING_PCM_16BIT;
        }
    }

    // PUBLIC ENUMS

    public enum State {
        INITIALIZED, RECORDING, PAUSED
    }

    public enum Source {
        MIC, DEFAULT
    }

    public enum Channel {
        MONO, STEREO
    }

    public enum SamplingRate {
        CD
    }

    public enum EncodingFormat {
        PCM_8_BIT, PCM_16_BIT
    }
}
