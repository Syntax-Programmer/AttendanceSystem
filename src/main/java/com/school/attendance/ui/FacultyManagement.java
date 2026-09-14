package com.school.attendance.ui;

import com.school.attendance.model.User;
import com.school.attendance.repository.UserRepository;
import com.school.attendance.service.AuthService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class FacultyManagement extends BorderPane {

    private final AuthService authService;

    private final TableView<User> facultyTable = new TableView<>();
    private final ObservableList<User> facultyList = FXCollections.observableArrayList();

    public FacultyManagement() {
        UserRepository userRepository = new UserRepository();
        authService = new AuthService(userRepository);
        setPadding(new Insets(25));
        // Title
        Label title = new Label("Faculty Management");
        title.setStyle(
            """
            -fx-font-size: 26px;
            -fx-font-weight: bold;
            """
        );

        Label subtitle = new Label("Manage faculty accounts and login access.");
        VBox heading = new VBox(5, title, subtitle);

        // Add faculty button
        Button addFacultyButton = new Button("+ Add Faculty");
        addFacultyButton.setOnAction(event -> showAddFacultyDialog());

        HBox actions = new HBox(addFacultyButton);
        actions.setPadding(new Insets(20, 0, 15, 0));

        // Table columns
        TableColumn<User, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getUsername())
        );

        TableColumn<User, String> roleColumn = new TableColumn<>("Role");
        roleColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getRole())
        );
        facultyTable.getColumns().addAll(usernameColumn, roleColumn);
        facultyTable.setItems(facultyList);

        setTop(new VBox(heading, actions));
        setCenter(facultyTable);

        loadFaculty();
    }

    private void loadFaculty() {
        try {
            facultyList.setAll(authService.getAllFaculty());
        } catch (Exception e) {
            showError("Could not load faculty.", e.getMessage());
        }
    }

    private void showAddFacultyDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Faculty");
        ButtonType createButton = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButton, ButtonType.CANCEL);
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        VBox content = new VBox(
            12,
            new Label("Username"),
            usernameField,
            new Label("Password"),
            passwordField
        );
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(button -> {
            if (button == createButton) {
                try {
                    authService.createUser(
                        usernameField.getText(),
                        passwordField.getText(),
                        "FACULTY"
                    );
                    loadFaculty();
                } catch (Exception e) {
                    showError("Could not create faculty.", e.getMessage());
                }
            }
            return button;
        });

        dialog.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
