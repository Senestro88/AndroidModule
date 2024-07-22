package com.official.senestro.core;

import androidx.annotation.NonNull;

import com.official.senestro.core.utils.AdvanceUtils;
import com.official.senestro.core.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ArchiveManager {

    private static final String DIR_SEPARATOR = "/";
    private final static String ZIP_EXTENSION = "zip";
    private final static String TAR_EXTENSION = "zip";
    private static final int TAR_BLOCK_SIZE = 512;

    private ArchiveManager() {
    }

    public static FileUtils createZip(@NonNull File name, @NonNull ArrayList<File> files) throws IOException {
        name = new File(AdvanceUtils.removeExtension(name.getAbsolutePath()) + "." + ZIP_EXTENSION);
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(name))) {
            for (File file : files) {
                // Add each file to the zip
                Zip.addItem(file.getAbsolutePath(), zipOut);
            }
            if (AdvanceUtils.isFile(name)) {
                return new FileUtils(name.getAbsolutePath());
            }
        }
        return null;
    }

    public static FileUtils createTar(@NonNull File name, @NonNull ArrayList<File> files) throws IOException {
        name = new File(AdvanceUtils.removeExtension(name.getAbsolutePath()) + "." + TAR_EXTENSION);
        try (FileOutputStream os = new FileOutputStream(name)) {
            for (File file : files) {
                Tar.add(file, "", os);
                // Add two empty blocks to signify end of archive
                byte[] emptyBlock = new byte[TAR_BLOCK_SIZE];
                os.write(emptyBlock);
                os.write(emptyBlock);
            }
            if (AdvanceUtils.isFile(name)) {
                return new FileUtils(name.getAbsolutePath());
            }
        }
        return null;
    }

    // PRIVATE CLASSES
    private static class Zip {
        // Adds a file or directory to the zip file
        private static void addItem(@NonNull String filename, @NonNull ZipOutputStream zos) throws IOException {
            File file = new File(filename);
            if (file.isDirectory()) {
                // Add directory if it's a directory
                addDir(file, filename, zos);
            } else {
                // Add file if it's a file
                addFile(file, zos);
            }
        }

        // Adds a single file to the zip file
        private static void addFile(@NonNull File file, @NonNull ZipOutputStream zos) throws IOException {
            if (file.isFile()) {
                FileInputStream inputStream = new FileInputStream(file);
                // Create a zip entry for the file
                ZipEntry entry = new ZipEntry(file.getName());
                // Add the zip entry to the output stream
                zos.putNextEntry(entry);
                // Copy the file content to the zip output stream
                byte[] bytes = new byte[1024];
                int length;
                while ((length = inputStream.read(bytes)) >= 0) {
                    // Write file content to the zip output stream
                    zos.write(bytes, 0, length);
                }
                // Close the input stream
                inputStream.close();

            }
        }

        // Adds a directory and its contents to the zip file
        private static void addDir(@NonNull File directory, @NonNull String parentDirectory, @NonNull ZipOutputStream zos) throws IOException {
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (AdvanceUtils.notNull(files)) {
                    for (File file : files) {
                        if (file.isDirectory()) {
                            // Recursively add subdirectories
                            addDir(file, parentDirectory + DIR_SEPARATOR + file.getName(), zos);
                            continue;
                        }
                        FileInputStream ins = new FileInputStream(file);
                        ZipEntry entry = new ZipEntry(parentDirectory + DIR_SEPARATOR + file.getName());
                        // Create a zip entry with the correct path
                        zos.putNextEntry(entry);
                        byte[] bytes = new byte[1024];
                        int length;
                        while ((length = ins.read(bytes)) >= 0) {
                            // Write file content to the zip output stream
                            zos.write(bytes, 0, length);
                        }
                        // Close the input stream
                        ins.close();
                    }
                }
            }
        }
    }

    private static class Tar {

        private static int calculateChecksum(TarHeader header) {
            ByteBuffer buffer = ByteBuffer.allocate(TAR_BLOCK_SIZE);
            header.write(buffer);
            buffer.rewind();
            int checksum = 0;
            for (int i = 0; i < TAR_BLOCK_SIZE; i++) {
                checksum += buffer.get(i) & 0xff;
            }
            return checksum;
        }

        private static void add(@NonNull File file, @NonNull String parentPath, @NonNull FileOutputStream os) throws IOException {
            String relativePath = parentPath.isEmpty() ? file.getName() : parentPath + "/" + file.getName();
            if (file.isDirectory()) {
                addDir(file, relativePath, os);
            } else {
                addFle(file, relativePath, os);
            }
        }

        private static void addFle(@NonNull File file, @NonNull String relativePath, @NonNull FileOutputStream os) throws IOException {
            TarHeader header = new TarHeader();
            header.name = relativePath;
            header.mode = "000644"; // Typical file permissions
            header.uid = 0;
            header.gid = 0;
            header.size = file.length();
            header.mtime = file.lastModified() / 1000; // Convert to seconds
            header.typeflag = '0'; // Regular file
            header.checksum = calculateChecksum(header);
            //Write header
            ByteBuffer headerBuffer = ByteBuffer.allocate(TAR_BLOCK_SIZE);
            header.write(headerBuffer);
            os.write(headerBuffer.array());
            // Write file content
            try (FileInputStream in = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) >= 0) {
                    os.write(buffer, 0, length);
                }
            }
            // Pad to 512-byte boundary
            int paddingBytes = (int) (TAR_BLOCK_SIZE - (header.size % TAR_BLOCK_SIZE));
            if (paddingBytes != TAR_BLOCK_SIZE) {
                byte[] paddingBuffer = new byte[paddingBytes];
                os.write(paddingBuffer);
            }
        }

        private static void addDir(@NonNull File directory, @NonNull String parentPath, @NonNull FileOutputStream fos) throws IOException {
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (AdvanceUtils.notNull(files)) {
                    for (File file : files) {
                        String relativePath = parentPath.isEmpty() ? file.getName() : parentPath + "/" + file.getName();
                        if (file.isDirectory()) {
                            // Recursively process subdirectories
                            addDir(file, relativePath, fos);
                        } else {
                            addFle(file, relativePath, fos);
                        }
                    }
                }
            }
        }
    }

    // Simple Tar Header representation (adapt as needed)
    private static class TarHeader {
        String name;
        String mode;
        int uid;
        int gid;
        long size;
        long mtime;
        char typeflag;
        int checksum;

        void write(ByteBuffer buffer) {
            writeString(buffer, name, 100);
            writeString(buffer, mode, 8);
            writeOctal(buffer, uid, 8);
            writeOctal(buffer, gid, 8);
            writeOctal(buffer, size, 12);
            writeOctal(buffer, mtime, 12);
            buffer.put((byte) typeflag);
            writeString(buffer, "", 100); // Link name (unused here)
            writeString(buffer, "ustar  ", 8); // Magic and version
            writeString(buffer, "", 32); // User name (unused here)
            writeString(buffer, "", 32); // Group name (unused here)
            writeOctal(buffer, 0, 8); // Device major number (unused here)
            writeOctal(buffer, 0, 8); // Device minor number (unused here)
            writeString(buffer, "", 155); // Prefix (unused here)
            writeString(buffer, String.format("%06o\0 ", checksum), 8); // Checksum
        }

        private void writeString(ByteBuffer buffer, String value, int maxLength) {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            int length = Math.min(bytes.length, maxLength - 1);
            buffer.put(bytes, 0, length);
            buffer.put((byte) 0); // Null terminator
            for (int i = length + 1; i < maxLength; i++) {
                buffer.put((byte) 0); // Pad with zeros
            }
        }

        private void writeOctal(ByteBuffer buffer, long value, int length) {
            String octalString = String.format("%0" + (length - 1) + "o", value);
            writeString(buffer, octalString, length);
        }
    }
}