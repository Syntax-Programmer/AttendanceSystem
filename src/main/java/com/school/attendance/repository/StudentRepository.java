package com.school.attendance.repository;

import com.school.attendance.database.DatabaseConnection;
import com.school.attendance.model.FacultyAssignment;
import com.school.attendance.model.Student;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentRepository {

    public void save(Student student) throws SQLException {
        String sql = """
        INSERT INTO students (
            roll_no, class_number, section, name, date_of_birth, gender,
            parent_name, parent_phone, address, parent_email
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
            statement.setString(10, student.getParentEmail());

            statement.executeUpdate();
        }
    }

    public void update(Student student) throws SQLException {
        String sql = """
        UPDATE students SET
            class_number=?, section=?, name=?, date_of_birth=?, gender=?,
            parent_name=?, parent_phone=?, address=?, parent_email=?
        WHERE roll_no=?
        """;
        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, student.getClassNumber());
            statement.setString(2, student.getSection());
            statement.setString(3, student.getName());
            statement.setObject(4, student.getDateOfBirth());
            statement.setString(5, student.getGender());
            statement.setString(6, student.getParentName());
            statement.setString(7, student.getParentPhone());
            statement.setString(8, student.getAddress());
            statement.setString(9, student.getParentEmail());
            statement.setInt(10, student.getRollNo());
            statement.executeUpdate();
        }
    }

    public Optional<Student> findByRollNo(int rollNo) throws SQLException {
        String sql = """
        SELECT
            roll_no, class_number, section, name, date_of_birth, gender,
            parent_name, parent_phone, address, parent_email
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
                    return Optional.of(mapRow(result));
                }
            }
        }

        return Optional.empty();
    }

    public List<Student> findAll() throws SQLException {
        String sql = """
        SELECT
            roll_no, class_number, section, name, date_of_birth, gender,
            parent_name, parent_phone, address, parent_email
        FROM students
        ORDER BY class_number, section, roll_no
        """;

        List<Student> students = new ArrayList<>();

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery()
        ) {
            while (result.next()) {
                students.add(mapRow(result));
            }
        }

        return students;
    }

    public long countStudents() throws SQLException {
        String sql = "SELECT COUNT(*) FROM students";
        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery()
        ) {
            result.next();
            return result.getLong(1);
        }
    }

    public List<Student> findByClassAndSection(int classNumber, String section)
        throws SQLException {
        String sql = """
        SELECT
            roll_no, class_number, section, name, date_of_birth, gender,
            parent_name, parent_phone, address, parent_email
        FROM students
        WHERE class_number = ? AND section = ?
        ORDER BY roll_no
        """;

        List<Student> students = new ArrayList<>();

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, classNumber);
            statement.setString(2, section);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    students.add(mapRow(result));
                }
            }
        }

        return students;
    }

    /**
     * Returns all students belonging to ANY of the given class/section assignments.
     * Used for faculty-restricted student views.
     */
    public List<Student> findByAssignedClasses(List<FacultyAssignment> assignments)
        throws SQLException {
        if (assignments == null || assignments.isEmpty()) {
            return new ArrayList<>();
        }
        // Build parameterized placeholders: (class_number=? AND section=?) OR ...
        StringBuilder whereClause = new StringBuilder();
        for (int i = 0; i < assignments.size(); i++) {
            if (i > 0) whereClause.append(" OR ");
            whereClause.append("(s.class_number = ? AND s.section = ?)");
        }
        String sql = """
            SELECT
                s.roll_no, s.class_number, s.section, s.name, s.date_of_birth, s.gender,
                s.parent_name, s.parent_phone, s.address, s.parent_email
            FROM students s
            WHERE """ + whereClause + """

            ORDER BY s.class_number, s.section, s.roll_no
            """;

        List<Student> students = new ArrayList<>();
        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            int idx = 1;
            for (FacultyAssignment assignment : assignments) {
                statement.setInt(idx++, assignment.getClassNumber());
                statement.setString(idx++, assignment.getSection());
            }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    students.add(mapRow(result));
                }
            }
        }
        return students;
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setRollNo(rs.getInt("roll_no"));
        student.setClassNumber(rs.getInt("class_number"));
        student.setSection(rs.getString("section"));
        student.setName(rs.getString("name"));
        student.setDateOfBirth(rs.getObject("date_of_birth", java.time.LocalDate.class));
        student.setGender(rs.getString("gender"));
        student.setParentName(rs.getString("parent_name"));
        student.setParentPhone(rs.getString("parent_phone"));
        student.setAddress(rs.getString("address"));
        student.setParentEmail(rs.getString("parent_email"));
        return student;
    }
}
