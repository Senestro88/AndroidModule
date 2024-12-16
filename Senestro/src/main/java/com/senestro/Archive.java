package com.senestro;

import com.senestro.annotations.NonNull;
import com.senestro.annotations.Nullable;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for handling archive creation (ZIP and TAR formats). Provides
 * methods to create, extract, and encrypt archives.
 *
 * @author Senestro
 */
public class Archive {

    // Constants for file processing
    public static final int BUFFER_SIZE = 2097152; // 2MB
    private static final String DIR_SEPARATOR = "/";
    private final static String ZIP_EXTENSION = ".zip";
    private final static String TAR_EXTENSION = ".tar";
    private final static String TAR_GZ_EXTENSION = ".tar.gz";
    private final static String BZIP2_EXTENSION = ".bz2";

    // Private constructor to prevent instantiation
    private Archive() {
    }

    // ========================= ZIP METHODS =========================
    /**
     * Creates a ZIP archive from files within a directory.
     *
     * @param file The output ZIP file.
     * @param dirPath The directory to include files from.
     * @param password Optional password for encryption.
     * @param comment Optional comment for the ZIP.
     * @param compressToBz2 Flag to specify if the archive should be compressed
     * to Bzip2.
     * @return A XFile object representing the created ZIP file.
     * @throws IOException If an I/O error occurs during the creation process.
     */
    public static XFile createZipFromPath(@NonNull File file, @Nullable String dirPath, @Nullable String password, @Nullable String comment, boolean compressToBz2) throws IOException {
        List<File> files = getFilesFromDirectoryRecursively(dirPath);
        return createZipArchive(file, dirPath, files, password, comment, compressToBz2);
    }

    /**
     * Creates a ZIP archive from a list of files.
     *
     * @param file The output ZIP file.
     * @param files List of files to include.
     * @param password Optional password for encryption.
     * @param comment Optional comment for the ZIP.
     * @param compressToBz2 Flag to specify if the archive should be compressed
     * to Bzip2.
     * @return A XFile object representing the created ZIP file.
     * @throws IOException If an I/O error occurs during the creation process.
     */
    public static XFile createZipFromFiles(@NonNull File file, @Nullable List<File> files, @Nullable String password, @Nullable String comment, boolean compressToBz2) throws IOException {
        return createZipArchive(file, null, files, password, comment, compressToBz2);
    }

    /**
     * Extracts files from a ZIP archive.
     *
     * @param file The input ZIP file.
     * @param destPath The destination directory.
     * @param entry Optional specific entry to extract.
     * @param password Optional password for encrypted archives.
     * @return True if extraction succeeds, false otherwise.
     * @throws IOException If an I/O error occurs during extraction.
     */
    public static boolean extractZip(@NonNull File file, @NonNull String destPath, @Nullable String entry, @Nullable String password) throws IOException {
        Utils.createDirectory(destPath);
        boolean extracted = false;
        // Normalize entry for cross-platform compatibility
        final String extract = entry != null ? entry.replace("\\", "/") : null;
        // Create a ZipFile object
        try (ZipFile zip = new ZipFile(file)) {
            // Check if the ZIP file is password-protected
            if (zip.isEncrypted()) {
                if (password == null || password.isEmpty()) {
                    throw new IllegalArgumentException("Password required for this ZIP file to be extracted.");
                }
                // Set the password for extraction
                zip.setPassword(password.toCharArray());
            }
            if (extract != null && !extract.isEmpty()) {
                // Check if the entry exists in the ZIP
                boolean extractExists = zip.getFileHeaders().stream().anyMatch(header -> header.getFileName().equals(extract));
                if (!extractExists) {
                    throw new IllegalArgumentException("Specified entry not found in the ZIP file: " + extract);
                }
                // Extract the specific entry
                zip.extractFile(extract, destPath);
            } else {
                // Extract all files to the destination directory
                zip.extractAll(destPath);
            }
            // Update flag if extraction succeeds
            extracted = true;
        } catch (ZipException exception) {
            String message = exception.getMessage();
            throw new IOException("Error extracting ZIP file: " + String.valueOf(message), exception);
        }
        return extracted;
    }

    // ========================= TAR METHODS =========================
    /**
     * Creates a TAR archive from files within a directory.
     *
     * @param file The output TAR file.
     * @param dirPath The directory to include files from.
     * @param compressToGz Flag to specify if the archive should be compressed
     * to Gzip.
     * @param compressToBz2 Flag to specify if the archive should be compressed
     * to Bzip2.
     * @return A XFile object representing the created TAR file.
     * @throws IOException If an I/O error occurs during the creation process.
     */
    public static XFile createTarFromPath(@NonNull File file, @Nullable String dirPath, boolean compressToGz, boolean compressToBz2) throws IOException {
        List<File> files = getFilesFromDirectoryRecursively(dirPath);
        return createTarArchive(file, dirPath, files, compressToGz, compressToBz2);
    }

    /**
     * Creates a TAR archive from a list of files.
     *
     * @param file The output TAR file.
     * @param files List of files to include.
     * @param compressToGz Flag to specify if the archive should be compressed
     * to Gzip.
     * @param compressToBz2 Flag to specify if the archive should be compressed
     * to Bzip2.
     * @return A XFile object representing the created TAR file.
     * @throws IOException If an I/O error occurs during the creation process.
     */
    public static XFile createTarFromFiles(@NonNull File file, @Nullable List<File> files, boolean compressToGz, boolean compressToBz2) throws IOException {
        return createTarArchive(file, null, files, compressToGz, compressToBz2);
    }

    // ========================= BZ2 METHOD =========================
    /**
     * Creates a Bzip2 compressed archive from a file.
     *
     * @param in The input file.
     * @param out The output compressed file.
     * @return A XFile object representing the created Bzip2 file.
     * @throws IOException If an I/O error occurs during compression.
     */
    public static XFile createBzip2Archive(@NonNull File in, @NonNull File out) throws IOException {
        // Append Bzip2 extension
        out = appendExtension(out, BZIP2_EXTENSION);
        // Delete existing file if it exists
        Utils.delete(out);
        try (RandomAccessFile raf = new RandomAccessFile(in, "r"); BZip2CompressorOutputStream bcos = new BZip2CompressorOutputStream(new FileOutputStream(out))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = raf.read(buffer)) != -1) {
                // Compress the file data
                bcos.write(buffer, 0, bytesRead);
            }
            if (Utils.isFile(out)) {
                // Return the compressed file object
                return new XFile(out);
            }
        }
        return null;
    }

    // ========================= PRIVATE HELPER METHODS =========================
    /**
     * Helper method to retrieve files from a given directory.
     *
     * @param dirPath The directory path to retrieve files from.
     * @return A list of files found in the directory (empty list if none).
     */
    private static List<File> getFilesFromDirectoryRecursively(@Nullable String dirPath) {
        List<File> files = new ArrayList<>();
        if (dirPath != null) {
            List<XFile> lists = Utils.listDir(dirPath, true);
            for (XFile mod : lists) {
                files.add(mod);
            }
        }
        return files;
    }

    /**
     * Normalizes file paths for cross-platform compatibility.
     *
     * @param pathname The original path.
     * @return The normalized path.
     */
    private static String normalizePath(@Nullable String pathname) {
        return pathname == null ? null : pathname.replaceAll("[\\\\/]", DIR_SEPARATOR);
    }

    /**
     * Removes all extensions from a file's name while preserving its directory
     * structure.
     *
     * @param file the {@link File} object to process. Must not be null.
     * @return a {@link String} representing the file path with all extensions
     * removed. If the file is in a directory, the directory path is included in
     * the result. If the file has no extensions, the original file name is
     * returned.
     * @throws IllegalArgumentException if the input file is null.
     */
    public static String removeExtensions(@NonNull File file) {
        // Normalize the parent path (handle null parent paths)
        String parentPath = normalizePath(file.getParent());
        // Extract the file name from the full path
        String name = file.getName();
        // Split the file name by "."
        String[] parts = name.split("\\.");
        // Get the first part (before the first dot) or just the name if not dots are found
        String first = parts.length > 0 ? parts[0] : name;
        // Reconstruct the new path without extensions
        return (parentPath == null ? "" : parentPath + DIR_SEPARATOR) + first;
    }

    /**
     * Appends the specified extension to the file's absolute path. If the file
     * already has an extension, it will be replaced by the new extension. This
     * method ensures that the final file name includes the given extension,
     * regardless of the original file's name.
     *
     * @param file The {@link XFile} object to which the extension will be
     * appended. Must not be null.
     * @param extension The extension to append. Must not be null and typically
     * starts with a dot (e.g., ".bz2").
     * @return A new {@link XFile} object with the extension appended to the
     * original file's path.
     * @throws IllegalArgumentException If either {@code file} or
     * {@code extension} is null.
     */
    public static XFile appendExtension(@NonNull File file, @NonNull String extension) {
        String absolutePath = normalizePath(file.getAbsolutePath());
        return new XFile(absolutePath + BZIP2_EXTENSION);
    }

    /**
     * Deletes the specified file and then creates a new, empty file at the same
     * location. This method is used to ensure that the file is fresh before
     * writing to it, effectively overwriting any existing file.
     *
     * @param file The {@link File} to delete and recreate. Must not be null.
     * @throws IOException If an I/O error occurs while attempting to delete or
     * create the file.
     */
    private static void recreateFile(@NonNull File file) throws IOException {
        // Delete the existing file
        file.delete();
        // Create a new, empty file
        file.createNewFile();
    }

    /**
     * Creates a ZIP archive from a list of files with optional encryption and
     * comments. The resulting ZIP file can include files with relative paths
     * based on an optional root directory.
     *
     * @param file The output ZIP file. The file extension will be automatically
     * set to ".zip" if not already present.
     * @param rootDir Optional root directory used to generate relative paths
     * for files within the ZIP.
     * @param files List of {@link File} objects to include in the ZIP archive.
     * May be null or empty.
     * @param password Optional password for encrypting the contents of the ZIP
     * file. If null or empty, no encryption is applied.
     * @param comment Optional comment to be added to the ZIP file. May be null.
     * @param compressToBz2 A boolean flag indicating whether to compress the
     * resulting ZIP file into a BZIP2 archive.
     * @return A {@link XFile} object representing the created ZIP file, or
     * null if an error occurs.
     * @throws IOException If an I/O error occurs during ZIP creation or file
     * operations.
     */
    private static XFile createZipArchive(@NonNull File file, @Nullable String rootDir, @Nullable List<File> files, @Nullable String password, @Nullable String comment, boolean compressToBz2) throws IOException {
        file = new File(removeExtensions(file) + ZIP_EXTENSION);
        // Recreate the file
        recreateFile(file);
        // Create a ZipFile object with the desired output path
        ZipFile zip = new ZipFile(file);
        // Set a comment for the entire ZIP
        zip.setComment(comment == null ? "" : comment);
        // Check if a password is provided
        if (password != null && !password.isEmpty()) {
            zip.setPassword(password.toCharArray());
        }
        // Configure parameters for ZIP creation
        ZipParameters parameters = new ZipParameters();
        // Enable encryption if password is provided
        parameters.setEncryptFiles(password != null && !password.isEmpty());
        // Use AES encryption
        parameters.setEncryptionMethod(EncryptionMethod.AES);
        // Compression level
        parameters.setCompressionLevel(CompressionLevel.NORMAL);
        if (files != null) {
            for (File list : files) {
                if (list.isFile() && list.canRead()) {
                    String absolutePath = list.getAbsolutePath();
                    String relativePath = normalizePath(rootDir == null ? file.getName() : absolutePath.substring(rootDir.length() + 1));
                    parameters.setFileNameInZip(relativePath);
                    zip.addFile(list, parameters);
                }
            }
        }
        if (Utils.isFile(file)) {
            return compressToBz2 ? createBzip2Archive(file, file) : new XFile(file);
        }
        return null;
    }

    /**
     * Creates a TAR archive from a list of files with optional compression to
     * GZIP or BZIP2 formats. The resulting TAR file can include files with
     * relative paths based on an optional root directory.
     *
     * @param file The output TAR file. The file extension will be set based on
     * the compression type (".tar.gz" or ".tar").
     * @param rootDir Optional root directory used to generate relative paths
     * for files within the TAR.
     * @param files List of {@link File} objects to include in the TAR archive.
     * May be null or empty.
     * @param compressToGz A boolean flag indicating whether to compress the
     * resulting TAR file with GZIP.
     * @param compressToBz2 A boolean flag indicating whether to compress the
     * resulting TAR file with BZIP2.
     * @return A {@link XFile} object representing the created TAR file, or
     * null if an error occurs.
     * @throws IOException If an I/O error occurs during TAR creation or file
     * operations.
     */
    private static XFile createTarArchive(@NonNull File file, @Nullable String rootDir, @Nullable List<File> files, boolean compressToGz, boolean compressToBz2) throws IOException {
        file = new File(removeExtensions(file) + (compressToGz ? TAR_GZ_EXTENSION : TAR_EXTENSION));
        // Recreate the file
        recreateFile(file);
        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(Paths.get(file.getAbsolutePath()))); TarArchiveOutputStream taos = new TarArchiveOutputStream(compressToGz ? new GzipCompressorOutputStream(bos) : bos)) {
            // Set long file mode to Gnu to support long filenames
            taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            if (files != null) {
                for (File list : files) {
                    String absPath = list.getAbsolutePath();
                    String relativePath = normalizePath(rootDir == null ? file.getName() : absPath.substring(rootDir.length() + 1));
                    Path path = Path.of(absPath);
                    TarArchiveEntry entry = new TarArchiveEntry(path.toFile(), relativePath);
                    entry.setSize(list.length());
                    taos.putArchiveEntry(entry);
                    // Copy file to TarArchiveOutputStream
                    Files.copy(path, taos);
                    taos.closeArchiveEntry();
                }
                taos.finish();
            }
            if (Utils.isFile(file)) {
                return compressToBz2 ? createBzip2Archive(file, file) : new XFile(file);
            }
        }
        return null;
    }
}
