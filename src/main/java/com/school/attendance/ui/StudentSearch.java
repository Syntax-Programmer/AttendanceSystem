package com.school.attendance.ui;

import com.school.attendance.model.Student;
import com.school.attendance.repository.StudentRepository;
import com.school.attendance.service.StudentService;
import java.util.Optional;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class StudentSearch extends VBox {

    private final StudentService studentService;
    private TextField rollNumberField;
    private VBox resultCard;

    public StudentSearch() {
        StudentRepository studentRepository = new StudentRepository();
        studentService = new StudentService(studentRepository);
        getStyleClass().add("content-area");
        setSpacing(25);
        buildUI();
    }

    private void buildUI() {
        Label title = new Label("Student Directory");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Search for a student using their roll number");
        subtitle.getStyleClass().add("page-subtitle");

        VBox header = new VBox(5, title, subtitle);
        VBox searchCard = createSearchCard();
        resultCard = new VBox();
        resultCard.getStyleClass().add("card");
        resultCard.setVisible(false);
        resultCard.setManaged(false);

        getChildren().addAll(header, searchCard, resultCard);
    }

    private VBox createSearchCard() {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");

        Label label = new Label("SEARCH STUDENT");
        label.getStyleClass().add("stat-title");
        rollNumberField = new TextField();
        rollNumberField.setPromptText("Enter roll number...");
        rollNumberField.setPrefHeight(45);

        Button searchButton = new Button("Search");
        searchButton.getStyleClass().add("primary-button");
        searchButton.setPrefHeight(45);
        searchButton.setOnAction(event -> searchStudent());
        rollNumberField.setOnAction(event -> searchStudent());

        HBox searchRow = new HBox(10, rollNumberField, searchButton);
        HBox.setHgrow(rollNumberField, javafx.scene.layout.Priority.ALWAYS);
        card.getChildren().addAll(label, searchRow);

        return card;
    }

    private void searchStudent() {
        String text = rollNumberField.getText().trim();

        if (text.isEmpty()) {
            showMessage("Enter a roll number.");
            return;
        }
        try {
            int rollNo = Integer.parseInt(text);
            Optional<Student> result = studentService.findStudent(rollNo);
            if (result.isEmpty()) {
                showMessage("No student found with roll number " + rollNo);
                return;
            }
            displayStudent(result.get());
        } catch (NumberFormatException e) {
            showMessage("Please enter a valid roll number.");
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Unable to search for student.");
        }
    }

    private void displayStudent(Student student) {
        resultCard.getChildren().clear();

        Label heading = new Label("STUDENT");
        heading.getStyleClass().add("stat-title");

        Label name = new Label(student.getName());
        name.getStyleClass().add("page-title");

        Label classInfo = new Label(
            "Class " + student.getClassNumber() + " • Section " + student.getSection()
        );
        classInfo.getStyleClass().add("page-subtitle");

        Label roll = new Label("Roll No: " + student.getRollNo());
        roll.getStyleClass().add("page-subtitle");

        Button profileButton = new Button("View Profile");
        profileButton.getStyleClass().add("primary-button");
        profileButton.setOnAction(event -> openProfile(student));

        HBox bottom = new HBox(profileButton);
        bottom.setAlignment(Pos.CENTER_LEFT);

        resultCard.getChildren().addAll(heading, name, classInfo, roll, bottom);
        resultCard.setVisible(true);
        resultCard.setManaged(true);
    }

    private void openProfile(Student student) {
        getChildren().clear();
        getChildren().add(new StudentProfile(student));
    }

    private void showMessage(String message) {
        resultCard.getChildren().clear();
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("page-subtitle");
        resultCard.getChildren().add(messageLabel);
        resultCard.setVisible(true);
        resultCard.setManaged(true);
    }
}
