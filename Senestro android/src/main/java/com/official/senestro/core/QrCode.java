package com.official.senestro.core;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.SparseArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.official.senestro.core.utils.XUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * QrCode class provides utility methods to create and read QR codes.
 * This class utilizes Google Vision and ZXing libraries for QR code operations.
 */
public class QrCode {
    private final Context context;
    private final Activity activity;

    /**
     * Constructor to initialize QrCode utility with context and activity.
     *
     * @param context  the application or activity context
     * @param activity the activity where QR code operations are performed
     */
    public QrCode(@NonNull Context context, @NonNull Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    /**
     * Creates a QR code bitmap with the given data, width, and height.
     * Uses default UTF-8 charset and low error correction level.
     *
     * @param data   the data to encode in the QR code
     * @param width  the width of the QR code
     * @param height the height of the QR code
     * @return the generated QR code as a Bitmap, or null if an error occurs
     */
    public Bitmap createQrCode(@NonNull String data, int width, int height) {
        if (!data.isEmpty()) {
            String charset = "UTF-8";
            Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            return createQrCode(data, width, height, charset, hints);
        }
        return null;
    }

    /**
     * Creates a QR code bitmap with the specified charset and error correction level.
     *
     * @param data    the data to encode in the QR code
     * @param width   the width of the QR code
     * @param height  the height of the QR code
     * @param charset the character encoding for the data
     * @param hints   the encoding hints, including error correction level
     * @return the generated QR code as a Bitmap, or null if an error occurs
     */
    public Bitmap createQrCode(@NonNull String data, int width, int height, @NonNull String charset, @NonNull Map<EncodeHintType, ErrorCorrectionLevel> hints) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    new String(data.getBytes(charset), StandardCharsets.UTF_8),
                    BarcodeFormat.QR_CODE,
                    width,
                    height,
                    hints
            );
            return convertBitMatrixToBitmap(bitMatrix);
        } catch (Throwable e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    /**
     * Creates a QR code bitmap with the specified charset and default error correction level (high).
     *
     * @param data    the data to encode in the QR code
     * @param width   the width of the QR code
     * @param height  the height of the QR code
     * @param charset the character encoding for the data
     * @return the generated QR code as a Bitmap, or null if an error occurs
     */
    public Bitmap createQrCode(@NonNull String data, int width, int height, @NonNull String charset) {
        try {
            Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(
                    new String(data.getBytes(charset), StandardCharsets.UTF_8),
                    BarcodeFormat.QR_CODE,
                    width,
                    height,
                    hints
            );
            return convertBitMatrixToBitmap(bitMatrix);
        } catch (Throwable e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    /**
     * Creates a QR code bitmap using default UTF-8 charset and provided hints.
     *
     * @param data   the data to encode in the QR code
     * @param width  the width of the QR code
     * @param height the height of the QR code
     * @param hints  the encoding hints, including error correction level
     * @return the generated QR code as a Bitmap, or null if an error occurs
     */
    public Bitmap createQrCode(@NonNull String data, int width, int height, @NonNull Map<EncodeHintType, ErrorCorrectionLevel> hints) {
        try {
            String charset = "UTF-8";
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(
                    new String(data.getBytes(charset), StandardCharsets.UTF_8),
                    BarcodeFormat.QR_CODE,
                    width,
                    height,
                    hints
            );
            return convertBitMatrixToBitmap(bitMatrix);
        } catch (Throwable e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    /**
     * Reads a QR code from the given bitmap and decodes its content.
     *
     * @param bitmap the bitmap containing the QR code
     * @return the decoded content as a string, or an empty string if no QR code is detected
     */
    public String readQrCode(@Nullable Bitmap bitmap) {
        if (bitmap != null) {
            BarcodeDetector detector = new BarcodeDetector.Builder(context)
                    .setBarcodeFormats(Barcode.ALL_FORMATS)
                    .build();
            if (detector.isOperational()) {
                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                SparseArray<Barcode> barcodes = detector.detect(frame);
                if (barcodes.size() >= 1) {
                    StringBuilder decoded = new StringBuilder();
                    for (int index = 0; index < barcodes.size(); index++) {
                        Barcode code = barcodes.valueAt(index);
                        decoded.append(code.displayValue).append("\n");
                    }
                    return decoded.toString().trim();
                }
            }
        }
        return "";
    }

    /**
     * Reads a QR code from the given file and decodes its content.
     *
     * @param file the file containing the QR code image
     * @return the decoded content as a string, or an empty string if no QR code is detected
     */
    public String readQrCode(@Nullable File file) {
        if (XUtils.notNull(file) && file.exists()) {
            return readQrCode(BitmapFactory.decodeFile(file.getAbsolutePath()));
        }
        return "";
    }

    // PRIVATE METHOD

    /**
     * Converts a BitMatrix to a Bitmap representation of the QR code.
     *
     * @param matrix the BitMatrix to convert
     * @return the resulting Bitmap
     */
    private Bitmap convertBitMatrixToBitmap(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bitmap;
    }
}
