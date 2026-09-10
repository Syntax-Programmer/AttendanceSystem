package com.school.attendance.service;

import com.school.attendance.model.Student;
import com.school.attendance.repository.StudentRepository;
import java.sql.SQLException;
import java.util.Optional;

public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public void addStudent(Student student) throws SQLException {
        validateStudent(student);
        if (studentRepository.findByRollNo(student.getRollNo()).isPresent()) {
            throw new IllegalArgumentException(
                "A student with roll number " + student.getRollNo() + " already exists."
            );
        }
        studentRepository.save(student);
    }

    public Optional<Student> findStudent(int rollNo) throws SQLException {
        return studentRepository.findByRollNo(rollNo);
    }

    private void validateStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null.");
        }
        if (student.getRollNo() <= 0) {
            throw new IllegalArgumentException("Roll number must be positive.");
        }
        if (student.getClassNumber() < 1 || student.getClassNumber() > 12) {
            throw new IllegalArgumentException("Class must be between 1 and 12.");
        }
        if (student.getSection() == null || student.getSection().isBlank()) {
            throw new IllegalArgumentException("Section cannot be empty.");
        }
        if (student.getName() == null || student.getName().isBlank()) {
            throw new IllegalArgumentException("Student name cannot be empty.");
        }
    }
}
