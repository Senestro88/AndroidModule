package com.official.senestro.core;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import com.official.senestro.core.utils.AdvanceUtils;

import java.io.File;

/**
 * Utility class for handling file Uris and retrieving their absolute paths.
 * This class supports Uris from various content providers and handles
 * different Android versions from API 21 to 30+.
 */
public class FileUriUtils {

    /**
     * Retrieves a File object from a given Uri.
     *
     * @param context The application context.
     * @param uri     The Uri to resolve.
     * @return A File object if the Uri points to a valid file, or null otherwise.
     */
    public static File getFileFromUri(@NonNull Context context, @NonNull Uri uri) {
        // Fallback using DocumentFile
        DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
        if (AdvanceUtils.notNull(documentFile) && documentFile.exists() && documentFile.isFile()) {
            return new File(documentFile.getUri().getPath());
        }
        return null;
    }

    /**
     * Retrieves a Uri for a given File object using FileProvider.
     *
     * @param context   The application context.
     * @param file      The File object to convert to a Uri.
     * @param authority The FileProvider authority defined in the manifest.
     * @return The Uri for the File, or null if the File is invalid.
     */
    public static Uri getUriFromFile(@NonNull Context context, @NonNull File file, @NonNull String authority) {
        if (AdvanceUtils.isNull(file) || !file.exists()) {
            return null;
        }
        return FileProvider.getUriForFile(context, authority, file);
    }

    // PRIVATE METHODS

    /**
     * Retrieves the value of the specified column (typically the file path) from the given Uri.
     *
     * @param context       The context used to access the content resolver.
     * @param uri           The Uri to query, typically pointing to a content provider.
     * @param selection     An optional SQL WHERE clause to filter the query results (nullable).
     * @param selectionArgs An optional array of arguments to replace placeholders in the WHERE clause (nullable).
     * @return The value of the column (e.g., file path) as a String, or null if the value could not be retrieved.
     * @throws SecurityException        If the app does not have the necessary permissions to access the Uri.
     * @throws IllegalArgumentException If the Uri is invalid or the column index is out of bounds.
     *                                  <p>
     *                                  Example Usage:
     *                                  <pre>
     *                                                                                                                                     Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
     *                                                                                                                                     String[] selectionArgs = new String[]{"12345"};
     *                                                                                                                                     String filePath = getColumnData(context, contentUri, "_id=?", selectionArgs);
     *                                                                                                                                     </pre>
     */
    private static String getColumnData(Context context, Uri uri, String selection, String[] selectionArgs) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                return cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Checks if a Uri authority is ExternalStorageProvider.
     *
     * @param uri The Uri to check.
     * @return True if the Uri authority is ExternalStorageProvider, false otherwise.
     */
    private static boolean isExternalStorageDocument(@NonNull Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * Checks if a Uri authority is DownloadsProvider.
     *
     * @param uri The Uri to check.
     * @return True if the Uri authority is DownloadsProvider, false otherwise.
     */
    private static boolean isDownloadsDocument(@NonNull Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * Checks if a Uri authority is MediaProvider.
     *
     * @param uri The Uri to check.
     * @return True if the Uri authority is MediaProvider, false otherwise.
     */
    private static boolean isMediaDocument(@NonNull Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
