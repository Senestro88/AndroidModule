package com.official.senestro.core;

import android.util.Log;
import androidx.annotation.NonNull;
import com.official.senestro.core.utils.AdvanceUtils;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;

public class ZipManager {
    private static final String TAG = ZipManager.class.getName();

    /**
     * Extracts all files from a ZIP archive.
     *
     * @param zipFilePath   Path to the ZIP file.
     * @param destDirectory Destination directory to extract files.
     * @param skipDestFile  If true, skips extracting files that already exist.
     * @return true if extraction is successful, false otherwise.
     */
    public static boolean unzipAll(@NonNull String zipFilePath, @NonNull String destDirectory, boolean skipDestFile) {
        try {
            ZipFile zipFile = new ZipFile(zipFilePath);
            if (zipFile.isValidZipFile()) {
                // Loop through each file in the ZIP
                for (FileHeader fileHeader : zipFile.getFileHeaders()) {
                    File destFile = new File(destDirectory, fileHeader.getFileName());
                    // Skip existing files if skipDestFile is true
                    if (skipDestFile && destFile.exists()) {
                        continue;
                    }
                    // Extract the file
                    zipFile.extractFile(fileHeader, destDirectory);
                }
                return true;
            }
        } catch (ZipException exception) {
            Log.e(TAG, exception.getMessage(), exception);
        }
        return false;
    }

    /**
     * Extracts a specific entry from a ZIP archive.
     *
     * @param zipFilePath    Path to the ZIP file.
     * @param entryToExtract The specific entry to extract.
     * @param skipDestFile   If true, skips extracting the file if it already exists.
     * @param destDirectory  Destination directory to extract the entry.
     * @return true if the extraction is successful or the file is skipped due to skipDestFile,
     * false if an error occurs or the entry does not exist.
     */
    public static boolean unzipEntry(@NonNull String zipFilePath, @NonNull String entryToExtract, boolean skipDestFile, @NonNull String destDirectory) {
        try {
            ZipFile zipFile = new ZipFile(zipFilePath);
            if (zipFile.isValidZipFile()) {
                FileHeader fileHeader = zipFile.getFileHeader(entryToExtract);
                if (AdvanceUtils.isNull(fileHeader)) {
                    System.err.println("Entry '" + entryToExtract + "' not found in the ZIP file.");
                    return false;
                }
                File destFile = new File(destDirectory, fileHeader.getFileName());
                // Skip existing file if skipDestFile is true
                if (skipDestFile && destFile.exists()) {
                    return true;
                }
                // Extract the file
                zipFile.extractFile(fileHeader, destDirectory);
                return true;
            }
        } catch (ZipException exception) {
            Log.e(TAG, exception.getMessage(), exception);
        }
        return false;
    }

    /**
     * Extracts a specific entry from a ZIP archive.
     *
     * @param zipFilePath    Path to the ZIP file.
     * @param entryToExtract The specific entry to extract.
     * @param skipDestFile   If true, skips extracting the file if it already exists.
     * @param destDirectory  Destination directory to extract the entry.
     * @param newName        The new name is destination directory
     * @return true if the extraction is successful or the file is skipped due to skipDestFile,
     * false if an error occurs or the entry does not exist.
     */
    public static boolean unzipEntry(@NonNull String zipFilePath, @NonNull String entryToExtract, boolean skipDestFile, @NonNull String destDirectory, @NonNull String newName) {
        try {
            ZipFile zipFile = new ZipFile(zipFilePath);
            if (zipFile.isValidZipFile()) {
                FileHeader fileHeader = zipFile.getFileHeader(entryToExtract);
                if (AdvanceUtils.isNull(fileHeader)) {
                    System.err.println("Entry '" + entryToExtract + "' not found in the ZIP file.");
                    return false;
                }
                File destFile = new File(destDirectory, newName);
                // Skip existing file if skipDestFile is true
                if (skipDestFile && destFile.exists()) {
                    return true;
                }
                // Extract the file
                zipFile.extractFile(fileHeader, destDirectory, newName);
                return true;
            }
        } catch (ZipException exception) {
            Log.e(TAG, exception.getMessage(), exception);
        }
        return false;
    }

}
