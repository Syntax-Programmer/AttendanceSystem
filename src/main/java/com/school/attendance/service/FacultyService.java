package com.school.attendance.service;

import com.school.attendance.database.DatabaseConnection;
import com.school.attendance.model.Faculty;
import com.school.attendance.model.User;
import com.school.attendance.repository.FacultyRepository;
import com.school.attendance.repository.UserRepository;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Service for faculty profile management.
 * Handles transactional creation of faculty user + profile together.
 */
public class FacultyService {

    private final UserRepository userRepository;
    private final FacultyRepository facultyRepository;

    public FacultyService(UserRepository userRepository, FacultyRepository facultyRepository) {
        this.userRepository = userRepository;
        this.facultyRepository = facultyRepository;
    }

    /**
     * Creates a faculty user account and profile in a single database transaction.
     * Rolls back both if either fails.
     */
    public void createFacultyWithProfile(
        String username,
        String password,
        Faculty profile
    ) throws SQLException {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        if (profile.getName() == null || profile.getName().isBlank()) {
            throw new IllegalArgumentException("Faculty name cannot be empty.");
        }

        // Check username uniqueness before opening transaction
        Optional<User> existing = userRepository.findByUsername(username);
        if (existing.isPresent()) {
            throw new IllegalArgumentException(
                "Username '" + username + "' already exists. Please choose another."
            );
        }

        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());

        // Use a transaction: both user and faculty profile must succeed together
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                User user = new User();
                user.setUsername(username);
                user.setPasswordHash(passwordHash);
                user.setRole("FACULTY");
                userRepository.saveWithConnection(conn, user);

                profile.setUserId(user.getUserId());
                saveFacultyWithConnection(conn, profile);

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void saveFacultyWithConnection(Connection conn, Faculty faculty) throws SQLException {
        String sql = """
        INSERT INTO faculty (user_id, name, date_of_birth, phone, email, address, gender, qualification)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, faculty.getUserId());
            ps.setString(2, faculty.getName());
            ps.setObject(3, faculty.getDateOfBirth());
            ps.setString(4, faculty.getPhone());
            ps.setString(5, faculty.getEmail());
            ps.setString(6, faculty.getAddress());
            ps.setString(7, faculty.getGender());
            ps.setString(8, faculty.getQualification());
            ps.executeUpdate();
        }
    }

    public Optional<Faculty> getFacultyProfile(int userId) throws SQLException {
        return facultyRepository.findByUserId(userId);
    }

    public List<Faculty> getAllFacultyProfiles() throws SQLException {
        return facultyRepository.findAll();
    }

    public void updateProfile(Faculty faculty) throws SQLException {
        if (faculty.getName() == null || faculty.getName().isBlank()) {
            throw new IllegalArgumentException("Faculty name cannot be empty.");
        }
        facultyRepository.update(faculty);
    }

    /**
     * Resets username for a FACULTY account with validation.
     */
    public void resetUsername(int userId, String newUsername) throws SQLException {
        if (newUsername == null || newUsername.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        // Check uniqueness
        Optional<User> existing = userRepository.findByUsername(newUsername);
        if (existing.isPresent() && existing.get().getUserId() != userId) {
            throw new IllegalArgumentException(
                "Username '" + newUsername + "' is already in use."
            );
        }
        userRepository.updateUsername(userId, newUsername);
    }
}
