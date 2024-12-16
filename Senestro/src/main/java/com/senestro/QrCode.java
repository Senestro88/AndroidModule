package com.senestro;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.senestro.annotations.NonNull;
import com.senestro.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Utility class for generating and reading QR Codes.
 * <p>
 * This class provides methods to generate QR codes from string data and save
 * them as images, as well as to read and decode QR codes from image files.
 * </p>
 * <p>
 * It uses the ZXing library for QR code generation and reading.
 * </p>
 *
 * @author Senestro
 */
public class QrCode {

    private static final String TAG = QrCode.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(TAG);

    // Default character set used for encoding/decoding QR code data
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    /**
     * Generates a QR code from the given data and saves it to a file.
     *
     * @param data The string data to encode in the QR code.
     * @param width The width of the generated QR code.
     * @param height The height of the generated QR code.
     * @param hints Optional hints for generating the QR code, such as error
     * correction level.
     * @param filename The name of the file where the QR code will be saved.
     * @return A {@link XFile} object representing the generated file, or null
     * if an error occurs.
     */
    public static XFile generate(@NonNull String data, int width, int height, @Nullable Map<EncodeHintType, ErrorCorrectionLevel> hints, @NonNull String filename) {
        try {
            // Create QR code writer instance
            QRCodeWriter writer = new QRCodeWriter();

            // If no hints are provided, initialize an empty map
            hints = hints == null ? new HashMap<>() : hints;

            // Encode the data into a BitMatrix (QR code representation)
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, width, height, hints);

            // Create the path to the file where the QR code will be saved
            Path path = FileSystems.getDefault().getPath(filename);

            // Write the BitMatrix as a PNG image to the specified file
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

            // Return the generated file wrapped in a XFile object
            return new XFile(filename);
        } catch (WriterException | IOException exception) {
            LOGGER.warning(message(exception)); // Log the error if any
        }
        return null; // Return null if an error occurred
    }

    /**
     * Reads a QR code from the given image file and decodes its data.
     *
     * @param filename The name of the file containing the QR code image.
     * @return The decoded data from the QR code, or null if an error occurs.
     * @throws IOException If there is an error reading the image file.
     */
    public static String read(@NonNull String filename) throws IOException {
        try {
            // Read the image file into a BufferedImage
            BufferedImage buffered = ImageIO.read(new File(filename));

            // Convert the BufferedImage to a BinaryBitmap for ZXing processing
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(buffered)));

            // Decode the QR code from the BinaryBitmap
            Result result = new MultiFormatReader().decode(bitmap);

            // Return the decoded text from the QR code
            return result.getText();
        } catch (NotFoundException | IOException exception) {
            LOGGER.warning(message(exception)); // Log the error if any
        }
        return null; // Return null if an error occurred
    }

    // PRIVATE METHODS
    /**
     * Creates an error message from the given exception.
     *
     * @param exception The exception to extract the message from.
     * @return A string message describing the error.
     */
    private static String message(@Nullable Exception exception) {
        return exception != null ? exception.getMessage() : "Unknown error";
    }
}
