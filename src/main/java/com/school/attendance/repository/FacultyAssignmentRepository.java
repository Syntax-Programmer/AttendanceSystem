package com.school.attendance.repository;

import com.school.attendance.database.DatabaseConnection;
import com.school.attendance.model.FacultyAssignment;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FacultyAssignmentRepository {

    public void save(FacultyAssignment assignment) throws SQLException {
        String sql = """
        INSERT INTO faculty_assignments (user_id, class_number, section)
        VALUES (?, ?, ?)
        """;

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, assignment.getUserId());
            statement.setInt(2, assignment.getClassNumber());
            statement.setString(3, assignment.getSection());
            statement.executeUpdate();
        }
    }

    public List<FacultyAssignment> findByUserId(int userId) throws SQLException {
        String sql = """
        SELECT assignment_id, user_id, class_number, section FROM faculty_assignments
        WHERE user_id = ?
        ORDER BY class_number, section
        """;

        List<FacultyAssignment> assignments = new ArrayList<>();

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    FacultyAssignment assignment = new FacultyAssignment();
                    assignment.setAssignmentId(result.getInt("assignment_id"));
                    assignment.setUserId(result.getInt("user_id"));
                    assignment.setClassNumber(result.getInt("class_number"));
                    assignment.setSection(result.getString("section"));
                    assignments.add(assignment);
                }
            }
        }

        return assignments;
    }

    public void deleteById(int assignmentId) throws SQLException {
        String sql = """
        DELETE FROM faculty_assignments
        WHERE assignment_id = ?
        """;

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, assignmentId);
            statement.executeUpdate();
        }
    }
}
