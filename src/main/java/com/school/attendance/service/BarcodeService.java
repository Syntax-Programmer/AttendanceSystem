package com.school.attendance.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * Generates QR codes encoding a student's roll number.
 * QR content is the roll number as a plain integer string, OR a full URL if phone scanning.
 * No barcode column is stored in the database; roll_no is the identifier.
 */
public class BarcodeService {

    /**
     * Generates a QR code image for the given roll number (encodes roll number as string).
     *
     * @param rollNo the student's roll number
     * @param size   pixel size for the QR code image (width and height)
     * @return a BufferedImage containing the QR code
     */
    public BufferedImage generateQrCode(int rollNo, int size) throws WriterException {
        return generateQrCodeFromString(String.valueOf(rollNo), size);
    }

    /**
     * Generates a QR code image from an arbitrary string content.
     * Used to encode full URLs (for phone-based scanning) or plain roll numbers.
     *
     * @param content the string to encode in the QR code
     * @param size    pixel size for the QR code image (width and height)
     * @return a BufferedImage containing the QR code
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
     * Parses a scanned value (from QR or barcode scanner keyboard wedge) into a roll number.
     *
     * @param scannedValue raw string from scanner
     * @return the roll number
     * @throws IllegalArgumentException if the value cannot be parsed as a roll number
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
