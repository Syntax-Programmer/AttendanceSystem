package com.school.attendance.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates a printable PDF attendance report using OpenPDF.
 *
 * Layout: landscape A4, tabular data matching the on-screen report.
 * Columns: Roll No | Student | Class | Section | Status | Marked At
 */
public class AttendanceReportPdfService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private static final Color HEADER_BG    = new Color(30, 41, 59);   // dark blue-gray
    private static final Color HEADER_FG    = Color.WHITE;
    private static final Color ROW_ALT_BG   = new Color(248, 250, 252);
    private static final Color BORDER_COLOR = new Color(203, 213, 225);
    private static final Color PRESENT_FG   = new Color(21, 128, 61);
    private static final Color LATE_FG      = new Color(180, 83, 9);
    private static final Color ABSENT_FG    = new Color(185, 28, 28);
    private static final Color UNMARKED_FG  = new Color(100, 116, 139);

    /**
     * Generates an attendance report PDF.
     *
     * @param records    attendance rows: [rollNo, name, class, section, status, Timestamp]
     * @param reportDate the date shown in the report header
     * @param classFilter optional class filter label (e.g. "Class 5") or "All"
     * @param sectionFilter optional section filter label (e.g. "A") or "All"
     * @param outputFile destination file
     * @throws Exception on PDF generation failure
     */
    public void generatePdf(
        List<Object[]> records,
        LocalDate reportDate,
        String classFilter,
        String sectionFilter,
        File outputFile
    ) throws Exception {

        // Use landscape A4 for more column space
        Rectangle pageSize = new Rectangle(PageSize.A4.getHeight(), PageSize.A4.getWidth());
        Document document = new Document(pageSize, 40, 40, 40, 40);

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            try {
                PdfWriter.getInstance(document, fos);
                document.open();

                addTitle(document, reportDate, classFilter, sectionFilter, records);
                addSummary(document, records);
                addTable(document, records);
            } finally {
                if (document.isOpen()) {
                    document.close();
                }
            }
        }
    }

    // ── Title ─────────────────────────────────────────────────────────────────

    private void addTitle(
        Document doc,
        LocalDate date,
        String classFilter,
        String sectionFilter,
        List<Object[]> records
    ) throws DocumentException {

        Font titleFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, HEADER_BG);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 11, new Color(71, 85, 105));

        Paragraph title = new Paragraph("Attendance Report", titleFont);
        title.setAlignment(Element.ALIGN_LEFT);
        doc.add(title);

        String subtitle = "Date: " + date.format(DATE_FMT)
            + "   |   Class: " + (classFilter == null || classFilter.equals("All") ? "All Classes" : classFilter)
            + "   |   Section: " + (sectionFilter == null || sectionFilter.equals("All") ? "All Sections" : sectionFilter)
            + "   |   Total Students: " + records.size();

        Paragraph sub = new Paragraph(subtitle, subtitleFont);
        sub.setAlignment(Element.ALIGN_LEFT);
        sub.setSpacingBefore(4);
        sub.setSpacingAfter(16);
        doc.add(sub);
    }

    // ── Summary bar ───────────────────────────────────────────────────────────

    private void addSummary(Document doc, List<Object[]> records) throws DocumentException {
        long present = records.stream()
            .filter(r -> r[4] != null && r[4].toString().equals("PRESENT")).count();
        long late    = records.stream()
            .filter(r -> r[4] != null && r[4].toString().equals("LATE")).count();
        long absent  = records.stream()
            .filter(r -> r[4] != null && r[4].toString().equals("ABSENT")).count();
        long notMarked = records.stream().filter(r -> r[4] == null).count();

        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.WHITE);

        PdfPTable summary = new PdfPTable(4);
        summary.setWidthPercentage(60);
        summary.setHorizontalAlignment(Element.ALIGN_LEFT);
        summary.setSpacingAfter(16);

        addSummaryCell(summary, "PRESENT", String.valueOf(present), PRESENT_FG, labelFont, valueFont);
        addSummaryCell(summary, "LATE",    String.valueOf(late),    LATE_FG,    labelFont, valueFont);
        addSummaryCell(summary, "ABSENT",  String.valueOf(absent),  ABSENT_FG,  labelFont, valueFont);
        addSummaryCell(summary, "NOT MARKED", String.valueOf(notMarked), UNMARKED_FG, labelFont, valueFont);

        doc.add(summary);
    }

    private void addSummaryCell(PdfPTable table, String label, String value,
                                Color bg, Font labelFont, Font valueFont) {
        PdfPTable inner = new PdfPTable(1);
        inner.setWidthPercentage(100);

        PdfPCell valCell = new PdfPCell(new Phrase(value, valueFont));
        valCell.setBorder(Rectangle.NO_BORDER);
        valCell.setBackgroundColor(bg);
        valCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        valCell.setPaddingTop(8);
        valCell.setPaddingBottom(2);
        inner.addCell(valCell);

        PdfPCell lblCell = new PdfPCell(new Phrase(label, labelFont));
        lblCell.setBorder(Rectangle.NO_BORDER);
        lblCell.setBackgroundColor(bg);
        lblCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        lblCell.setPaddingBottom(8);
        inner.addCell(lblCell);

        PdfPCell wrapper = new PdfPCell(inner);
        wrapper.setPadding(0);
        wrapper.setBorder(Rectangle.NO_BORDER);
        wrapper.setPaddingRight(6);
        table.addCell(wrapper);
    }

    // ── Data table ────────────────────────────────────────────────────────────

    private void addTable(Document doc, List<Object[]> records) throws DocumentException {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, HEADER_FG);
        Font cellFont   = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(30, 41, 59));

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.2f, 3.5f, 1.2f, 1.2f, 1.8f, 1.8f});

        // Header row
        addHeaderCell(table, "Roll No.",  headerFont, HEADER_BG);
        addHeaderCell(table, "Student",   headerFont, HEADER_BG);
        addHeaderCell(table, "Class",     headerFont, HEADER_BG);
        addHeaderCell(table, "Section",   headerFont, HEADER_BG);
        addHeaderCell(table, "Status",    headerFont, HEADER_BG);
        addHeaderCell(table, "Marked At", headerFont, HEADER_BG);

        // Data rows
        boolean alt = false;
        for (Object[] row : records) {
            Color rowBg = alt ? ROW_ALT_BG : Color.WHITE;
            alt = !alt;

            String statusStr = row[4] == null ? "NOT MARKED" : row[4].toString();
            Color statusColor = switch (statusStr) {
                case "PRESENT"    -> PRESENT_FG;
                case "LATE"       -> LATE_FG;
                case "ABSENT"     -> ABSENT_FG;
                default           -> UNMARKED_FG;
            };

            Timestamp ts = (Timestamp) row[5];
            String markedAt = ts == null ? "—"
                : ts.toLocalDateTime().format(TIME_FMT);

            addDataCell(table, String.valueOf(row[0]), cellFont, rowBg, Element.ALIGN_CENTER);
            addDataCell(table, String.valueOf(row[1]), cellFont, rowBg, Element.ALIGN_LEFT);
            addDataCell(table, String.valueOf(row[2]), cellFont, rowBg, Element.ALIGN_CENTER);
            addDataCell(table, String.valueOf(row[3]), cellFont, rowBg, Element.ALIGN_CENTER);

            // Status cell with color
            Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, statusColor);
            PdfPCell statusCell = new PdfPCell(new Phrase(statusStr, statusFont));
            statusCell.setBackgroundColor(rowBg);
            statusCell.setBorderColor(BORDER_COLOR);
            statusCell.setBorderWidth(0.5f);
            statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            statusCell.setPaddingTop(6);
            statusCell.setPaddingBottom(6);
            table.addCell(statusCell);

            addDataCell(table, markedAt, cellFont, rowBg, Element.ALIGN_CENTER);
        }

        doc.add(table);
    }

    private void addHeaderCell(PdfPTable table, String text, Font font, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setBorderColor(BORDER_COLOR);
        cell.setBorderWidth(0.5f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPaddingTop(8);
        cell.setPaddingBottom(8);
        table.addCell(cell);
    }

    private void addDataCell(PdfPTable table, String text, Font font, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setBorderColor(BORDER_COLOR);
        cell.setBorderWidth(0.5f);
        cell.setHorizontalAlignment(align);
        cell.setPaddingTop(6);
        cell.setPaddingBottom(6);
        table.addCell(cell);
    }
}
