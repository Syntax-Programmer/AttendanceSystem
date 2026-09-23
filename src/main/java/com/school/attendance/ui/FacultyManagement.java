package com.school.attendance.ui;

import com.school.attendance.model.Faculty;
import com.school.attendance.model.FacultyAssignment;
import com.school.attendance.model.Student;
import com.school.attendance.model.User;
import com.school.attendance.repository.FacultyAssignmentRepository;
import com.school.attendance.repository.FacultyRepository;
import com.school.attendance.repository.StudentRepository;
import com.school.attendance.repository.UserRepository;
import com.school.attendance.service.AuthService;
import com.school.attendance.service.FacultyAssignmentService;
import com.school.attendance.service.FacultyService;
import com.school.attendance.service.StudentService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

/**
 * Faculty Management screen.
 * Features:
 *  - Create faculty with full profile (name, DOB, phone, email, gender, qualification) + login
 *  - Reset password
 *  - Reset username
 *  - Delete faculty
 *  - Manage class assignments
 *  - View all students in a selected faculty's assigned class/section
 */
public class FacultyManagement extends BorderPane {

    private final AuthService authService;
    private final FacultyService facultyService;
    private final FacultyAssignmentService assignmentService;
    private final StudentService studentService;

    // Combined display model: User + Faculty profile data
    private record FacultyRow(User user, Faculty profile) {}

    private final TableView<FacultyRow> facultyTable = new TableView<>();
    private final ObservableList<FacultyRow> facultyList = FXCollections.observableArrayList();

    public FacultyManagement() {
        UserRepository userRepository = new UserRepository();
        FacultyRepository facultyRepository = new FacultyRepository();
        FacultyAssignmentRepository assignmentRepository = new FacultyAssignmentRepository();
        StudentRepository studentRepository = new StudentRepository();

        authService = new AuthService(userRepository);
        facultyService = new FacultyService(userRepository, facultyRepository);
        assignmentService = new FacultyAssignmentService(assignmentRepository, userRepository);
        studentService = new StudentService(studentRepository);

        setPadding(new Insets(25));

        // Title
        Label title = new Label("Faculty Management");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");
        Label subtitle = new Label("Manage faculty accounts, profiles, and class assignments.");
        VBox heading = new VBox(5, title, subtitle);

        // Action buttons
        Button addFacultyButton = new Button("+ Add Faculty");
        addFacultyButton.setOnAction(e -> showAddFacultyDialog());
        addFacultyButton.getStyleClass().add("primary-button");

        Button deleteFacultyButton = new Button("Delete Faculty");
        deleteFacultyButton.setOnAction(e -> deleteSelectedFaculty());
        deleteFacultyButton.getStyleClass().add("danger-button");

        Button resetPasswordButton = new Button("Reset Password");
        resetPasswordButton.setOnAction(e -> resetSelectedFacultyPassword());
        resetPasswordButton.getStyleClass().add("secondary-button");

        Button resetUsernameButton = new Button("Reset Username");
        resetUsernameButton.setOnAction(e -> resetSelectedFacultyUsername());
        resetUsernameButton.getStyleClass().add("secondary-button");

        Button viewStudentsButton = new Button("View Class Students");
        viewStudentsButton.setOnAction(e -> showClassStudentsDialog());
        viewStudentsButton.getStyleClass().add("secondary-button");

        HBox actions = new HBox(10,
            addFacultyButton, resetPasswordButton, resetUsernameButton,
            deleteFacultyButton, viewStudentsButton
        );
        actions.setPadding(new Insets(20, 0, 15, 0));

        // Table columns
        TableColumn<FacultyRow, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().user().getUsername())
        );
        usernameColumn.setPrefWidth(130);

        TableColumn<FacultyRow, String> nameColumn = new TableColumn<>("Full Name");
        nameColumn.setCellValueFactory(data -> {
            Faculty p = data.getValue().profile();
            return new SimpleStringProperty(p != null ? p.getName() : "—");
        });
        nameColumn.setPrefWidth(160);

        TableColumn<FacultyRow, String> phoneColumn = new TableColumn<>("Phone");
        phoneColumn.setCellValueFactory(data -> {
            Faculty p = data.getValue().profile();
            return new SimpleStringProperty(p != null && p.getPhone() != null ? p.getPhone() : "—");
        });
        phoneColumn.setPrefWidth(120);

        TableColumn<FacultyRow, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(data -> {
            Faculty p = data.getValue().profile();
            return new SimpleStringProperty(p != null && p.getEmail() != null ? p.getEmail() : "—");
        });
        emailColumn.setPrefWidth(180);

        TableColumn<FacultyRow, String> qualColumn = new TableColumn<>("Qualification");
        qualColumn.setCellValueFactory(data -> {
            Faculty p = data.getValue().profile();
            return new SimpleStringProperty(p != null && p.getQualification() != null ? p.getQualification() : "—");
        });
        qualColumn.setPrefWidth(130);

        TableColumn<FacultyRow, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button manageButton = new Button("Manage Classes");
            {
                manageButton.getStyleClass().add("secondary-button");
                manageButton.setOnAction(e -> {
                    FacultyRow row = getTableView().getItems().get(getIndex());
                    showManageClassesDialog(row.user());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : manageButton);
            }
        });

        facultyTable.getColumns().addAll(
            usernameColumn, nameColumn, phoneColumn, emailColumn, qualColumn, actionsColumn
        );
        facultyTable.setItems(facultyList);
        facultyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        setTop(new VBox(heading, actions));
        setCenter(facultyTable);

        loadFaculty();
    }

    // ==================== DATA LOADING ====================

    private void loadFaculty() {
        facultyList.clear();
        try {
            List<User> users = authService.getAllFaculty();
            for (User user : users) {
                Faculty profile = null;
                try {
                    profile = facultyService.getFacultyProfile(user.getUserId()).orElse(null);
                } catch (Exception ignored) {}
                facultyList.add(new FacultyRow(user, profile));
            }
        } catch (Exception e) {
            showError("Could not load faculty.", e.getMessage());
        }
    }

    // ==================== ADD FACULTY DIALOG ====================

    private void showAddFacultyDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Faculty Member");
        dialog.setHeaderText("Create faculty account with profile information");
        ButtonType createButton = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButton, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(520);

        // ── Login credentials ──
        Label credSection = new Label("LOGIN CREDENTIALS");
        credSection.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #7b8794;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        // ── Profile information ──
        Label profileSection = new Label("PROFILE INFORMATION");
        profileSection.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #7b8794;");
        profileSection.setPadding(new Insets(10, 0, 0, 0));

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name *");

        DatePicker dobPicker = new DatePicker();
        dobPicker.setPromptText("Date of Birth");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");

        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");

        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("Male", "Female", "Other", "Prefer not to say");
        genderBox.setPromptText("Gender");

        TextField qualField = new TextField();
        qualField.setPromptText("Qualification / Designation");

        TextArea addressArea = new TextArea();
        addressArea.setPromptText("Address");
        addressArea.setPrefRowCount(2);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #b91c1c; -fx-font-size: 12px;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.setPadding(new Insets(15));

        int row = 0;
        grid.add(credSection, 0, row++, 2, 1);
        grid.add(new Label("Username *"), 0, row);
        grid.add(usernameField, 1, row++);
        grid.add(new Label("Password *"), 0, row);
        grid.add(passwordField, 1, row++);

        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(profileSection, 0, row++, 2, 1);
        grid.add(new Label("Full Name *"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("Date of Birth"), 0, row);
        grid.add(dobPicker, 1, row++);
        grid.add(new Label("Phone"), 0, row);
        grid.add(phoneField, 1, row++);
        grid.add(new Label("Email"), 0, row);
        grid.add(emailField, 1, row++);
        grid.add(new Label("Gender"), 0, row);
        grid.add(genderBox, 1, row++);
        grid.add(new Label("Qualification"), 0, row);
        grid.add(qualField, 1, row++);
        grid.add(new Label("Address"), 0, row);
        grid.add(addressArea, 1, row++);
        grid.add(errorLabel, 0, row, 2, 1);

        ColumnConstraints col1 = new ColumnConstraints(110);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        // Make all text fields wide
        for (var node : new javafx.scene.Node[]{
                usernameField, passwordField, nameField, phoneField, emailField,
                qualField, addressArea, genderBox, dobPicker}) {
            if (node instanceof Region r) {
                r.setMaxWidth(Double.MAX_VALUE);
                GridPane.setHgrow(r, Priority.ALWAYS);
            }
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(450);
        dialog.getDialogPane().setContent(scrollPane);

        dialog.setResultConverter(button -> {
            if (button == createButton) {
                errorLabel.setText("");
                try {
                    Faculty profile = new Faculty();
                    profile.setName(nameField.getText().trim());
                    profile.setDateOfBirth(dobPicker.getValue());
                    profile.setPhone(phoneField.getText().trim());
                    profile.setEmail(emailField.getText().trim());
                    profile.setGender(genderBox.getValue());
                    profile.setQualification(qualField.getText().trim());
                    profile.setAddress(addressArea.getText().trim());

                    facultyService.createFacultyWithProfile(
                        usernameField.getText().trim(),
                        passwordField.getText(),
                        profile
                    );
                    loadFaculty();
                } catch (IllegalArgumentException e) {
                    errorLabel.setText(e.getMessage());
                    return null; // keep dialog open
                } catch (Exception e) {
                    showError("Could not create faculty.", e.getMessage());
                }
            }
            return button;
        });

        dialog.showAndWait();
    }

    // ==================== MANAGE CLASSES ====================

    private void showManageClassesDialog(User user) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Manage Classes");
        dialog.setHeaderText("Classes assigned to " + user.getUsername());
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label assignedTitle = new Label("Assigned Classes");
        assignedTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox assignmentList = new VBox(8);
        ScrollPane assignmentScroll = new ScrollPane(assignmentList);
        assignmentScroll.setFitToWidth(true);
        assignmentScroll.setPrefViewportHeight(150);
        assignmentScroll.setMaxHeight(150);
        loadAssignments(user, assignmentList);

        Label assignTitle = new Label("Assign New Class");
        assignTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ComboBox<String> classBox = new ComboBox<>();
        for (int i = 1; i <= 12; i++) classBox.getItems().add("Class " + i);
        classBox.setValue("Class 1");

        ComboBox<String> sectionBox = new ComboBox<>();
        sectionBox.getItems().addAll("A", "B", "C", "D");
        sectionBox.setValue("A");

        Button assignButton = new Button("Assign");
        assignButton.getStyleClass().add("primary-button");
        assignButton.setOnAction(e -> {
            try {
                int classNumber = classBox.getSelectionModel().getSelectedIndex() + 1;
                String section = sectionBox.getValue();
                assignmentService.assignClass(user.getUserId(), classNumber, section);
                loadAssignments(user, assignmentList);
            } catch (Exception ex) {
                showError("Could not assign class.", ex.getMessage());
            }
        });

        HBox assignBox = new HBox(10, classBox, sectionBox, assignButton);
        content.getChildren().addAll(assignedTitle, assignmentScroll, assignTitle, assignBox);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void loadAssignments(User user, VBox assignmentList) {
        assignmentList.getChildren().clear();
        try {
            List<FacultyAssignment> assignments = assignmentService.getAssignments(user.getUserId());
            if (assignments.isEmpty()) {
                assignmentList.getChildren().add(new Label("No classes assigned."));
                return;
            }
            for (FacultyAssignment assignment : assignments) {
                Label classLabel = new Label(
                    "Class " + assignment.getClassNumber() + " - " + assignment.getSection()
                );
                Button removeButton = new Button("Remove");
                removeButton.getStyleClass().add("danger-button");
                removeButton.setOnAction(e -> {
                    try {
                        assignmentService.removeAssignment(assignment.getAssignmentId());
                        loadAssignments(user, assignmentList);
                    } catch (Exception ex) {
                        showError("Could not remove assignment.", ex.getMessage());
                    }
                });
                HBox row = new HBox(15, classLabel, removeButton);
                row.setAlignment(Pos.CENTER_LEFT);
                assignmentList.getChildren().add(row);
            }
        } catch (Exception e) {
            showError("Could not load assignments.", e.getMessage());
        }
    }

    // ==================== VIEW CLASS STUDENTS ====================

    private void showClassStudentsDialog() {
        FacultyRow selected = facultyTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Faculty Selected", "Please select a faculty member first.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Class Students");
        dialog.setHeaderText("Students for faculty: " + selected.user().getUsername());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefWidth(700);
        dialog.getDialogPane().setPrefHeight(550);

        VBox content = new VBox(12);
        content.setPadding(new Insets(15));

        // Load assignments for this faculty
        List<FacultyAssignment> assignments;
        try {
            assignments = assignmentService.getAssignments(selected.user().getUserId());
        } catch (Exception e) {
            showError("Error", "Could not load assignments: " + e.getMessage());
            return;
        }

        if (assignments.isEmpty()) {
            content.getChildren().add(new Label("This faculty has no class assignments."));
            dialog.getDialogPane().setContent(content);
            dialog.showAndWait();
            return;
        }

        // Class/section selector
        ComboBox<FacultyAssignment> classSelector = new ComboBox<>();
        classSelector.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(FacultyAssignment a) {
                return a == null ? "" : "Class " + a.getClassNumber() + " - " + a.getSection();
            }
            @Override
            public FacultyAssignment fromString(String s) { return null; }
        });
        classSelector.setItems(FXCollections.observableArrayList(assignments));
        classSelector.getSelectionModel().selectFirst();

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #687786; -fx-font-size: 12px;");

        // Student table
        TableView<Student> studentTable = new TableView<>();
        studentTable.setPlaceholder(new Label("Select a class to load students."));

        TableColumn<Student, String> rollCol = new TableColumn<>("Roll No");
        rollCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getRollNo())));
        rollCol.setPrefWidth(70);

        TableColumn<Student, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        nameCol.setPrefWidth(180);

        TableColumn<Student, String> classCol = new TableColumn<>("Class");
        classCol.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getClassNumber() + " - " + d.getValue().getSection()
        ));
        classCol.setPrefWidth(80);

        TableColumn<Student, String> parentCol = new TableColumn<>("Parent");
        parentCol.setCellValueFactory(d -> {
            String name = d.getValue().getParentName();
            return new SimpleStringProperty(name != null ? name : "—");
        });
        parentCol.setPrefWidth(140);

        TableColumn<Student, String> phoneCol = new TableColumn<>("Parent Phone");
        phoneCol.setCellValueFactory(d -> {
            String phone = d.getValue().getParentPhone();
            return new SimpleStringProperty(phone != null ? phone : "—");
        });
        phoneCol.setPrefWidth(120);

        TableColumn<Student, String> emailCol = new TableColumn<>("Parent Email");
        emailCol.setCellValueFactory(d -> {
            String email = d.getValue().getParentEmail();
            return new SimpleStringProperty(email != null ? email : "—");
        });
        emailCol.setPrefWidth(160);

        studentTable.getColumns().addAll(rollCol, nameCol, classCol, parentCol, phoneCol, emailCol);
        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(studentTable, Priority.ALWAYS);

        Button loadButton = new Button("Load Students");
        loadButton.getStyleClass().add("primary-button");
        loadButton.setOnAction(e -> {
            FacultyAssignment sel = classSelector.getValue();
            if (sel == null) return;
            try {
                List<Student> students = studentService.getStudentsByClassAndSection(
                    sel.getClassNumber(), sel.getSection()
                );
                studentTable.setItems(FXCollections.observableArrayList(students));
                statusLabel.setText("Loaded " + students.size() + " student(s) for Class "
                    + sel.getClassNumber() + " - " + sel.getSection());
            } catch (Exception ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });

        HBox selectorRow = new HBox(12, new Label("Class:"), classSelector, loadButton);
        selectorRow.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(selectorRow, statusLabel, studentTable);
        dialog.getDialogPane().setContent(content);

        // Auto-load first class
        loadButton.fire();

        dialog.showAndWait();
    }

    // ==================== DELETE FACULTY ====================

    private void deleteSelectedFaculty() {
        FacultyRow selectedRow = facultyTable.getSelectionModel().getSelectedItem();
        if (selectedRow == null) {
            showError("No Faculty Selected", "Please select a faculty member first.");
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Faculty");
        confirmation.setHeaderText("Delete faculty account?");
        confirmation.setContentText("Username: " + selectedRow.user().getUsername()
            + (selectedRow.profile() != null ? "\nName: " + selectedRow.profile().getName() : ""));
        ButtonType result = confirmation.showAndWait().orElse(ButtonType.CANCEL);
        if (result != ButtonType.OK) return;
        try {
            authService.deleteUser(selectedRow.user().getUserId());
            loadFaculty();
        } catch (Exception e) {
            showError("Could not delete faculty.", e.getMessage());
        }
    }

    // ==================== RESET PASSWORD ====================

    private void resetSelectedFacultyPassword() {
        FacultyRow selectedRow = facultyTable.getSelectionModel().getSelectedItem();
        if (selectedRow == null) {
            showError("No Faculty Selected", "Please select a faculty member first.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Reset password for " + selectedRow.user().getUsername());
        dialog.setContentText("New password:");
        dialog.showAndWait().ifPresent(newPassword -> {
            try {
                authService.resetPassword(selectedRow.user().getUserId(), newPassword);
                showInfo("Password Reset", "Password reset successfully for " + selectedRow.user().getUsername() + ".");
            } catch (Exception e) {
                showError("Could not reset password.", e.getMessage());
            }
        });
    }

    // ==================== RESET USERNAME ====================

    private void resetSelectedFacultyUsername() {
        FacultyRow selectedRow = facultyTable.getSelectionModel().getSelectedItem();
        if (selectedRow == null) {
            showError("No Faculty Selected", "Please select a faculty member first.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog(selectedRow.user().getUsername());
        dialog.setTitle("Reset Username");
        dialog.setHeaderText("Reset username for "
            + (selectedRow.profile() != null ? selectedRow.profile().getName() : selectedRow.user().getUsername()));
        dialog.setContentText("New username:");
        dialog.showAndWait().ifPresent(newUsername -> {
            if (newUsername.isBlank()) {
                showError("Invalid Username", "Username cannot be blank.");
                return;
            }
            try {
                authService.resetUsername(selectedRow.user().getUserId(), newUsername.trim());
                loadFaculty();
                showInfo("Username Reset", "Username updated to '" + newUsername.trim() + "' successfully.");
            } catch (Exception e) {
                showError("Could not reset username.", e.getMessage());
            }
        });
    }

    // ==================== HELPERS ====================

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
