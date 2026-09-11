package com.school.attendance.ui;

import com.school.attendance.model.Student;
import com.school.attendance.repository.StudentRepository;
import com.school.attendance.service.StudentService;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
        searchButton.setOnAction(event -> searchStudent());
        showAllButton.setOnAction(event -> loadStudents());
        searchField.setOnAction(event -> searchStudent());

        HBox row = new HBox(10, searchField, searchButton, showAllButton);
        HBox.setHgrow(searchField, javafx.scene.layout.Priority.ALWAYS);
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

        VBox.setVgrow(studentTable, javafx.scene.layout.Priority.ALWAYS);
        card.getChildren().addAll(title, studentTable);
        VBox.setVgrow(card, javafx.scene.layout.Priority.ALWAYS);

        return card;
    }

    private void createTableColumns() {
        TableColumn<Student, String> rollColumn = new TableColumn<>("Roll No.");
        rollColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                String.valueOf(data.getValue().getRollNo())
            )
        );

        TableColumn<Student, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getName())
        );

        TableColumn<Student, String> classColumn = new TableColumn<>("Class");
        classColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                String.valueOf(data.getValue().getClassNumber())
            )
        );

        TableColumn<Student, String> sectionColumn = new TableColumn<>("Section");
        sectionColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getSection())
        );
        TableColumn<Student, Void> actionColumn = new TableColumn<>("Action");

        actionColumn.setCellFactory(column ->
            new TableCell<>() {
                private final Button viewButton = new Button("View Profile");

                {
                    viewButton.getStyleClass().add("secondary-button");
                    viewButton.setOnAction(event -> {
                        Student student = getTableView().getItems().get(getIndex());
                        openProfile(student);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(viewButton);
                    }
                }
            }
        );
        rollColumn.setPrefWidth(100);
        nameColumn.setPrefWidth(250);
        classColumn.setPrefWidth(100);
        sectionColumn.setPrefWidth(100);
        actionColumn.setPrefWidth(150);
        studentTable
            .getColumns()
            .addAll(rollColumn, nameColumn, classColumn, sectionColumn, actionColumn);
        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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

    private void openProfile(Student student) {
        getChildren().clear();
        getChildren().add(new StudentProfile(student));
    }
}
