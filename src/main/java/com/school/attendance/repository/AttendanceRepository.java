package com.school.attendance.repository;

import com.school.attendance.database.DatabaseConnection;
import com.school.attendance.model.Attendance;
import com.school.attendance.model.AttendanceStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AttendanceRepository {

    public void save(Attendance attendance) throws SQLException {
        String sql = """
        INSERT INTO attendance (
            roll_no, attendance_date, status
        ) VALUES (?, ?, ?)
        """;

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, attendance.getRollNo());
            statement.setObject(2, attendance.getAttendanceDate());
            statement.setString(3, attendance.getStatus().name());
            statement.executeUpdate();
        }
    }

    public Optional<Attendance> findByRollNoAndDate(int rollNo, LocalDate date)
        throws SQLException {
        String sql = """
        SELECT
            attendance_id, roll_no, attendance_date, status, marked_at
        FROM attendance
        WHERE roll_no = ? AND attendance_date = ?
        """;

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, rollNo);
            statement.setObject(2, date);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapRow(result));
            }
        }
    }

    public List<Attendance> findByRollNo(int rollNo) throws SQLException {
        String sql = """
        SELECT
            attendance_id, roll_no, attendance_date, status, marked_at
        FROM attendance
        WHERE roll_no = ?
        ORDER BY attendance_date DESC
        """;

        List<Attendance> records = new ArrayList<>();
        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, rollNo);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    records.add(mapRow(result));
                }
            }
        }

        return records;
    }

    public List<Attendance> findByDate(LocalDate date) throws SQLException {
        String sql = """
        SELECT
            attendance_id, roll_no, attendance_date, status, marked_at
        FROM attendance
        WHERE attendance_date = ?
        ORDER BY roll_no
        """;

        List<Attendance> records = new ArrayList<>();
        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setObject(1, date);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    records.add(mapRow(result));
                }
            }
        }

        return records;
    }

    private Attendance mapRow(ResultSet result) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setAttendanceId(result.getLong("attendance_id"));
        attendance.setRollNo(result.getInt("roll_no"));
        attendance.setAttendanceDate(result.getObject("attendance_date", LocalDate.class));
        attendance.setStatus(AttendanceStatus.valueOf(result.getString("status")));
        if (result.getTimestamp("marked_at") != null) {
            attendance.setMarkedAt(result.getTimestamp("marked_at").toLocalDateTime());
        }

        return attendance;
    }

    public long countByDateAndStatus(java.time.LocalDate date, AttendanceStatus status)
        throws SQLException {
        String sql = """
        SELECT COUNT(*) FROM attendance
        WHERE attendance_date = ? AND status = ?
        """;

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setObject(1, date);
            statement.setString(2, status.name());
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getLong(1);
            }
        }
    }
}
