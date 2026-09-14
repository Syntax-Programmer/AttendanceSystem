package com.school.attendance.service;

import com.school.attendance.model.FacultyAssignment;
import com.school.attendance.model.User;
import com.school.attendance.repository.FacultyAssignmentRepository;
import com.school.attendance.repository.UserRepository;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class FacultyAssignmentService {

    private final FacultyAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    public FacultyAssignmentService(
        FacultyAssignmentRepository assignmentRepository,
        UserRepository userRepository
    ) {
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
    }

    public void assignClass(int userId, int classNumber, String section) throws SQLException {
        if (classNumber < 1 || classNumber > 12) {
            throw new IllegalArgumentException("Class must be between 1 and 12.");
        }
        if (section == null || section.isBlank()) {
            throw new IllegalArgumentException("Section cannot be empty.");
        }
        section = section.trim().toUpperCase();
        if (
            !section.equals("A") &&
            !section.equals("B") &&
            !section.equals("C") &&
            !section.equals("D")
        ) {
            throw new IllegalArgumentException("Section must be A, B, C, or D.");
        }
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("Faculty user does not exist.");
        }
        if (!user.get().getRole().equals("FACULTY")) {
            throw new IllegalArgumentException("Only faculty users can be assigned classes.");
        }
        List<FacultyAssignment> existing = assignmentRepository.findByUserId(userId);
        for (FacultyAssignment assignment : existing) {
            if (
                assignment.getClassNumber() == classNumber &&
                assignment.getSection().equals(section)
            ) {
                throw new IllegalArgumentException(
                    "This class is already assigned to this faculty member."
                );
            }
        }

        FacultyAssignment assignment = new FacultyAssignment();
        assignment.setUserId(userId);
        assignment.setClassNumber(classNumber);
        assignment.setSection(section);
        assignmentRepository.save(assignment);
    }

    public List<FacultyAssignment> getAssignments(int userId) throws SQLException {
        return assignmentRepository.findByUserId(userId);
    }

    public void removeAssignment(int assignmentId) throws SQLException {
        assignmentRepository.deleteById(assignmentId);
    }
}
