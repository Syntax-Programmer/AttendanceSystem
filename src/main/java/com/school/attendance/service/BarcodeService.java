package com.school.attendance.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * Generates QR codes and Code 128 barcodes encoding a student's roll number.
 * No barcode column is stored in the database; roll_no is the identifier.
 *
 * Code 128: preferred for traditional USB barcode scanners.
 * QR code: preferred for phone-camera scanning (can encode URLs).
 */
public class BarcodeService {

    /**
     * Generates a QR code image for the given roll number (encodes roll number as string).
     */
    public BufferedImage generateQrCode(int rollNo, int size) throws WriterException {
        return generateQrCodeFromString(String.valueOf(rollNo), size);
    }

    /**
     * Generates a QR code image from an arbitrary string content.
     * Used to encode full URLs (for phone-based scanning) or plain roll numbers.
     */
    public BufferedImage generateQrCodeFromString(String content, int size) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = Map.of(
            EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN, 2
        );
        BitMatrix matrix = writer.encode(
            content,
            BarcodeFormat.QR_CODE,
            size,
            size,
            hints
        );
        return MatrixToImageWriter.toBufferedImage(matrix);
    }

    /**
     * Generates a Code 128 barcode image encoding the student's roll number.
     * Code 128 is the standard for USB barcode scanner wedges.
     *
     * @param rollNo the student's roll number
     * @param width  image width in pixels
     * @param height image height in pixels
     * @return a BufferedImage containing the Code 128 barcode
     */
    public BufferedImage generateCode128(int rollNo, int width, int height) throws WriterException {
        return generateCode128FromString(String.valueOf(rollNo), width, height);
    }

    /**
     * Generates a Code 128 barcode image from an arbitrary string.
     */
    public BufferedImage generateCode128FromString(String content, int width, int height)
            throws WriterException {
        Code128Writer writer = new Code128Writer();
        Map<EncodeHintType, Object> hints = Map.of(EncodeHintType.MARGIN, 2);
        BitMatrix matrix = writer.encode(
            content,
            BarcodeFormat.CODE_128,
            width,
            height,
            hints
        );
        return MatrixToImageWriter.toBufferedImage(matrix);
    }

    /**
     * Parses a scanned value (from QR or barcode scanner keyboard wedge) into a roll number.
     */
    public int parseRollNumber(String scannedValue) {
        if (scannedValue == null || scannedValue.isBlank()) {
            throw new IllegalArgumentException("Scanned value is empty.");
        }
        String trimmed = scannedValue.trim();
        try {
            int rollNo = Integer.parseInt(trimmed);
            if (rollNo <= 0) {
                throw new IllegalArgumentException("Roll number must be positive.");
            }
            return rollNo;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid roll number: '" + trimmed + "'.");
        }
    }
}
