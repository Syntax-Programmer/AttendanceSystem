package com.school.attendance.ui;

import com.school.attendance.model.User;
import javafx.scene.layout.BorderPane;

public class ManagementDashboard extends BorderPane {

    private final User user;

    public ManagementDashboard(User user) {
        this.user = user;
        Dashboard dashboard = new Dashboard();
        setCenter(dashboard);
    }

    public User getUser() {
        return user;
    }
}
