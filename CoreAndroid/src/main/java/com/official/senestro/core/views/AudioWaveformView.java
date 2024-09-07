package com.official.senestro.core.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class AudioWaveformView extends View {
    private Paint paint = new Paint();
    private int[] amplitudes = new int[100]; // Store 100 amplitude values
    private int vw;
    private int vh;
    private boolean roundCorners = true;
    private int color = Color.MAGENTA;
    private float width = 2;
    private float height = 100;
    private Direction direction = Direction.ltr;

    public AudioWaveformView(@NonNull Context context) {
        super(context);
    }

    public AudioWaveformView(@NonNull Context context, boolean corners, int color, float width, float height, @NonNull Direction direction) {
        super(context);
        this.setOptions(corners, color, width, height, direction);
    }

    public void setOptions(boolean roundCorners, int color, float width, float height, @NonNull Direction direction) {
        this.roundCorners = roundCorners;
        this.color = color;
        this.width = convertDPToPX(width);
        this.height = convertDPToPX(height);
        this.direction = direction;
    }

    public void setRoundCorners(boolean corners) {
        this.roundCorners = corners;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setWidth(float width) {
        this.width = convertDPToPX(width);
    }

    public void setHeight(float height) {
        this.height = convertDPToPX(height);
    }

    public void setDirection(@NonNull Direction direction) {
        this.direction = direction;
    }

    public void draw(int maxAmplitude) {
        // Shift all values to the left
        System.arraycopy(amplitudes, 1, amplitudes, 0, amplitudes.length - 1);
        // Insert the new amplitude at the end
        amplitudes[amplitudes.length - 1] = maxAmplitude;
        // Redraw the view
        invalidate();
    }

    public void reset() {
        Arrays.fill(amplitudes, 0);
        invalidate(); // Redraw the view to reflect the reset state
    }

    // PROTECTED
    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        this.vw = width;
        this.vh = height;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(color);
        paint.setStrokeWidth(width);
        float centerY = vh / 2f;
        float scaleX = (float) vw / amplitudes.length;
        if (direction == Direction.rtl || direction == Direction.ltr) {
            for (int i = 0; i < amplitudes.length; i++) {
                float x = direction == Direction.rtl ? vw - (i * scaleX) : i * scaleX;
                float amplitude = amplitudes[i] / 32767f; // Normalize amplitude
                float scaledAmplitude = amplitude * (height > vh ? vh : height) / 2;
                float startY = centerY - scaledAmplitude;
                float endY = centerY + scaledAmplitude;
                paint.setStrokeCap(roundCorners ? Paint.Cap.ROUND : Paint.Cap.BUTT);
                canvas.drawLine(x, startY, x, endY, paint);
            }
        }
    }

    // PRIVATE
    private float convertDPToPX(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    // PUBLIC ENUMS
    public enum Direction {
        rtl, ltr
    }
}
