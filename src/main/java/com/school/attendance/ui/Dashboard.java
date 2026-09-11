package com.school.attendance.ui;

import com.school.attendance.repository.AttendanceRepository;
import com.school.attendance.repository.StudentRepository;
import com.school.attendance.service.AttendanceService;
import com.school.attendance.service.StudentService;
import java.time.LocalDate;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Dashboard extends BorderPane {

    private final StudentService studentService;
    private final AttendanceService attendanceService;

    private Button dashboardButton;
    private Button attendanceButton;
    private Button studentsButton;
    private Button reportsButton;
    private Button settingsButton;

    public Dashboard() {
        StudentRepository studentRepository = new StudentRepository();
        AttendanceRepository attendanceRepository = new AttendanceRepository();
        studentService = new StudentService(studentRepository);
        attendanceService = new AttendanceService(studentRepository, attendanceRepository);

        setLeft(createSidebar());
        setCenter(createDashboardContent());
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(12);

        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);

        // Brand
        Label brand = new Label("ATTENDANCE");
        brand.getStyleClass().add("brand");
        Label subtitle = new Label("SCHOOL MANAGEMENT");
        subtitle.getStyleClass().add("brand-subtitle");
        VBox brandBox = new VBox(3, brand, subtitle);

        // Navigation buttons
        dashboardButton = createNavButton("Dashboard");
        attendanceButton = createNavButton("Attendance");
        studentsButton = createNavButton("Students");
        reportsButton = createNavButton("Reports");
        settingsButton = createNavButton("Settings");

        // Dashboard is active initially
        setActiveButton(dashboardButton);

        dashboardButton.setOnAction(event -> {
            setCenter(createDashboardContent());
            setActiveButton(dashboardButton);
        });
        attendanceButton.setOnAction(event -> {
            setCenter(new AttendanceScreen());
            setActiveButton(attendanceButton);
        });
        studentsButton.setOnAction(event -> {
            setCenter(new StudentSearch());
            setActiveButton(studentsButton);
        });
        reportsButton.setOnAction(event -> {
            setCenter(new Reports());
            setActiveButton(reportsButton);
        });
        settingsButton.setOnAction(event -> {
            showPlaceholder("Settings");
            setActiveButton(settingsButton);
        });
        // Navigation container
        VBox navigation = new VBox(
            6,
            dashboardButton,
            attendanceButton,
            studentsButton,
            reportsButton
        );
        // Push Settings to bottom
        VBox spacer = new VBox();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        sidebar.getChildren().addAll(brandBox, navigation, spacer, settingsButton);

        return sidebar;
    }

    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);

        return button;
    }

    private void setActiveButton(Button activeButton) {
        dashboardButton.getStyleClass().remove("active");
        attendanceButton.getStyleClass().remove("active");
        studentsButton.getStyleClass().remove("active");
        reportsButton.getStyleClass().remove("active");
        settingsButton.getStyleClass().remove("active");

        activeButton.getStyleClass().add("active");
    }

    private VBox createDashboardContent() {
        VBox content = new VBox(25);
        content.getStyleClass().add("content-area");
        HBox header = createHeader();
        HBox statistics = createStatistics();
        HBox lowerSection = createLowerSection();
        content.getChildren().addAll(header, statistics, lowerSection);

        return content;
    }

    private HBox createHeader() {
        Label title = new Label("Dashboard");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Overview of today's attendance");
        subtitle.getStyleClass().add("page-subtitle");

        VBox text = new VBox(5, title, subtitle);
        HBox header = new HBox(text);
        header.setAlignment(Pos.CENTER_LEFT);

        return header;
    }

    private HBox createStatistics() {
        HBox statistics = new HBox(15);
        LocalDate today = LocalDate.now();

        try {
            long totalStudents = studentService.getStudentCount();
            long present = attendanceService.getPresentCount(today);
            long late = attendanceService.getLateCount(today);
            long absent = attendanceService.getAbsentCount(today);
            double attendancePercentage = 0;
            if (totalStudents > 0) {
                attendancePercentage = ((double) present / totalStudents) * 100;
            }

            statistics
                .getChildren()
                .addAll(
                    createStatCard(
                        "TOTAL STUDENTS",
                        String.valueOf(totalStudents),
                        "Registered students"
                    ),
                    createStatCard(
                        "PRESENT TODAY",
                        String.valueOf(present),
                        String.format("%.1f%% attendance", attendancePercentage)
                    ),
                    createStatCard("LATE", String.valueOf(late), "Students marked late"),
                    createStatCard("ABSENT", String.valueOf(absent), "Students absent today")
                );
        } catch (Exception e) {
            e.printStackTrace();
            statistics
                .getChildren()
                .addAll(
                    createStatCard("TOTAL STUDENTS", "—", "Unable to load"),
                    createStatCard("PRESENT TODAY", "—", "Unable to load"),
                    createStatCard("LATE", "—", "Unable to load"),
                    createStatCard("ABSENT", "—", "Unable to load")
                );
        }

        return statistics;
    }

    private VBox createStatCard(String title, String value, String description) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");

        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("stat-description");

        VBox card = new VBox(10, titleLabel, valueLabel, descriptionLabel);
        card.getStyleClass().add("stat-card");

        HBox.setHgrow(card, javafx.scene.layout.Priority.ALWAYS);

        return card;
    }

    private HBox createLowerSection() {
        VBox overview = new VBox(15);
        overview.getStyleClass().add("card");

        Label overviewTitle = new Label("Today's Attendance");
        overviewTitle.getStyleClass().add("page-title");
        Label overviewText = new Label("Attendance overview will appear here.");
        overviewText.getStyleClass().add("page-subtitle");
        overview.getChildren().addAll(overviewTitle, overviewText);

        VBox quickActions = new VBox(15);
        quickActions.getStyleClass().add("card");
        quickActions.setPrefWidth(280);

        Label actionsTitle = new Label("Quick Actions");
        actionsTitle.getStyleClass().add("page-title");

        Button markAttendance = new Button("Mark Attendance");
        markAttendance.getStyleClass().add("primary-button");
        markAttendance.setMaxWidth(Double.MAX_VALUE);

        Button findStudent = new Button("Find Student");
        findStudent.getStyleClass().add("secondary-button");
        findStudent.setMaxWidth(Double.MAX_VALUE);

        Button reports = new Button("View Reports");
        reports.getStyleClass().add("secondary-button");
        reports.setMaxWidth(Double.MAX_VALUE);

        // Quick action navigation
        markAttendance.setOnAction(event -> {
            setCenter(new AttendanceScreen());
            setActiveButton(attendanceButton);
        });
        findStudent.setOnAction(event -> {
            setCenter(new StudentSearch());
            setActiveButton(studentsButton);
        });
        reports.setOnAction(event -> {
            showPlaceholder("Attendance Reports");
            setActiveButton(reportsButton);
        });
        quickActions.getChildren().addAll(actionsTitle, markAttendance, findStudent, reports);

        HBox section = new HBox(20, overview, quickActions);
        HBox.setHgrow(overview, javafx.scene.layout.Priority.ALWAYS);

        return section;
    }

    private void showPlaceholder(String titleText) {
        VBox content = new VBox();
        content.getStyleClass().add("content-area");

        Label title = new Label(titleText);
        title.getStyleClass().add("page-title");

        content.getChildren().add(title);
        setCenter(content);
    }
}
