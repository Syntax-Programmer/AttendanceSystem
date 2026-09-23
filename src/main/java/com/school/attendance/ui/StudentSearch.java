package com.school.attendance.ui;

import com.school.attendance.model.Student;
import com.school.attendance.repository.StudentRepository;
import com.school.attendance.service.StudentService;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Student Directory screen.
 * Shows all students, supports search by roll number, view profile, and add new student.
 */
public class StudentSearch extends VBox {

    private final StudentService studentService;
    private final TableView<Student> studentTable = new TableView<>();
    private TextField searchField;

    public StudentSearch() {
        StudentRepository studentRepository = new StudentRepository();
        studentService = new StudentService(studentRepository);
        getStyleClass().add("content-area");
        setSpacing(20);
        buildUI();
        loadStudents();
    }

    private void buildUI() {
        Label title = new Label("Student Directory");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Manage and view registered students");
        subtitle.getStyleClass().add("page-subtitle");

        VBox header = new VBox(5, title, subtitle);
        VBox searchCard = createSearchCard();
        VBox tableCard = createTableCard();
        getChildren().addAll(header, searchCard, tableCard);
    }

    private VBox createSearchCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");

        Label label = new Label("SEARCH STUDENTS");
        label.getStyleClass().add("stat-title");

        searchField = new TextField();
        searchField.setPromptText("Enter roll number...");
        searchField.setPrefHeight(42);

        Button searchButton = new Button("Search");
        searchButton.getStyleClass().add("primary-button");
        searchButton.setPrefHeight(42);

        Button showAllButton = new Button("Show All");
        showAllButton.getStyleClass().add("secondary-button");
        showAllButton.setPrefHeight(42);

        Button addStudentButton = new Button("+ Add Student");
        addStudentButton.getStyleClass().add("primary-button");
        addStudentButton.setPrefHeight(42);

        searchButton.setOnAction(e -> searchStudent());
        showAllButton.setOnAction(e -> loadStudents());
        searchField.setOnAction(e -> searchStudent());
        addStudentButton.setOnAction(e -> showAddStudentDialog());

        HBox row = new HBox(10, searchField, searchButton, showAllButton, addStudentButton);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        card.getChildren().addAll(label, row);

        return card;
    }

    private VBox createTableCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");

        Label title = new Label("REGISTERED STUDENTS");
        title.getStyleClass().add("stat-title");
        createTableColumns();

        studentTable.setPlaceholder(new Label("No students found."));
        VBox.setVgrow(studentTable, Priority.ALWAYS);
        card.getChildren().addAll(title, studentTable);
        VBox.setVgrow(card, Priority.ALWAYS);

        return card;
    }

    private void createTableColumns() {
        TableColumn<Student, String> rollColumn = new TableColumn<>("Roll No.");
        rollColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getRollNo()))
        );

        TableColumn<Student, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getName())
        );

        TableColumn<Student, String> classColumn = new TableColumn<>("Class");
        classColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getClassNumber()))
        );

        TableColumn<Student, String> sectionColumn = new TableColumn<>("Section");
        sectionColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getSection())
        );

        TableColumn<Student, String> parentEmailColumn = new TableColumn<>("Parent Email");
        parentEmailColumn.setCellValueFactory(data -> {
            String email = data.getValue().getParentEmail();
            return new javafx.beans.property.SimpleStringProperty(email != null ? email : "—");
        });

        TableColumn<Student, Void> actionColumn = new TableColumn<>("Action");
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button viewButton = new Button("View Profile");
            {
                viewButton.getStyleClass().add("secondary-button");
                viewButton.setOnAction(e -> {
                    Student student = getTableView().getItems().get(getIndex());
                    openProfile(student);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewButton);
            }
        });

        rollColumn.setPrefWidth(80);
        nameColumn.setPrefWidth(200);
        classColumn.setPrefWidth(70);
        sectionColumn.setPrefWidth(70);
        parentEmailColumn.setPrefWidth(180);
        actionColumn.setPrefWidth(120);
        studentTable.getColumns().addAll(rollColumn, nameColumn, classColumn, sectionColumn, parentEmailColumn, actionColumn);
        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private void loadStudents() {
        try {
            List<Student> students = studentService.getAllStudents();
            studentTable.setItems(FXCollections.observableArrayList(students));
            searchField.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void searchStudent() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            loadStudents();
            return;
        }
        try {
            int rollNo = Integer.parseInt(text);
            Optional<Student> result = studentService.findStudent(rollNo);
            if (result.isPresent()) {
                studentTable.setItems(FXCollections.observableArrayList(result.get()));
            } else {
                studentTable.setItems(FXCollections.observableArrayList());
            }
        } catch (NumberFormatException e) {
            studentTable.setItems(FXCollections.observableArrayList());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAddStudentDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Student");
        dialog.setHeaderText("Register a new student");
        ButtonType saveButton = new ButtonType("Add Student", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(460);

        TextField rollField = new TextField();
        rollField.setPromptText("Roll Number *");

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name *");

        Spinner<Integer> classSpinner = new Spinner<>(1, 12, 1);
        classSpinner.setEditable(true);

        ComboBox<String> sectionBox = new ComboBox<>();
        sectionBox.getItems().addAll("A", "B", "C", "D", "E", "F");
        sectionBox.setValue("A");

        DatePicker dobPicker = new DatePicker();

        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("Male", "Female", "Other", "Prefer not to say");

        TextField parentNameField = new TextField();
        parentNameField.setPromptText("Parent Name");

        TextField parentPhoneField = new TextField();
        parentPhoneField.setPromptText("Parent Phone");

        TextField parentEmailField = new TextField();
        parentEmailField.setPromptText("Parent Email");

        TextArea addressArea = new TextArea();
        addressArea.setPromptText("Address");
        addressArea.setPrefRowCount(2);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #b91c1c; -fx-font-size: 12px;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.setPadding(new Insets(15));

        int r = 0;
        grid.add(new Label("Roll Number *"), 0, r); grid.add(rollField, 1, r++);
        grid.add(new Label("Full Name *"), 0, r); grid.add(nameField, 1, r++);
        grid.add(new Label("Class *"), 0, r); grid.add(classSpinner, 1, r++);
        grid.add(new Label("Section *"), 0, r); grid.add(sectionBox, 1, r++);
        grid.add(new Label("Date of Birth"), 0, r); grid.add(dobPicker, 1, r++);
        grid.add(new Label("Gender"), 0, r); grid.add(genderBox, 1, r++);
        grid.add(new Label("Parent Name"), 0, r); grid.add(parentNameField, 1, r++);
        grid.add(new Label("Parent Phone"), 0, r); grid.add(parentPhoneField, 1, r++);
        grid.add(new Label("Parent Email"), 0, r); grid.add(parentEmailField, 1, r++);
        grid.add(new Label("Address"), 0, r); grid.add(addressArea, 1, r++);
        grid.add(errorLabel, 0, r, 2, 1);

        ColumnConstraints col1 = new ColumnConstraints(120);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        for (var node : new javafx.scene.Node[]{
                rollField, nameField, parentNameField, parentPhoneField, parentEmailField,
                addressArea, sectionBox, genderBox, dobPicker, classSpinner}) {
            if (node instanceof Region reg) {
                reg.setMaxWidth(Double.MAX_VALUE);
                GridPane.setHgrow(reg, Priority.ALWAYS);
            }
        }

        dialog.getDialogPane().setContent(new ScrollPane(grid) {{
            setFitToWidth(true);
            setPrefViewportHeight(420);
        }});

        dialog.setResultConverter(button -> {
            if (button == saveButton) {
                errorLabel.setText("");
                try {
                    int rollNo = Integer.parseInt(rollField.getText().trim());
                    Student student = new Student();
                    student.setRollNo(rollNo);
                    student.setName(nameField.getText().trim());
                    student.setClassNumber(classSpinner.getValue());
                    student.setSection(sectionBox.getValue());
                    student.setDateOfBirth(dobPicker.getValue());
                    student.setGender(genderBox.getValue());
                    student.setParentName(parentNameField.getText().trim());
                    student.setParentPhone(parentPhoneField.getText().trim());
                    student.setParentEmail(parentEmailField.getText().trim());
                    student.setAddress(addressArea.getText().trim());
                    studentService.addStudent(student);
                    loadStudents();
                } catch (NumberFormatException e) {
                    errorLabel.setText("Roll number must be a valid integer.");
                    return null;
                } catch (IllegalArgumentException e) {
                    errorLabel.setText(e.getMessage());
                    return null;
                } catch (Exception e) {
                    errorLabel.setText("Error: " + e.getMessage());
                    return null;
                }
            }
            return button;
        });

        dialog.showAndWait();
    }

    private void openProfile(Student student) {
        getChildren().clear();
        getChildren().add(new StudentProfile(student));
    }
}
