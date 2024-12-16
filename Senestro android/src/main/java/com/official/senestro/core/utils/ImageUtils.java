package com.official.senestro.core.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageUtils {
    private final String tag = ImageUtils.class.getName();

    private final Context context;

    public ImageUtils(Context context) {
        this.context = context;
    }

    public void convertDrawableResource(int resourceId, @NonNull String filename) {
        Drawable drawable = AppCompatResources.getDrawable(context, resourceId);
        if (drawable != null) {
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            saveBitmap(bitmap, filename);
        }
    }

    public void saveBitmap(@NonNull Bitmap bitmap, @NonNull String filename) {
        File file = new File(filename);
        try (FileOutputStream stream = new FileOutputStream(file)) {
            ByteBuffer buffer = ByteBuffer.allocate(bitmap.getByteCount());
            bitmap.copyPixelsToBuffer(buffer);
            byte[] byteArray = buffer.array();
            stream.write(byteArray);
            stream.flush();
            stream.flush();
        } catch (IOException e) {
            Log.e(tag, e.getMessage(), e);
        }
    }

    public void saveBitmap(@NonNull Bitmap bitmap, @NonNull String filename, @NonNull Bitmap.CompressFormat format, int quality) {
        try (FileOutputStream stream = new FileOutputStream(filename)) {
            bitmap.compress(format, quality, stream);
            stream.flush();
        } catch (IOException e) {
            Log.e(tag, e.getMessage(), e);
        }
    }
}