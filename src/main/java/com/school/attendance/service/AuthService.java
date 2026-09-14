package com.school.attendance.service;

import com.school.attendance.model.User;
import com.school.attendance.repository.UserRepository;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> login(String username, String password) throws SQLException {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        if (password == null || password.isBlank()) {
            return Optional.empty();
        }
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return Optional.empty();
        }
        User user = userOptional.get();
        boolean passwordMatches = BCrypt.checkpw(password, user.getPasswordHash());

        if (!passwordMatches) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    public void createUser(String username, String password, String role) throws SQLException {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        if (!role.equals("MANAGEMENT") && !role.equals("FACULTY")) {
            throw new IllegalArgumentException("Invalid role.");
        }

        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordHash);
        user.setRole(role);

        userRepository.save(user);
    }

    public List<User> getAllFaculty() throws SQLException {
        return userRepository.findAllFaculty();
    }

    public void deleteUser(int userId) throws SQLException {
        userRepository.deleteById(userId);
    }
}
