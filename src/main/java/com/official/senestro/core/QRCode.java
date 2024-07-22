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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class QRCode {
    private final Context context;
    private final Activity activity;

    public QRCode(@NonNull Context context, @NonNull Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public Bitmap generateQRCode(@NonNull String data, int width, int height) {
        if (!data.isEmpty()) {
            String charset = "UTF-8";
            Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            return generateQRCode(data, width, height, charset, hints);
        }
        return null;
    }

    public Bitmap generateQRCode(@NonNull String data, int width, int height, @NonNull String charset, @NonNull Map<EncodeHintType, ErrorCorrectionLevel> hints) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(new String(data.getBytes(charset), StandardCharsets.UTF_8), BarcodeFormat.QR_CODE, width, height, hints);
            return convertBitMatrixToBitmap(bitMatrix);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap generateQRCode(@NonNull String data, int width, int height, @NonNull String charset) {
        try {
            Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(new String(data.getBytes(charset), StandardCharsets.UTF_8), BarcodeFormat.QR_CODE, width, height, hints);
            return convertBitMatrixToBitmap(bitMatrix);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap generateQRCode(@NonNull String data, int width, int height, @NonNull Map<EncodeHintType, ErrorCorrectionLevel> hints) {
        try {
            String charset = "UTF-8";
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(new String(data.getBytes(charset), StandardCharsets.UTF_8), BarcodeFormat.QR_CODE, width, height, hints);
            return convertBitMatrixToBitmap(bitMatrix);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public String decodeQRCode(@Nullable Bitmap bitmap) {
        if (bitmap != null) {
            BarcodeDetector detector = new BarcodeDetector.Builder(context).setBarcodeFormats(Barcode.ALL_FORMATS).build();
            if (detector.isOperational()) {
                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                SparseArray<Barcode> barcodes = detector.detect(frame);
                if (barcodes.size() >= 1) {
                    StringBuilder decoded = new StringBuilder();
                    for (int index = 0; index < barcodes.size(); index++) {
                        Barcode code = barcodes.valueAt(index);
                        String value = code.displayValue;
                        decoded.append(code.displayValue).append("\n");
                    }
                    return decoded.toString();
                }
            }
        }
        return "";
    }

    public String decodeQRCode(@Nullable File file) {
        if (file != null && file.exists()) {
            return decodeQRCode(BitmapFactory.decodeFile(file.getAbsolutePath()));
        }
        return "";
    }

    // PRIVATE
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