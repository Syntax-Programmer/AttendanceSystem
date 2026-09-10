package com.school.attendance.repository;

import com.school.attendance.database.DatabaseConnection;
import com.school.attendance.model.Student;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class StudentRepository {

    public void save(Student student) throws SQLException {
        String sql = """
        INSERT INTO students (
            roll_no, class_number, section, name, date_of_birth, gender,
            parent_name, parent_phone, address
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, student.getRollNo());
            statement.setInt(2, student.getClassNumber());
            statement.setString(3, student.getSection());
            statement.setString(4, student.getName());
            statement.setObject(5, student.getDateOfBirth());
            statement.setString(6, student.getGender());
            statement.setString(7, student.getParentName());
            statement.setString(8, student.getParentPhone());
            statement.setString(9, student.getAddress());

            statement.executeUpdate();
        }
    }

    public Optional<Student> findByRollNo(int rollNo) throws SQLException {
        String sql = """
        SELECT
            roll_no, class_number, section, name, date_of_birth, gender, 
            parent_name, parent_phone, address
        FROM students
        WHERE roll_no = ?
        """;

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, rollNo);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    Student student = new Student();

                    student.setRollNo(result.getInt("roll_no"));
                    student.setClassNumber(result.getInt("class_number"));
                    student.setSection(result.getString("section"));
                    student.setName(result.getString("name"));
                    student.setDateOfBirth(
                        result.getObject("date_of_birth", java.time.LocalDate.class)
                    );
                    student.setGender(result.getString("gender"));
                    student.setParentName(result.getString("parent_name"));
                    student.setParentPhone(result.getString("parent_phone"));
                    student.setAddress(result.getString("address"));

                    return Optional.of(student);
                }
            }
        }

        return Optional.empty();
    }
}
