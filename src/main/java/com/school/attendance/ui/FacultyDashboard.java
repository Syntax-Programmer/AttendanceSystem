package com.school.attendance.ui;

import com.school.attendance.model.FacultyAssignment;
import com.school.attendance.model.User;
import com.school.attendance.repository.FacultyAssignmentRepository;
import com.school.attendance.repository.UserRepository;
import com.school.attendance.service.FacultyAssignmentService;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FacultyDashboard extends BorderPane {

    private final User user;
    private final FacultyAssignmentService assignmentService;

    private final FlowPane classContainer = new FlowPane();

    public FacultyDashboard(User user) {
        this.user = user;

        FacultyAssignmentRepository assignmentRepository = new FacultyAssignmentRepository();
        UserRepository userRepository = new UserRepository();
        assignmentService = new FacultyAssignmentService(assignmentRepository, userRepository);
        setLeft(createSidebar());
        setCenter(createDashboardContent());

        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                String css = getClass().getResource("/css/app.css").toExternalForm();
                if (!newScene.getStylesheets().contains(css)) {
                    newScene.getStylesheets().add(css);
                }
            }
        });

        loadClasses();
    }

    private void setActiveButton(Button activeButton, Button... buttons) {
        for (Button button : buttons) {
            button.getStyleClass().remove("active");
        }

        if (!activeButton.getStyleClass().contains("active")) {
            activeButton.getStyleClass().add("active");
        }
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(12);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);
        sidebar.setPadding(new Insets(20));

        Label brand = new Label("ATTENDANCE");
        brand.getStyleClass().add("brand");

        Label subtitle = new Label("FACULTY PORTAL");
        subtitle.getStyleClass().add("brand-subtitle");

        VBox brandBox = new VBox(3, brand, subtitle);
        Button dashboardButton = createNavButton("Dashboard");
        Button attendanceButton = createNavButton("Attendance");
        Button studentsButton = createNavButton("Students");
        Button reportsButton = createNavButton("Reports");
        Button[] navigationButtons = {
            dashboardButton,
            attendanceButton,
            studentsButton,
            reportsButton,
        };
        dashboardButton.getStyleClass().add("active");
        dashboardButton.setOnAction(event -> {
            setActiveButton(dashboardButton, navigationButtons);
            setCenter(createDashboardContent());
        });

        attendanceButton.setOnAction(event -> {
            setActiveButton(attendanceButton, navigationButtons);
            Label label = new Label("Attendance");
            label.getStyleClass().add("page-title");
            setCenter(label);
        });
        studentsButton.setOnAction(event -> {
            setActiveButton(studentsButton, navigationButtons);
            Label label = new Label("Students");
            label.getStyleClass().add("page-title");
            setCenter(label);
        });
        reportsButton.setOnAction(event -> {
            setActiveButton(reportsButton, navigationButtons);
            Label label = new Label("Reports");
            label.getStyleClass().add("page-title");
            setCenter(label);
        });

        VBox navigation = new VBox(
            6,
            dashboardButton,
            attendanceButton,
            studentsButton,
            reportsButton
        );
        VBox spacer = new VBox();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button logoutButton = createNavButton("Logout");
        logoutButton.getStyleClass().add("logout-button");
        logoutButton.setOnAction(event -> logout());
        sidebar.getChildren().addAll(brandBox, navigation, spacer, logoutButton);

        return sidebar;
    }

    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);

        return button;
    }

    private VBox createDashboardContent() {
        VBox content = new VBox(25);
        content.getStyleClass().add("content-area");

        Label title = new Label("Faculty Dashboard");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Welcome, " + user.getUsername());
        subtitle.getStyleClass().add("page-subtitle");
        VBox heading = new VBox(5, title, subtitle);

        Label classesTitle = new Label("My Classes");
        classesTitle.getStyleClass().add("page-title");
        classContainer.setHgap(15);
        classContainer.setVgap(15);

        ScrollPane scrollPane = new ScrollPane(classContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        VBox classesSection = new VBox(15, classesTitle, scrollPane);
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);
        content.getChildren().addAll(heading, classesSection);
        VBox.setVgrow(classesSection, javafx.scene.layout.Priority.ALWAYS);

        return content;
    }

    private void loadClasses() {
        classContainer.getChildren().clear();
        try {
            List<FacultyAssignment> assignments = assignmentService.getAssignments(
                user.getUserId()
            );
            if (assignments.isEmpty()) {
                Label emptyLabel = new Label("No classes have been assigned to you.");
                emptyLabel.getStyleClass().add("page-subtitle");
                classContainer.getChildren().add(emptyLabel);
                return;
            }
            for (FacultyAssignment assignment : assignments) {
                classContainer.getChildren().add(createClassCard(assignment));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Label errorLabel = new Label("Unable to load your classes.");
            classContainer.getChildren().add(errorLabel);
        }
    }

    private VBox createClassCard(FacultyAssignment assignment) {
        Label classLabel = new Label(
            "Class " + assignment.getClassNumber() + " - " + assignment.getSection()
        );
        classLabel.getStyleClass().add("page-title");

        Label description = new Label("View students and attendance");
        description.getStyleClass().add("page-subtitle");

        Button openButton = new Button("Open Class");
        openButton.getStyleClass().add("primary-button");
        openButton.setOnAction(event -> openClass(assignment));

        VBox card = new VBox(12, classLabel, description, openButton);
        card.getStyleClass().add("card");
        card.setPrefWidth(250);
        card.setPadding(new Insets(20));

        return card;
    }

    private void openClass(FacultyAssignment assignment) {
        System.out.println(
            "Opening Class " + assignment.getClassNumber() + "-" + assignment.getSection()
        );
    }

    private void logout() {
        LoginScreen loginScreen = new LoginScreen();
        Scene scene = new Scene(loginScreen, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());

        Stage stage = (Stage) getScene().getWindow();
        stage.setTitle("School Attendance System");
        stage.setScene(scene);
    }

    public User getUser() {
        return user;
    }
}
