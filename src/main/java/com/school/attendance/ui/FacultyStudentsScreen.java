package com.school.attendance.ui;

import com.school.attendance.model.FacultyAssignment;
import com.school.attendance.model.Student;
import com.school.attendance.model.User;
import com.school.attendance.repository.FacultyAssignmentRepository;
import com.school.attendance.repository.StudentRepository;
import com.school.attendance.repository.UserRepository;
import com.school.attendance.service.FacultyAssignmentService;
import com.school.attendance.service.StudentService;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

/**
 * Shows all students belonging to the faculty member's assigned classes.
 * Only queries students from assigned classes — no client-side filtering.
 */
public class FacultyStudentsScreen extends VBox {

    private final User user;
    private final FacultyAssignmentService assignmentService;
    private final StudentService studentService;
    private final TableView<Student> studentTable = new TableView<>();

    public FacultyStudentsScreen(User user) {
        this.user = user;
        FacultyAssignmentRepository assignmentRepository = new FacultyAssignmentRepository();
        UserRepository userRepository = new UserRepository();
        this.assignmentService = new FacultyAssignmentService(assignmentRepository, userRepository);
        this.studentService = new StudentService(new StudentRepository());

        getStyleClass().add("content-area");
        setSpacing(20);
        setPadding(new Insets(30));

        Label title = new Label("My Students");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Students from your assigned classes");
        subtitle.getStyleClass().add("page-subtitle");

        VBox header = new VBox(5, title, subtitle);

        buildTable();
        VBox tableCard = new VBox(12);
        tableCard.getStyleClass().add("card");
        Label tableTitle = new Label("STUDENT LIST");
        tableTitle.getStyleClass().add("stat-title");
        tableCard.getChildren().addAll(tableTitle, studentTable);
        VBox.setVgrow(studentTable, javafx.scene.layout.Priority.ALWAYS);
        VBox.setVgrow(tableCard, javafx.scene.layout.Priority.ALWAYS);

        getChildren().addAll(header, tableCard);

        loadStudents();
    }

    private void buildTable() {
        TableColumn<Student, Integer> rollColumn = new TableColumn<>("Roll No");
        rollColumn.setCellValueFactory(new PropertyValueFactory<>("rollNo"));

        TableColumn<Student, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Student, Integer> classColumn = new TableColumn<>("Class");
        classColumn.setCellValueFactory(new PropertyValueFactory<>("classNumber"));

        TableColumn<Student, String> sectionColumn = new TableColumn<>("Section");
        sectionColumn.setCellValueFactory(new PropertyValueFactory<>("section"));

        TableColumn<Student, String> genderColumn = new TableColumn<>("Gender");
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));

        studentTable.getColumns().addAll(rollColumn, nameColumn, classColumn, sectionColumn, genderColumn);
        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        studentTable.setPlaceholder(new Label("No students found."));
    }

    private void loadStudents() {
        try {
            List<FacultyAssignment> assignments = assignmentService.getAssignments(user.getUserId());
            List<Student> students = studentService.getStudentsByAssignedClasses(assignments);
            studentTable.setItems(FXCollections.observableArrayList(students));
        } catch (Exception e) {
            e.printStackTrace();
            studentTable.setPlaceholder(new Label("Error loading students: " + e.getMessage()));
        }
    }
}
