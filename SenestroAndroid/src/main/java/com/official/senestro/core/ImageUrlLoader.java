package com.official.senestro.core;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import com.official.senestro.core.utils.AdvanceUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageUrlLoader {
    private static final String TAG = ImageUrlLoader.class.getName();

    public static void load(@NonNull String url, @NonNull ImageView view, int errorResource) {
        String extension = AdvanceUtils.getExtension(url);
        if (ImageFileValidator.validFileExtension(extension)) {
            SingleLoader loader = new SingleLoader(url, view, errorResource);
            loader.load(0, 0);
        }
    }

    public static void load(@NonNull String url, @NonNull ImageView view, int width, int height, int errorResource) {
        String extension = AdvanceUtils.getExtension(url);
        if (ImageFileValidator.validFileExtension(extension)) {
            SingleLoader loader = new SingleLoader(url, view, errorResource);
            loader.load(width, height);
        }
    }

    // PRIVATE METHODS
    private static void postOnMainThread(@NonNull Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    private static class SingleLoader {
        private final String url;
        private final ImageView view;
        private final OkHttpClient client;
        private final LruCache<String, Bitmap> cache;
        private static final int maxRetires = 3;
        private final ExecutorService executor;
        private final int errorResource;

        private SingleLoader(String url, ImageView view, int errorResource) {
            this.url = url;
            this.view = view;
            this.client = AdvanceUtils.getUnsafeOkHttpClient(null, 3000, 3000);
            // Initialize LruCache for caching images
            final int size = (int) (Runtime.getRuntime().maxMemory() / 1024) / 8; // Use 1/8th of available memory
            this.cache = new LruCache<>(size);
            this.executor = Executors.newFixedThreadPool(4);
            this.errorResource = errorResource;
        }

        private void setBitmap(@NonNull Bitmap bitmap) {
            postOnMainThread(() -> view.setImageBitmap(bitmap));
        }

        private void load(int width, int height) {
            // Check if the image is already cached
            Bitmap cachedBitmap = cache.get(url);
            if (AdvanceUtils.notNull(cachedBitmap)) {
                setBitmap(cachedBitmap);
            } else {
                executor.submit(() -> {
                    Bitmap bitmap = null;
                    int retries = 0;
                    while (retries < maxRetires) {
                        try {
                            // Make GET request
                            Request request = new Request.Builder().url(url).get().build();
                            Response response = client.newCall(request).execute();
                            if (response.isSuccessful()) {
                                ResponseBody body = response.body();
                                if (AdvanceUtils.notNull(body)) {
                                    // Get image bytes
                                    byte[] bytes = response.body().bytes();
                                    // Convert bytes to Bitmap
                                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    break; // Exit retry loop if successful
                                }
                            }
                        } catch (Throwable throwable) {
                            retries++;
                            System.err.println("Failed to load image from url: " + url);
                            System.err.println(throwable);
                        }
                    }
                    if (AdvanceUtils.notNull(bitmap)) {
                        bitmap = validResizeParams(width, height) ? resizeBitmap(bitmap, width, height, true) : bitmap;
                        cache.put(url, bitmap);
                        setBitmap(bitmap);
                    } else {
                        postOnMainThread(() -> view.setImageResource(errorResource));
                    }
                });
            }
        }

        private boolean validResizeParams(int width, int height) {
            return width > 0 && height > 0;
        }

        // Resizes a bitmap either maintaining its aspect ratio or without maintaining it
        private Bitmap resizeBitmap(@NonNull Bitmap source, int width, int height, boolean useAspectRatio) {
            if (useAspectRatio) {
                return resizeWithAspectRatio(source, width, height);
            } else {
                return resizeWithoutAspectRatio(source, width, height);
            }
        }

        // Resizes the bitmap without maintaining aspect ratio
        private Bitmap resizeWithoutAspectRatio(@NonNull Bitmap source, int width, int height) {
            // Ensure the source bitmap is valid
            validateResizeBitmapDimensions(source);
            // Directly resize the bitmap without aspect ratio preservation
            return Bitmap.createScaledBitmap(source, width, height, true);
        }

        // Resizes the bitmap while maintaining the aspect ratio
        private Bitmap resizeWithAspectRatio(@NonNull Bitmap source, int width, int height) {
            // Ensure the source bitmap is valid
            validateResizeBitmapDimensions(source);
            int originalWidth = source.getWidth();
            int originalHeight = source.getHeight();
            // Calculate scaling factor to maintain aspect ratio
            float scaleFactor = Math.min((float) width / originalWidth, (float) height / originalHeight);
            int newWidth = Math.round(originalWidth * scaleFactor);
            int newHeight = Math.round(originalHeight * scaleFactor);
            // Ensure that the resized dimensions are at least 1x1
            newWidth = Math.max(1, newWidth);
            newHeight = Math.max(1, newHeight);
            // Resize the bitmap with smoothing filter
            return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
        }

        // Crops the image to the specified width and height
        private Bitmap cropBitmap(@NonNull Bitmap source, int width, int height) {
            // Validate the target dimensions
            validateCropBitmapDimensions(source, width, height);
            // Calculate the position to crop (center the image)
            int x = (source.getWidth() - width) / 2;
            int y = (source.getHeight() - height) / 2;
            // Crop and return the image
            return Bitmap.createBitmap(source, x, y, width, height);
        }

        // Validates that the bitmap dimensions are greater than 0
        private void validateResizeBitmapDimensions(@NonNull Bitmap source) {
            if (source.getWidth() == 0 || source.getHeight() == 0) {
                throw new IllegalArgumentException("Bitmap dimensions must be greater than 0");
            }
        }

        // Validates that the target width and height are smaller than the source dimensions
        private void validateCropBitmapDimensions(@NonNull Bitmap source, int width, int height) {
            if (width > source.getWidth() || height > source.getHeight()) {
                throw new IllegalArgumentException("Target dimensions must be smaller than the source image.");
            }
        }
    }
}