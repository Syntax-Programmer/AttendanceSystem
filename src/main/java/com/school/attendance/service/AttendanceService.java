package com.school.attendance.service;

import com.school.attendance.model.Attendance;
import com.school.attendance.model.AttendanceStatus;
import com.school.attendance.model.Student;
import com.school.attendance.repository.AttendanceRepository;
import com.school.attendance.repository.StudentRepository;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class AttendanceService {

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;

    public AttendanceService(
        StudentRepository studentRepository,
        AttendanceRepository attendanceRepository
    ) {
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
    }

    public void markAttendance(int rollNo, AttendanceStatus status) throws SQLException {
        // 1. Make sure the student exists
        Optional<Student> student = studentRepository.findByRollNo(rollNo);
        if (student.isEmpty()) {
            throw new IllegalArgumentException(
                "Student with roll number " + rollNo + " does not exist."
            );
        }
        // 2. Get today's date
        LocalDate today = LocalDate.now();
        // 3. Check whether attendance already exists
        Optional<Attendance> existing = attendanceRepository.findByRollNoAndDate(rollNo, today);
        if (existing.isPresent()) {
            throw new IllegalStateException(
                "Attendance has already been marked for " + "roll number " + rollNo + " today."
            );
        }
        // 4. Create attendance record
        Attendance attendance = new Attendance(rollNo, today, status);
        // 5. Save it
        attendanceRepository.save(attendance);
    }

    public List<Attendance> getStudentAttendance(int rollNo) throws SQLException {
        return attendanceRepository.findByRollNo(rollNo);
    }

    public List<Attendance> getAttendanceForDate(LocalDate date) throws SQLException {
        return attendanceRepository.findByDate(date);
    }

    public Optional<Student> findStudent(int rollNo) throws SQLException {
        return studentRepository.findByRollNo(rollNo);
    }

    public long getPresentCount(LocalDate date) throws SQLException {
        return attendanceRepository.countByDateAndStatus(date, AttendanceStatus.PRESENT);
    }

    public long getLateCount(LocalDate date) throws SQLException {
        return attendanceRepository.countByDateAndStatus(date, AttendanceStatus.LATE);
    }

    public long getAbsentCount(LocalDate date) throws SQLException {
        return attendanceRepository.countByDateAndStatus(date, AttendanceStatus.ABSENT);
    }
}
