package com.school.attendance.service;

import com.google.zxing.WriterException;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.school.attendance.model.Student;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;

/**
 * Generates a printable PDF containing Code128 barcodes and QR codes
 * for a list of students in a class/section.
 *
 * Layout: 2 students per row, each card showing:
 *   - Student name
 *   - Roll number
 *   - Class / Section
 *   - Code 128 barcode (for USB scanner)
 *   - QR code (for phone scanning)
 */
public class BarcodePdfService {

    private static final BarcodeService barcodeService = new BarcodeService();

    /**
     * Generates a PDF file with barcode cards for all given students.
     *
     * @param students     list of students to generate barcodes for
     * @param outputFile   destination file for the PDF
     * @param serverBaseUrl  if non-null and non-blank, QR encodes the URL; else encodes roll number
     * @throws Exception  if PDF generation or barcode generation fails
     */
    public void generatePdf(
        List<Student> students,
        File outputFile,
        String serverBaseUrl
    ) throws Exception {
        if (students == null || students.isEmpty()) {
            throw new IllegalArgumentException("No students provided.");
        }

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            PdfWriter.getInstance(document, fos);
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            Student first = students.get(0);
            Paragraph title = new Paragraph(
                "Barcode Cards — Class " + first.getClassNumber() + " / " + first.getSection(),
                titleFont
            );
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(16);
            document.add(title);

            // 2-column table for student cards
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(8);
            table.getDefaultCell().setPadding(0);
            table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            for (Student student : students) {
                PdfPCell card = buildStudentCard(student, serverBaseUrl, headerFont, normalFont);
                table.addCell(card);
            }

            // Fill last row if odd number of students
            if (students.size() % 2 != 0) {
                PdfPCell empty = new PdfPCell();
                empty.setBorder(Rectangle.NO_BORDER);
                table.addCell(empty);
            }

            document.add(table);
        } finally {
            if (document.isOpen()) document.close();
        }
    }

    private PdfPCell buildStudentCard(
        Student student,
        String serverBaseUrl,
        Font headerFont,
        Font normalFont
    ) {
        PdfPTable inner = new PdfPTable(1);
        inner.setWidthPercentage(100);

        // Student info header
        PdfPCell nameCell = new PdfPCell(new Phrase(student.getName(), headerFont));
        nameCell.setBorder(Rectangle.NO_BORDER);
        nameCell.setPaddingBottom(2);
        inner.addCell(nameCell);

        PdfPCell infoCell = new PdfPCell(new Phrase(
            "Roll: " + student.getRollNo()
            + "   Class: " + student.getClassNumber() + "-" + student.getSection(),
            normalFont
        ));
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setPaddingBottom(6);
        inner.addCell(infoCell);

        // Code 128 barcode
        try {
            BufferedImage barcodeImg = barcodeService.generateCode128(student.getRollNo(), 280, 60);
            Image pdfBarcode = toPdfImage(barcodeImg);
            pdfBarcode.setAlignment(Image.ALIGN_CENTER);
            pdfBarcode.scaleToFit(230, 55);
            PdfPCell barcodeCell = new PdfPCell(pdfBarcode, true);
            barcodeCell.setBorder(Rectangle.NO_BORDER);
            barcodeCell.setPaddingBottom(4);
            inner.addCell(barcodeCell);
        } catch (Exception e) {
            PdfPCell err = new PdfPCell(new Phrase("Barcode error", normalFont));
            err.setBorder(Rectangle.NO_BORDER);
            inner.addCell(err);
        }

        // QR code
        try {
            String qrContent = (serverBaseUrl != null && !serverBaseUrl.isBlank())
                ? serverBaseUrl + "/mark?roll=" + student.getRollNo()
                : String.valueOf(student.getRollNo());
            BufferedImage qrImg = barcodeService.generateQrCodeFromString(qrContent, 100);
            Image pdfQr = toPdfImage(qrImg);
            pdfQr.setAlignment(Image.ALIGN_CENTER);
            pdfQr.scaleToFit(80, 80);
            PdfPCell qrCell = new PdfPCell(pdfQr, true);
            qrCell.setBorder(Rectangle.NO_BORDER);
            qrCell.setPaddingBottom(8);
            inner.addCell(qrCell);
        } catch (Exception e) {
            PdfPCell err = new PdfPCell(new Phrase("QR error", normalFont));
            err.setBorder(Rectangle.NO_BORDER);
            inner.addCell(err);
        }

        // Card wrapper
        PdfPCell card = new PdfPCell(inner);
        card.setPadding(10);
        card.setBorderWidth(0.5f);
        card.setBorderColor(new java.awt.Color(200, 210, 220));
        card.setBackgroundColor(new java.awt.Color(252, 253, 255));
        return card;
    }

    private Image toPdfImage(BufferedImage bufferedImage) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "PNG", baos);
        return Image.getInstance(baos.toByteArray());
    }
}
