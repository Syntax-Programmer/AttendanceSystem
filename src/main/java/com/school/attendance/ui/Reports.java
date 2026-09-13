package com.school.attendance.ui;

import com.school.attendance.model.AttendanceStatus;
import com.school.attendance.repository.AttendanceRepository;
import com.school.attendance.repository.StudentRepository;
import com.school.attendance.service.AttendanceService;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class Reports extends BorderPane {

    private final ComboBox<String> classFilter = new ComboBox<>();
    private final ComboBox<String> sectionFilter = new ComboBox<>();

    private final AttendanceService attendanceService;
    private final DatePicker datePicker = new DatePicker(LocalDate.now());
    private final TableView<Object[]> reportTable = new TableView<>();
    private final Label totalValue = new Label("0");
    private final Label presentValue = new Label("0");
    private final Label lateValue = new Label("0");
    private final Label absentValue = new Label("0");

    public Reports() {
        StudentRepository studentRepository = new StudentRepository();
        AttendanceRepository attendanceRepository = new AttendanceRepository();
        attendanceService = new AttendanceService(studentRepository, attendanceRepository);
        getStyleClass().add("content-area");
        setPadding(new Insets(30));
        buildUI();
        loadReport();
    }

    private void buildUI() {
        Label title = new Label("Attendance Reports");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("View attendance records for any date");
        subtitle.getStyleClass().add("page-subtitle");

        VBox header = new VBox(5, title, subtitle);
        VBox filterCard = createFilterCard();
        HBox statistics = createStatistics();
        VBox tableCard = createTableCard();
        VBox content = new VBox(20, header, filterCard, statistics, tableCard);
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        setCenter(content);
    }

    private VBox createFilterCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");

        Label label = new Label("REPORT FILTERS");
        label.getStyleClass().add("stat-title");
        // Date
        datePicker.setPrefHeight(42);
        // Class
        classFilter.getItems().add("All");
        for (int i = 1; i <= 12; i++) {
            classFilter.getItems().add("Class " + i);
        }
        classFilter.setValue("All");
        classFilter.setPrefHeight(42);
        classFilter.setPrefWidth(150);
        // Section
        sectionFilter.getItems().addAll("All", "A", "B", "C", "D");
        sectionFilter.setValue("All");
        sectionFilter.setPrefHeight(42);
        sectionFilter.setPrefWidth(130);

        // View button
        Button viewButton = new Button("View Report");
        viewButton.getStyleClass().add("primary-button");
        viewButton.setPrefHeight(42);
        viewButton.setOnAction(event -> loadReport());

        Label dateLabel = new Label("Date");
        Label classLabel = new Label("Class");
        Label sectionLabel = new Label("Section");
        VBox dateBox = new VBox(5, dateLabel, datePicker);
        VBox classBox = new VBox(5, classLabel, classFilter);
        VBox sectionBox = new VBox(5, sectionLabel, sectionFilter);
        HBox row = new HBox(20, dateBox, classBox, sectionBox, viewButton);
        row.setAlignment(javafx.geometry.Pos.BOTTOM_LEFT);
        card.getChildren().addAll(label, row);

        return card;
    }

    private HBox createStatistics() {
        HBox statistics = new HBox(15);
        statistics
            .getChildren()
            .addAll(
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

    private VBox createTableCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");

        Label title = new Label("ATTENDANCE");
        title.getStyleClass().add("stat-title");
        createTableColumns();
        reportTable.setPlaceholder(new Label("No students found."));

        VBox.setVgrow(reportTable, Priority.ALWAYS);
        card.getChildren().addAll(title, reportTable);

        return card;
    }

    private void createTableColumns() {
        TableColumn<Object[], String> rollColumn = new TableColumn<>("Roll No.");
        rollColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue()[0]))
        );
        TableColumn<Object[], String> nameColumn = new TableColumn<>("Student");
        nameColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue()[1]))
        );
        TableColumn<Object[], String> classColumn = new TableColumn<>("Class");
        classColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue()[2]))
        );
        TableColumn<Object[], String> sectionColumn = new TableColumn<>("Section");
        sectionColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue()[3]))
        );
        TableColumn<Object[], String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data -> {
            Object status = data.getValue()[4];

            String text = status == null ? "NOT MARKED" : status.toString();

            return new javafx.beans.property.SimpleStringProperty(text);
        });
        TableColumn<Object[], String> markedAtColumn = new TableColumn<>("Marked At");
        markedAtColumn.setCellValueFactory(data -> {
            Timestamp timestamp = (Timestamp) data.getValue()[5];
            String text =
                timestamp == null
                    ? "—"
                    : timestamp.toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            return new javafx.beans.property.SimpleStringProperty(text);
        });
        reportTable
            .getColumns()
            .addAll(
                rollColumn,
                nameColumn,
                classColumn,
                sectionColumn,
                statusColumn,
                markedAtColumn
            );
        reportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadReport() {
        LocalDate date = datePicker.getValue();
        if (date == null) {
            return;
        }
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
            List<Object[]> records = attendanceService.getAttendanceReport(
                date,
                classNumber,
                section
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
        long present = records
            .stream()
            .filter(
                row -> row[4] != null && row[4].toString().equals(AttendanceStatus.PRESENT.name())
            )
            .count();
        long late = records
            .stream()
            .filter(row -> row[4] != null && row[4].toString().equals(AttendanceStatus.LATE.name()))
            .count();
        long absent = records
            .stream()
            .filter(
                row -> row[4] != null && row[4].toString().equals(AttendanceStatus.ABSENT.name())
            )
            .count();
        totalValue.setText(String.valueOf(total));
        presentValue.setText(String.valueOf(present));
        lateValue.setText(String.valueOf(late));
        absentValue.setText(String.valueOf(absent));
    }
}
