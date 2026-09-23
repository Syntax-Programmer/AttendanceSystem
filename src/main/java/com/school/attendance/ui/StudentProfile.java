package com.school.attendance.ui;

import com.school.attendance.model.Attendance;
import com.school.attendance.model.AttendanceStatus;
import com.school.attendance.model.Student;
import com.school.attendance.repository.AttendanceRepository;
import com.school.attendance.repository.StudentRepository;
import com.school.attendance.service.AttendanceService;
import com.school.attendance.service.StudentService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Student profile view showing all student details including parent email,
 * attendance statistics, and attendance history.
 * Includes Edit Student functionality.
 */
public class StudentProfile extends VBox {

    private Student student;
    private final AttendanceService attendanceService;
    private final StudentService studentService;

    private final TableView<Attendance> attendanceTable = new TableView<>();

    // Info labels that can be refreshed after edit
    private Label nameLabel;
    private Label classInfoLabel;
    private Label rollNoLabel;
    private Label parentLabel;
    private Label phoneLabel;
    private Label emailLabel;
    private Label dobLabel;
    private Label genderLabel;
    private Label addressLabel;

    public StudentProfile(Student student) {
        this.student = student;
        AttendanceRepository attendanceRepository = new AttendanceRepository();
        attendanceService = new AttendanceService(
            new StudentRepository(),
            attendanceRepository
        );
        studentService = new StudentService(new StudentRepository());
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
        VBox card = new VBox(10);
        card.getStyleClass().add("card");

        Label sectionTitle = new Label("STUDENT INFORMATION");
        sectionTitle.getStyleClass().add("stat-title");

        nameLabel = new Label(student.getName());
        nameLabel.getStyleClass().add("page-title");

        classInfoLabel = new Label(
            "Class " + student.getClassNumber() + " • Section " + student.getSection()
        );
        classInfoLabel.getStyleClass().add("page-subtitle");

        rollNoLabel = new Label("Roll No: " + student.getRollNo());
        rollNoLabel.getStyleClass().add("page-subtitle");

        parentLabel = new Label("Parent: " + safeValue(student.getParentName()));
        phoneLabel  = new Label("Phone: " + safeValue(student.getParentPhone()));
        emailLabel  = new Label("Parent Email: " + safeValue(student.getParentEmail()));
        dobLabel    = new Label("Date of Birth: " + safeValue(
            student.getDateOfBirth() == null ? null : student.getDateOfBirth().toString()
        ));
        genderLabel  = new Label("Gender: " + safeValue(student.getGender()));
        addressLabel = new Label("Address: " + safeValue(student.getAddress()));

        Button editButton = new Button("Edit Student");
        editButton.getStyleClass().add("secondary-button");
        editButton.setOnAction(e -> showEditDialog());

        card.getChildren().addAll(
            sectionTitle, nameLabel, classInfoLabel, rollNoLabel,
            parentLabel, phoneLabel, emailLabel, dobLabel, genderLabel, addressLabel,
            editButton
        );
        return card;
    }

    private void showEditDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Student");
        dialog.setHeaderText("Edit information for Roll No. " + student.getRollNo());
        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(460);

        TextField nameField = new TextField(nvl(student.getName()));
        nameField.setPromptText("Full Name *");

        Spinner<Integer> classSpinner = new Spinner<>(1, 12, student.getClassNumber());
        classSpinner.setEditable(true);

        ComboBox<String> sectionBox = new ComboBox<>();
        sectionBox.getItems().addAll("A", "B", "C", "D", "E", "F");
        sectionBox.setValue(student.getSection());

        DatePicker dobPicker = new DatePicker(student.getDateOfBirth());

        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("Male", "Female", "Other", "Prefer not to say");
        genderBox.setValue(student.getGender());

        TextField parentNameField = new TextField(nvl(student.getParentName()));
        parentNameField.setPromptText("Parent Name");

        TextField parentPhoneField = new TextField(nvl(student.getParentPhone()));
        parentPhoneField.setPromptText("Parent Phone");

        TextField parentEmailField = new TextField(nvl(student.getParentEmail()));
        parentEmailField.setPromptText("Parent Email");

        TextArea addressArea = new TextArea(nvl(student.getAddress()));
        addressArea.setPromptText("Address");
        addressArea.setPrefRowCount(2);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #b91c1c; -fx-font-size: 12px;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.setPadding(new Insets(15));

        int r = 0;
        grid.add(new Label("Full Name *"), 0, r); grid.add(nameField, 1, r++);
        grid.add(new Label("Class"), 0, r); grid.add(classSpinner, 1, r++);
        grid.add(new Label("Section"), 0, r); grid.add(sectionBox, 1, r++);
        grid.add(new Label("Date of Birth"), 0, r); grid.add(dobPicker, 1, r++);
        grid.add(new Label("Gender"), 0, r); grid.add(genderBox, 1, r++);
        grid.add(new Label("Parent Name"), 0, r); grid.add(parentNameField, 1, r++);
        grid.add(new Label("Parent Phone"), 0, r); grid.add(parentPhoneField, 1, r++);
        grid.add(new Label("Parent Email"), 0, r); grid.add(parentEmailField, 1, r++);
        grid.add(new Label("Address"), 0, r); grid.add(addressArea, 1, r++);
        grid.add(errorLabel, 0, r, 2, 1);

        ColumnConstraints col1 = new ColumnConstraints(110);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        for (var node : new javafx.scene.Node[]{
                nameField, parentNameField, parentPhoneField, parentEmailField,
                addressArea, sectionBox, genderBox, dobPicker}) {
            if (node instanceof Region reg) {
                reg.setMaxWidth(Double.MAX_VALUE);
                GridPane.setHgrow(reg, Priority.ALWAYS);
            }
        }

        dialog.getDialogPane().setContent(new ScrollPane(grid) {{
            setFitToWidth(true);
            setPrefViewportHeight(400);
        }});

        dialog.setResultConverter(button -> {
            if (button == saveButton) {
                errorLabel.setText("");
                String newName = nameField.getText().trim();
                if (newName.isBlank()) {
                    errorLabel.setText("Name cannot be empty.");
                    return null;
                }
                student.setName(newName);
                student.setClassNumber(classSpinner.getValue());
                student.setSection(sectionBox.getValue() != null ? sectionBox.getValue() : student.getSection());
                student.setDateOfBirth(dobPicker.getValue());
                student.setGender(genderBox.getValue());
                student.setParentName(parentNameField.getText().trim());
                student.setParentPhone(parentPhoneField.getText().trim());
                student.setParentEmail(parentEmailField.getText().trim());
                student.setAddress(addressArea.getText().trim());
                try {
                    new StudentRepository().update(student);
                    refreshInfoLabels();
                } catch (Exception e) {
                    showError("Save failed.", e.getMessage());
                }
            }
            return button;
        });

        dialog.showAndWait();
    }

    private void refreshInfoLabels() {
        nameLabel.setText(student.getName());
        classInfoLabel.setText("Class " + student.getClassNumber() + " • Section " + student.getSection());
        rollNoLabel.setText("Roll No: " + student.getRollNo());
        parentLabel.setText("Parent: " + safeValue(student.getParentName()));
        phoneLabel.setText("Phone: " + safeValue(student.getParentPhone()));
        emailLabel.setText("Parent Email: " + safeValue(student.getParentEmail()));
        dobLabel.setText("Date of Birth: " + safeValue(
            student.getDateOfBirth() == null ? null : student.getDateOfBirth().toString()
        ));
        genderLabel.setText("Gender: " + safeValue(student.getGender()));
        addressLabel.setText("Address: " + safeValue(student.getAddress()));
    }

    private HBox createAttendanceStatistics() {
        HBox statistics = new HBox(15);
        try {
            List<Attendance> records = attendanceService.getStudentAttendance(student.getRollNo());
            long present = records.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
            long late    = records.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
            long absent  = records.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
            statistics.getChildren().addAll(
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
        HBox.setHgrow(card, Priority.ALWAYS);
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
        VBox.setVgrow(attendanceTable, Priority.ALWAYS);
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
        return (value == null || value.isBlank()) ? "Not provided" : value;
    }

    private String nvl(String value) {
        return value != null ? value : "";
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
