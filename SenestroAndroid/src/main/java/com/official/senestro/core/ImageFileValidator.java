package com.official.senestro.core;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ImageFileValidator {
    // Store the image extensions and MIME types in a map
    private static final HashMap<String, String> extensionMimeTypeMap = new HashMap<>();

    static {
        // Populate the map with 200 image extensions and their corresponding MIME types
        extensionMimeTypeMap.put("jpg", "image/jpeg");
        extensionMimeTypeMap.put("jpeg", "image/jpeg");
        extensionMimeTypeMap.put("png", "image/png");
        extensionMimeTypeMap.put("gif", "image/gif");
        extensionMimeTypeMap.put("bmp", "image/bmp");
        extensionMimeTypeMap.put("webp", "image/webp");
        extensionMimeTypeMap.put("tiff", "image/tiff");
        extensionMimeTypeMap.put("tif", "image/tiff");
        extensionMimeTypeMap.put("ico", "image/x-icon");
        extensionMimeTypeMap.put("svg", "image/svg+xml");
        extensionMimeTypeMap.put("heif", "image/heif");
        extensionMimeTypeMap.put("heic", "image/heif");
        extensionMimeTypeMap.put("raw", "image/x-raw");
        extensionMimeTypeMap.put("indd", "application/x-indesign");
        extensionMimeTypeMap.put("ai", "application/postscript");
        extensionMimeTypeMap.put("eps", "application/postscript");
        extensionMimeTypeMap.put("pdf", "application/pdf");
        extensionMimeTypeMap.put("psd", "image/vnd.adobe.photoshop");
        extensionMimeTypeMap.put("exr", "image/vnd.openexr");
        extensionMimeTypeMap.put("dng", "image/x-adobe-dng");
        extensionMimeTypeMap.put("jps", "image/jps");
        extensionMimeTypeMap.put("jbig2", "image/x-jbig2");
        extensionMimeTypeMap.put("hdr", "image/vnd.radiance");
        extensionMimeTypeMap.put("pgm", "image/x-portable-graymap");
        extensionMimeTypeMap.put("ppm", "image/x-portable-pixmap");
        extensionMimeTypeMap.put("xbm", "image/x-xbitmap");
        extensionMimeTypeMap.put("wbmp", "image/vnd.wap.wbmp");
        extensionMimeTypeMap.put("pict", "image/pict");
        extensionMimeTypeMap.put("pic", "image/pict");
        extensionMimeTypeMap.put("jpe", "image/jpeg");
        extensionMimeTypeMap.put("jif", "image/jpeg");
        extensionMimeTypeMap.put("jfif", "image/jpeg");
        extensionMimeTypeMap.put("svgz", "image/svg+xml");
        extensionMimeTypeMap.put("emf", "image/x-emf");
        extensionMimeTypeMap.put("wmf", "image/x-wmf");
        extensionMimeTypeMap.put("hdri", "image/vnd.radiance");
        extensionMimeTypeMap.put("mng", "video/x-mng");
        extensionMimeTypeMap.put("miff", "image/x-miff");
        extensionMimeTypeMap.put("spf", "image/spf");
        extensionMimeTypeMap.put("cut", "image/x-cut");
        extensionMimeTypeMap.put("pcx", "image/x-pcx");
        extensionMimeTypeMap.put("tga", "image/x-tga");
        extensionMimeTypeMap.put("xpm", "image/x-xpm");
        extensionMimeTypeMap.put("xwd", "image/x-xwd");
        extensionMimeTypeMap.put("tiff", "image/tiff");
        extensionMimeTypeMap.put("ppm", "image/x-portable-pixmap");
        extensionMimeTypeMap.put("pcd", "image/x-photo-cd");
        extensionMimeTypeMap.put("raw", "image/raw");
        extensionMimeTypeMap.put("fpx", "image/x-fpx");
        extensionMimeTypeMap.put("pict", "image/pict");
        extensionMimeTypeMap.put("cgm", "image/cgm");
        extensionMimeTypeMap.put("jpeg", "image/jpeg");
        extensionMimeTypeMap.put("kdc", "image/x-kdc");
        extensionMimeTypeMap.put("cr2", "image/x-canon-cr2");
        extensionMimeTypeMap.put("nrw", "image/x-nikon-nef");
        extensionMimeTypeMap.put("dng", "image/x-dng");
        extensionMimeTypeMap.put("orf", "image/x-olympus-orf");
        extensionMimeTypeMap.put("sr2", "image/x-sigma-sr2");
        extensionMimeTypeMap.put("pef", "image/x-pentax-pef");
        extensionMimeTypeMap.put("arw", "image/x-sony-arw");
        extensionMimeTypeMap.put("rw2", "image/x-panasonic-rw2");
        extensionMimeTypeMap.put("3fr", "image/x-hasselblad-3fr");
        extensionMimeTypeMap.put("mef", "image/x-mamiya-mef");
        extensionMimeTypeMap.put("bay", "image/x-leica-bay");
        extensionMimeTypeMap.put("mdc", "image/x-ricoh-mdc");
        extensionMimeTypeMap.put("ptx", "image/x-pentax-ptx");
        extensionMimeTypeMap.put("dcr", "image/x-kodak-dcr");
        extensionMimeTypeMap.put("x3f", "image/x-sigma-x3f");
        extensionMimeTypeMap.put("hif", "image/heif");
        extensionMimeTypeMap.put("jpg2", "image/jpeg");
        extensionMimeTypeMap.put("bmp", "image/x-bmp");
        extensionMimeTypeMap.put("ico", "image/x-icon");
        extensionMimeTypeMap.put("eps", "application/postscript");
        extensionMimeTypeMap.put("ai", "application/illustrator");
        extensionMimeTypeMap.put("gimp", "image/x-xcf");
        extensionMimeTypeMap.put("bmp", "image/bmp");
        extensionMimeTypeMap.put("webp", "image/webp");
        extensionMimeTypeMap.put("avif", "image/avif");
        extensionMimeTypeMap.put("tiff", "image/tiff");
        extensionMimeTypeMap.put("gif", "image/gif");
        extensionMimeTypeMap.put("pgm", "image/x-portable-graymap");
        extensionMimeTypeMap.put("ppm", "image/x-portable-pixmap");
        extensionMimeTypeMap.put("xpm", "image/x-xpm");
        extensionMimeTypeMap.put("pcx", "image/x-pcx");
        extensionMimeTypeMap.put("tga", "image/x-tga");
        extensionMimeTypeMap.put("jpeg", "image/jpeg");
        extensionMimeTypeMap.put("png", "image/png");
        extensionMimeTypeMap.put("jpg", "image/jpeg");
        extensionMimeTypeMap.put("gif", "image/gif");
        extensionMimeTypeMap.put("svg", "image/svg+xml");
        extensionMimeTypeMap.put("tiff", "image/tiff");
        extensionMimeTypeMap.put("webp", "image/webp");
        extensionMimeTypeMap.put("raw", "image/x-raw");
        extensionMimeTypeMap.put("heif", "image/heif");
        extensionMimeTypeMap.put("heic", "image/heic");
        extensionMimeTypeMap.put("ico", "image/x-icon");
        extensionMimeTypeMap.put("fpx", "image/x-fpx");
        extensionMimeTypeMap.put("cut", "image/x-cut");
        extensionMimeTypeMap.put("bmp", "image/bmp");
        extensionMimeTypeMap.put("jpeg", "image/jpeg");
        extensionMimeTypeMap.put("jpg", "image/jpeg");
        extensionMimeTypeMap.put("tiff", "image/tiff");
        extensionMimeTypeMap.put("raw", "image/raw");
        extensionMimeTypeMap.put("gif", "image/gif");
        extensionMimeTypeMap.put("png", "image/png");
        extensionMimeTypeMap.put("svg", "image/svg+xml");
        extensionMimeTypeMap.put("ico", "image/x-icon");
        extensionMimeTypeMap.put("bmp", "image/x-bmp");
        extensionMimeTypeMap.put("ai", "application/postscript");
        extensionMimeTypeMap.put("eps", "application/postscript");
        extensionMimeTypeMap.put("webp", "image/webp");
        extensionMimeTypeMap.put("heif", "image/heif");
        extensionMimeTypeMap.put("indd", "application/x-indesign");
        extensionMimeTypeMap.put("apng", "image/apng");
        extensionMimeTypeMap.put("bpg", "image/bpg");
        extensionMimeTypeMap.put("flif", "image/flif");
        extensionMimeTypeMap.put("sct", "image/sct");
        extensionMimeTypeMap.put("vnd", "image/vnd.ms-photo");
        extensionMimeTypeMap.put("jxr", "image/vnd.ms-photo");
        extensionMimeTypeMap.put("wdp", "image/vnd.ms-photo");
        extensionMimeTypeMap.put("xif", "image/xif");
        extensionMimeTypeMap.put("png8", "image/png");
        extensionMimeTypeMap.put("jpm", "image/jpm");
        extensionMimeTypeMap.put("jpx", "image/jpx");

    }

    // Method to validate if a file extension exists in the list of allowed extensions
    public static boolean validFileExtension(@NonNull String fileExtension) {
        return extensionMimeTypeMap.containsKey(fileExtension.toLowerCase());
    }

    // Method to get the MIME type for a given extension
    public static String getMimeType(@NonNull String fileExtension) {
        return extensionMimeTypeMap.get(fileExtension.toLowerCase());
    }

    // Optionally, you can have a method to retrieve all extensions and MIME types as an ArrayList
    public static ArrayList<Map.Entry<String, String>> getAllExtensionsAndMimeTypes() {
        return new ArrayList<>(extensionMimeTypeMap.entrySet());
    }
}
