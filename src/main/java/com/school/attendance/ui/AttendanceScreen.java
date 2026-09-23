package com.school.attendance.ui;

import com.school.attendance.model.AttendanceStatus;
import com.school.attendance.model.Student;
import com.school.attendance.repository.AttendanceRepository;
import com.school.attendance.repository.StudentRepository;
import com.school.attendance.service.AttendanceService;
import com.school.attendance.service.EmailService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Logger;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Management Attendance screen.
 * Accepts roll number (typed or scanned via USB barcode scanner keyboard wedge).
 * Marks attendance through AttendanceService, then sends parent email notification
 * through EmailService. Email failure never blocks attendance from being saved.
 */
public class AttendanceScreen extends BorderPane {

    private static final Logger LOGGER = Logger.getLogger(AttendanceScreen.class.getName());

    private final AttendanceService attendanceService;
    private final EmailService emailService;

    private TextField rollNumberField;

    private Label studentName;
    private Label studentClass;
    private Label studentRoll;

    private VBox studentCard;

    // The currently displayed student (used when marking attendance)
    private Student currentStudent;

    public AttendanceScreen() {
        StudentRepository studentRepository = new StudentRepository();
        AttendanceRepository attendanceRepository = new AttendanceRepository();
        attendanceService = new AttendanceService(studentRepository, attendanceRepository);
        emailService = new EmailService();

        setCenter(createContent());
    }

    private VBox createContent() {
        VBox content = new VBox(25);
        content.getStyleClass().add("content-area");

        Label title = new Label("Mark Attendance");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Scan a barcode or enter a student's roll number");
        subtitle.getStyleClass().add("page-subtitle");

        VBox header = new VBox(5, title, subtitle);

        VBox searchCard = createSearchCard();
        studentCard = createStudentCard();
        studentCard.setVisible(false);
        studentCard.setManaged(false);

        content.getChildren().addAll(header, searchCard, studentCard);
        return content;
    }

    private VBox createSearchCard() {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");

        Label label = new Label("STUDENT IDENTIFICATION");
        label.getStyleClass().add("stat-title");

        rollNumberField = new TextField();
        rollNumberField.setPromptText("Enter or scan roll number...");
        rollNumberField.setPrefHeight(45);

        Button searchButton = new Button("Find Student");
        searchButton.getStyleClass().add("primary-button");
        searchButton.setPrefHeight(45);
        searchButton.setOnAction(event -> findStudent());

        // USB barcode scanner sends roll number followed by Enter — this handles it
        rollNumberField.setOnAction(event -> findStudent());

        HBox searchRow = new HBox(10, rollNumberField, searchButton);
        HBox.setHgrow(rollNumberField, javafx.scene.layout.Priority.ALWAYS);

        card.getChildren().addAll(label, searchRow);
        return card;
    }

    private VBox createStudentCard() {
        VBox card = new VBox(20);
        card.getStyleClass().add("card");

        Label heading = new Label("STUDENT FOUND");
        heading.getStyleClass().add("stat-title");

        studentName = new Label();
        studentName.getStyleClass().add("page-title");

        studentClass = new Label();
        studentClass.getStyleClass().add("page-subtitle");

        studentRoll = new Label();
        studentRoll.getStyleClass().add("page-subtitle");

        Button presentButton = new Button("PRESENT");
        presentButton.getStyleClass().add("primary-button");

        Button lateButton = new Button("LATE");
        lateButton.getStyleClass().add("secondary-button");

        Button absentButton = new Button("ABSENT");
        absentButton.getStyleClass().add("secondary-button");

        presentButton.setOnAction(event -> markAttendance(AttendanceStatus.PRESENT));
        lateButton.setOnAction(event -> markAttendance(AttendanceStatus.LATE));
        absentButton.setOnAction(event -> markAttendance(AttendanceStatus.ABSENT));

        HBox buttons = new HBox(10, presentButton, lateButton, absentButton);
        buttons.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(heading, studentName, studentClass, studentRoll, buttons);
        return card;
    }

    private void findStudent() {
        String text = rollNumberField.getText().trim();
        if (text.isEmpty()) return;

        try {
            int rollNo = Integer.parseInt(text);
            Optional<Student> result = attendanceService.findStudent(rollNo);

            if (result.isEmpty()) {
                currentStudent = null;
                studentCard.setVisible(false);
                studentCard.setManaged(false);
                showMessage("Student not found for roll number: " + rollNo);
                return;
            }

            currentStudent = result.get();
            studentName.setText(currentStudent.getName());
            studentClass.setText(
                "Class " + currentStudent.getClassNumber() + " \u2022 Section " + currentStudent.getSection()
            );
            studentRoll.setText("Roll No: " + currentStudent.getRollNo());
            studentCard.setVisible(true);
            studentCard.setManaged(true);

        } catch (NumberFormatException e) {
            showMessage("Please enter a valid roll number.");
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Unable to search for student.");
        }
    }

    private void markAttendance(AttendanceStatus status) {
        if (currentStudent == null) {
            showMessage("Please find a student first.");
            return;
        }

        int rollNo = currentStudent.getRollNo();

        try {
            // 1. Mark attendance (throws if already marked today)
            attendanceService.markAttendance(rollNo, status);

            // 2. Send parent email notification (non-blocking)
            String emailStatus = sendEmailNotification(currentStudent, status);

            showMessage("Attendance marked as " + status.name() + "." + emailStatus);

        } catch (IllegalStateException e) {
            // Duplicate attendance — do NOT send email
            showMessage("Attendance has already been marked for roll " + rollNo + " today.");
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Unable to mark attendance: " + e.getMessage());
        }
    }

    /**
     * Sends parent email notification. Returns a status string for the UI.
     * Never throws — any SMTP failure is caught and logged.
     * Attendance has already been saved before this is called.
     */
    private String sendEmailNotification(Student student, AttendanceStatus status) {
        String parentEmail = student.getParentEmail();
        if (parentEmail == null || parentEmail.isBlank()) {
            LOGGER.info("[AttendanceScreen] No parent email for roll " + student.getRollNo() + " — skipping.");
            return " (No parent email configured.)";
        }

        if (!emailService.isConfigured()) {
            LOGGER.info("[AttendanceScreen] SMTP not configured — skipping email.");
            return " (Email not configured.)";
        }

        try {
            emailService.sendAttendanceNotification(
                parentEmail,
                student.getName(),
                student.getRollNo(),
                student.getClassNumber(),
                student.getSection(),
                status.name(),
                LocalDate.now(),
                LocalDateTime.now()
            );
            return " Email sent to parent.";
        } catch (Exception e) {
            LOGGER.warning("[AttendanceScreen] Email failed for roll "
                + student.getRollNo() + ": " + e.getMessage());
            return " (Email delivery failed — attendance saved.)";
        }
    }

    private void showMessage(String message) {
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: #687786; -fx-font-size: 13px;");
        messageLabel.setWrapText(true);

        VBox content = (VBox) getCenter();
        if (content.getChildren().size() > 3) {
            content.getChildren().remove(content.getChildren().size() - 1);
        }
        content.getChildren().add(messageLabel);
    }
}
