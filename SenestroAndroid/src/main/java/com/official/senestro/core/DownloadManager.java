package com.official.senestro.core;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import com.official.senestro.core.utils.AdvanceUtils;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadManager {
    private static final Logger log = LoggerFactory.getLogger(DownloadManager.class);
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static Database database;

    private static final ConcurrentHashMap<String, ArrayList<Callback>> callbacks = new ConcurrentHashMap<>();
    private static final ArrayList<String> cancelFlags = new ArrayList<>();

    private static final int SOCKET_TIMEOUT = 6000;
    private static final int READ_TIMEOUT = 6000;

    public static void setDatabase(@NonNull Context context) {
        DownloadManager.context = context;
        DownloadManager.database = new Database(context);
    }

    /**
     * Add a callback for a specific download URL
     *
     * @param url
     * @param callback
     */
    public static void onCallback(@NonNull String url, @NonNull Callback callback) {
        ArrayList<Callback> urlcallbacks = getCallbacksForUrl(url);
        // Checks if the exact reference is not in the list
        if (!callbackInList(urlcallbacks, callback)) {
            urlcallbacks.add(callback);
        }
    }

    /**
     * Removes a specific callback for a given download URL.
     *
     * @param url
     * @param callback
     */
    public static void offCallback(@NonNull String url, @NonNull Callback callback) {
        ArrayList<Callback> urlcallbacks = getCallbacksForUrl(url);
        // Checks if the exact reference is already in the list
        if (callbackInList(urlcallbacks, callback)) {
            urlcallbacks.remove(callback);
            if (urlcallbacks.isEmpty()) {
                callbacks.remove(url);
            }
        }
    }

    /**
     * Removes all callbacks for a given download URL.
     *
     * @param url
     */
    public static void offCallbacks(@NonNull String url) {
        callbacks.remove(url);
    }

    /**
     * Removes all callbacks
     */
    public static void offCallbacks() {
        callbacks.clear();
    }

    /**
     * Get a download status by the given url
     *
     * @param url
     * @return {@code Status}
     */
    public static Status getDownloadStatus(@NonNull String url) {
        String status = queryDownloadByColumn(url, Database.COLUMN_STATUS);
        return notNull(status) ? Status.valueOf(status) : Status.UNDEFINED;
    }

    /**
     * Get a saved download total bytes
     *
     * @param url
     * @return -1 or total bytes
     */
    public static long getSavedDownloadTotalBytes(@NonNull String url) {
        String bytes = queryDownloadByColumn(url, Database.COLUMN_TOTAL_BYTES);
        return notNull(bytes) ? Long.parseLong(bytes) : -1;
    }

    /**
     * Get all downloads metadata
     *
     * @return {@code ArrayList<Metadata>}
     */
    public static ArrayList<Metadata> getAllDownloads() {
        ArrayList<Metadata> metadata = new ArrayList<>();
        if (databaseValid()) {
            SQLiteDatabase db = database.getReadableDatabase();
            Cursor cursor = db.query(Database.TABLE, null, null, null, null, null, null);
            metadata = getDownloadByCursor(cursor);
        }
        return metadata;
    }

    /**
     * Get a download metadata by the given url
     *
     * @param url
     * @return {@code Metadata}
     */
    public static Metadata getDownload(@NonNull String url) {
        Metadata metadata = new Metadata();
        if (databaseValid()) {
            SQLiteDatabase db = database.getReadableDatabase();
            Cursor cursor = db.query(Database.TABLE, null, Database.COLUMN_URL + "=?", new String[]{url}, null, null, null);
            ArrayList<Metadata> downloads = getDownloadByCursor(cursor);
            if (!downloads.isEmpty()) {
                metadata = getFirstMetadata(downloads);
            }
        }
        return metadata;
    }

    public static Metadata getFirstMetadata(@NonNull ArrayList<Metadata> metadata) {
        try {
            return metadata.iterator().next();
        } catch (Throwable throwable) {
            return new Metadata();
        }
    }

    /**
     * Get all active downloads
     *
     * @return {@code ArrayList<Metadata>}
     */
    public static ArrayList<Metadata> getActiveDownloads() {
        ArrayList<Metadata> active = new ArrayList<>();
        if (databaseValid()) {
            SQLiteDatabase db = database.getReadableDatabase();
            Cursor cursor = db.query(Database.TABLE, null, Database.COLUMN_STATUS + "=?", new String[]{getStatusName(Status.DOWNLOADING)}, null, null, null);
            active = getDownloadByCursor(cursor);
        }
        return active;
    }

    /**
     * Get all completed downloads
     *
     * @return {@code ArrayList<Metadata>}
     */
    public static ArrayList<Metadata> getCompletedDownloads() {
        ArrayList<Metadata> active = new ArrayList<>();
        if (databaseValid()) {
            SQLiteDatabase db = database.getReadableDatabase();
            Cursor cursor = db.query(Database.TABLE, null, Database.COLUMN_STATUS + "=?", new String[]{getStatusName(Status.COMPLETED)}, null, null, null);
            active = getDownloadByCursor(cursor);
        }
        return active;
    }

    public static void pauseDownload(@NonNull String url) {
        Metadata metadata = getDownload(url);
        if (metadata.valid()) {
            Status status = metadata.getStatus();
            if (status.equals(Status.DOWNLOADING)) {
                updateDownloadStatus(url, Status.PAUSED);
            } else {
                notifyMessage(url, "The download must be running.");
            }
        } else {
            notifyMessage(url, "The metadata is not found.");
        }
    }

    public static void resumeDownload(@NonNull String url) {
        Metadata metadata = getDownload(url);
        if (metadata.valid()) {
            Status status = metadata.getStatus();
            if (status.equals(Status.COMPLETED)) {
                notifyComplete(url);
            } else if (status.equals(Status.PAUSED) || status.equals(Status.QUEUED)) {
                submitDownload(url);
            } else {
                notifyMessage(url, "Failed to resume download.");
            }
        } else {
            notifyMessage(url, "The metadata is not found.");
        }
    }

    public static void cancelDownload(@NonNull String url) {
        Metadata metadata = getDownload(url);
        if (metadata.valid()) {
            Status status = metadata.getStatus();
            if (!status.equals(Status.UNDEFINED)) {
                if (status.equals(Status.DOWNLOADING)) {
                    cancelFlags.add(url);
                } else {
                    cancelMetadata(url, metadata);
                }
            }
        } else {
            notifyMessage(url, "Failed to cancel the download");
        }
    }

    public static boolean addDownload(@NonNull String url, @NonNull String basename, @NonNull String downloadDir, @NonNull String extraData) {
        Metadata metadata = getDownload(url);
        if (!metadata.valid()) {
            return insertDownload(url, basename, downloadDir, getStatusName(Status.QUEUED), 0, 0, extraData);
        }
        return true;
    }

    public static void startDownload(@NonNull String url, @NonNull String basename, @NonNull String downloadDir, @NonNull String extraData) {
        if (addDownload(url, basename, downloadDir, extraData)) {
            submitDownload(url);
        } else {
            notifyMessage(url, "Failed to save download metadata.");
        }
    }

    public static int calculateProgress(long downloadedBytes, long totalBytes) {
        if (totalBytes == 0) {
            return 0; // Prevent division by zero
        } else {
            return (int) ((downloadedBytes * 100L) / totalBytes);
        }
    }

    private static void throwableFailure(@NonNull String url, @NonNull Throwable throwable) {
        String message = throwable.getMessage();
        message = AdvanceUtils.isNull(message) || message.isEmpty() ? "An error has occurred while downloading " + url : message;
        notifyError(url, message);
    }

    // PRIVATE METHODS
    private static void submitDownload(@NonNull String url) {
        // For managing multiple downloads
        ExecutorService executor = Executors.newFixedThreadPool(4);
        executor.submit(() -> {
            try {
                Metadata metadata = getDownload(url);
                if (metadata.valid()) {
                    notifyMessage(url, "Please wait...");
                    long totalBytes = getSavedDownloadTotalBytes(url);
                    totalBytes = totalBytes < 1 ? getRemoteFileBytes(url) : totalBytes;
                    if (totalBytes > 0) {
                        File file = new File(metadata.getDownloadDir(), metadata.getBasename());
                        long initialDownloadedBytes = file.length();
                        updateDownloadedBytes(url, initialDownloadedBytes);
                        updateTotalBytes(url, totalBytes);
                        Request.Builder builder = new Request.Builder();
                        builder.url(url);
                        // Resume from the last byte
                        builder.addHeader("Range", "bytes=" + initialDownloadedBytes + "-");
                        Request request = builder.build();
                        OkHttpClient client = AdvanceUtils.getUnsafeOkHttpClient(null, SOCKET_TIMEOUT, READ_TIMEOUT);
                        Call call = client.newCall(request);
                        enqueueDownload(url, file, initialDownloadedBytes, totalBytes, call);
                    } else {
                        notifyMessage(url, "Failed to get the remote file size.");
                    }
                } else {
                    notifyMessage(url, "Failed to submit the download request, metadata not found.");
                }
            } catch (Throwable throwable) {
                throwableFailure(url, throwable);
            }
        });
    }

    private static void enqueueDownload(@NonNull String url, @NonNull File file, long initialDownloadedBytes, long totalBytes, @NonNull Call call) {
        call.enqueue(new okhttp3.Callback() {
            private void throwableFailure(@NonNull Throwable throwable) {
                String message = throwable.getMessage();
                message = AdvanceUtils.isNull(message) || message.isEmpty() ? "An error has occurred while downloading " + url : message;
                notifyErrorWithPausedState(message);
            }

            private void notifyErrorWithPausedState(@NonNull String message) {
                updateDownloadStatus(url, Status.PAUSED);
                notifyError(url, message);
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException exception) {
                throwableFailure(exception);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    if (!response.isSuccessful()) {
                        notifyErrorWithPausedState("HTTP Error: " + response.code());
                    } else {
                        notifyStart(url);
                        updateDownloadStatus(url, Status.DOWNLOADING);
                        updateDownloadedBytes(url, initialDownloadedBytes);
                        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file, true)); InputStream in = response.body().byteStream()) {
                            byte[] buffer = new byte[1024 * 64]; // 64KB buffer
                            int bytesRead;
                            long lastUpdateTime = System.currentTimeMillis();
                            long downloadedBytes = initialDownloadedBytes;
                            long dBytes = 0;
                            while ((bytesRead = in.read(buffer)) != -1) {
                                Status status = getDownloadStatus(url);
                                if (status.equals(Status.PAUSED)) {
                                    notifyPause(url);
                                    // Pause the download and break out of the loop
                                    break;
                                } else if (cancelFlags.contains(url)) {
                                    cancelFlags.remove(url);
                                    cancel(url);
                                    // Cancel the download and break out of the loop
                                    break;
                                } else {
                                    out.write(buffer, 0, bytesRead);
                                    downloadedBytes += bytesRead;
                                    dBytes += bytesRead;
                                    // Speed and ETA Calculation
                                    // Calculate elapsed time in seconds
                                    long currentTime = System.currentTimeMillis();
                                    long elapsedTime = currentTime - lastUpdateTime;
                                    // Update every second
                                    if (currentTime - lastUpdateTime >= 100) {
                                        // Speed in bytes per second
                                        double speedBps = (double) (dBytes * 100) / elapsedTime;
                                        // Convert ETA to milliseconds
                                        long etaMillis = (long) ((totalBytes - downloadedBytes) / speedBps * 1000);
                                        // Calculate progress percentage
                                        int progress = calculateProgress(downloadedBytes, totalBytes);
                                        // Notify progress and update database
                                        notifyProgress(url, downloadedBytes, totalBytes, speedBps, etaMillis, progress);
                                        updateDownloadedBytes(url, downloadedBytes);
                                        // ArrayList the last update time to the current time
                                        dBytes = 0;
                                        lastUpdateTime = currentTime;
                                    }
                                }
                            }
                            // ArrayList the final downloaded bytes and check if the download is completed
                            updateDownloadedBytes(url, downloadedBytes);
                            if (downloadedBytes >= totalBytes) {
                                updateDownloadStatus(url, Status.COMPLETED);
                                AdvanceTimer.schedule(() -> notifyComplete(url), 500);
                            }
                        }
                    }
                } catch (Throwable throwable) {
                    throwableFailure(throwable);
                } finally {
                    AdvanceUtils.closeQuietly(response);
                }
            }
        });
    }

    private static void cancel(@NonNull String url) {
        Metadata metadata = getDownload(url);
        if (metadata.valid()) {
            cancelMetadata(url, metadata);
        }
    }

    private static String value(@NonNull Object o) {
        return String.valueOf(o);
    }

    private static void cancelMetadata(@NonNull String url, @NonNull Metadata metadata) {
        AdvanceUtils.delete(new File(metadata.getDownloadDir(), metadata.getBasename()));
        notifyCancel(url);
        deleteDownload(url);
    }

    /**
     * Get download callbacks for the given url
     *
     * @param url
     * @return {@code ArrayList<Callback>}
     */
    private static ArrayList<Callback> getCallbacksForUrl(@NonNull String url) {
        ArrayList<Callback> urlcallbacks = callbacks.get(url);
        if (isNull(urlcallbacks)) {
            urlcallbacks = new ArrayList<>();
            callbacks.put(url, urlcallbacks);
        }
        return urlcallbacks;
    }

    private static String getStatusName(@NonNull Status status) {
        return status.name();
    }

    // Utility methods for notifying callbacks
    private static void notifyStart(@NonNull String url) {
        if (callbacks.containsKey(url)) {
            for (Callback callback : Objects.requireNonNull(callbacks.get(url))) {
                postOnMainThread(() -> callback.onStart(url));
            }
        }
    }

    private static void notifyProgress(@NonNull String url, long downloadedBytes, long totalBytes, double speedBps, long etaMillis, int progress) {
        if (callbacks.containsKey(url)) {
            for (Callback callback : Objects.requireNonNull(callbacks.get(url))) {
                postOnMainThread(() -> callback.onProgress(url, downloadedBytes, totalBytes, speedBps, etaMillis, progress));
            }
        }
    }

    private static void notifyPause(@NonNull String url) {
        if (callbacks.containsKey(url)) {
            for (Callback callback : Objects.requireNonNull(callbacks.get(url))) {
                postOnMainThread(() -> callback.onPause(url));
            }
        }
    }

    private static void notifyComplete(@NonNull String url) {
        if (callbacks.containsKey(url)) {
            for (Callback callback : Objects.requireNonNull(callbacks.get(url))) {
                postOnMainThread(() -> callback.onComplete(url));
            }
        }
    }

    private static void notifyCancel(@NonNull String url) {
        if (callbacks.containsKey(url)) {
            for (Callback callback : Objects.requireNonNull(callbacks.get(url))) {
                postOnMainThread(() -> callback.onCancel(url));
            }
        }
    }

    private static void notifyMessage(@NonNull String url, @NonNull String message) {
        if (callbacks.containsKey(url)) {
            for (Callback callback : Objects.requireNonNull(callbacks.get(url))) {
                postOnMainThread(() -> callback.onMessage(url, message));
            }
        }
    }

    private static void notifyError(@NonNull String url, @NonNull String message) {
        if (callbacks.containsKey(url)) {
            for (Callback callback : Objects.requireNonNull(callbacks.get(url))) {
                postOnMainThread(() -> callback.onError(url, message));
            }
        }
    }

    private static boolean updateDownloadStatus(@NonNull String url, @NonNull Status status) {
        return updateDownloadByColumn(url, Database.COLUMN_STATUS, status.name());
    }

    private static boolean updateDownloadedBytes(@NonNull String url, long downloadedBytes) {
        return updateDownloadByColumn(url, Database.COLUMN_DOWNLOADED_BYTES, value(downloadedBytes));
    }

    private static boolean updateTotalBytes(@NonNull String url, long totalBytes) {
        return updateDownloadByColumn(url, Database.COLUMN_TOTAL_BYTES, value(totalBytes));
    }

    private static boolean updateDownloadByColumn(@NonNull String url, @NonNull String column, @NonNull String value) {
        boolean updated = false;
        Metadata metadata = getDownload(url);
        if (metadata.valid() && databaseValid()) {
            SQLiteDatabase db = database.getWritableDatabase();
            db.beginTransaction();
            try {
                ContentValues values = new ContentValues();
                values.put(column, value);
                String tablename = Database.TABLE;
                String whereclause = Database.COLUMN_URL + "=?";
                String[] whereerguments = new String[]{url};
                int affected = db.update(tablename, values, whereclause, whereerguments);
                db.setTransactionSuccessful();
                updated = affected > 0;
            } finally {
                db.endTransaction();
            }
        }
        return updated;
    }

    private static boolean insertDownload(@NonNull String url, @NonNull String basename, @NonNull String downloadDir, @NonNull String status, long downloadedBytes, long totalBytes, @NonNull String extraData) {
        boolean inserted = false;
        Metadata metadata = getDownload(url);
        if (!metadata.valid() && databaseValid()) {
            SQLiteDatabase db = database.getWritableDatabase();
            db.beginTransaction();
            try {
                ContentValues values = new ContentValues();
                values.put(Database.COLUMN_URL, url);
                values.put(Database.COLUMN_BASENAME, basename);
                values.put(Database.COLUMN_DOWNLOAD_DIR, downloadDir);
                values.put(Database.COLUMN_STATUS, status);
                values.put(Database.COLUMN_DOWNLOADED_BYTES, downloadedBytes);
                values.put(Database.COLUMN_TOTAL_BYTES, totalBytes);
                values.put(Database.COLUMN_EXTRA_DATA, extraData);
                long affected = db.insert(Database.TABLE, null, values);
                db.setTransactionSuccessful();
                inserted = affected > 0;
            } finally {
                db.endTransaction();
            }
        }
        return inserted;
    }

    /**
     * Query a download by the gievn url and column
     *
     * @param url
     * @param column
     * @return {@code String} or null
     */
    @SuppressLint("Range")
    private static String queryDownloadByColumn(@NonNull String url, @NonNull String column) {
        String result = null;
        if (databaseValid()) {
            SQLiteDatabase db = database.getWritableDatabase();
            String tablename = Database.TABLE;
            String[] columns = new String[]{column};
            String selection = Database.COLUMN_URL + "=?";
            String[] selectionarguments = new String[]{url};
            Cursor cursor = db.query(tablename, columns, selection, selectionarguments, null, null, null);
            // Retrieve the result if the record exists
            if (cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndex(column));
            }
            cursor.close();
        }
        return result;
    }

    /**
     * Get the metadatas by the given cursor
     *
     * @param cursor
     * @return {@code ArrayList<Metadata>}
     */
    @SuppressLint("Range")
    private static ArrayList<Metadata> getDownloadByCursor(@NonNull Cursor cursor) {
        ArrayList<Metadata> metadata = new ArrayList<>();
        while (cursor.moveToNext()) {
            String url = cursor.getString(cursor.getColumnIndex(Database.COLUMN_URL));
            String basename = cursor.getString(cursor.getColumnIndex(Database.COLUMN_BASENAME));
            String downloadDir = cursor.getString(cursor.getColumnIndex(Database.COLUMN_DOWNLOAD_DIR));
            String status = cursor.getString(cursor.getColumnIndex(Database.COLUMN_STATUS));
            long downloadedBytes = cursor.getLong(cursor.getColumnIndex(Database.COLUMN_DOWNLOADED_BYTES));
            long totalBytes = cursor.getLong(cursor.getColumnIndex(Database.COLUMN_TOTAL_BYTES));
            String extraData = cursor.getString(cursor.getColumnIndex(Database.COLUMN_EXTRA_DATA));
            metadata.add(new Metadata(url, basename, downloadDir, status, downloadedBytes, totalBytes, extraData));
        }
        cursor.close();
        return metadata;
    }

    private static void deleteDownload(@NonNull String url) {
        if (databaseValid()) {
            SQLiteDatabase db = database.getWritableDatabase();
            db.beginTransaction();
            try {
                db.delete(Database.TABLE, Database.COLUMN_URL + "=?", new String[]{url});
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    private static void postOnMainThread(@NonNull Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    private static boolean databaseValid() {
        return notNull(context) && notNull(database);
    }

    private static boolean callbackInList(@NonNull ArrayList<Callback> list, @NonNull Callback callback) {
        for (Callback handler : list) {
            // Reference equality check
            if (handler == callback) {
                return true;
            }
        }
        return false;
    }

    /**
     * Make a head request to get the total url file size
     *
     * @param url
     * @return total size on -1
     */
    private static long getRemoteFileBytes(@NonNull String url) {
        OkHttpClient client = AdvanceUtils.getUnsafeOkHttpClient(null, SOCKET_TIMEOUT, READ_TIMEOUT);
        // Perform a HEAD request
        Request request = new Request.Builder().url(url).head().build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                // Get the "Content-Length" header
                String contentLength = response.header("Content-Length");
                if (AdvanceUtils.notNull(contentLength)) {
                    return Long.parseLong(contentLength);
                }
            }
        } catch (IOException exception) {
            log.error("e: ", exception);
        }
        return -1; // Return -1 if the size cannot be determined
    }

    private static boolean isNull(Object o) {
        return AdvanceUtils.isNull(o);
    }

    private static boolean notNull(Object o) {
        return AdvanceUtils.notNull(o);
    }

    // PUBLIC ENUM
    public enum Status {
        UNDEFINED, QUEUED, DOWNLOADING, PAUSED, COMPLETED
    }

    // PUBLIC CLASS
    public static class Metadata {
        private String url;
        private String basename;
        private String downloadDir;
        private String status;
        private long downloadedBytes;
        private long totalBytes;
        private String extraData;

        public Metadata() {
        }

        public Metadata(@NonNull String url, @NonNull String basename, @NonNull String downloadDir, @NonNull String status, long downloadedBytes, long totalBytes, @NonNull String extraData) {
            this.url = url;
            this.basename = basename;
            this.downloadDir = downloadDir;
            this.status = status;
            this.downloadedBytes = downloadedBytes;
            this.totalBytes = totalBytes;
            this.extraData = extraData;
        }

        public boolean valid() {
            return notNull(url) && notNull(basename) && notNull(downloadDir) && notNull(status) && notNull(extraData);
        }

        public String getUrl() {
            return url;
        }

        public String getBasename() {
            return basename;
        }

        public String getDownloadDir() {
            return downloadDir;
        }

        public Status getStatus() {
            return Status.valueOf(status);
        }

        public long getDownloadedBytes() {
            return downloadedBytes;
        }

        public long getTotalBytes() {
            return totalBytes;
        }

        public String getExtraData() {
            return extraData;
        }
    }

    // PUBLIC INTERFACE
    public interface Callback {
        void onStart(@NonNull String url);

        void onProgress(@NonNull String url, long downloadedBytes, long totalBytes, double speedBps, long etaMillis, int progress);

        void onPause(@NonNull String url);

        void onComplete(@NonNull String url);

        void onCancel(@NonNull String url);

        void onMessage(@NonNull String url, @NonNull String message);

        void onError(@NonNull String url, @NonNull String message);
    }

    // PRIVATE CLASS
    private static class Database extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "download-manager-downloads.db";
        private static final int DATABASE_VERSION = 1;
        public static final String TABLE = "downloads";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_BASENAME = "basename";
        public static final String COLUMN_DOWNLOAD_DIR = "downloadDir";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_DOWNLOADED_BYTES = "downloadedBytes";
        public static final String COLUMN_TOTAL_BYTES = "totalBytes";
        public static final String COLUMN_EXTRA_DATA = "extraData";

        public Database(@NonNull Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTable = "CREATE TABLE " + TABLE + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_URL + " TEXT UNIQUE, " + COLUMN_BASENAME + " TEXT, " + COLUMN_DOWNLOAD_DIR + " TEXT, " + COLUMN_STATUS + " TEXT, " + COLUMN_DOWNLOADED_BYTES + " INTEGER, " + COLUMN_TOTAL_BYTES + " INTEGER, " + COLUMN_EXTRA_DATA + " TEXT)";
            db.execSQL(createTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE);
            onCreate(db);
        }
    }
}
