package com.school.attendance.ui;

import com.school.attendance.model.User;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class FacultyDashboard extends BorderPane {

    private final User user;

    public FacultyDashboard(User user) {
        this.user = user;
        setPadding(new Insets(30));
        Label title = new Label("Faculty Dashboard\nWelcome, " + user.getUsername());
        title.setStyle(
            """
            -fx-font-size: 28px;
            -fx-font-weight: bold;
            """
        );
        setCenter(title);
    }

    public User getUser() {
        return user;
    }
}
