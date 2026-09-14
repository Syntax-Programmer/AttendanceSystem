package com.school.attendance;

import com.school.attendance.ui.LoginScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        LoginScreen loginScreen = new LoginScreen();
        Scene scene = new Scene(loginScreen, 900, 600);
        stage.setTitle("School Attendance System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
