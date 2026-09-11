package com.school.attendance.ui;

import com.school.attendance.model.Attendance;
import com.school.attendance.model.AttendanceStatus;
import com.school.attendance.model.Student;
import com.school.attendance.repository.AttendanceRepository;
import com.school.attendance.service.AttendanceService;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class StudentProfile extends VBox {

    private final Student student;
    private final AttendanceService attendanceService;

    private final TableView<Attendance> attendanceTable = new TableView<>();

    public StudentProfile(Student student) {
        this.student = student;
        AttendanceRepository attendanceRepository = new AttendanceRepository();
        attendanceService = new AttendanceService(
            new com.school.attendance.repository.StudentRepository(),
            attendanceRepository
        );
        getStyleClass().add("content-area");
        setSpacing(20);
        buildUI();
    }

    private void buildUI() {
        Label title = new Label("Student Profile");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Student information and attendance history");
        subtitle.getStyleClass().add("page-subtitle");
        VBox header = new VBox(5, title, subtitle);
        VBox studentInfo = createStudentInfo();
        HBox statistics = createAttendanceStatistics();
        VBox history = createAttendanceHistory();

        getChildren().addAll(header, studentInfo, statistics, history);
    }

    private VBox createStudentInfo() {
        VBox card = new VBox(12);

        card.getStyleClass().add("card");
        Label sectionTitle = new Label("STUDENT INFORMATION");
        sectionTitle.getStyleClass().add("stat-title");
        Label name = new Label(student.getName());
        name.getStyleClass().add("page-title");
        Label classInfo = new Label(
            "Class " + student.getClassNumber() + " • Section " + student.getSection()
        );
        classInfo.getStyleClass().add("page-subtitle");
        Label rollNo = new Label("Roll No: " + student.getRollNo());
        rollNo.getStyleClass().add("page-subtitle");
        Label parent = new Label("Parent: " + safeValue(student.getParentName()));
        Label phone = new Label("Phone: " + safeValue(student.getParentPhone()));
        Label dob = new Label(
            "Date of Birth: " +
                safeValue(
                    student.getDateOfBirth() == null ? null : student.getDateOfBirth().toString()
                )
        );

        Label gender = new Label("Gender: " + safeValue(student.getGender()));
        card.getChildren().addAll(
            sectionTitle,
            name,
            classInfo,
            rollNo,
            parent,
            phone,
            dob,
            gender
        );

        return card;
    }

    private HBox createAttendanceStatistics() {
        HBox statistics = new HBox(15);

        try {
            List<Attendance> records = attendanceService.getStudentAttendance(student.getRollNo());
            long present = records
                .stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();
            long late = records
                .stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                .count();
            long absent = records
                .stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();
            statistics
                .getChildren()
                .addAll(
                    createStatCard("PRESENT", String.valueOf(present)),
                    createStatCard("LATE", String.valueOf(late)),
                    createStatCard("ABSENT", String.valueOf(absent))
                );
        } catch (Exception e) {
            e.printStackTrace();
            statistics.getChildren().add(createStatCard("ATTENDANCE", "ERROR"));
        }

        return statistics;
    }

    private VBox createStatCard(String title, String value) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");
        VBox card = new VBox(8, titleLabel, valueLabel);
        card.getStyleClass().add("stat-card");
        HBox.setHgrow(card, javafx.scene.layout.Priority.ALWAYS);

        return card;
    }

    private VBox createAttendanceHistory() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        Label title = new Label("Attendance History");
        title.getStyleClass().add("page-title");
        createTableColumns();
        loadAttendance();
        card.getChildren().addAll(title, attendanceTable);
        VBox.setVgrow(attendanceTable, javafx.scene.layout.Priority.ALWAYS);

        return card;
    }

    private void createTableColumns() {
        TableColumn<Attendance, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getAttendanceDate().toString()
            )
        );
        TableColumn<Attendance, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus().name())
        );
        TableColumn<Attendance, String> markedAtColumn = new TableColumn<>("Marked At");
        markedAtColumn.setCellValueFactory(data -> {
            Attendance attendance = data.getValue();
            if (attendance.getMarkedAt() == null) {
                return new javafx.beans.property.SimpleStringProperty("—");
            }
            return new javafx.beans.property.SimpleStringProperty(
                attendance.getMarkedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
            );
        });

        dateColumn.setPrefWidth(180);
        statusColumn.setPrefWidth(150);
        markedAtColumn.setPrefWidth(250);
        attendanceTable.getColumns().addAll(dateColumn, statusColumn, markedAtColumn);
        attendanceTable.setPrefHeight(250);
    }

    private void loadAttendance() {
        try {
            List<Attendance> records = attendanceService.getStudentAttendance(student.getRollNo());
            attendanceTable.getItems().setAll(records);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String safeValue(String value) {
        if (value == null || value.isBlank()) {
            return "Not provided";
        }

        return value;
    }
}
