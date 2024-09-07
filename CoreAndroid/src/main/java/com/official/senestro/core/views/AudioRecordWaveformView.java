package com.official.senestro.core.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.official.senestro.core.R;
import com.official.senestro.core.utils.AdvanceUtils;

import java.util.ArrayList;
import java.util.List;

public class AudioRecordWaveformView extends View {
    private final String TAG = AudioRecordWaveformView.class.getName();

    private final Context context;

    private static final float maxReportableAmp = 22760f; //Effective size,  max fft = 32760
    private static final float uninitialized = 0f;

    private final List<Float> waveHeights = new ArrayList<>();
    private final List<Float> waveWidths = new ArrayList<>();

    private AlignTo waveAlignTo = AlignTo.CENTER;
    private Direction waveDirection = Direction.LeftToRight;
    private final Paint wavePaint = new Paint();
    private float usageWidth = 0f;

    private long lastUpdateTime = 0L;
    private float TBPadding = convertDPToPX(6); // Top and bottom padding

    private boolean waveSoftTransition = false;
    private int waveColor = Color.RED;
    private float waveWidth = convertDPToPX(2);
    private float waveSpace = convertDPToPX(1);
    private float waveMaxHeight = uninitialized;
    private float waveMinHeight = convertDPToPX(3); // Recommended size > 10 dp
    private boolean waveRoundedCorners = false;

    public AudioRecordWaveformView(@NonNull Context context) {
        super(context);
        this.context = context;
        initialize(null);
    }

    public AudioRecordWaveformView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initialize(attrs);
    }

    public AudioRecordWaveformView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initialize(attrs);
    }

    public AudioRecordWaveformView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        initialize(attrs);
    }

    public void setWaveAlignTo(@NonNull AlignTo waveAlignTo) {
        this.waveAlignTo = waveAlignTo;
    }

    public void setWaveDirection(@NonNull Direction waveDirection) {
        this.waveDirection = waveDirection;
    }

    public void setWaveSoftTransition(boolean waveSoftTransition) {
        this.waveSoftTransition = waveSoftTransition;
    }

    public void setWaveColor(int waveColor) {
        this.waveColor = waveColor;
        wavePaint.setColor(waveColor);
    }

    public void setWaveRoundedCorners(boolean waveRoundedCorners) {
        this.waveRoundedCorners = waveRoundedCorners;
        wavePaint.setStrokeCap(waveRoundedCorners ? Paint.Cap.ROUND : Paint.Cap.BUTT);
    }

    public void recreate() {
        usageWidth = 0f;
        waveWidths.clear();
        waveHeights.clear();
        invalidate();
    }

    /**
     * Call this function when you need to add a new wave
     *
     * @param amplitude Used to draw the height of each wave.
     */
    public void update(int amplitude) {
        if (getHeight() == 0) {
            Log.w(TAG, "You must call the update function when the view is displayed");
        } else {
            try {
                handleAmplitude(amplitude);
                invalidate(); // Call to the onDraw function
                lastUpdateTime = System.currentTimeMillis();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            }
        }
    }

    // PRIVATE METHODS

    private void initialize(@Nullable AttributeSet attrs) {
        if (AdvanceUtils.notNull(attrs)) {
            try (TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.AudioRecordWaveformView)) {
                waveSpace = attributes.getDimension(R.styleable.AudioRecordWaveformView_waveSpace, waveSpace);
                waveMaxHeight = attributes.getDimension(R.styleable.AudioRecordWaveformView_waveMaxHeight, waveMaxHeight);
                waveMinHeight = attributes.getDimension(R.styleable.AudioRecordWaveformView_waveMinHeight, waveMinHeight);
                waveRoundedCorners = attributes.getBoolean(R.styleable.AudioRecordWaveformView_waveRoundedCorners, waveRoundedCorners);
                waveWidth = attributes.getDimension(R.styleable.AudioRecordWaveformView_waveWidth, waveWidth);
                waveColor = attributes.getColor(R.styleable.AudioRecordWaveformView_waveColor, waveColor);
                int alTo = attributes.getInt(R.styleable.AudioRecordWaveformView_waveAlignTo, waveAlignTo.value);
                waveAlignTo = alTo == 1 ? AlignTo.CENTER : AlignTo.BOTTOM;
                int diInt = attributes.getInt(R.styleable.AudioRecordWaveformView_waveDirection, waveDirection.value);
                waveDirection = diInt == 1 ? Direction.RightToLeft : Direction.LeftToRight;
                waveSoftTransition = attributes.getBoolean(R.styleable.AudioRecordWaveformView_waveSoftTransition, waveSoftTransition);
                setWillNotDraw(false);
                wavePaint.setAntiAlias(true);
                attributes.recycle();
            } catch (Throwable e) {
                Log.e(TAG, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            }
        } else {
            initialize();
        }
    }

    private void initialize() {
        wavePaint.setStrokeWidth(waveWidth);
        wavePaint.setColor(waveColor);
        wavePaint.setStrokeCap(waveRoundedCorners ? Paint.Cap.ROUND : Paint.Cap.BUTT);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawWaves(canvas);
    }

    private void handleAmplitude(int amplitude) {
        if (amplitude >= 1) {
            float horizontalScale = waveWidth + waveSpace;
            float maxCount = getWidth() / horizontalScale;
            if (!waveHeights.isEmpty() && waveHeights.size() >= maxCount) {
                waveHeights.remove(0);
            } else {
                usageWidth += horizontalScale;
                waveWidths.add(usageWidth);
            }
            if (waveMaxHeight == uninitialized) {
                waveMaxHeight = getHeight() - (TBPadding * 2);
            } else if (waveMaxHeight > getHeight() - (TBPadding * 2)) {
                waveMaxHeight = getHeight() - (TBPadding * 2);
            }
            float verticalDrawScale = waveMaxHeight - waveMinHeight;
            if (verticalDrawScale >= 1f) {
                float point = maxReportableAmp / verticalDrawScale;
                if (point >= 1f) {
                    float amplitudePoint = amplitude / point;
                    if (waveSoftTransition && !waveHeights.isEmpty()) {
                        long updateTimeInterval = System.currentTimeMillis() - lastUpdateTime;
                        float scaleFactor = scaleFactor(updateTimeInterval);
                        float prevAmplitudeWithoutAdditionalSize = waveHeights.get(waveHeights.size() - 1) - waveMinHeight;
                        amplitudePoint = softTransition(amplitudePoint, prevAmplitudeWithoutAdditionalSize, 2.2f, scaleFactor);
                    }
                    amplitudePoint += waveMinHeight;
                    if (amplitudePoint > waveMaxHeight) {
                        amplitudePoint = waveMaxHeight;
                    } else if (amplitudePoint < waveMinHeight) {
                        amplitudePoint = waveMinHeight;
                    }
                    waveHeights.add(waveHeights.size(), amplitudePoint);
                }
            }
        }
    }

    private float scaleFactor(long updateTimeInterval) {
        if (updateTimeInterval <= 50) return 1.6f;
        else if (updateTimeInterval <= 100) return 2.2f;
        else if (updateTimeInterval <= 150) return 2.8f;
        else if (updateTimeInterval <= 200) return 4.2f;
        else if (updateTimeInterval <= 500) return 4.8f;
        else return 5.4f;
    }

    private void drawWaves(@NonNull Canvas canvas) {
        if (waveAlignTo == AlignTo.BOTTOM) {
            drawOnBottom(canvas);
        } else {
            drawOnCenter(canvas);
        }
    }

    private void drawOnCenter(@NonNull Canvas canvas) {
        float verticalCenter = getHeight() / 2f;
        for (int i = 0; i < waveHeights.size() - 1; i++) {
            float chunkX = getWaveX(i);
            float startY = verticalCenter - waveHeights.get(i) / 2;
            float stopY = verticalCenter + waveHeights.get(i) / 2;
            canvas.drawLine(chunkX, startY, chunkX, stopY, wavePaint);
        }
    }

    private void drawOnBottom(@NonNull Canvas canvas) {
        for (int i = 0; i < waveHeights.size() - 1; i++) {
            float chunkX = getWaveX(i);
            float startY = getHeight() - TBPadding;
            float stopY = startY - waveHeights.get(i);
            canvas.drawLine(chunkX, startY, chunkX, stopY, wavePaint);
        }
    }

    private float getWaveX(int index) {
        return waveDirection == Direction.RightToLeft ? getWidth() - waveWidths.get(index) : waveWidths.get(index);
    }

    private float convertDPToPX(float dp) {
        // return dp * getResources().getDisplayMetrics().density;
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private float softTransition(float value, float compareWith, float allowedDiff, float scaleFactor) {
        if (scaleFactor == 0f) return value; // avoid ArithmeticException (divide by zero)
        float result = value;
        if (compareWith > value && compareWith / value > allowedDiff) {
            float diff = Math.max(compareWith, value) - Math.min(compareWith, value);
            result += diff / scaleFactor;
        } else if (value > compareWith && value / compareWith > allowedDiff) {
            float diff = Math.max(compareWith, value) - Math.min(compareWith, value);
            result -= diff / scaleFactor;
        }
        return result;
    }

    // PUBLIC ENUMS

    public enum AlignTo {
        CENTER(1),
        BOTTOM(2);

        private final int value;

        AlignTo(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum Direction {
        RightToLeft(1),
        LeftToRight(2);

        private final int value;

        Direction(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}