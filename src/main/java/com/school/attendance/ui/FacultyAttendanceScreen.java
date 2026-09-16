package com.school.attendance.ui;

import com.school.attendance.model.AttendanceStatus;
import com.school.attendance.model.FacultyAssignment;
import com.school.attendance.model.Student;
import com.school.attendance.model.User;
import com.school.attendance.repository.AttendanceRepository;
import com.school.attendance.repository.FacultyAssignmentRepository;
import com.school.attendance.repository.StudentRepository;
import com.school.attendance.repository.UserRepository;
import com.school.attendance.service.AttendanceService;
import com.school.attendance.service.FacultyAssignmentService;
import com.school.attendance.service.StudentService;
import java.time.LocalDate;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

/**
 * Faculty Attendance screen.
 * Faculty can only mark attendance for their assigned classes.
 * Assignment check is enforced in application logic (not just UI).
 */
public class FacultyAttendanceScreen extends VBox {

    private final User user;
    private final AttendanceService attendanceService;
    private final FacultyAssignmentService assignmentService;
    private final StudentService studentService;

    private final ComboBox<FacultyAssignment> classSelector = new ComboBox<>();
    private final DatePicker datePicker = new DatePicker(LocalDate.now());
    private final TableView<Student> studentTable = new TableView<>();
    private final Label statusLabel = new Label();
    private List<FacultyAssignment> assignments;

    public FacultyAttendanceScreen(User user) {
        this.user = user;

        StudentRepository studentRepository = new StudentRepository();
        AttendanceRepository attendanceRepository = new AttendanceRepository();
        FacultyAssignmentRepository assignmentRepository = new FacultyAssignmentRepository();
        UserRepository userRepository = new UserRepository();

        this.attendanceService = new AttendanceService(studentRepository, attendanceRepository);
        this.assignmentService = new FacultyAssignmentService(assignmentRepository, userRepository);
        this.studentService = new StudentService(studentRepository);

        getStyleClass().add("content-area");
        setSpacing(20);
        setPadding(new javafx.geometry.Insets(30));

        Label title = new Label("Mark Attendance");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Mark attendance for your assigned classes");
        subtitle.getStyleClass().add("page-subtitle");

        VBox header = new VBox(5, title, subtitle);

        VBox filterCard = buildFilterCard();
        VBox tableCard = buildTableCard();

        statusLabel.setStyle("-fx-text-fill: #687786; -fx-font-size: 13px;");

        getChildren().addAll(header, filterCard, statusLabel, tableCard);
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        loadAssignments();
    }

    private VBox buildFilterCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");

        Label label = new Label("SELECT CLASS");
        label.getStyleClass().add("stat-title");

        // Class selector displays faculty's assigned classes only
        classSelector.setConverter(new javafx.util.StringConverter<FacultyAssignment>() {
            @Override
            public String toString(FacultyAssignment a) {
                return a == null ? "" : "Class " + a.getClassNumber() + " - " + a.getSection();
            }
            @Override
            public FacultyAssignment fromString(String s) { return null; }
        });
        classSelector.setPrefHeight(42);
        classSelector.setPrefWidth(200);

        datePicker.setPrefHeight(42);

        Button loadButton = new Button("Load Students");
        loadButton.getStyleClass().add("primary-button");
        loadButton.setPrefHeight(42);
        loadButton.setOnAction(e -> loadStudentsForClass());

        Label classLabel = new Label("Class");
        Label dateLabel = new Label("Date");
        VBox classBox = new VBox(5, classLabel, classSelector);
        VBox dateBox = new VBox(5, dateLabel, datePicker);
        HBox row = new HBox(20, classBox, dateBox, loadButton);
        row.setAlignment(Pos.BOTTOM_LEFT);

        card.getChildren().addAll(label, row);
        return card;
    }

    private VBox buildTableCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");

        Label label = new Label("STUDENTS");
        label.getStyleClass().add("stat-title");

        TableColumn<Student, Integer> rollColumn = new TableColumn<>("Roll No");
        rollColumn.setCellValueFactory(new PropertyValueFactory<>("rollNo"));
        rollColumn.setPrefWidth(80);

        TableColumn<Student, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Student, Void> actionColumn = new TableColumn<>("Mark Attendance");
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button presentBtn = new Button("Present");
            private final Button lateBtn = new Button("Late");
            private final Button absentBtn = new Button("Absent");
            private final HBox buttons = new HBox(8, presentBtn, lateBtn, absentBtn);

            {
                presentBtn.getStyleClass().add("primary-button");
                lateBtn.getStyleClass().add("secondary-button");
                absentBtn.getStyleClass().add("secondary-button");

                presentBtn.setOnAction(e -> markAttendance(getIndex(), AttendanceStatus.PRESENT));
                lateBtn.setOnAction(e -> markAttendance(getIndex(), AttendanceStatus.LATE));
                absentBtn.setOnAction(e -> markAttendance(getIndex(), AttendanceStatus.ABSENT));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });

        studentTable.getColumns().addAll(rollColumn, nameColumn, actionColumn);
        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        studentTable.setPlaceholder(new Label("Select a class and click 'Load Students'."));
        VBox.setVgrow(studentTable, Priority.ALWAYS);

        card.getChildren().addAll(label, studentTable);
        VBox.setVgrow(card, Priority.ALWAYS);
        return card;
    }

    private void loadAssignments() {
        try {
            assignments = assignmentService.getAssignments(user.getUserId());
            classSelector.setItems(FXCollections.observableArrayList(assignments));
            if (!assignments.isEmpty()) {
                classSelector.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showStatus("Error loading assignments: " + e.getMessage());
        }
    }

    private void loadStudentsForClass() {
        FacultyAssignment selected = classSelector.getValue();
        if (selected == null) {
            showStatus("Please select a class first.");
            return;
        }
        // Security: verify this assignment belongs to this faculty
        if (!attendanceService.isAssignmentAllowed(
                selected.getClassNumber(), selected.getSection(), assignments)) {
            showStatus("Access denied: you are not assigned to this class.");
            return;
        }
        try {
            List<Student> students = studentService.getStudentsByClassAndSection(
                selected.getClassNumber(), selected.getSection()
            );
            studentTable.setItems(FXCollections.observableArrayList(students));
            showStatus("Loaded " + students.size() + " student(s) for Class " +
                selected.getClassNumber() + "-" + selected.getSection());
        } catch (Exception e) {
            e.printStackTrace();
            showStatus("Error loading students: " + e.getMessage());
        }
    }

    private void markAttendance(int index, AttendanceStatus status) {
        if (index < 0 || index >= studentTable.getItems().size()) return;
        Student student = studentTable.getItems().get(index);
        FacultyAssignment selected = classSelector.getValue();
        LocalDate date = datePicker.getValue();

        // Security: re-check assignment before marking
        if (!attendanceService.isAssignmentAllowed(
                selected.getClassNumber(), selected.getSection(), assignments)) {
            showStatus("Access denied: you are not assigned to this class.");
            return;
        }

        try {
            attendanceService.markOrUpdateAttendance(student.getRollNo(), date, status);
            showStatus("Marked " + student.getName() + " as " + status.name() +
                " on " + date + ".");
        } catch (Exception e) {
            e.printStackTrace();
            showStatus("Error marking attendance: " + e.getMessage());
        }
    }

    private void showStatus(String message) {
        statusLabel.setText(message);
    }
}
