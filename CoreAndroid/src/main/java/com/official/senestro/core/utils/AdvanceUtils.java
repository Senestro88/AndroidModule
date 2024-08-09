package com.official.senestro.core.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
//noinspection ExifInterface
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Lifecycle;

import com.official.senestro.core.ApkInfoExtractor;
import com.official.senestro.core.callbacks.interfaces.ClickCallback;
import com.official.senestro.core.callbacks.interfaces.CopyBytesChangedCallback;
import com.official.senestro.core.callbacks.interfaces.SpeechCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class AdvanceUtils {
    // PUBLIC VARIABLES
    /* ----------------------------------------------------------------- */
    public static int bufferSize = 2097152; // 2MB

    // PRIVATE VARIABLES
    /* ----------------------------------------------------------------- */
    private static TextToSpeech textToSpeech = null;

    private AdvanceUtils() {
    }

    // PUBLIC METHODS
    /* ----------------------------------------------------------------- */

    public static String formatMillisecondsToTime(int milliseconds) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        long hours = TimeUnit.SECONDS.toHours(seconds);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60;
        return hours + ":" + minutes + ":" + seconds % 60;
    }

    public static String formatSecondsToTime(int seconds) {
        long hours = TimeUnit.SECONDS.toHours(seconds);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60;
        return hours + ":" + minutes + ":" + seconds % 60;
    }

    // Start - CUSTOM TOAST
    public static void customToast(@NonNull Context context, @NonNull String message, int textColor, int textSize, int backgroundColor, int radius, int gravity, int length) {
        Toast toast = Toast.makeText(context, message, length);
        View view = toast.getView();
        if (view != null) {
            TextView textView = view.findViewById(android.R.id.message);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            textView.setTextColor(textColor);
            textView.setGravity(Gravity.CENTER);
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setColor(backgroundColor);
            gradientDrawable.setCornerRadius(radius);
            view.setBackground(gradientDrawable);
            view.setPadding(4, 4, 4, 4);
            view.setElevation(10);
            switch (gravity) {
                case 1:
                    toast.setGravity(Gravity.TOP, 0, 150);
                    break;
                case 2:
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    break;
                case 3:
                    toast.setGravity(Gravity.BOTTOM, 0, 150);
                    break;
            }
            toast.show();
        }
    }

    public static void customToast(@NonNull Context context, @NonNull String message, int textColor, int textSize, int backgroundColor, int radius, int gravity, int length, int icon) {
        Toast toast = Toast.makeText(context, message, length);
        View view = toast.getView();
        if (view != null) {
            TextView textView = view.findViewById(android.R.id.message);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            textView.setTextColor(textColor);
            textView.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
            textView.setGravity(Gravity.CENTER);
            textView.setCompoundDrawablePadding(10);
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setColor(backgroundColor);
            gradientDrawable.setCornerRadius(radius);
            view.setBackground(gradientDrawable);
            view.setPadding(4, 4, 4, 4);
            view.setElevation(10);
            switch (gravity) {
                case 1:
                    toast.setGravity(Gravity.TOP, 0, 150);
                    break;
                case 2:
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    break;
                case 3:
                    toast.setGravity(Gravity.BOTTOM, 0, 150);
                    break;
            }
            toast.show();
        }
    }
    // End - CUSTOM TOAST

    public static boolean isInternetConnected(@NonNull Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static int hexColorToIntColor(@NonNull String hexColor) {
        return Color.parseColor(hexColor);
    }

    public static String intColorToHexColor(int intColor) {
        return "#" + Integer.toHexString(intColor).toUpperCase();
    }

    // Start - RIPPLE EFFECT
    public static void rippleClickEffect(@NonNull View view, int backgroundColor, int clickColor, float radius) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(backgroundColor);
        gd.setCornerRadius(radius);
        RippleDrawable rd = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{clickColor}), gd, null);
        view.setBackground(rd);
    }

    public static void rippleClickEffect(@NonNull View view, @NonNull String backgroundColor, @NonNull String clickColor, float radius) {
        rippleClickEffect(view, Color.parseColor(backgroundColor), Color.parseColor(clickColor), radius);
    }
    // End - RIPPLE EFFECT

    public static void setOnClickEffect(@NonNull View view, int clickColor, float clickRadius, ClickCallback callback) {
        Drawable initialDrawable = view.getBackground();
        view.setOnClickListener(v -> {
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setColor(clickColor);
            gradientDrawable.setCornerRadius(clickRadius);
            view.setBackground(gradientDrawable);
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                view.setBackground(initialDrawable);
                handler.removeCallbacksAndMessages(null);
                if (callback != null) {
                    callback.onClick(view);
                }
            }, 100);
        });
    }

    public static void setClickEffect(@NonNull View view, int clickColor, float clickRadius, ClickCallback callback) {
        Drawable initialDrawable = view.getBackground();
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(clickColor);
        gradientDrawable.setCornerRadius(clickRadius);
        view.setBackground(gradientDrawable);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            view.setBackground(initialDrawable);
            handler.removeCallbacksAndMessages(null);
            if (callback != null) {
                callback.onClick(view);
            }
        }, 100);
    }

    public static void setCornerReadius(@NonNull View view, float clickRadius) {
        Drawable initialDrawable = view.getBackground();
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColorFilter(initialDrawable.getColorFilter());
        gradientDrawable.setCornerRadius(clickRadius);
        view.setBackground(gradientDrawable);
    }

    public static void setCornerReadius(@NonNull View view, int backgroundColor, float clickRadius) {
        Drawable initialDrawable = view.getBackground();
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(backgroundColor);
        gradientDrawable.setCornerRadius(clickRadius);
        view.setBackground(gradientDrawable);
    }

    // Start - Camera
    public static String getDeviceCameraId(@NonNull Context context) {
        try {
            // Get the CameraManager instance
            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            // Get the cameraId for the back-facing camera (usually the one with flash)
            return cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            return null;
        }
    }

    public static boolean deviceHasFlashlight(@NonNull Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    public static void switchDeviceFlashlight(@NonNull Context context, boolean switchOn) {
        try {
            if (deviceHasFlashlight(context)) {
                // Get the CameraManager instance
                CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
                String cameraId = getDeviceCameraId(context);
                if (cameraId != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        cameraManager.setTorchMode(cameraId, switchOn);
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    // End - Camera

    public static Activity convertContextToActivity(@NonNull Context context) {
        if (context instanceof ContextWrapper) {
            Context baseContext = ((ContextWrapper) context).getBaseContext();
            if (baseContext instanceof Activity) {
                return (Activity) baseContext;
            }
        }
        return null; // Return null if the context is not an activity
    }

    public static Context convertActivityToContext(@NonNull Activity activity) {
        return activity.getBaseContext(); // Get the base context of the activity, which is a Context object
    }

    public static long getFileSize(@NonNull String path) {
        return isExist(path) ? new File(path).length() : 0;
    }

    public static String readableSize(@NonNull String path) {
        long size = getFileSize(path);
        return readableSize(size);
    }

    public static String readableSize(long size) {
        if (size > 0) {
            final String[] units = {"B", "KB", "MB", "GB", "TB"};
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            DecimalFormat decimalFormat = new DecimalFormat("#,##0.0");
            return decimalFormat.format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        }
        return "0 B";
    }

    public static List<HashMap<String, Object>> listDir(@NonNull File dirFile, boolean recursively) {
        List<HashMap<String, Object>> lists = new ArrayList<>();
        if (dirFile.isDirectory()) {
            File[] listedFiles = dirFile.listFiles();
            if (listedFiles != null) {
                List<FileUtils> infoUtils = new ArrayList<>();
                for (File list : listedFiles) {
                    infoUtils.add(new FileUtils(list.getAbsolutePath()));
                }
                Collections.sort(infoUtils, new FileUtils.FileUtilsComparator());
                for (FileUtils list : infoUtils) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("absolutePath", list.getAbsolutePath());
                    map.put("size", list.length());
                    if (list.isDirectory() && recursively) {
                        lists.addAll(listDir(new File(list.getPath()), true));
                    }
                    lists.add(map);
                }
            }
        }
        return lists;
    }

    public static List<HashMap<String, Object>> listDir(@NonNull String dirPath, boolean recursively) {
        return listDir(new File(dirPath), recursively);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static List<HashMap<String, Object>> listDir(@NonNull Context context, @NonNull File dirFile, boolean recursively) {
        List<HashMap<String, Object>> lists = new ArrayList<>();
        if (dirFile.isDirectory()) {
            ContentResolver contentResolver = context.getContentResolver();
            Uri collection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL) : MediaStore.Files.getContentUri("external");
            String column = MediaStore.Files.FileColumns.DATA;
            String[] projection = {column};
            String selection = column + " LIKE ? AND " + column + " NOT LIKE ?";
            String[] selectionArgs = new String[]{"%" + dirFile.getAbsolutePath() + "/%", "%" + dirFile.getAbsolutePath() + "/%/%"};
            String sortOrder = column + " DESC";
            Cursor cursor = contentResolver.query(collection, projection, selection, selectionArgs, sortOrder);
            if (cursor != null) {
                try {
                    List<FileUtils> infoUtils = new ArrayList<>();
                    // Cache column indices.
                    int dataColumn = cursor.getColumnIndexOrThrow(column);
                    while (cursor.moveToNext()) {
                        // Get values of columns indices
                        String path = cursor.getString(dataColumn);
                        infoUtils.add(new FileUtils(path));
                    }
                    Collections.sort(infoUtils, new FileUtils.FileUtilsComparator());
                    for (FileUtils list : infoUtils) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("absolutePath", list.getAbsolutePath());
                        map.put("size", list.length());
                        if (list.isDirectory() && recursively) {
                            lists.addAll(listDir(context, new File(list.getAbsolutePath()), true));
                        }
                        lists.add(map);
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return lists;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static List<HashMap<String, Object>> listDir(@NonNull Context context, @NonNull String dirPath, boolean recursively) {
        return listDir(context, new File(dirPath), recursively);
    }

    public static String getExtension(@NonNull String name) {
        String extension = "";
        int lastIndex = name.lastIndexOf(".");
        if (lastIndex != -1) {
            return name.substring(lastIndex + 1);
        }
        return extension;
    }

    public static String getExtensionFromUrl(@NonNull Uri uri) {
        return MimeTypeMap.getFileExtensionFromUrl(uri.toString());
    }

    public static String getPathLastSegment(@NonNull String name) {
        int lastIndex = name.lastIndexOf(File.separator);
        if (lastIndex != -1) {
            return name.substring(lastIndex + 1);
        }
        return name;
    }

    public static String removeExtension(@NonNull String name) {
        int lastIndex = name.lastIndexOf(".");
        if (lastIndex != -1) {
            return name.substring(0, lastIndex);
        }
        return name;
    }

    public static boolean isServiceRunning(@NonNull Context context, @NonNull Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void startBackgroundService(@NonNull Context context, @NonNull Class<?> serviceClass) {
        if (!isServiceRunning(context, serviceClass)) {
            Intent serviceIntent = new Intent(context, serviceClass);
            context.startService(serviceIntent);
        }
    }

    public static void startForegroundService(@NonNull Context context, @NonNull Class<?> serviceClass) {
        if (!isServiceRunning(context, serviceClass) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, serviceClass));
        }
    }

    public static void stopService(@NonNull Context context, @NonNull Class<?> serviceClass) {
        if (isServiceRunning(context, serviceClass)) {
            Intent serviceIntent = new Intent(context, serviceClass);
            context.stopService(serviceIntent);
        }
    }

    public static String generateRandomText(int length) {
        int textLength = Math.max(1, Math.min(length, 32));
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            builder.append(characters.charAt(index));
        }
        return builder.toString();
    }

    public static int generateRandomInt(int length) {
        int textLength = Math.max(1, Math.min(length, 32));
        Random random = new Random();
        int min = (int) Math.pow(10, length - 1);
        int max = (int) Math.pow(10, length) - 1;
        return random.nextInt(max - min + 1) + min;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }

    public static boolean validJson(@NonNull String jsonContent) {
        try {
            new JSONObject(jsonContent);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    public static void setTextIsSelectable(@NonNull TextView textView, boolean isSelectable) {
        textView.setTextIsSelectable(isSelectable);
    }

    public static void clearFocus(@NonNull View view) {
        view.clearFocus();
    }

    public static void clearAnimation(@NonNull View view) {
        view.clearAnimation();
    }

    public static boolean isSeekbarPressed(@NonNull SeekBar seekBar) {
        return seekBar.isPressed();
    }

    public static void openWhatsapp(@NonNull Context context, String message, String number) {
        try {
            // Open WhatsApp using Intent
            Uri uri = Uri.parse("https://api.whatsapp.com/send?phone=" + number + "&text=" + message);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void openLink(@NonNull Context context, @NonNull String url) {
        try {
            openUrlInBrowser(context, url);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void registerContextMenu(@NonNull Activity activity, @NonNull View view) {
        activity.registerForContextMenu(view);
    }

    public static void setStatusBarColor(@NonNull Activity activity, int color) {
        Window window = activity.getWindow();
        window.setStatusBarColor(color);
    }

    public static void setTranslucentStatusBar(@NonNull Activity activity, boolean addFlags) {
        Window window = activity.getWindow();
        if (addFlags) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    public static void speechEngine(@NonNull Context context, @NonNull Locale localLanguage, SpeechCallback speechCallback) {
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int resultLanguage = textToSpeech.setLanguage(localLanguage);
                if (resultLanguage == TextToSpeech.LANG_MISSING_DATA || resultLanguage == TextToSpeech.LANG_NOT_SUPPORTED) {
                    if (speechCallback != null) {
                        speechCallback.onError("Language isn't supported.");
                    }
                } else {
                    if (speechCallback != null) {
                        speechCallback.onSuccess(textToSpeech);
                    }
                }
            } else {
                if (speechCallback != null) {
                    speechCallback.onError("Initialization failed.");
                }
            }
        });
    }

    public static void cropImage(@NonNull Activity activity, @NonNull String path, int requestCode) {
        try {
            Intent intent = new Intent("com.android.camera.action.CROP");
            File file = new File(path);
            Uri contentUri = Uri.fromFile(file);
            intent.setDataAndType(contentUri, "image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 280);
            intent.putExtra("outputY", 280);
            intent.putExtra("return-data", false);
            activity.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, "Your device doesn't support the crop action!", Toast.LENGTH_SHORT).show();
        }
    }

    public static void hideKeyboard(@NonNull Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        }
    }

    public static void showToast(@NonNull Context context, @NonNull String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static int getLocationX(@NonNull View view) {
        int[] location = new int[2];
        view.getLocationInWindow(location);
        return location[0];
    }

    public static int getLocationY(@NonNull View view) {
        int[] location = new int[2];
        view.getLocationInWindow(location);
        return location[1];
    }

    public static int getRandom(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    public static int getDisplayWidthPixels(@NonNull Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getDisplayHeightPixels(@NonNull Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static float getDip(@NonNull Context context, int input) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, input, context.getResources().getDisplayMetrics());
    }

    public static boolean validImage(@NonNull String path) {
        List<String> knownImageExtensions = Arrays.asList("png", "jpg", "jpeg", "gif", "bmp", "webp", "jfif", "tiff", "heif", "bat", "bpg", "svg");
        return isFile(path) && knownImageExtensions.contains(getExtension(path));
    }

    public static boolean validVideo(@NonNull String path) {
        List<String> knownVideoExtensions = Arrays.asList("mp4", "3gp", "mkv", "avi", "flv", "mov", "wmv", "webm", "vob", "ogv", "mpg", "m4v", "m2ts", "ts", "mpeg", "divx", "asf", "rm", "ram");
        return isFile(path) && knownVideoExtensions.contains(getExtension(path));
    }

    public static HashMap<String, String> getDefaultFilesMime() {
        HashMap<String, String> mimes = new HashMap<>();
        mimes.put("css", "text/css");
        mimes.put("htm", "text/html");
        mimes.put("html", "text/html");
        mimes.put("xml", "text/xml");
        mimes.put("java", "text/x-java-source, text/java");
        mimes.put("md", "text/plain");
        mimes.put("txt", "text/plain");
        mimes.put("asc", "text/plain");
        mimes.put("gif", "image/gif");
        mimes.put("jpg", "image/jpeg");
        mimes.put("jpeg", "image/jpeg");
        mimes.put("png", "image/png");
        mimes.put("svg", "image/svg+xml");
        mimes.put("mp3", "audio/mpeg");
        mimes.put("m3u", "audio/mpeg-url");
        mimes.put("mp4", "video/mp4");
        mimes.put("ogv", "video/ogg");
        mimes.put("flv", "video/x-flv");
        mimes.put("mov", "video/quicktime");
        mimes.put("swf", "application/x-shockwave-flash");
        mimes.put("js", "application/javascript");
        mimes.put("pdf", "application/pdf");
        mimes.put("doc", "application/msword");
        mimes.put("ogg", "application/x-ogg");
        mimes.put("zip", "application/octet-stream");
        mimes.put("json", "application/json");
        mimes.put("exe", "application/octet-stream");
        mimes.put("class", "application/octet-stream");
        mimes.put("m3u8", "application/vnd.apple.mpegurl");
        mimes.put("ts", "video/mp2t");
        // Additional MIME types
        mimes.put("tif", "image/tiff");
        mimes.put("tiff", "image/tiff");
        mimes.put("heif", "image/heif");
        mimes.put("bat", "application/bat");
        mimes.put("bpg", "image/bpg");
        mimes.put("jfif", "image/jpeg");
        mimes.put("webp", "image/webp");
        mimes.put("webm", "video/webm");
        mimes.put("vob", "video/dvd");
        mimes.put("ogm", "video/ogg");
        mimes.put("mpeg", "video/mpeg");
        mimes.put("divx", "video/x-divx");
        mimes.put("asf", "video/x-ms-asf");
        mimes.put("rm", "application/vnd.rn-realmedia");
        mimes.put("ram", "audio/x-pn-realaudio");
        return mimes;
    }

    public static String getMimeFromExtension(@Nullable String extension) {
        HashMap<String, String> mimes = getDefaultFilesMime();
        return extension != null && mimes.containsKey(extension) ? mimes.get(extension) : null;
    }

    public static String getMimeTypeFromExtension(@Nullable String extension) {
        return extension != null ? MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase()) : null;
    }

    public static Bitmap extractVideoFrame(@NonNull String path, int milliseconds) {
        try {
            MediaMetadataRetriever receiver = new MediaMetadataRetriever();
            receiver.setDataSource(path);
            Bitmap bitmap = receiver.getFrameAtTime(milliseconds, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            receiver.release();
            receiver.close();
            return bitmap;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    // Method to close a closeable
    public static void closeQuietly(@Nullable Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (Exception ignored) {
        }
    }

    public static void createFile(@Nullable String path) {
        if (path != null && !isExist(path)) {
            try {
                boolean create = new File(path).createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void createFile(@Nullable File file) {
        if (file != null) {
            createFile(file.getAbsolutePath());
        }
    }

    public static String createTempFile() throws IOException {
        return File.createTempFile(generateRandomText(32), "").getAbsolutePath();
    }

    public static void createDirectory(@Nullable String path) {
        if (path != null && !isExist(path)) {
            try {
                boolean create = new File(path).mkdirs();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public static void createDirectory(@Nullable File file) {
        if (file != null && !file.exists()) {
            try {
                boolean create = file.mkdirs();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public static void delete(@Nullable String path) {
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                if (file.isFile()) {
                    boolean delete = file.delete();
                } else if (file.isDirectory()) {
                    File[] lists = Objects.requireNonNull(file.listFiles());
                    for (File list : lists) {
                        if (list.isDirectory()) {
                            delete(list.getAbsolutePath());
                        } else if (list.isFile()) {
                            boolean delete = list.delete();
                        }
                    }
                    boolean delete = file.delete();
                }
            }
        }
    }

    public static void delete(@Nullable File absoluteFile) {
        if (absoluteFile != null) {
            delete(absoluteFile.getAbsolutePath());
        }
    }

    public static boolean isExist(@Nullable String path) {
        if (path != null) {
            return new File(path).exists();
        }
        return false;
    }

    public static String getFileContent(@Nullable String path) {
        StringBuilder builder = new StringBuilder();
        if (path != null) {
            if (isExist(path)) {
                try (FileInputStream fis = new FileInputStream(path)) {
                    byte[] buffer = new byte[bufferSize];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        builder.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }

    public static String getFileContent(@Nullable String path, boolean deletePath) {
        String content = getFileContent(path);
        if (deletePath) {
            delete(path);
        }
        return content;
    }

    public static long getUriFileSize(@NonNull Context context, @NonNull Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        try (Cursor cursor = contentResolver.query(uri, null, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    return cursor.getLong(sizeIndex);
                }
            }
        }
        return 0; // Return -1 if file size information is not available
    }

    public static void copyFile(@NonNull String sourcePath, @NonNull String destinationPath, @Nullable CopyBytesChangedCallback callback) throws IOException {
        if (isFile(sourcePath) && !sourcePath.equalsIgnoreCase(destinationPath)) {
            try (FileInputStream reader = new FileInputStream(sourcePath); FileOutputStream writer = new FileOutputStream(destinationPath, false)) {
                int totalBytes = (int) new File(sourcePath).length();
                int bytesToTotalBytes = 0;
                byte[] buffer = new byte[bufferSize];
                int bytesRead;
                while ((bytesRead = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, bytesRead);
                    bytesToTotalBytes = bytesToTotalBytes + bytesRead;
                    if (callback != null) {
                        callback.onChanged(bytesToTotalBytes, totalBytes, (int) Math.min((bytesToTotalBytes * 100L) / totalBytes, 100));
                    }
                }
            }
        }
    }

    public static void copyUri(@NonNull Context context, @NonNull Uri sourceUri, @NonNull String destPath, @Nullable CopyBytesChangedCallback callback) throws IOException {
        try (InputStream reader = context.getContentResolver().openInputStream(sourceUri); FileOutputStream writer = new FileOutputStream(destPath)) {
            if (reader != null) {
                int totalBytes = (int) getUriFileSize(context, sourceUri);
                int bytesToTotalBytes = 0;
                byte[] buffer = new byte[bufferSize];
                int bytesRead;
                while ((bytesRead = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, bytesRead);
                    bytesToTotalBytes = bytesToTotalBytes + bytesRead;
                    if (callback != null) {
                        callback.onChanged(bytesToTotalBytes, totalBytes, (int) Math.min((bytesToTotalBytes * 100L) / totalBytes, 100));
                    }
                }
            }
        }
    }

    public static String copyUriToTempFile(@NonNull Context context, @NonNull Uri sourceUri, @NonNull String tempName, @Nullable CopyBytesChangedCallback callback) throws IOException {
        try (InputStream reader = context.getContentResolver().openInputStream(sourceUri)) {
            if (reader != null) {
                int totalBytes = (int) getUriFileSize(context, sourceUri);
                int bytesToTotalBytes = 0;
                File destFile = File.createTempFile(tempName, null, context.getCacheDir());
                try (FileOutputStream writer = new FileOutputStream(destFile)) {
                    byte[] buffer = new byte[bufferSize];
                    int bytesRead;
                    while ((bytesRead = reader.read(buffer)) != -1) {
                        writer.write(buffer, 0, bytesRead);
                        bytesToTotalBytes = bytesToTotalBytes + bytesRead;
                        if (callback != null) {
                            callback.onChanged(bytesToTotalBytes, totalBytes, (int) Math.min((bytesToTotalBytes * 100L) / totalBytes, 100));
                        }
                    }
                }
                return destFile.getAbsolutePath();
            }
        }
        return null;
    }

    public static String getUriBasename(@NonNull Context context, @NonNull Uri uri) {
        String displayName = null;
        ContentResolver contentResolver = context.getContentResolver();
        try (Cursor cursor = contentResolver.query(uri, null, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (displayNameIndex != -1) {
                    displayName = cursor.getString(displayNameIndex);
                }
            }
        }
        return displayName != null ? displayName : uri.getLastPathSegment();
    }

    public static void copyDirectory(@NonNull String sourcePath, @NonNull String destinationPath) {
        File source = new File(sourcePath);
        File destination = new File(destinationPath);
        if (source.isDirectory()) {
            createDirectory(destination.getAbsolutePath());
            File[] files = source.listFiles();
            if (files != null) {
                for (File file : files) {
                    copyDirectory(file.getAbsolutePath(), new File(destination.getAbsolutePath(), file.getName()).getAbsolutePath());
                }
            }
        } else {
            try {
                copyFile(source.getAbsolutePath(), destination.getAbsolutePath());
            } catch (Throwable ignored) {
            }
        }
    }

    public static void copyFile(@NonNull String sourcePath, @NonNull String destPath) throws IOException {
        try (FileChannel sourceChannel = new FileInputStream(sourcePath).getChannel(); FileChannel destChannel = new FileOutputStream(destPath).getChannel()) {
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
    }

    public static void moveOrRenameFile(@NonNull String sourcePath, @NonNull String destPath) {
        File sourceFile = new File(sourcePath);
        File destFile = new File(destPath);
        if (sourceFile.isFile()) {
            String parent = destFile.getParent();
            if (parent != null) {
                createDirectory(parent);
                if (isDirectory(parent)) {
                    boolean renamed = sourceFile.renameTo(destFile);
                }
            }
        }
    }

    public static boolean isDirectory(@Nullable String path) {
        return notNull(path) && isDirectory(new File(path));
    }

    public static boolean isDirectory(@Nullable File path) {
        return notNull(path) && path.isDirectory();
    }

    public static boolean isFile(@Nullable String path) {
        return notNull(path) && isFile(new File(path));
    }

    public static boolean isFile(@Nullable File path) {
        return notNull(path) && path.isFile();
    }

    public static long getFileLengthOrSize(@NonNull String path) {
        return isExist(path) ? new File(path).length() : 0;
    }

    public static String getExternalStorageDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static String getPackageDataDir(@NonNull Context context) {
        File dir = context.getExternalFilesDir(null);
        return dir != null ? dir.getAbsolutePath() : null;
    }

    public static String getPublicDir(@NonNull String type) {
        return !type.isEmpty() ? Environment.getExternalStoragePublicDirectory(type).getAbsolutePath() : null;
    }

    public static String convertUriToFilePath(@NonNull Context context, @NonNull Uri uri) {
        String filePath = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                if ("primary".equalsIgnoreCase(split[0])) {
                    filePath = Environment.getExternalStorageDirectory() + File.separator + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                if (!id.isEmpty()) {
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:", "");
                    }
                }
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));
                filePath = getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equalsIgnoreCase(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equalsIgnoreCase(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equalsIgnoreCase(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                filePath = getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if (ContentResolver.SCHEME_CONTENT.equalsIgnoreCase(uri.getScheme())) {
            filePath = getDataColumn(context, uri, null, null);
        } else if (ContentResolver.SCHEME_FILE.equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }
        if (filePath != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    return URLDecoder.decode(filePath, StandardCharsets.UTF_8);
                }
            } catch (Throwable ignored) {
                return null;
            }
        }
        return null;
    }

    public static void saveBitmap(@NonNull Bitmap bitmap, @NonNull String destPath) {
        createFile(destPath);
        try (FileOutputStream writer = new FileOutputStream(destPath)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap getBitmap(@NonNull Context context, @Nullable File file) {
        if (file != null && file.isFile()) {
            try {
                Uri uri = Uri.fromFile(file);
                int targetW = 600;
                int targetH = 600;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
                int photoW = options.outWidth;
                int photoH = options.outHeight;
                int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
                options.inJustDecodeBounds = false;
                options.inSampleSize = scaleFactor;
                return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Bitmap getScaledBitmap(@NonNull String path, int max) {
        Bitmap src = BitmapFactory.decodeFile(path);
        int width = src.getWidth();
        int height = src.getHeight();
        float rate = width > height ? max / (float) width : max / (float) height;
        width = (int) (width * rate);
        height = (int) (height * rate);
        return Bitmap.createScaledBitmap(src, width, height, true);
    }

    public static int calculateInSampleSize(@NonNull BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int width = options.outWidth;
        final int height = options.outHeight;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampleBitmapFromPath(@NonNull String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static void resizeBitmapFileRetainRatio(@NonNull String fromPath, @NonNull String destPath, int max) {
        if (!isExist(fromPath)) return;
        Bitmap bitmap = getScaledBitmap(fromPath, max);
        saveBitmap(bitmap, destPath);
    }

    public static void resizeBitmapFileToSquare(@NonNull String fromPath, @NonNull String destPath, int max) {
        if (!isExist(fromPath)) return;
        Bitmap src = BitmapFactory.decodeFile(fromPath);
        Bitmap bitmap = Bitmap.createScaledBitmap(src, max, max, true);
        saveBitmap(bitmap, destPath);
    }

    public static void resizeBitmapFileToCircle(@NonNull String fromPath, @NonNull String destPath) {
        if (!isExist(fromPath)) return;
        Bitmap src = BitmapFactory.decodeFile(fromPath);
        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawCircleOnCanvas(canvas, src);
        saveBitmap(bitmap, destPath);
    }

    public static void resizeBitmapFileWithRoundedBorder(@NonNull String fromPath, @NonNull String destPath, int pixels) {
        if (!isExist(fromPath)) return;
        Bitmap src = BitmapFactory.decodeFile(fromPath);
        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawRoundedBorderOnCanvas(canvas, src, pixels);
        saveBitmap(bitmap, destPath);
    }

    public static void drawCircleOnCanvas(@NonNull Canvas canvas, @NonNull Bitmap src) {
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, src.getWidth(), src.getHeight());
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle((float) src.getWidth() / 2, (float) src.getHeight() / 2, (float) src.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(src, rect, rect, paint);
    }

    public static void drawRoundedBorderOnCanvas(@NonNull Canvas canvas, @NonNull Bitmap src, int pixels) {
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, src.getWidth(), src.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, (float) pixels, (float) pixels, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(src, rect, rect, paint);
    }

    public static void cropBitmapFileFromCenter(@NonNull String fromPath, @NonNull String destPath, int w, int h) {
        if (!isExist(fromPath)) return;
        Bitmap src = BitmapFactory.decodeFile(fromPath);
        int width = src.getWidth();
        int height = src.getHeight();
        if (width < w && height < h) return;
        int x = 0;
        int y = 0;
        if (width > w) x = (width - w) / 2;
        if (height > h) y = (height - h) / 2;
        int cw = w;
        int ch = h;
        if (w > width) cw = width;
        if (h > height) ch = height;
        Bitmap bitmap = Bitmap.createBitmap(src, x, y, cw, ch);
        saveBitmap(bitmap, destPath);
    }

    public static void rotateBitmapFile(@NonNull String fromPath, @NonNull String destPath, float angle) {
        if (!isExist(fromPath)) return;
        Bitmap src = BitmapFactory.decodeFile(fromPath);
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap bitmap = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        saveBitmap(bitmap, destPath);
    }

    public static void scaleBitmapFile(@NonNull String fromPath, @NonNull String destPath, float x, float y) {
        if (!isExist(fromPath)) return;
        Bitmap src = BitmapFactory.decodeFile(fromPath);
        Matrix matrix = new Matrix();
        matrix.postScale(x, y);
        int w = src.getWidth();
        int h = src.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(src, 0, 0, w, h, matrix, true);
        saveBitmap(bitmap, destPath);
    }

    public static void skewBitmapFile(@NonNull String fromPath, @NonNull String destPath, float x, float y) {
        if (!isExist(fromPath)) return;
        Bitmap src = BitmapFactory.decodeFile(fromPath);
        Matrix matrix = new Matrix();
        matrix.postSkew(x, y);
        int w = src.getWidth();
        int h = src.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(src, 0, 0, w, h, matrix, true);
        saveBitmap(bitmap, destPath);
    }

    public static void setBitmapFileColorFilter(@NonNull String fromPath, @NonNull String destPath, int color) {
        if (!isExist(fromPath)) return;
        Bitmap src = BitmapFactory.decodeFile(fromPath);
        Bitmap bitmap = Bitmap.createBitmap(src, 0, 0, src.getWidth() - 1, src.getHeight() - 1);
        Paint p = new Paint();
        ColorFilter filter = new LightingColorFilter(color, 1);
        p.setColorFilter(filter);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmap, 0, 0, p);
        saveBitmap(bitmap, destPath);
    }

    public static void setBitmapFileBrightness(@NonNull String fromPath, @NonNull String destPath, float brightness) {
        if (!isExist(fromPath)) return;
        Bitmap src = BitmapFactory.decodeFile(fromPath);
        ColorMatrix cm = new ColorMatrix(new float[]{1, 0, 0, 0, brightness, 0, 1, 0, 0, brightness, 0, 0, 1, 0, brightness, 0, 0, 0, 1, 0});
        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(src, 0, 0, paint);
        saveBitmap(bitmap, destPath);
    }

    public static void setBitmapFileContrast(@NonNull String fromPath, @NonNull String destPath, float contrast) {
        if (!isExist(fromPath)) return;
        Bitmap src = BitmapFactory.decodeFile(fromPath);
        ColorMatrix cm = new ColorMatrix(new float[]{contrast, 0, 0, 0, 0, 0, contrast, 0, 0, 0, 0, 0, contrast, 0, 0, 0, 0, 0, 1, 0});
        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(src, 0, 0, paint);
        saveBitmap(bitmap, destPath);
    }

    public static int getJpegRotate(@NonNull String filePath) {
        int rotate = 0;
        try {
            ExifInterface exif = new ExifInterface(filePath);
            int iOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);

            switch (iOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }
        } catch (IOException e) {
            return 0;
        }

        return rotate;
    }

    public static File createNewPictureFile(@NonNull Context context) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String fileName = date.format(new Date()) + ".png";
        return new File(Objects.requireNonNull(context.getExternalFilesDir(Environment.DIRECTORY_DCIM)).getAbsolutePath() + File.separator + fileName);
    }

    public static long directorySize(@Nullable String path, boolean recursively) {
        long size = 0;
        if (path != null && isDirectory(path)) {
            File[] listedFiles = new File(path).listFiles();
            if (listedFiles != null) {
                for (File listedFile : listedFiles) {
                    if (listedFile.isDirectory() && recursively) {
                        size += directorySize(listedFile.getAbsolutePath(), true);
                    } else {
                        size += listedFile.length();
                    }
                }
            }
        }
        return size;
    }

    public static boolean isDirectoryEmpty(@Nullable String path) {
        int countedFiles = countFiles(path, null, false);
        return countedFiles < 1;
    }

    public static void renameFile(@Nullable String source, @Nullable String destination) {
        if (source != null && destination != null && isFile(source)) {
            try {
                boolean renamed = new File(source).renameTo(new File(destination));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean canWrite(@NonNull String path) {
        File absoluteFile = new File(path);
        return absoluteFile.exists() && absoluteFile.canWrite();
    }

    public static boolean canExecute(@NonNull String path) {
        File absoluteFile = new File(path);
        return absoluteFile.exists() && absoluteFile.canExecute();
    }

    public static boolean canRead(@NonNull String path) {
        File absoluteFile = new File(path);
        return absoluteFile.exists() && absoluteFile.canRead();
    }

    public static String getParentPath(@Nullable String path) {
        if (path != null) {
            File absoluteFile = new File(path);
            if (absoluteFile.exists()) {
                File parent = absoluteFile.getParentFile();
                if (parent != null) {
                    return parent.getAbsolutePath();
                }
            }

        }
        return null;
    }

    public static int countFiles(@Nullable String path, @Nullable ArrayList<String> extensions, boolean recursively) {
        int count = 0;
        if (path != null && isDirectory(path)) {
            File[] files = new File(path).listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        if (extensions == null || hasMatchingExtension(file, extensions)) {
                            count++;
                        }
                    } else if (file.isDirectory()) {
                        count += recursively ? countFiles(file.getAbsolutePath(), extensions, true) : 1;
                    }
                }
            }
        }
        return count;
    }

    public static String getMimeType(@NonNull String path) {
        return URLConnection.getFileNameMap().getContentTypeFor(path);
    }

    public static String randomBasename(@NonNull String extension) {
        return AdvanceUtils.generateRandomText(20) + Calendar.getInstance().getTimeInMillis() + "." + extension;
    }

    public static boolean hasMatchingExtension(@NonNull File file, @NonNull ArrayList<String> extensions) {
        for (String extension : extensions) {
            if (file.getName().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    public static void copyAssetDir(@NonNull Context context, @NonNull String assetDir, @NonNull String destinationDir) {
        try {
            AssetManager assetManager = context.getAssets();
            String[] assets = assetManager.list(assetDir);
            if (assets != null) {
                createDirectory(destinationDir);
                for (String name : assets) {
                    if (name.contains(".")) {
                        copyAssetFile(assetManager, assetDir + File.separator + name, destinationDir + File.separator + name);
                    } else {
                        copyAssetDir(context, assetDir + File.separator + name, destinationDir + File.separator + name);
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void copyAssetFile(@NonNull AssetManager assetManager, @NonNull String fromAssetPath, @NonNull String toPath) {
        try {
            InputStream inputStream = assetManager.open(fromAssetPath);
            createFile(toPath);
            try (FileOutputStream writer = new FileOutputStream(toPath)) {
                byte[] buffer = new byte[5120]; // 5MB Chunk size
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    writer.write(buffer, 0, length);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    // Unzips a zip file to the specified destination directory
    public static void unzipAll(@NonNull String zipFilePath, @NonNull String destDirectory, boolean skipDestFile) {
        try {
            // Create the destination directory if it does not exist
            createDirectory(destDirectory);
            // Open the ZIP file
            ZipFile zip = new ZipFile(zipFilePath);
            // Get an enumeration of the entries in the ZIP file
            Enumeration<?> enumeration = zip.entries();
            // Iterate over each entry in the ZIP file
            while (enumeration.hasMoreElements()) {
                // Get the current entry and name
                ZipEntry entry = (ZipEntry) enumeration.nextElement();
                String name = entry.getName();
                // Create a File object for the entry's destination path
                File file = new File(destDirectory, name);
                // If the checkExistance flag is set to true, it checks if the destination file already exists. If it does, the entry is skipped.
                if (!(skipDestFile && file.exists())) {
                    // If the entry is a directory, create the directory
                    boolean isDir = entry.isDirectory() || name.endsWith("/");
                    if (isDir) {
                        createDirectory(file);
                    } else {
                        // Create parent directories if they do not exist
                        createParentDirForFile(file);
                        // Create an InputStream to read the entry's data
                        InputStream is = zip.getInputStream(entry);
                        // Create an OutputStream to write the entry's data to the destination file
                        OutputStream os = new FileOutputStream(file);
                        // Buffer to hold data during the read/write process
                        byte[] buffer = new byte[bufferSize];
                        int length;
                        // Read data from the entry and write it to the destination file
                        while ((length = is.read(buffer)) >= 0) {
                            os.write(buffer, 0, length);
                        }
                        // Close the InputStream and OutputStream
                        is.close();
                        os.close();
                    }
                }
            }
            // Close the ZIP file
            zip.close();
        } catch (Throwable e) {
            // Handle exceptions appropriately in a real application
            e.printStackTrace();
        }
    }

    // Extracts a specific entry from a zip file to the destination directory
    public static void unzipEntry(@NonNull String zipFilePath, @NonNull String entryToExtract, @NonNull String destDirectory) {
        try {
            // Create the destination directory if it does not exist
            createDirectory(destDirectory);
            // Open the ZIP file
            ZipFile zip = new ZipFile(zipFilePath);
            // Get the specified entry from the ZIP file
            ZipEntry entry = zip.getEntry(entryToExtract);
            // Check if the entry exists in the ZIP file
            if (entry != null) {
                // Create a File object for the entry's destination path
                File file = new File(destDirectory, entryToExtract);
                // If the entry is a directory, create the directory
                if (entry.isDirectory()) {
                    createDirectory(file);
                } else {
                    // Create parent directories if they do not exist
                    createParentDirForFile(file);
                    // Create an InputStream to read the entry's data
                    InputStream is = zip.getInputStream(entry);
                    // Create an OutputStream to write the entry's data to the destination file
                    OutputStream os = new FileOutputStream(file);
                    // Buffer to hold data during the read/write process
                    byte[] buffer = new byte[bufferSize];
                    int length;
                    // Read data from the entry and write it to the destination file
                    while ((length = is.read(buffer)) >= 0) {
                        os.write(buffer, 0, length);
                    }
                    // Close the InputStream and OutputStream
                    is.close();
                    os.close();
                }
            }
            // Close the ZIP file
            zip.close();
        } catch (Throwable e) {
            // Handle exceptions appropriately in a real application
            e.printStackTrace();
        }
    }

    public static String getZipEntryExtension(@NonNull String zipFilePath, @NonNull String entryName) {
        String extension = "";
        try {
            ZipFile zip = new ZipFile(zipFilePath);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                boolean isDir = entry.isDirectory() || name.endsWith("/");
                if (!isDir) {
                    String fileName = entry.getName();
                    if (fileName.startsWith(entryName) || fileName.equals(entryName)) {
                        int lastDotIndex = fileName.lastIndexOf('.');
                        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
                            extension = fileName.substring(lastDotIndex + 1);
                        }
                    }
                }
            }
            zip.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return extension;
    }

    public static void createParentDirForFile(@Nullable File file) {
        if (file != null) {
            createParentDirForFile(file.getAbsolutePath());
        }
    }

    public static void createParentDirForFile(@Nullable String path) {
        if (path != null) {
            File file = new File(path);
            File parent = file.getParentFile();
            if (parent != null) {
                createDirectory(parent.getAbsolutePath());
            }
        }
    }

    public static void writeDataToFile(@NonNull String path, @NonNull String data) {
        createFile(path);
        try (FileWriter writer = new FileWriter(path, false)) {
            writer.write(data);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeBytesToFile(@NonNull String path, @NonNull byte[] bytes) {
        createFile(path);
        try (FileOutputStream stream = new FileOutputStream(path)) {
            stream.write(bytes, 0, bytes.length);
            stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readAndEscapeHtmlFile(@NonNull String path) {
        return readAndEscapeHtmlContent(getFileContent(path));
    }

    public static String readAndEscapeHtmlContent(@NonNull String htmlContent) {
        StringBuilder builder = new StringBuilder();
        for (char c : htmlContent.toCharArray()) {
            switch (c) {
                case '<':
                    builder.append("&lt;");
                    break;
                case '>':
                    builder.append("&gt;");
                    break;
                case '\"':
                    builder.append("&quot;");
                    break;
                case '\'':
                    builder.append("&apos;");
                    break;
                default:
                    builder.append(c);
                    break;
            }
        }
        return builder.toString();
    }

    public static ArrayList<String> getEmptyDirectories(@NonNull String path) {
        ArrayList<String> lists = new ArrayList<>();
        try {
            File absoluteFile = new File(path);
            if (absoluteFile.exists() && absoluteFile.isDirectory()) {
                File[] listedFiles = absoluteFile.listFiles();
                if (listedFiles != null) {
                    if (listedFiles.length > 0) {
                        for (File listedFile : listedFiles) {
                            lists.addAll(getEmptyDirectories(listedFile.getAbsolutePath()));
                        }
                    } else {
                        lists.add(path);
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return lists;
    }

    public static void deleteEmptyDirectories(@NonNull String path) {
        ArrayList<String> emptyDirectories = AdvanceUtils.getEmptyDirectories(path);
        for (String directory : emptyDirectories) {
            delete(directory);
        }
    }

    /**
     * The flag indicate whether you want to list user installed, system installed, or all installed packages.
     * Values are 0, 1, 2. Where 0 is user installed, 1 system installed, 2 all installed packages.
     * Defaults to 0
     */
    public static List<PackageInfo> getInstalledPackagesInformation(@NonNull Context context, int flag) {
        List<PackageInfo> packages = new ArrayList<>();
        try {
            PackageManager packageManager = context.getPackageManager();
            List<PackageInfo> installedPackages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA);
            for (PackageInfo packageInfo : installedPackages) {
                int listFlag = flag != 0 && flag != 1 && flag != 2 ? 0 : flag;
                boolean isSystem = isSystemInstalledPackage(packageInfo);
                if ((listFlag == 0 && !isSystem) || (listFlag == 1 && isSystem) || listFlag == 2) {
                    packages.add(packageInfo);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }


        if (flag == 0 || flag == 1 || flag == 2) {

        }
        return packages;
    }

    public static ArrayList<HashMap<String, Object>> getPackagesInfoData(@NonNull Context context, @NonNull List<PackageInfo> packagesInfo) {
        ArrayList<HashMap<String, Object>> packagesData = new ArrayList<>();
        try {
            PackageManager packageManager = context.getPackageManager();
            for (PackageInfo packageInfo : packagesInfo) {
                HashMap<String, Object> data = new HashMap<>();
                String appName = packageInfo.applicationInfo.loadLabel(packageManager).toString();
                String packageName = packageInfo.packageName;
                int versionCode = packageInfo.versionCode;
                String versionName = packageInfo.versionName;
                Drawable appIcon = packageInfo.applicationInfo.loadIcon(packageManager);
                String sourceDir = packageInfo.applicationInfo.sourceDir;
                data.put("appName", appName);
                data.put("packageName", packageName);
                data.put("appSize", new FileUtils(sourceDir).length());
                data.put("appIcon", appIcon);
                data.put("versionCode", versionCode);
                data.put("versionName", versionName);
                data.put("dataDir", packageInfo.applicationInfo.dataDir);
                data.put("deviceProtectedDataDir", Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? packageInfo.applicationInfo.deviceProtectedDataDir : packageInfo.applicationInfo.dataDir);
                data.put("nativeLibraryDir", packageInfo.applicationInfo.nativeLibraryDir);
                data.put("sourceDir", sourceDir);
                data.put("publicSourceDir", packageInfo.applicationInfo.publicSourceDir);
                packagesData.add(data);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return packagesData;
    }

    public static boolean isSystemInstalledPackage(@NonNull PackageInfo packagesInfo) {
        // If this method is returning 0 as result, it means it's user installed apps
        return (packagesInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    public static File getInternalCacheDirForPackage(@NonNull Context context, @NonNull String packageName) {
        try {
            return context.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY).getCacheDir();
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File getExternalCacheDirForPackage(@NonNull Context context, @NonNull String packageName) {
        try {
            return context.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY).getExternalCacheDir();
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File getInternalFilesDirForPackage(@NonNull Context context, @NonNull String packageName) {
        try {
            return context.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY).getFilesDir();
        } catch (PackageManager.NameNotFoundException e) {
            // Handle the exception if the package is not found
            e.printStackTrace();
            return null;
        }
    }

    public static File getExternalFilesDirForPackage(@NonNull Context context, @NonNull String packageName) {
        try {
            return context.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY).getExternalFilesDir(null);
        } catch (PackageManager.NameNotFoundException e) {
            // Handle the exception if the package is not found
            e.printStackTrace();
            return null;
        }
    }

    public static boolean copyToClipboard(@NonNull Context context, @NonNull String label, @NonNull String textToCopy) {
        try {
            // Get the clipboard manager
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            // Create ClipData object to store the copied text
            ClipData clipData = ClipData.newPlainText(label, textToCopy);
            // Set the ClipData to the clipboard
            clipboardManager.setPrimaryClip(clipData);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void setCornerRadius(@NonNull View view, float radius) {
        try {
            Drawable backgroundDrawable = view.getBackground();
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(GradientDrawable.RECTANGLE);
            gradientDrawable.setCornerRadius(radius);
            setDrawableColor(gradientDrawable, backgroundDrawable);
            view.setBackground(gradientDrawable);
        } catch (Throwable ignored) {
        }
    }

    public static void setDrawableColor(@NonNull GradientDrawable drawable, @NonNull Drawable backgroundDrawable) {
        if (backgroundDrawable instanceof ColorDrawable) {
            ColorDrawable colorDrawable = (ColorDrawable) backgroundDrawable;
            int color = colorDrawable.getColor();
            drawable.setColor(color);
        }
    }

    public static void restartActivity(@NonNull Activity activity) {
        try {
            Intent intent = activity.getIntent();
            activity.startActivity(intent);
            activity.finish();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void restartActivity(@NonNull Context context, @NonNull Activity activity, @NonNull Class<?> targetClass) {
        try {
            Intent intent = new Intent(context, targetClass);
            activity.startActivity(intent);
            activity.finish();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void keepScreen(@NonNull Activity activity, boolean on) {
        try {
            int flag = on ? WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON : 0;
            activity.runOnUiThread(() -> activity.getWindow().addFlags(flag));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, Object> getMediaInformation(@NonNull Context context, @NonNull String videoPath) {
        HashMap<String, Object> information = new HashMap<>();
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, Uri.parse(videoPath));
            information.put("width", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            information.put("height", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            information.put("duration", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            information.put("genre", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
            information.put("title", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            retriever.release();
            retriever.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return information;
    }

    public static long getRemoteFileSize(@NonNull String fileUrl) {
        long size = -1;
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            size = connection.getContentLength();
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return size;
    }

    public static String getDefaultBrowserPackageName(@NonNull Context context) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://"));
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(browserIntent, PackageManager.MATCH_DEFAULT_ONLY);
        return !resolveInfoList.isEmpty() ? resolveInfoList.get(0).activityInfo.packageName : null;
    }

    public static void openUrlInBrowser(@NonNull Context context, @NonNull String url, @NonNull String packageName) throws Exception {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.setPackage(packageName);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Throwable e) {
            throw new Exception("Error opening URL in browser", e);
        }
    }

    public static void openUrlInBrowser(@NonNull Context context, @NonNull String url) throws Exception {
        try {
            String defaultBrowserPackageName = AdvanceUtils.getDefaultBrowserPackageName(context);
            String browserPackageName = defaultBrowserPackageName == null ? "com.android.chrome" : defaultBrowserPackageName;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.setPackage(browserPackageName);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Throwable e) {
            throw new Exception("Error opening URL in browser", e);
        }
    }

    private static Intent createDispatchTakePictureIntent(@NonNull Context context) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            return intent;
        }
        return null;
    }

    public static Uri dispatchTakePictureIntent(@NonNull Context context, @NonNull Activity activity, @NonNull String authority, int requestCode) {
        Intent intent = createDispatchTakePictureIntent(context);
        if (intent != null) {
            try {
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    StorageUtils storageUtils = new StorageUtils(context);
                    String directory = storageUtils.getExternalStorageDirectory() + File.separator + "Pictures";
                    AdvanceUtils.createDirectory(directory);
                    String path = directory + File.separator + generateFilename("", "jpg", true);
                    Uri uriForFile = FileProvider.getUriForFile(context, authority, new File(path));
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);
                    activity.startActivityForResult(intent, requestCode);
                    return uriForFile;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void dispatchTakePictureIntent(@NonNull Context context, @NonNull Activity activity, int requestCode) {
        Intent intent = createDispatchTakePictureIntent(context);
        if (intent != null) {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    public static String generateFilename(@Nullable String prefix, @Nullable String surfix, boolean toLowercase) {
        String filename = (prefix != null ? prefix : "") + generateRandomText(32) + (surfix != null ? "." + surfix : "");
        return toLowercase ? filename.toLowerCase() : filename.toUpperCase();
    }

    public static String generateCacheFilename(@NonNull Context context, @Nullable String prefix, @Nullable String surfix, boolean toLowercase) {
        String filename = context.getCacheDir().getAbsolutePath() + (prefix != null ? prefix : "") + generateRandomText(32) + (surfix != null ? "." + surfix : "");
        return toLowercase ? filename.toLowerCase() : filename.toUpperCase();
    }

    @SuppressLint("ObsoleteSdkInt")
    public static boolean isBatteryOptimizingApp(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            String packageName = context.getPackageName();
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return powerManager.isIgnoringBatteryOptimizations(packageName);
        }
        return true; // Return true on older Android versions as the permission is not needed
    }

    @SuppressLint({"BatteryLife", "ObsoleteSdkInt"})
    public static void requestAppBatteryOptimization(@NonNull Context context, @NonNull Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            Intent optimizationIntent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            optimizationIntent.setData(Uri.parse("package:" + context.getPackageName()));
            activity.startActivityForResult(optimizationIntent, requestCode);
        }
    }

    public static <T extends DeviceAdminReceiver> void requestDeviceAdminPermission(@NonNull Context context, @NonNull Activity activity, Class<T> deviceAdminClass, @NonNull String explanation, int requestCode) {
        if (!isDeviceAdminEnabled(context, deviceAdminClass)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            ComponentName componentName = new ComponentName(context, deviceAdminClass);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, explanation);
            activity.startActivityForResult(intent, requestCode);
        }
    }

    public static <T extends DeviceAdminReceiver> boolean isDeviceAdminEnabled(@NonNull Context context, Class<T> deviceAdminClass) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (devicePolicyManager != null) {
            ComponentName componentName = new ComponentName(context, deviceAdminClass);
            return devicePolicyManager.isAdminActive(componentName);
        }
        return false;
    }

    public static void clearAppFromRecent(@NonNull Context context) {
        ClearRecentUtils clearRecentUtils = new ClearRecentUtils(context);
        clearRecentUtils.clearAppFromRecent();
    }

    public static void clearAppCache(@NonNull Context context) {
        ClearCacheUtils clearCacheUtils = new ClearCacheUtils(context);
        clearCacheUtils.clearInternalCache();
        clearCacheUtils.clearExternalCache();
    }

    public static boolean isActivityDestroyed(@NonNull Context context) {
        if (context instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) context;
            return activity.isFinishing() || activity.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED;
        }
        return true;
    }

    public static String executeCommand(@NonNull String command) {
        Process process = null;
        try {
            StringBuilder builder = new StringBuilder();
            // Execute the command
            process = Runtime.getRuntime().exec(command);
            // Obtain input, output, and error streams
            OutputStream outputStream = process.getOutputStream();
            InputStream inputStream = process.getInputStream();
            InputStream errorStream = process.getErrorStream();
            // Read output lines
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            // Wait for the command to finish
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return builder.toString();
            } else {
                return null;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    public static boolean isPackageLaunchable(@NonNull Context context, @NonNull String packageName) {
        PackageManager pm = context.getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
        return launchIntent != null;
    }

    public static String getApplicationLabel(@NonNull Context context) {
        String label = null;
        try {
            String packageName = context.getPackageName(); // Get the application's package name
            PackageManager packageManager = context.getPackageManager(); // Get the PackageManager
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0); // Retrieve the ApplicationInfo
            label = packageManager.getApplicationLabel(applicationInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return label;
    }

    public static String getInstanceOf(@Nullable Object object) {
        String instance = "Unkown";
        if (object instanceof String) {
            instance = "String";
        } else if (object instanceof Integer) {
            instance = "Integer";
        } else if (object instanceof Float) {
            instance = "Float";
        } else if (object instanceof Double) {
            instance = "Double";
        } else if (object instanceof Long) {
            instance = "Long";
        }
        return instance;
    }

    public static boolean isValidAPKFile(File file) {
        if (isFile(file.getAbsolutePath())) {
            try {
                ApkInfoExtractor info = new ApkInfoExtractor(file);
                info.getAppInfo();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean isValidAPKFile(@NonNull String filename) {
        return isValidAPKFile(new File(filename));
    }

    public static boolean notNull(@Nullable Object arg) {
        return !isNull(arg);
    }

    public static boolean isNull(@Nullable Object arg) {
        return arg == null;
    }

    public static String getBasename(@NonNull String absolutePath) {
        return AdvanceUtils.getPathLastSegment(absolutePath);
    }


    // PRIVATE METHODS
    /* ----------------------------------------------------------------- */

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = MediaStore.Files.FileColumns.DATA;
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int columnIndex = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(columnIndex);
            }
        } catch (Throwable ignored) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
}