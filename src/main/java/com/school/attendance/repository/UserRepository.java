package com.school.attendance.repository;

import com.school.attendance.database.DatabaseConnection;
import com.school.attendance.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = """
        SELECT
            user_id, username, password_hash, role
        FROM users WHERE username = ?
        """;

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    User user = new User();
                    user.setUserId(result.getInt("user_id"));
                    user.setUsername(result.getString("username"));
                    user.setPasswordHash(result.getString("password_hash"));
                    user.setRole(result.getString("role"));
                    return Optional.of(user);
                }
            }
        }

        return Optional.empty();
    }

    public void save(User user) throws SQLException {
        String sql = """
        INSERT INTO users ( username, password_hash, role ) VALUES (?, ?, ?)
        """;

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getRole());

            statement.executeUpdate();
        }
    }

    public List<User> findAllFaculty() throws SQLException {
        String sql = """
        SELECT user_id, username, password_hash, role FROM users
        WHERE role = 'FACULTY'
        ORDER BY username
        """;

        List<User> faculty = new ArrayList<>();
        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery()
        ) {
            while (result.next()) {
                User user = new User();
                user.setUserId(result.getInt("user_id"));
                user.setUsername(result.getString("username"));
                user.setPasswordHash(result.getString("password_hash"));
                user.setRole(result.getString("role"));
                faculty.add(user);
            }
        }

        return faculty;
    }

    public void deleteById(int userId) throws SQLException {
        String sql = """
        DELETE FROM users
        WHERE user_id = ? AND role = 'FACULTY'
        """;

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        }
    }

    public void updatePassword(int userId, String passwordHash) throws SQLException {
        String sql = """
        UPDATE users
        SET password_hash = ?
        WHERE user_id = ? AND role = 'FACULTY'
        """;

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, passwordHash);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    public Optional<User> findById(int userId) throws SQLException {
        String sql = """
        SELECT user_id, username, password_hash, role FROM users
        WHERE user_id = ?
        """;

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    User user = new User();
                    user.setUserId(result.getInt("user_id"));
                    user.setUsername(result.getString("username"));
                    user.setPasswordHash(result.getString("password_hash"));
                    user.setRole(result.getString("role"));
                    return Optional.of(user);
                }
            }
        }

        return Optional.empty();
    }
}
