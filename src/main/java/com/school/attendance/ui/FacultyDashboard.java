package com.school.attendance.ui;

import com.school.attendance.model.User;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FacultyDashboard extends BorderPane {

    private final User user;

    public FacultyDashboard(User user) {
        this.user = user;

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        Label title = new Label("Faculty Dashboard");
        title.setStyle(
            """
            -fx-font-size: 28px;
            -fx-font-weight: bold;
            """
        );

        Label welcome = new Label("Welcome, " + user.getUsername());
        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("logout-button");
        logoutButton.setOnAction(event -> logout());
        content.getChildren().addAll(title, welcome, logoutButton);
        setCenter(content);
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
