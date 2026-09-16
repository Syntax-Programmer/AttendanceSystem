package com.school.attendance.ui;

import com.school.attendance.model.AttendanceStatus;
import com.school.attendance.model.FacultyAssignment;
import com.school.attendance.model.User;
import com.school.attendance.repository.AttendanceRepository;
import com.school.attendance.repository.FacultyAssignmentRepository;
import com.school.attendance.repository.StudentRepository;
import com.school.attendance.repository.UserRepository;
import com.school.attendance.service.AttendanceService;
import com.school.attendance.service.FacultyAssignmentService;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

/**
 * Faculty Reports screen.
 * Only shows data for the faculty's assigned classes.
 */
public class FacultyReportsScreen extends VBox {

    private final User user;
    private final AttendanceService attendanceService;
    private final FacultyAssignmentService assignmentService;
    private List<FacultyAssignment> assignments;

    private final ComboBox<String> classFilter = new ComboBox<>();
    private final ComboBox<String> sectionFilter = new ComboBox<>();
    private final DatePicker datePicker = new DatePicker(LocalDate.now());
    private final TableView<Object[]> reportTable = new TableView<>();
    private final Label totalValue = new Label("0");
    private final Label presentValue = new Label("0");
    private final Label lateValue = new Label("0");
    private final Label absentValue = new Label("0");

    public FacultyReportsScreen(User user) {
        this.user = user;

        StudentRepository studentRepository = new StudentRepository();
        AttendanceRepository attendanceRepository = new AttendanceRepository();
        FacultyAssignmentRepository assignmentRepository = new FacultyAssignmentRepository();
        UserRepository userRepository = new UserRepository();

        this.attendanceService = new AttendanceService(studentRepository, attendanceRepository);
        this.assignmentService = new FacultyAssignmentService(assignmentRepository, userRepository);

        getStyleClass().add("content-area");
        setSpacing(20);
        setPadding(new javafx.geometry.Insets(30));

        Label title = new Label("Attendance Reports");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("View attendance records for your assigned classes");
        subtitle.getStyleClass().add("page-subtitle");

        VBox header = new VBox(5, title, subtitle);

        loadAssignments();

        VBox filterCard = buildFilterCard();
        HBox statistics = buildStatistics();
        VBox tableCard = buildTableCard();

        getChildren().addAll(header, filterCard, statistics, tableCard);
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        loadReport();
    }

    private void loadAssignments() {
        try {
            assignments = assignmentService.getAssignments(user.getUserId());
        } catch (Exception e) {
            e.printStackTrace();
            assignments = List.of();
        }
    }

    private VBox buildFilterCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");

        Label label = new Label("REPORT FILTERS");
        label.getStyleClass().add("stat-title");

        datePicker.setPrefHeight(42);

        // Class filter - only from assigned classes
        classFilter.getItems().add("All");
        if (assignments != null) {
            for (FacultyAssignment a : assignments) {
                String key = "Class " + a.getClassNumber();
                if (!classFilter.getItems().contains(key)) {
                    classFilter.getItems().add(key);
                }
            }
        }
        classFilter.setValue("All");
        classFilter.setPrefHeight(42);
        classFilter.setPrefWidth(150);

        // Section filter - only from assigned classes
        sectionFilter.getItems().add("All");
        if (assignments != null) {
            for (FacultyAssignment a : assignments) {
                if (!sectionFilter.getItems().contains(a.getSection())) {
                    sectionFilter.getItems().add(a.getSection());
                }
            }
        }
        sectionFilter.setValue("All");
        sectionFilter.setPrefHeight(42);
        sectionFilter.setPrefWidth(130);

        Button viewButton = new Button("View Report");
        viewButton.getStyleClass().add("primary-button");
        viewButton.setPrefHeight(42);
        viewButton.setOnAction(e -> loadReport());

        Button exportButton = new Button("Export CSV");
        exportButton.getStyleClass().add("secondary-button");
        exportButton.setPrefHeight(42);
        exportButton.setOnAction(e -> exportCsv());

        Label dateLabel = new Label("Date");
        Label classLabel = new Label("Class");
        Label sectionLabel = new Label("Section");
        VBox dateBox = new VBox(5, dateLabel, datePicker);
        VBox classBox = new VBox(5, classLabel, classFilter);
        VBox sectionBox = new VBox(5, sectionLabel, sectionFilter);
        HBox row = new HBox(20, dateBox, classBox, sectionBox, viewButton, exportButton);
        row.setAlignment(Pos.BOTTOM_LEFT);
        card.getChildren().addAll(label, row);

        return card;
    }

    private HBox buildStatistics() {
        HBox statistics = new HBox(15);
        statistics.getChildren().addAll(
            createStatCard("TOTAL STUDENTS", totalValue),
            createStatCard("PRESENT", presentValue),
            createStatCard("LATE", lateValue),
            createStatCard("ABSENT", absentValue)
        );
        return statistics;
    }

    private VBox createStatCard(String title, Label value) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");
        value.getStyleClass().add("stat-value");
        VBox card = new VBox(8, titleLabel, value);
        card.getStyleClass().add("stat-card");
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private VBox buildTableCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");

        Label title = new Label("ATTENDANCE");
        title.getStyleClass().add("stat-title");

        buildTableColumns();
        reportTable.setPlaceholder(new Label("No records found."));
        VBox.setVgrow(reportTable, Priority.ALWAYS);
        card.getChildren().addAll(title, reportTable);
        VBox.setVgrow(card, Priority.ALWAYS);
        return card;
    }

    private void buildTableColumns() {
        TableColumn<Object[], String> rollColumn = new TableColumn<>("Roll No.");
        rollColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue()[0])));

        TableColumn<Object[], String> nameColumn = new TableColumn<>("Student");
        nameColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue()[1])));

        TableColumn<Object[], String> classColumn = new TableColumn<>("Class");
        classColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue()[2])));

        TableColumn<Object[], String> sectionColumn = new TableColumn<>("Section");
        sectionColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue()[3])));

        TableColumn<Object[], String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data -> {
            Object status = data.getValue()[4];
            String text = status == null ? "NOT MARKED" : status.toString();
            return new javafx.beans.property.SimpleStringProperty(text);
        });

        TableColumn<Object[], String> markedAtColumn = new TableColumn<>("Marked At");
        markedAtColumn.setCellValueFactory(data -> {
            Timestamp timestamp = (Timestamp) data.getValue()[5];
            String text = timestamp == null
                ? "—"
                : timestamp.toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            return new javafx.beans.property.SimpleStringProperty(text);
        });

        reportTable.getColumns().addAll(rollColumn, nameColumn, classColumn, sectionColumn, statusColumn, markedAtColumn);
        reportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private void loadReport() {
        LocalDate date = datePicker.getValue();
        if (date == null) return;

        Integer classNumber = null;
        String selectedClass = classFilter.getValue();
        if (selectedClass != null && !selectedClass.equals("All")) {
            classNumber = Integer.parseInt(selectedClass.replace("Class ", ""));
        }

        String section = null;
        String selectedSection = sectionFilter.getValue();
        if (selectedSection != null && !selectedSection.equals("All")) {
            section = selectedSection;
        }

        try {
            List<Object[]> records = attendanceService.getReportByAssignedClasses(
                date, assignments, classNumber, section
            );
            reportTable.setItems(FXCollections.observableArrayList(records));
            updateStatistics(records);
        } catch (Exception e) {
            e.printStackTrace();
            totalValue.setText("—");
            presentValue.setText("—");
            lateValue.setText("—");
            absentValue.setText("—");
        }
    }

    private void updateStatistics(List<Object[]> records) {
        long total = records.size();
        long present = records.stream()
            .filter(row -> row[4] != null && row[4].toString().equals(AttendanceStatus.PRESENT.name()))
            .count();
        long late = records.stream()
            .filter(row -> row[4] != null && row[4].toString().equals(AttendanceStatus.LATE.name()))
            .count();
        long absent = records.stream()
            .filter(row -> row[4] != null && row[4].toString().equals(AttendanceStatus.ABSENT.name()))
            .count();
        totalValue.setText(String.valueOf(total));
        presentValue.setText(String.valueOf(present));
        lateValue.setText(String.valueOf(late));
        absentValue.setText(String.valueOf(absent));
    }

    private void exportCsv() {
        List<Object[]> records = reportTable.getItems();
        if (records == null || records.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export CSV");
            alert.setHeaderText(null);
            alert.setContentText("No data to export.");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Attendance CSV");
        fileChooser.setInitialFileName("attendance_report.csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file == null) return; // User cancelled

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Header
            writer.write("Roll No,Name,Class,Section,Status,Marked At");
            writer.newLine();
            for (Object[] row : records) {
                Timestamp timestamp = (Timestamp) row[5];
                String markedAt = timestamp == null
                    ? ""
                    : timestamp.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String status = row[4] == null ? "NOT MARKED" : row[4].toString();
                writer.write(
                    csvEscape(String.valueOf(row[0])) + "," +
                    csvEscape(String.valueOf(row[1])) + "," +
                    csvEscape(String.valueOf(row[2])) + "," +
                    csvEscape(String.valueOf(row[3])) + "," +
                    csvEscape(status) + "," +
                    csvEscape(markedAt)
                );
                writer.newLine();
            }
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Export CSV");
            success.setHeaderText(null);
            success.setContentText("CSV exported successfully to:\n" + file.getAbsolutePath());
            success.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Export CSV");
            error.setHeaderText(null);
            error.setContentText("Failed to save CSV: " + e.getMessage());
            error.showAndWait();
        }
    }

    /**
     * Escapes a CSV field - wraps in quotes if it contains comma, quote, or newline.
     */
    private String csvEscape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
