package com.school.attendance.ui;

import com.school.attendance.model.FacultyAssignment;
import com.school.attendance.model.Student;
import com.school.attendance.model.User;
import com.school.attendance.repository.StudentRepository;
import com.school.attendance.service.StudentService;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class FacultyClassScreen extends BorderPane {

    private final User user;
    private final FacultyAssignment assignment;
    private final StudentService studentService;

    private final TableView<Student> studentTable = new TableView<>();

    public FacultyClassScreen(User user, FacultyAssignment assignment) {
        this.user = user;
        this.assignment = assignment;
        this.studentService = new StudentService(new StudentRepository());
        setPadding(new Insets(25));

        setTop(createHeader());
        setCenter(createStudentTable());

        loadStudents();
    }

    private TableView<Student> createStudentTable() {
        TableColumn<Student, Integer> rollColumn = new TableColumn<>("Roll No");
        rollColumn.setCellValueFactory(new PropertyValueFactory<>("rollNo"));
        TableColumn<Student, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Student, String> sectionColumn = new TableColumn<>("Section");
        sectionColumn.setCellValueFactory(new PropertyValueFactory<>("section"));
        TableColumn<Student, String> genderColumn = new TableColumn<>("Gender");
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        studentTable.getColumns().addAll(rollColumn, nameColumn, sectionColumn, genderColumn);
        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        return studentTable;
    }

    private void loadStudents() {
        try {
            List<Student> students = studentService.getStudentsByClassAndSection(
                assignment.getClassNumber(),
                assignment.getSection()
            );
            studentTable.setItems(FXCollections.observableArrayList(students));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox createHeader() {
        Button backButton = new Button("← Back");
        backButton.getStyleClass().add("secondary-button");
        backButton.setOnAction(event -> goBack());

        Label title = new Label(
            "Class " + assignment.getClassNumber() + " - " + assignment.getSection()
        );
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Students and attendance");
        subtitle.getStyleClass().add("page-subtitle");

        VBox text = new VBox(5, title, subtitle);
        HBox header = new HBox(20, backButton, text);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));

        return header;
    }

    private void goBack() {
        FacultyDashboard dashboard = new FacultyDashboard(this.user);
        javafx.scene.Scene scene = new javafx.scene.Scene(dashboard, 1200, 750);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        javafx.stage.Stage stage = (javafx.stage.Stage) getScene().getWindow();
        stage.setTitle("Faculty - School Attendance System");
        stage.setScene(scene);
    }
}
