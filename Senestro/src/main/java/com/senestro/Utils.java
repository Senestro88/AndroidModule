package com.senestro;

import com.senestro.annotations.NonNull;
import com.senestro.annotations.Nullable;
import com.senestro.interfaces.CopyBytesCallback;
import com.senestro.streams.Streamer;
import com.senestro.streams.TextStream;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * The Utilities Class
 *
 * @author Senestro
 */
public class Utils {

    private static String TAG = Utils.class.getSimpleName();
    public static int BUFFER_SIZE = 2097152; // 2MB
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final Logger LOGGER = Logger.getLogger(TAG);

    /**
     * Prevent instance initialization
     */
    private Utils() {
    }

    public static boolean notNull(@Nullable Object argument) {
        return !isNull(argument);
    }

    public static boolean isNull(@Nullable Object argument) {
        return argument == null;
    }

    public static String bytesToHex(@NonNull byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                builder.append('0');
            }
            builder.append(hex);
        }
        return builder.toString();
    }

    public static byte[] hexToBytes(@NonNull String string) {
        int len = string.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(string.charAt(i), 16) << 4) + Character.digit(string.charAt(i + 1), 16));
        }
        return bytes;
    }

    public static byte[] combineBytes(final byte[] a, final byte[] b) {
        byte[] combined = new byte[a.length + b.length];
        System.arraycopy(a, 0, combined, 0, a.length);
        System.arraycopy(b, 0, combined, a.length, b.length);
        return combined;
    }

    public static byte[] toBase64(@NonNull String data) throws Exception {
        return Base64.getEncoder().encode(data.getBytes(CHARSET));
    }

    public static byte[] fromBase64(@NonNull String data) throws Exception {
        return Base64.getDecoder().decode(data);
    }

    public static boolean isFile(@Nullable String filename) {
        return notNull(filename) && new File(filename).isFile();
    }

    public static boolean isDirectory(@Nullable String dirname) {
        return notNull(dirname) && new File(dirname).isDirectory();
    }

    public static boolean isDirectory(@Nullable File file) {
        return notNull(file) && file.isDirectory();
    }

    public static boolean isFile(@Nullable File file) {
        return notNull(file) && file.isFile();
    }

    public static String hashHmac(@NonNull byte[] dataBytes, @NonNull byte[] keyBytes) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(keyBytes, "HmacSHA256"));
        return bytesToHex(mac.doFinal(dataBytes));
    }

    public static String newHex(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytesToHex(bytes);
    }

    /**
     * Converts a long value to a byte array.
     *
     * @param value The long value to convert.
     * @return A byte array representing the long value.
     */
    public static byte[] longToBytes(long value) {
        byte[] result = new byte[8]; // A long is 8 bytes
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        return result;
    }

    public static void close(@Nullable Closeable closeable) {
        if (notNull(closeable)) {
            try {
                closeable.close();
            } catch (Exception exception) {
                LOGGER.warning(message(exception));
            }
        }
    }

    public static String getExtension(@NonNull String name) {
        String extension = "";
        int lastIndex = name.lastIndexOf(".");
        if (lastIndex != -1) {
            return name.substring(lastIndex + 1);
        }
        return extension;
    }

    public static String getBasename(@NonNull String name) {
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

    public static boolean validImage(@NonNull String path) {
        List<String> extensions = Arrays.asList("png", "jpg", "jpeg", "gif", "bmp", "webp", "jfif", "tiff", "heif", "bat", "bpg", "svg");
        return isFile(path) && extensions.contains(getExtension(path));
    }

    public static boolean validVideo(@NonNull String path) {
        List<String> extensions = Arrays.asList("mp4", "3gp", "mkv", "avi", "flv", "mov", "wmv", "webm", "vob", "ogv", "mpg", "m4v", "m2ts", "ts", "mpeg", "divx", "asf", "rm", "ram");
        return isFile(path) && extensions.contains(getExtension(path));
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
        return notNull(extension) && mimes.containsKey(extension) ? mimes.get(extension) : null;
    }

    public static void delete(@Nullable String file) {
        if (notNull(file)) {
            File f = new File(file);
            if (f.exists()) {
                if (f.isFile()) {
                    f.delete();
                } else if (f.isDirectory()) {
                    File[] lists = f.listFiles();
                    if (notNull(lists)) {
                        for (File list : lists) {
                            if (list.isDirectory()) {
                                delete(list.getAbsolutePath());
                            } else if (list.isFile()) {
                                list.delete();
                            }
                        }
                        f.delete();
                    }
                }
            }
        }
    }

    public static void delete(@Nullable File file) {
        if (notNull(file)) {
            delete(file.getAbsolutePath());
        }
    }

    public static boolean isExist(@Nullable String filename) {
        if (notNull(filename)) {
            return new File(filename).exists();
        }
        return false;
    }

    public static byte[] getFileBytes(@Nullable String filename) {
        byte[] bytes = new byte[0];
        if (isFile(filename)) {
            try {
                bytes = TextStream.readBinaryFileToBytes(filename);
            } catch (Exception exception) {
                LOGGER.warning(message(exception));
                bytes = new byte[0];
            }
        }
        return bytes;
    }

    public static byte[] getFileBytes(@Nullable String filename, boolean delete) {
        byte[] bytes = getFileBytes(filename);
        if (delete && bytes.length > 0) {
            delete(filename);
        }
        return bytes;
    }

    public static String millisecondsToTime(int millisecondsTime) {
        long hours = TimeUnit.MILLISECONDS.toHours(millisecondsTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millisecondsTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisecondsTime));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millisecondsTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisecondsTime));
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static long getFileSize(@NonNull String filename) {
        return isExist(filename) ? new File(filename).length() : 0;
    }

    public static String readableSize(@NonNull String filename) {
        long size = getFileSize(filename);
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

    public static List<XFile> listDir(@NonNull String dirPath, boolean recursively) {
        return listDir(new File(dirPath), recursively);
    }

    public static List<XFile> listDir(@NonNull File dirFile, boolean recursively) {
        List<XFile> lists = new ArrayList<>();
        if (dirFile.isDirectory()) {
            File[] listedFiles = dirFile.listFiles();
            if (listedFiles != null) {
                List<XFile> mod = new ArrayList<>();
                for (File list : listedFiles) {
                    mod.add(new XFile(list.getAbsolutePath()));
                }
                Collections.sort(mod, new XFile.XFileComparator());
                for (XFile list : mod) {
                    if (list.isDirectory() && recursively) {
                        lists.addAll(listDir(new File(list.getPath()), true));
                    } else {
                        lists.add(list);
                    }
                }
            }
        }
        return lists;
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

    public static boolean validJson(@NonNull String data) {
        try {
            new JSONObject(data);
            return true;
        } catch (JSONException exception) {
            LOGGER.warning(message(exception));
            return false;
        }
    }

    public static int getRandom(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    public static void createFile(@Nullable String filaname) {
        if (notNull(filaname) && !isExist(filaname)) {
            try {
                new File(filaname).createNewFile();
            } catch (IOException exception) {
                LOGGER.warning(message(exception));
            }
        }
    }

    public static void createFile(@Nullable File filaname) {
        if (notNull(filaname)) {
            createFile(filaname.getAbsolutePath());
        }
    }

    public static String createTempFile() throws IOException {
        return File.createTempFile(generateRandomText(32), "").getAbsolutePath();
    }

    public static void createDirectory(@Nullable String dirPath) {
        if (notNull(dirPath) && !isExist(dirPath)) {
            try {
                boolean create = new File(dirPath).mkdirs();
            } catch (SecurityException exception) {
                LOGGER.warning(message(exception));
            }
        }
    }

    public static void createDirectory(@Nullable File dirFile) {
        if (notNull(dirFile) && !dirFile.exists()) {
            try {
                dirFile.mkdirs();
            } catch (SecurityException exception) {
                LOGGER.warning(message(exception));
            }
        }
    }

    public static void copyFile(@NonNull String sourcePath, @NonNull String destinationPath, @Nullable CopyBytesCallback callback) throws Exception {
        if (isFile(sourcePath) && !sourcePath.equalsIgnoreCase(destinationPath)) {
            try (Streamer reader = new Streamer(sourcePath, Streamer.Mode.READ_ONLY); Streamer writer = new Streamer(destinationPath, Streamer.Mode.READ_AND_WRITE)) {
                int total = (int) new File(sourcePath).length();
                int written = 0;
                byte[] buffer = new byte[BUFFER_SIZE];
                int read;
                while ((read = reader.readBytes(buffer)) != -1) {
                    if (writer.writeBytes(buffer)) {
                        written += read;
                        int progress = (int) Math.min((written * 100L) / total, 100);
                        if (notNull(callback)) {
                            callback.onBytes(written, total, progress);
                        }
                    }
                }
                LOGGER.warning(writer.error().message());
            }
        }
    }

    public static void copyFile(@NonNull String sourcePath, @NonNull String destPath) throws Exception {
        try (FileChannel sourceChannel = new FileInputStream(sourcePath).getChannel(); FileChannel destChannel = new FileOutputStream(destPath).getChannel()) {
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
    }

    public static void renameFile(@Nullable String sourcePath, @Nullable String destPath) {
        if (notNull(sourcePath) && notNull(destPath)) {
            File sourceFile = new File(sourcePath);
            File destFile = new File(destPath);
            if (sourceFile.isFile()) {
                String parent = destFile.getParent();
                if (notNull(parent)) {
                    createDirectory(parent);
                    if (isDirectory(parent)) {
                        sourceFile.renameTo(destFile);
                    }
                }
            }
        }
    }

    public static long directorySize(@Nullable String dirPath, boolean recursively) {
        long size = 0;
        if (notNull(dirPath) && isDirectory(dirPath)) {
            File[] files = new File(dirPath).listFiles();
            if (notNull(files)) {
                for (File file : files) {
                    if (file.isDirectory() && recursively) {
                        size += directorySize(file.getAbsolutePath(), true);
                    } else {
                        size += file.length();
                    }
                }
            }
        }
        return size;
    }

    public static boolean hasMatchingExtension(@NonNull File file, @NonNull ArrayList<String> extensions) {
        for (String extension : extensions) {
            if (file.getName().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    public static int countFiles(@Nullable String dirPath, @Nullable ArrayList<String> extensions, boolean recursively) {
        int count = 0;
        if (notNull(dirPath) && isDirectory(dirPath)) {
            File[] files = new File(dirPath).listFiles();
            if (notNull(files)) {
                for (File file : files) {
                    if (file.isFile()) {
                        if (isNull(extensions) || hasMatchingExtension(file, extensions)) {
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

    public static boolean isDirectoryEmpty(@Nullable String path) {
        int countedFiles = countFiles(path, null, false);
        return countedFiles < 1;
    }

    public static boolean canWrite(@Nullable String path) {
        if (notNull(path)) {
            File absoluteFile = new File(path);
            return absoluteFile.exists() && absoluteFile.canWrite();
        }
        return false;
    }

    public static boolean canExecute(@Nullable String path) {
        if (notNull(path)) {
            File absoluteFile = new File(path);
            return absoluteFile.exists() && absoluteFile.canExecute();
        }
        return false;
    }

    public static boolean canRead(@Nullable String path) {
        if (notNull(path)) {
            File absoluteFile = new File(path);
            return absoluteFile.exists() && absoluteFile.canRead();
        }
        return false;
    }

    public static String getParentPath(@Nullable String path) {
        if (notNull(path)) {
            File absoluteFile = new File(path);
            if (absoluteFile.exists()) {
                File parent = absoluteFile.getParentFile();
                if (notNull(parent)) {
                    return parent.getAbsolutePath();
                }
            }

        }
        return null;
    }

    public static String randomBasename(@NonNull String extension) {
        return generateRandomText(20) + Calendar.getInstance().getTimeInMillis() + "." + extension;
    }

    public static void createParentDirForFile(@Nullable File file) {
        if (notNull(file)) {
            createParentDirForFile(file.getAbsolutePath());
        }
    }

    public static void createParentDirForFile(@Nullable String path) {
        if (notNull(path)) {
            File file = new File(path);
            File parent = file.getParentFile();
            if (notNull(parent)) {
                createDirectory(parent.getAbsolutePath());
            }
        }
    }

    public static void unzipAll(@NonNull String zip, @NonNull String destDir, boolean skipDestFile) {
        try {
            Archive.extractZip(new File(zip), destDir, null, "");
        } catch (IOException exception) {
            LOGGER.warning(message(exception));
        }
    }

    public static void unzipEntry(@NonNull String zip, @NonNull String entry, @NonNull String destDir) {
        try {
            Archive.extractZip(new File(zip), destDir, entry, "");
        } catch (IOException exception) {
            LOGGER.warning(message(exception));
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
        } catch (Exception exception) {
            LOGGER.warning(message(exception));
        }
        return extension;
    }

    public static String message(@Nullable Exception exception) {
        return exception != null ? exception.getMessage() : "Unknown error";
    }

    public static String readAndEscapeHtmlFile(@NonNull String filename) {
        return readAndEscapeHtmlContent(new String(getFileBytes(filename, false), CHARSET));
    }

    public static String readAndEscapeHtmlContent(@NonNull String html) {
        StringBuilder builder = new StringBuilder();
        for (char c : html.toCharArray()) {
            switch (c) {
                case '<' ->
                    builder.append("&lt;");
                case '>' ->
                    builder.append("&gt;");
                case '\"' ->
                    builder.append("&quot;");
                case '\'' ->
                    builder.append("&apos;");
                default ->
                    builder.append(c);
            }
        }
        return builder.toString();
    }

    public static ArrayList<String> getEmptyDirectories(@NonNull String dirPath) {
        ArrayList<String> lists = new ArrayList<>();
        try {
            File absoluteFile = new File(dirPath);
            if (absoluteFile.exists() && absoluteFile.isDirectory()) {
                File[] listedFiles = absoluteFile.listFiles();
                if (notNull(listedFiles)) {
                    if (listedFiles.length > 0) {
                        for (File listedFile : listedFiles) {
                            lists.addAll(getEmptyDirectories(listedFile.getAbsolutePath()));
                        }
                    } else {
                        lists.add(dirPath);
                    }
                }
            }
        } catch (Exception exception) {
            LOGGER.warning(message(exception));
        }
        return lists;
    }

    public static void deleteEmptyDirectories(@NonNull String dirPath) {
        ArrayList<String> directories = getEmptyDirectories(dirPath);
        for (String directory : directories) {
            delete(directory);
        }
    }

    public static long getRemoteFileSize(@NonNull String location) {
        long size = -1;
        try {
            URL url = new URL(location);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            size = connection.getContentLength();
            connection.disconnect();
        } catch (Exception exception) {
            LOGGER.warning(message(exception));
        }
        return size;
    }

    public static String generateFilename(@Nullable String prefix, @Nullable String surfix, boolean toLowercase) {
        String filename = (notNull(prefix) ? prefix : "") + generateRandomText(32) + (notNull(surfix) ? "." + surfix : "");
        return toLowercase ? filename.toLowerCase() : filename.toUpperCase();
    }

    public static String executeCommand(@NonNull String[] command) {
        Process process = null;
        try {
            StringBuilder builder = new StringBuilder();
            // Execute the command
            process = Runtime.getRuntime().exec(command);
            // Obtain input, output, and error streams
            java.io.OutputStream outputStream = process.getOutputStream();
            java.io.InputStream inputStream = process.getInputStream();
            java.io.InputStream errorStream = process.getErrorStream();
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
        } catch (IOException | InterruptedException exception) {
            LOGGER.warning(message(exception));
            return null;
        } finally {
            if (notNull(process)) {
                process.destroy();
            }
        }
    }

    public static String getCurrentMethodName() {
        // The 2nd element in the stack trace corresponds to the caller method
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }

    /**
     * Creates an X509TrustManager for SSL context configuration.
     *
     * @param certFile Optional certificate file for server verification.
     * @return An X509TrustManager instance.
     * @throws GeneralSecurityException If an SSL issue occurs.
     * @throws IOException If the certificate file cannot be read.
     */
    public static X509TrustManager trustManager(@Nullable XFile certFile) throws GeneralSecurityException, IOException {
        final Certificate[] certificate = {null};
        if (certFile != null && certFile.isFile() && certFile.canRead()) {
            try (InputStream inputStream = new BufferedInputStream(new FileInputStream(certFile))) {
                CertificateFactory factory = CertificateFactory.getInstance("X.509");
                certificate[0] = factory.generateCertificate(inputStream);
            }
        }
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                // Do nothing - trust all clients
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                // Trust server certificate with custom certitificate if specified
                if (Utils.notNull(certificate[0])) {
                    for (X509Certificate cert : chain) {
                        try {
                            cert.verify(certificate[0].getPublicKey());
                            return; // Verification succeeded
                        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException | CertificateException e) {
                            throw new CertificateException("Server certificate verification failed.", e);
                        }
                    }
                }
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }
}
