package com.school.attendance.ui;

import com.school.attendance.model.AttendanceStatus;
import com.school.attendance.model.Student;
import com.school.attendance.repository.AttendanceRepository;
import com.school.attendance.repository.StudentRepository;
import com.school.attendance.service.AttendanceService;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AttendanceScreen extends BorderPane {

    private final AttendanceService attendanceService;

    private TextField rollNumberField;

    private Label studentName;
    private Label studentClass;
    private Label studentRoll;

    private VBox studentCard;

    public AttendanceScreen() {
        StudentRepository studentRepository = new StudentRepository();

        AttendanceRepository attendanceRepository = new AttendanceRepository();

        attendanceService = new AttendanceService(studentRepository, attendanceRepository);

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

        rollNumberField.setPromptText("Enter roll number...");

        rollNumberField.setPrefHeight(45);

        Button searchButton = new Button("Find Student");

        searchButton.getStyleClass().add("primary-button");

        searchButton.setPrefHeight(45);

        searchButton.setOnAction(event -> findStudent());

        // Pressing Enter also searches
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

        if (text.isEmpty()) {
            return;
        }

        try {
            int rollNo = Integer.parseInt(text);

            Optional<Student> result = attendanceService.findStudent(rollNo);

            if (result.isEmpty()) {
                studentCard.setVisible(false);
                studentCard.setManaged(false);

                showMessage("Student not found.");

                return;
            }

            Student student = result.get();

            studentName.setText(student.getName());

            studentClass.setText(
                "Class " + student.getClassNumber() + " • Section " + student.getSection()
            );

            studentRoll.setText("Roll No: " + student.getRollNo());

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
        try {
            int rollNo = Integer.parseInt(rollNumberField.getText().trim());

            attendanceService.markAttendance(rollNo, status);

            showMessage("Attendance marked as " + status.name() + ".");
        } catch (IllegalStateException e) {
            showMessage("Attendance has already been marked today.");
        } catch (Exception e) {
            e.printStackTrace();

            showMessage("Unable to mark attendance.");
        }
    }

    private void showMessage(String message) {
        Label messageLabel = new Label(message);

        messageLabel.setStyle("-fx-text-fill: #687786; " + "-fx-font-size: 13px;");

        VBox content = (VBox) getCenter();

        if (content.getChildren().size() > 3) {
            content.getChildren().remove(content.getChildren().size() - 1);
        }

        content.getChildren().add(messageLabel);
    }
}
