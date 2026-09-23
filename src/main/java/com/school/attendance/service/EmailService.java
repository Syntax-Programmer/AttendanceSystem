package com.school.attendance.service;

import io.github.cdimascio.dotenv.Dotenv;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Sends parent email notifications after attendance is marked.
 *
 * Configuration via .env:
 *   SMTP_HOST, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD, SMTP_FROM
 *
 * If SMTP_USERNAME is blank, email sending is skipped (not configured).
 * Attendance is NEVER blocked or rolled back due to email failure.
 */
public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());

    private final String smtpHost;
    private final int smtpPort;
    private final String smtpUsername;
    private final String smtpPassword;
    private final String smtpFrom;
    private final boolean configured;

    public EmailService() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        this.smtpHost     = dotenv.get("SMTP_HOST", "smtp.gmail.com");
        this.smtpPort     = parsePort(dotenv.get("SMTP_PORT", "587"));
        this.smtpUsername = dotenv.get("SMTP_USERNAME", "");
        this.smtpPassword = dotenv.get("SMTP_PASSWORD", "");
        this.smtpFrom     = dotenv.get("SMTP_FROM", "noreply@school.local");
        this.configured   = smtpUsername != null && !smtpUsername.isBlank();
    }

    /**
     * Returns true if SMTP credentials are configured.
     */
    public boolean isConfigured() {
        return configured;
    }

    /**
     * Sends an attendance notification email to the parent.
     *
     * @param parentEmail   recipient email address
     * @param studentName   student's full name
     * @param rollNo        student roll number
     * @param classNumber   class number
     * @param section       class section
     * @param status        attendance status (PRESENT/ABSENT/LATE)
     * @param date          attendance date
     * @param markedAt      when it was marked (can be null)
     * @throws Exception    if sending fails (caller decides how to handle)
     */
    public void sendAttendanceNotification(
        String parentEmail,
        String studentName,
        int rollNo,
        int classNumber,
        String section,
        String status,
        LocalDate date,
        LocalDateTime markedAt
    ) throws Exception {
        if (!configured) {
            LOGGER.info("[EmailService] SMTP not configured — skipping email to " + parentEmail);
            return;
        }
        if (parentEmail == null || parentEmail.isBlank()) {
            LOGGER.info("[EmailService] No parent email for roll " + rollNo + " — skipping.");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", String.valueOf(smtpPort));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUsername, smtpPassword);
            }
        });

        String dateStr = date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        String timeStr = markedAt != null
            ? markedAt.format(DateTimeFormatter.ofPattern("HH:mm"))
            : "—";

        String subject = "Attendance Update: " + studentName + " — " + dateStr;
        String body = buildEmailBody(studentName, rollNo, classNumber, section, status, dateStr, timeStr);

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(smtpFrom));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(parentEmail));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
        LOGGER.info("[EmailService] Attendance email sent to " + parentEmail
            + " for roll " + rollNo + " status=" + status);
    }

    private String buildEmailBody(
        String studentName, int rollNo, int classNumber, String section,
        String status, String dateStr, String timeStr
    ) {
        return "Dear Parent/Guardian,\n\n"
            + "This is an automated attendance notification from the School Attendance System.\n\n"
            + "Student Details:\n"
            + "  Name      : " + studentName + "\n"
            + "  Roll No   : " + rollNo + "\n"
            + "  Class     : " + classNumber + " - " + section + "\n\n"
            + "Attendance Record:\n"
            + "  Date      : " + dateStr + "\n"
            + "  Time      : " + timeStr + "\n"
            + "  Status    : " + status + "\n\n"
            + "If you have any questions, please contact the school office.\n\n"
            + "Regards,\n"
            + "School Attendance System";
    }

    private int parsePort(String portStr) {
        try {
            return Integer.parseInt(portStr.trim());
        } catch (Exception e) {
            return 587;
        }
    }
}
