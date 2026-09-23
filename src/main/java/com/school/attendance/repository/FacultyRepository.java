package com.school.attendance.repository;

import com.school.attendance.database.DatabaseConnection;
import com.school.attendance.model.Faculty;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC repository for the faculty profile table.
 * Handles CRUD for faculty profile data (NOT login credentials — those stay in users).
 */
public class FacultyRepository {

    public void save(Faculty faculty) throws SQLException {
        String sql = """
        INSERT INTO faculty (user_id, name, date_of_birth, phone, email, address, gender, qualification)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            ps.setInt(1, faculty.getUserId());
            ps.setString(2, faculty.getName());
            ps.setObject(3, faculty.getDateOfBirth());
            ps.setString(4, faculty.getPhone());
            ps.setString(5, faculty.getEmail());
            ps.setString(6, faculty.getAddress());
            ps.setString(7, faculty.getGender());
            ps.setString(8, faculty.getQualification());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) faculty.setFacultyId(keys.getInt(1));
            }
        }
    }

    public Optional<Faculty> findByUserId(int userId) throws SQLException {
        String sql = """
        SELECT faculty_id, user_id, name, date_of_birth, phone, email, address, gender, qualification
        FROM faculty WHERE user_id = ?
        """;
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public List<Faculty> findAll() throws SQLException {
        String sql = """
        SELECT faculty_id, user_id, name, date_of_birth, phone, email, address, gender, qualification
        FROM faculty ORDER BY name
        """;
        List<Faculty> list = new ArrayList<>();
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public void update(Faculty faculty) throws SQLException {
        String sql = """
        UPDATE faculty
        SET name=?, date_of_birth=?, phone=?, email=?, address=?, gender=?, qualification=?
        WHERE user_id=?
        """;
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, faculty.getName());
            ps.setObject(2, faculty.getDateOfBirth());
            ps.setString(3, faculty.getPhone());
            ps.setString(4, faculty.getEmail());
            ps.setString(5, faculty.getAddress());
            ps.setString(6, faculty.getGender());
            ps.setString(7, faculty.getQualification());
            ps.setInt(8, faculty.getUserId());
            ps.executeUpdate();
        }
    }

    public void deleteByUserId(int userId) throws SQLException {
        String sql = "DELETE FROM faculty WHERE user_id = ?";
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private Faculty mapRow(ResultSet rs) throws SQLException {
        Faculty f = new Faculty();
        f.setFacultyId(rs.getInt("faculty_id"));
        f.setUserId(rs.getInt("user_id"));
        f.setName(rs.getString("name"));
        f.setDateOfBirth(rs.getObject("date_of_birth", java.time.LocalDate.class));
        f.setPhone(rs.getString("phone"));
        f.setEmail(rs.getString("email"));
        f.setAddress(rs.getString("address"));
        f.setGender(rs.getString("gender"));
        f.setQualification(rs.getString("qualification"));
        return f;
    }
}
