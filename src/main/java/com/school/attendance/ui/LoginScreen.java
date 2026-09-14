package com.school.attendance.ui;

import com.school.attendance.model.User;
import com.school.attendance.repository.UserRepository;
import com.school.attendance.service.AuthService;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginScreen extends VBox {

    private final AuthService authService;

    private final TextField usernameField;
    private final PasswordField passwordField;
    private final Label messageLabel;

    public LoginScreen() {
        UserRepository userRepository = new UserRepository();
        authService = new AuthService(userRepository);

        setSpacing(15);
        setPadding(new Insets(40));
        setAlignment(Pos.CENTER);
        Label title = new Label("School Attendance System");
        title.setStyle(
            """
            -fx-font-size: 28px;
            -fx-font-weight: bold;
            """
        );

        Label subtitle = new Label("Sign in to continue");
        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(300);
        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);

        Button loginButton = new Button("Login");
        loginButton.setMaxWidth(300);
        loginButton.getStyleClass().add("primary-button");
        messageLabel = new Label();
        loginButton.setOnAction(event -> handleLogin());
        passwordField.setOnAction(event -> handleLogin());
        getChildren().addAll(
            title,
            subtitle,
            usernameField,
            passwordField,
            loginButton,
            messageLabel
        );
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            Optional<User> result = authService.login(username, password);
            if (result.isPresent()) {
                User user = result.get();
                Stage stage = (Stage) getScene().getWindow();
                if (user.getRole().equals("MANAGEMENT")) {
                    ManagementDashboard dashboard = new ManagementDashboard(user);

                    Scene scene = new Scene(dashboard, 1200, 750);
                    scene
                        .getStylesheets()
                        .add(getClass().getResource("/css/app.css").toExternalForm());

                    stage.setTitle("Management - School Attendance System");
                    stage.setScene(scene);
                } else if (user.getRole().equals("FACULTY")) {
                    FacultyDashboard dashboard = new FacultyDashboard(user);
                    Scene scene = new Scene(dashboard, 1200, 750);
                    stage.setTitle("Faculty - School Attendance System");
                    stage.setScene(scene);
                }
            } else {
                messageLabel.setText("Invalid username or password.");
            }
        } catch (Exception e) {
            messageLabel.setText("Login error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
